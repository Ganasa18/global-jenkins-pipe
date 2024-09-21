def call(Map config = [:]) {
    pipeline {
        agent any

        stages {
            stage('Prepare Environment') {
                steps {
                    script {
                        // Skip SCM checkout if the project type is 'dockerfile'
                        if (config.projectType != 'dockerfile') {
                            checkout scm
                            env.GIT_COMMIT_HASH = sh(
                                script: 'git rev-parse --short HEAD',
                                returnStdout: true
                            ).trim()
                            echo "Git Commit Hash: ${env.GIT_COMMIT_HASH}"
                        } else {
                            echo "Skipping SCM checkout for Dockerfile project type"
                        }
                    }
                }
            }

            stage('Build') {
                steps {
                    script {
                        sh 'ls -la'

                        switch (config.projectType) {
                            case 'laravel':
                                echo "docker build -t laravel-${config.projectName}:${env.GIT_COMMIT_HASH} ."
                                break
                            case 'golang':
                                echo "docker build -t golang-${config.projectName}:${env.GIT_COMMIT_HASH} ."
                                break
                            case 'java':
                                echo "docker build -t java-${config.projectName}:${env.GIT_COMMIT_HASH} ."
                                break
                            case 'dockerfile':
                                echo "Pulling Docker image: ${config.dockerImage}"
                                sh "docker pull ${config.dockerImage}"
                                break
                            default:
                                echo "docker build -t other-${config.projectName}:${env.GIT_COMMIT_HASH} ."
                                break
                        }
                    }
                }
            }

            stage('Approval') {
                steps {
                    script {
                         def userInput = input(
                            message: 'Do you want to proceed with the deployment?',
                            ok: 'Yes',
                            submitterParameter: 'submitter'
                        )

                        if (userInput == 'No') {
                            error("Deployment was not approved. Aborting pipeline.")
                        } else {
                            echo "Deployment approved. Proceeding with the pipeline."
                        }
                    }
                }
            }

            stage('Test') {
                steps {
                    script {
                        if (config.projectType == 'java') {
                            if (config.shouldRunJavaUnitTest) {
                                echo "Running Java unit tests: mvn test"
                            }
                            if (config.shouldRunJavaIntegrationTest) {
                                echo "Running Java integration tests: mvn integration-test"
                            }
                        } else if (config.projectType == 'golang' && config.shouldRunGoUnitTest) {
                            echo "Running Go unit tests: go test ./..."
                        }
                    }
                }
            }

            stage('Deploy') {
                steps {
                    script {
                        if (config.createHelm) {
                            echo "Running Helm upgrade/install: helm upgrade --install ${config.projectName} mychart"
                        }

                        if (config.projectType == 'dockerfile') {
                            // Check if exposedPort and appPort are defined, else return error
                            if (!config.exposedPort || !config.appPort) {
                                error("Exposed port and app port must be defined for Dockerfile project type")
                            }

                            def exposedPort = config.exposedPort
                            def appPort = config.appPort
                            def containerName = config.containerName ?: config.projectName
                            def imageName = config.dockerImage

                            // Check if container exists
                            def containerExists = sh(script: "docker container ls -a -q -f name=${containerName}", returnStdout: true).trim()

                            if (containerExists) {
                                echo "Stopping Docker container: ${containerName}"
                                sh "docker stop ${containerName} || true" // Stop container if running

                                echo "Removing Docker container: ${containerName}"
                                sh "docker rm ${containerName} || true"   // Remove the container if it exists
                            } else {
                                echo "Container ${containerName} does not exist. Skipping stop and remove steps."
                            }

                            echo "Running Docker container: docker run -d --restart always -p ${exposedPort}:${appPort} --name ${containerName} ${imageName}"
                            sh "docker run -d --restart always -p ${exposedPort}:${appPort} --name ${containerName} ${imageName}"
                            
                            // Optional sleep after container start
                            sleep time: 10, unit: 'SECONDS'
                            
                            echo "Pruning Docker images"
                            sh "docker image prune --all -f"
                        } else {
                            echo "Pushing Docker image: docker push ${config.projectType}-${config.projectName}:${env.GIT_COMMIT_HASH}"
                            sh "docker push ${config.projectType}-${config.projectName}:${env.GIT_COMMIT_HASH}"
                        }
                    }
                }
            }
        }
    }
}
