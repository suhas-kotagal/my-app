def serialID = ['kong-services-ci-2': "KADBJ0B022600811", 'kong-services-ci-1': "KADBJ0B022600913"]

pipeline {
    parameters {
	 string(name: 'DEVICE_SERIAL_ID', defaultValue: '', description: 'Kong device ID. Leave it empty for the default behavior')
    }
    agent any
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
