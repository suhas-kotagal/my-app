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
		stage('Junit report') { 
            steps {
                junit '**/app/build/outputs/androidTest-results/connected/flavors/debugAndroidTest/*.xml'
		sh "ls"
            }
        }
		 stage('Benchmark') { 
            steps {
                benchmark '**/app/build/outputs/connected_android_test_additional_output/debugAndroidTest/connected/*.json'
		sh "ls"
            }
        }
    }
}
