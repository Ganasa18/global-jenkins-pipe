def call(body) {
    def config = body

    pipeline {
        agent any

        stages {
            stage('Build') {
                steps {
                    if (config.projectType == 'laravel') {
                        println "docker build -t my-laravel-image ."
                    } else if (config.projectType == 'golang') {
                        println "docker build -t my-golang-image ."
                    } else if (config.projectType == 'java') {
                        println "docker build -t my-java-image ."
                    } else {
                        println "docker build -t my-image ."
                    }
                }
            }
            stage('Deploy') {
                steps {
                    if (config.createHelm) {
                        println "helm upgrade --install ${config.projectName} mychart"
                    }
                }
            }
            stage('Test') {
                steps {
                    if (config.projectType == 'java' && config.shouldRunJavaUnitTest) {
                        println "mvn test"
                    }
                    if (config.projectType == 'java' && config.shouldRunJavaIntegrationTest) {
                        println "mvn integration-test"
                    }
                    if (config.projectType == 'golang' && config.shouldRunGoUnitTest) {
                        println "go test ./..."
                    }
                }
            }
        }
    }
}