def serialID = ['kong-services-ci-2': "KADBJ0B022600811", 'kong-services-ci-1': "KADBJ0B022600913"]

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
    options {
        timestamps()
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
sh "ls"
            }
        }

    }
}
