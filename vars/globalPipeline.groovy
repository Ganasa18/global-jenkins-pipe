def call(Map config = [:]) {
    pipeline {
        agent any

        stages {
            stage('Build') {
                steps {
                    script {
                        // Run shell command using 'sh'
                        sh 'ls -la'

                        if (config.projectType == 'laravel') {
                            echo "docker build -t laravel-${config.projectName}"
                        } else if (config.projectType == 'golang') {
                            echo "docker build -t golang-${config.projectName}"
                        } else if (config.projectType == 'java') {
                            echo "docker build -t java-${config.projectName}"
                        } else {
                            echo "docker build -t other-${config.projectName}"
                        }
                    }
                }
            }
            stage('Approval') {
                steps {
                    script {
                        // Interactive prompt for approval
                        def userInput = input(
                            message: 'Do you want to proceed with the deployment?',
                            parameters: [
                                [$class: 'BooleanParameterDefinition', defaultValue: true, description: 'Approve Deployment?', name: 'Approve']
                            ]
                        )

                        if (!userInput) {
                            error("Deployment was not approved. Aborting pipeline.")
                        }
                    }
                }
            }
            stage('Deploy') {
                steps {
                    script {
                        if (config.createHelm) {
                            echo "helm upgrade --install ${config.projectName} mychart"
                        }
                        echo "docker push ${config.projectType}-${config.projectName}"
                    }
                }
            }
            stage('Test') {
                steps {
                    script {
                        if (config.projectType == 'java' && config.shouldRunJavaUnitTest) {
                            echo "mvn test"
                        }
                        if (config.projectType == 'java' && config.shouldRunJavaIntegrationTest) {
                            echo "mvn integration-test"
                        }
                        if (config.projectType == 'golang' && config.shouldRunGoUnitTest) {
                            echo "go test ./..."
                        }
                    }
                }
            }
        }
    }
}
