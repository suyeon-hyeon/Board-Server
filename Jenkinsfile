pipeline {
    agent any

    stages {
        stage("CI/CD start") {
            steps {
                script {
                    def Author_ID = sh(script: "git show -s --pretty=%an", returnStdout: true).trim()
                    def Author_Name = sh(script: "git show -s --pretty=%ae", returnStdout: true).trim()
                }
            }
        }

        stage("Clone Repository") {
            steps {
                echo '클론 시작'
                git branch: 'release', credentialsId: 'github-user', url: 'https://github.com/suyeon-hyeon/Board-Server.git'
                echo '클론 끝'
            }
        }

        stage("Build BE JAR to Docker Image") {
            steps {
                echo '백엔드 도커 이미지 빌드 시작!'
                dir("./BE") {
                    // 빌드된 JAR 파일을 Docker 이미지로 빌드
                    sh "docker build -t urzor/board-gcp-be:latest ."
                }
                echo '백엔드 도커 이미지 빌드 완료!'
            }
        }

        stage("Push to Docker Hub-BE") {
            steps {
                echo '백엔드 도커 이미지를 Docker Hub에 푸시 시작!'
                withCredentials([usernamePassword(credentialsId: 'docker-user', usernameVariable: 'DOCKER_USERNAME', passwordVariable: 'DOCKER_PASSWORD')]) {
                    sh "docker login -u $DOCKER_USERNAME -p $DOCKER_PASSWORD"
                }
                dir("./BE") {
                    sh "docker push urzor/board-gcp-be:latest"
                }
                echo '백엔드 도커 이미지를 Docker Hub에 푸시 완료!'
            }
        }

        stage("Deploy to E2-BE") {
            steps {
                echo '백엔드 E2에 배포 시작!'
                // 여기에서는 SSH 플러그인이나 SSH 스크립트를 사용하여 E2로 연결하고 Docker 컨테이너 실행
                
                sh "docker rm -f BE"
                sh "docker rmi urzor/board-gcp-be:latest"
                sh "docker image prune -f"
                sh "docker pull urzor/board-gcp-be:latest && docker run -d -p 8080:8080 --name BE urzor/board-gcp-be:latest"
                
                echo '백엔드 E2에 배포 완료!'
            }
        }
    }
}
