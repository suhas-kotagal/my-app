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
            }
        }
    }
post {
     always {
     junit '/KongIntegrationTest/app/build/outputs/androidTest-results/connected/flavors/releaseAndroidTest/*.xml'
     }
   }
}
