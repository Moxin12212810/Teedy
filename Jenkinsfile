pipeline {
    agent any

    environment {
        // Define environment variables
        // Jenkins credentials configuration
        DOCKER_HUB_CREDENTIALS = credentials('1') // Docker Hub credentials ID stored in Jenkins
        // Docker Hub repository name
        DOCKER_IMAGE = 'mox413/teedy-app' // Your Docker Hub username and repository name
        DOCKER_TAG = "${env.BUILD_NUMBER}" // Use build number as tag
    }

    stages {
        stage('Build') {
            steps {
                checkout scmGit(
                    branches: [[name: '*/b-12212810']],
                    extensions: [],
                    userRemoteConfigs: [[url: 'https://github.com/Moxin12212810/Teedy.git']] // Your GitHub repository
                )
                sh 'mvn -B -DskipTests clean package'
            }
        }

        stage('Building image') {
            steps {
                script {
                    // Assume Dockerfile is located at project root
                    docker.build("${env.DOCKER_IMAGE}:${env.DOCKER_TAG}")
                }
            }
        }

        stage('Upload image') {
            steps {
                script {
                    // Sign in to Docker Hub
                    docker.withRegistry('https://registry.hub.docker.com', '1') {
                        // Push image
                        docker.image("${env.DOCKER_IMAGE}:${env.DOCKER_TAG}").push()
                        // Optional: also tag as latest
                        docker.image("${env.DOCKER_IMAGE}:${env.DOCKER_TAG}").push('latest')
                    }
                }
            }
        }

        stage('Run containers') {
            steps {
                script {
                    // Stop and remove container if it exists
                    sh 'docker stop teedy-container-8081 || true'
                    sh 'docker rm teedy-container-8081 || true'

                    // Run container
                    docker.image("${env.DOCKER_IMAGE}:${env.DOCKER_TAG}").run(
                        '--name teedy-container-8081 -d -p 8081:8080'
                    )

                    // Optional: list all teedy-containers
                    sh 'docker ps --filter "name=teedy-container"'
                }
            }
        }
    }
}