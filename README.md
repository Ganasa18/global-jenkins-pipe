# global-jenkins-pipe

# EXAMPLE FOR DOCKERFILE PIPELINE

@Library('global-jenkins-pipe@master') \_

globalPipeline(
projectName: 'trufle_api_container',
projectType: 'dockerfile',
exposedPort: 3001,
appPort: 3001,
dockerImage: 'docker/image',

<!-- optional  -->

containerName:
)

# EXAMPLE FOR PROJECTGIT PIPELINE
