pipeline {
    agent any

    triggers {
        cron('0 6 * * 1')  // Monday 06:00
    }

    parameters {
        choice(
            name: 'PROJECT',
            choices: ['ALL', 'or2light', 'master'],  
            description: 'Choose the project to scan (ALL for both)'
        )
        booleanParam(
            name: 'SKIP_SONAR',
            defaultValue: false,
            description: 'Skip SonarQube scan?'
        )
        booleanParam(
            name: 'SONAR_ONLY',
            defaultValue: false,
            description: 'SonarQube (without build)?'
        )
    }

    environment {
        JAVA_HOME = '/usr/lib/jvm/java-17-amazon-corretto'
        SONAR_HOST_URL = 'YOUR_SONARQUBE_URL'  // ✅ change for your real url
        MAVEN_OPTS = '-Xms1g -Xmx4g -XX:+UseG1GC'
        SONAR_SCANNER_OPTS = '-Xmx2g'
        MAVEN_SETTINGS = "${WORKSPACE}/settings.xml"  // ✅ temporary settings
    }

    stages {

        // ✅ NEW STAGE — creating settings.xml with HTTPS for Confluent
        stage('Prepare Maven Settings') {
            steps {
                writeFile file: 'settings.xml', text: '''<?xml version="1.0" encoding="UTF-8"?>
<settings>
    <pluginGroups>
        <pluginGroup>com.m3.versions</pluginGroup>
    </pluginGroups>
    <mirrors>
        <mirror>
            <id>confluent</id>
            <mirrorOf>confluent</mirrorOf>
            <url>https://packages.confluent.io/maven/</url>
            <blocked>false</blocked>
        </mirror>
    </mirrors>
    <profiles>
        <profile>
            <id>confluent-profile</id>
            <repositories>
                <repository>
                    <id>confluent</id>
                    <url>https://packages.confluent.io/maven/</url>
                    <releases><enabled>true</enabled></releases>
                    <snapshots><enabled>true</enabled></snapshots>
                </repository>
            </repositories>
        </profile>
    </profiles>
    <activeProfiles>
        <activeProfile>confluent-profile</activeProfile>
    </activeProfiles>
</settings>'''
            }
        }

        stage('Build and Scan') {
            steps {
                script {
                    def projects = [
                        'or2light': [
                            sonarKey: 'your_sonar_key',  // ✅ change for your real sonar project key
                            gitUrl: 'your_git_url',  // ✅ change for your real git url
                            branch: 'your_branch',  // ✅ change for your real branch
                            credentialId: 'jenkins-master-key',
                            pomPath: 'pom.xml',
                            excludeModules: '',
                        javaHome: '/usr/lib/jvm/java-17-amazon-corretto' 
                        ],
                        'master': [
                            sonarKey: 'your_sonar_key',  // ✅ change for your real sonar project key
                            gitUrl: 'your_git_url',  // ✅ change for your real git url
                            branch: 'your_branch',  // ✅ change for your real branch
                            credentialId: 'jenkins-master-key', 
                            pomPath: 'pom.xml',
                            excludeModules: '',
                            javaHome: '/usr/lib/jvm/java-11-amazon-corretto'
                        ]
                    ]
                    
                    def projectsToScan = params.PROJECT == 'ALL' ? 
                        projects.keySet().toList() : [params.PROJECT]
                    
                    echo "📋 Projects to scan: ${projectsToScan}"
                    echo "SKIP_SONAR: ${params.SKIP_SONAR}"
                    echo "SONAR_ONLY: ${params.SONAR_ONLY}"
                    
                    projectsToScan.each { projectName ->
                        def config = projects[projectName]
                        
                        echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
                        echo "🚀 Processing: ${projectName}"
                        echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
                        dir("workspace-${projectName}") {

                            echo "📦 Checking out: ${projectName}"
                            timeout(time: 20, unit: 'MINUTES') {
                                git(
                                    url: config.gitUrl,
                                    credentialsId: config.credentialId,
                                    branch: config.branch,
                                    extensions: [[$class: 'CloneOption', depth: 1, noTags: true, shallow: true]]
                                )
                            }
                            sh 'echo "PWD: $(pwd)" && ls -la'  
                            sh 'find . -name "pom.xml" -maxdepth 2 | head -20'

                            // 🔨 BUILD
                            if (!params.SONAR_ONLY) {
                                echo "🔨 Building ${projectName}..."
                                def excludeArgs = config.excludeModules ? "-pl '${config.excludeModules}'" : ''
                                def javaHome = config.javaHome
                                sh """
                                    export JAVA_HOME=${javaHome}
                                    export PATH=${javaHome}/bin:\$PATH
                                    mvn clean install \
                                        -DskipTests \
                                        -s ${MAVEN_SETTINGS} \
                                        -f ${config.pomPath} \
                                        ${excludeArgs} 
                                """
                            } else {
                                echo "⏭️ Skipping build (SONAR_ONLY=true)"
                            }
                            
                            // 🔍 SONARQUBE SCAN
                            if (!params.SKIP_SONAR) {
                                echo "🔍 Scanning ${projectName} on SonarQube..."
                                withCredentials([string(credentialsId: 'sonar-m2-jenkins-token', variable: 'SONAR_TOKEN')]) {
                                    def pomPath = config.pomPath
                                    def projectKey = config.sonarKey
                                    def sonarUrl = SONAR_HOST_URL
                                    def mavenSettings = MAVEN_SETTINGS
                                    def excludeArgs = config.excludeModules ? "-pl '${config.excludeModules}'" : ''
                                    def sonarJavaHome = '/usr/lib/jvm/java-17-amazon-corretto'
                                    
                                    sh '''
                                        export JAVA_HOME=''' + sonarJavaHome + '''
                                        export PATH=''' + sonarJavaHome + '''/bin:$PATH
                                        mvn sonar:sonar \
                                            -f ''' + pomPath + ''' \
                                            ''' + excludeArgs + ''' \
                                            -s ''' + mavenSettings + ''' \
                                            -Dsonar.projectKey=''' + projectKey + ''' \
                                            -Dsonar.host.url=''' + sonarUrl + ''' \
                                            -Dsonar.login=$SONAR_TOKEN 
                                    '''
                                }
                            } else {
                                echo "⏭️ Skipping SonarQube scan (SKIP_SONAR=true)"
                            }
                            
                            echo "✅ Completed: ${projectName}"
                            echo ""
                        }
                    }
                }
            }
        }

        stage('Quality Gate') {
            when {
                expression { !params.SKIP_SONAR } 
            }
            steps {
                
            echo "✅ Sonar analysis submitted successfully. Check results at: ${SONAR_HOST_URL}"
            }
       }
    }

    post {
        success {
            echo "✅ All scans completed successfully!"
            echo "📊 SonarQube: ${SONAR_HOST_URL}"
        }
        unstable {
            echo "⚠️ Pipeline completed with warnings (Quality Gate failed)"
        }
        failure {
            echo "❌ Pipeline FAILED"
        }
        always {
            echo "⏱️ Build time: ${currentBuild.durationString}"
        }
    }
}