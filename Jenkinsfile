pipeline {
    agent any 
    triggers {
        cron('H 21 * * 1-5')
    }
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
     success {
     junit '**/app/build/outputs/androidTest-results/connected/flavors/debugAndroidTest/*.xml'
     }
	always {
         benchmark '**/app/build/outputs/connected_android_test_additional_output/debugAndroidTest/connected/**/*.json'
	}
   }
}
