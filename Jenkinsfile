pipeline {
    parameters {
	 string(name: 'BUILD_AGENT_LINUX', defaultValue: 'kong-services-ci', description: 'Name of the build agent')
	 string(name: 'DEVICE_SERIAL_ID', defaultValue: '', description: 'Kong device ID. Leave it empty for the default behavior')
    }
    agent {
        label "${params.BUILD_AGENT_LINUX}"
    }
    environment {
        DEVICE_SERIAL_ID = "${params.DEVICE_SERIAL_ID ? params.DEVICE_SERIAL_ID : serialID.get(NODE_NAME)}"
    } 
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
                benchmark (	inputLocation: '', schemaSelection: 'Simplest - 1 level - One result only with parameters & thresholds.' , truncateStrings: 'true', altInputSchema: '', altInputSchemaLocation: '')
            }
        }
    }
}
