def call(Map config = [:]) {
    pipeline {
        agent any

        stages {
            stage('Checkout') {
                steps {
                    script {
                        checkout scm
                        env.GIT_COMMIT_HASH = sh(
                            script: 'git rev-parse --short HEAD',
                            returnStdout: true
                        ).trim()

                        echo "Git Commit Hash: ${env.GIT_COMMIT_HASH}"
                    }
                }
            }
            stage('Build') {
                steps {
                    script {
                        // Run shell command using 'sh'
                        sh 'ls -la'

                        if (config.projectType == 'laravel') {
                            echo "docker build -t laravel-${config.projectName}:${env.GIT_COMMIT_HASH}."
                        } else if (config.projectType == 'golang') {
                            echo "docker build -t golang-${config.projectName}:${env.GIT_COMMIT_HASH}."
                        } else if (config.projectType == 'java') {
                            echo "docker build -t java-${config.projectName}:${env.GIT_COMMIT_HASH}."
                        } else {
                            echo "docker build -t other-${config.projectName}:${env.GIT_COMMIT_HASH}."
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
                        echo "docker push ${config.projectType}-${config.projectName}:${env.GIT_COMMIT_HASH}"
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