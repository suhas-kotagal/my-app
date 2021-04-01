pipeline {
    agent any 
    stages {
        stage('Build & Clean') { 
            steps {
                sh "./gradlew clean"
            }
        }
        stage('Test') { 
            steps {
                sh "ls"
            }
        }
    }
}
