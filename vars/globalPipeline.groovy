def call(Map body = [:]) {
    pipeline {
        agent any

        stages {
            stage('Build') {
                steps {
                    script {
                        if (config.projectType == 'laravel') {
                            echo "docker build -t my-laravel-image ."
                        } else if (config.projectType == 'golang') {
                            echo "docker build -t my-golang-image ."
                        } else if (config.projectType == 'java') {
                            echo "docker build -t my-java-image ."
                        } else {
                            echo "docker build -t my-image ."
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