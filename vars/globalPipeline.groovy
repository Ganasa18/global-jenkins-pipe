def call(body) {
    def config = body

    pipeline {
        agent any

        stages {
            stage('Build') {
                steps {
                    if (config.projectType == 'laravel') {
                        sh "echo docker build -t my-laravel-image ."
                    } else if (config.projectType == 'golang') {
                         sh "echo docker build -t my-golang-image ."
                    } else if (config.projectType == 'java') {
                         sh "echo docker build -t my-java-image ."
                    } else {
                         sh "echo docker build -t my-image ."
                    }
                }
            }
            stage('Deploy') {
                steps {
                    if (config.createHelm) {
                         sh "echo helm upgrade --install ${config.projectName} mychart"
                    }
                }
            }
            stage('Test') {
                steps {
                    if (config.projectType == 'java' && config.shouldRunJavaUnitTest) {
                         sh "echo mvn test"
                    }
                    if (config.projectType == 'java' && config.shouldRunJavaIntegrationTest) {
                         sh "echo mvn integration-test"
                    }
                    if (config.projectType == 'golang' && config.shouldRunGoUnitTest) {
                         sh "echo go test ./..."
                    }
                }
            }
        }
    }
}