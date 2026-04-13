# 🔍 Jenkins SonarQube Code Quality Pipeline

> Automated code quality scanning and analysis for Java projects using Jenkins, Maven, and SonarQube.

---

## 🎯 Description

This Jenkins pipeline automates:
- **Multi-project scanning** (one, two projects)
- **Maven builds** with Confluent repository support
- **SonarQube analysis** for code quality metrics
- **Scheduled execution** (Every Monday at 06:00 UTC)
- **Flexible parameters** for custom scanning options

---

## 📋 Project Structure
```
. 
├── 📄 Jenkinsfile # Pipeline configuration 
├── 📄 README.md # This file 
└── 📁 (Java projects monitored) 
├── pom.xml # Maven configuration 
└── src/ # Source code
```


---

## 🚀 Features

### ✅ Multi-Project Support
- **ALL** - Scan both projects
- **one** - Scan one project only
- **two** - Scan two project only

### ✅ Flexible Execution Modes
- **Build + Scan** - Full Maven build with SonarQube analysis
- **Skip SonarQube** - Only Maven build, no quality scan
- **SonarQube Only** - Analysis without rebuilding

### ✅ Java Version Support
- one: **Java 17** (Amazon Corretto)
- two: **Java 11** (Amazon Corretto)

### ✅ Confluent Repository Integration
- HTTPS support with Maven mirror configuration
- Automatic settings.xml generation

---

## ⚙️ Environment Setup

### Required Variables
```
| Variable | Value | Description |
|----------|-------|-----------|
| `JAVA_HOME` | `/usr/lib/jvm/java-17-amazon-corretto` | Java installation path |
| `SONAR_HOST_URL` | `https://your-sonarqube-url` | SonarQube server URL |
| `MAVEN_OPTS` | `-Xms1g -Xmx4g -XX:+UseG1GC` | Maven memory settings |
| `SONAR_SCANNER_OPTS` | `-Xmx2g` | SonarQube scanner memory |
```
### Required Jenkins Credentials
```
| Credential ID | Type | Description |
|--------------|------|-----------|
| `jenkins-master-key` | SSH Key | Git repository access |
| `sonar-m2-jenkins-token` | Secret Text | SonarQube authentication token |
```
---

## 📝 Pipeline Parameters

### 1️⃣ PROJECT Selection


Choose which projects to scan:
- **ALL** - Scan both or2light and master
- **one** - Scan only or2light project
- **two** - Scan only master project

### 2️⃣ SKIP_SONAR


Run SonarQube analysis without building:
- `false` - Build and scan
- `true` - Scan only (code must already be built)

---

## 🔧 Configuration

### Step 1: Update Project Configuration

Edit the `projects` map in the pipeline:

```groovy
def projects = [
    'one': [
        sonarKey: 'your_sonar_key',           // ✅ Set your SonarQube project key
        gitUrl: 'your_git_url',               // ✅ Set your Git repository URL
        branch: 'your_branch',                // ✅ Set your branch name
        credentialId: 'jenkins-master-key',   // Git credentials
        pomPath: 'pom.xml',                   // POM file location
        excludeModules: '',                   // Modules to exclude (optional)
        javaHome: '/usr/lib/jvm/java-17-amazon-corretto'
    ],
    'two': [
        sonarKey: 'your_sonar_key',
        gitUrl: 'your_git_url',
        branch: 'your_branch',
        credentialId: 'jenkins-master-key',
        pomPath: 'pom.xml',
        excludeModules: '',
        javaHome: '/usr/lib/jvm/java-11-amazon-corretto'
    ]
]
```groovy

Step 2: Configure SonarQube URL

environment {
    SONAR_HOST_URL = 'https://your-sonarqube-url'  // ✅ Your SonarQube server
}
```
Step 3: Add Jenkins Credentials
SSH Key for Git:

Manage Jenkins → Credentials → System → Global credentials
- Kind: SSH Username with private key
- ID: jenkins-master-key
- Username: jenkins
- Private Key: [Paste your SSH private key]

SonarQube Token:

Manage Jenkins → Credentials → System → Global credentials
- Kind: Secret text
- ID: sonar-m2-jenkins-token
- Secret: [Your SonarQube authentication token]

🔄 Pipeline Stages
1️⃣ Prepare Maven Settings
Generates settings.xml with:

Confluent repository mirror
HTTPS repository configuration
Maven plugin configuration

Mirror: https://packages.confluent.io/maven/


2️⃣ Build and Scan
For each selected project:

Checkout code from Git
Build Maven project (if SONAR_ONLY=false)
Scan with SonarQube (if SKIP_SONAR=false)
3️⃣ Quality Gate
Reports SonarQube analysis results and links to dashboard.

Thinking
markdown


# 🔍 Jenkins SonarQube Code Quality Pipeline

> Automated code quality scanning and analysis for Java projects using Jenkins, Maven, and SonarQube.

---

## 🎯 Description

This Jenkins pipeline automates:
- **Multi-project scanning** (or2light, master projects)
- **Maven builds** with Confluent repository support
- **SonarQube analysis** for code quality metrics
- **Scheduled execution** (Every Monday at 06:00 UTC)
- **Flexible parameters** for custom scanning options

---

## 📋 Project Structure
. ├── 📄 Jenkinsfile # Pipeline configuration ├── 📄 README.md # This file └── 📁 (Java projects monitored) ├── pom.xml # Maven configuration └── src/ # Source code




---

## 🚀 Features

### ✅ Multi-Project Support
- **ALL** - Scan both projects
- **or2light** - Scan or2light project only
- **master** - Scan master project only

### ✅ Flexible Execution Modes
- **Build + Scan** - Full Maven build with SonarQube analysis
- **Skip SonarQube** - Only Maven build, no quality scan
- **SonarQube Only** - Analysis without rebuilding

### ✅ Java Version Support
- or2light: **Java 17** (Amazon Corretto)
- master: **Java 11** (Amazon Corretto)

### ✅ Confluent Repository Integration
- HTTPS support with Maven mirror configuration
- Automatic settings.xml generation

---

## ⚙️ Environment Setup

### Required Variables

| Variable | Value | Description |
|----------|-------|-----------|
| `JAVA_HOME` | `/usr/lib/jvm/java-17-amazon-corretto` | Java installation path |
| `SONAR_HOST_URL` | `https://your-sonarqube-url` | SonarQube server URL |
| `MAVEN_OPTS` | `-Xms1g -Xmx4g -XX:+UseG1GC` | Maven memory settings |
| `SONAR_SCANNER_OPTS` | `-Xmx2g` | SonarQube scanner memory |

### Required Jenkins Credentials

| Credential ID | Type | Description |
|--------------|------|-----------|
| `jenkins-master-key` | SSH Key | Git repository access |
| `sonar-m2-jenkins-token` | Secret Text | SonarQube authentication token |

---

## 📝 Pipeline Parameters

### 1️⃣ PROJECT Selection
Choice: ['ALL', 'or2light', 'master'] Default: ALL




Choose which projects to scan:
- **ALL** - Scan both or2light and master
- **or2light** - Scan only or2light project
- **master** - Scan only master project

### 2️⃣ SKIP_SONAR
Boolean: true/false Default: false




Skip SonarQube scanning:
- `false` - Run SonarQube analysis
- `true` - Skip SonarQube, only build

### 3️⃣ SONAR_ONLY
Boolean: true/false Default: false




Run SonarQube analysis without building:
- `false` - Build and scan
- `true` - Scan only (code must already be built)

---

## 🔧 Configuration

### Step 1: Update Project Configuration

Edit the `projects` map in the pipeline:

```groovy
def projects = [
    'or2light': [
        sonarKey: 'your_sonar_key',           // ✅ Set your SonarQube project key
        gitUrl: 'your_git_url',               // ✅ Set your Git repository URL
        branch: 'your_branch',                // ✅ Set your branch name
        credentialId: 'jenkins-master-key',   // Git credentials
        pomPath: 'pom.xml',                   // POM file location
        excludeModules: '',                   // Modules to exclude (optional)
        javaHome: '/usr/lib/jvm/java-17-amazon-corretto'
    ],
    'master': [
        sonarKey: 'your_sonar_key',
        gitUrl: 'your_git_url',
        branch: 'your_branch',
        credentialId: 'jenkins-master-key',
        pomPath: 'pom.xml',
        excludeModules: '',
        javaHome: '/usr/lib/jvm/java-11-amazon-corretto'
    ]
]

Step 2: Configure SonarQube URL


environment {
    SONAR_HOST_URL = 'https://your-sonarqube-url'  // ✅ Your SonarQube server
}
Step 3: Add Jenkins Credentials
SSH Key for Git:



Manage Jenkins → Credentials → System → Global credentials
- Kind: SSH Username with private key
- ID: jenkins-master-key
- Username: jenkins
- Private Key: [Paste your SSH private key]
SonarQube Token:



Manage Jenkins → Credentials → System → Global credentials
- Kind: Secret text
- ID: sonar-m2-jenkins-token
- Secret: [Your SonarQube authentication token]
🔄 Pipeline Stages
1️⃣ Prepare Maven Settings
Generates settings.xml with:

Confluent repository mirror
HTTPS repository configuration
Maven plugin configuration
xml


Mirror: https://packages.confluent.io/maven/
2️⃣ Build and Scan
For each selected project:

Checkout code from Git
Build Maven project (if SONAR_ONLY=false)
Scan with SonarQube (if SKIP_SONAR=false)
3️⃣ Quality Gate
Reports SonarQube analysis results and links to dashboard.

🚀 Usage
Manual Trigger

1. Jenkins Dashboard → [Pipeline Name] → Build with Parameters
2. Select PROJECT (ALL, or2light, or master)
3. Configure SKIP_SONAR and SONAR_ONLY
4. Click Build

Scheduled Execution
Automatic trigger every Monday at 06:00 UTC:

triggers {
    cron('0 6 * * 1')  // Monday 06:00
}

Command Line (Jenkins CLI)

java -jar jenkins-cli.jar -s http://jenkins-url/ build \
  "SonarQube-Pipeline" \
  -p PROJECT=ALL \
  -p SKIP_SONAR=false \
  -p SONAR_ONLY=false

