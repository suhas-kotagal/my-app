pipeline {
    agent any 
    stages {
        stage('Build & Clean') { 
            steps {
		sh "./gradlew installDebug"
		sh "./gradlew installDebugAndroidTest"
            }
        }
        stage('Test') { 
            steps {
                sh "./gradlew connectedCheck -Pandroid.testInstrumentationRunnerArguments.class=com.logitech.integration.test.config.ConfigTest"
		sh "ls"
            }
        }
    }
post {
     always {
     junit '**/app/build/outputs/androidTest-results/connected/flavors/debugAndroidTest/*.xml'
     }
   }
}
