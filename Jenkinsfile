def START_TIME

pipeline {
    agent any

    stages {
        stage("CI/CD start") {
            steps {
                script {
                    env.START_TIME = System.currentTimeMillis().toString()
                    def Author_ID = sh(script: "git show -s --pretty=%an", returnStdout: true).trim()
                    def Author_Name = sh(script: "git show -s --pretty=%ae", returnStdout: true).trim()

                    withCredentials([string(credentialsId: 'discord-webhook', variable: 'DISCORD_WEBHOOK')]){
                        sh """
                        curl -X POST \
                            -H "Content-Type: application/json" \
                            -d '{
                                    "username": "Jenkins",
                                    "embeds":[{
                                        "description": "🚀 ** 배포 시작 **\\n프로젝트: Board-Server\\n브랜치: release\\n요청자: ${Author_ID} (${Author_Name})\\n빌드 번호: #${BUILD_NUMBER}\\n",
                                        "color": 3447003
                                    }]
                                }' \
                            ${DISCORD_WEBHOOK}
                        """
                    }
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

        stage("secret.yml download") {
            steps {
                withCredentials([file(credentialsId: 'secret-db', variable: 'dbConfigFile')]) {
                    script {
                        sh 'cp -rf $dbConfigFile ./BE/src/main/resources/application-db.yml'
                    }
                }

                withCredentials([file(credentialsId: 'secret-security', variable: 'securityConfigFile')]) {
                    script {
                        sh 'cp -rf $securityConfigFile ./BE/src/main/resources/application-security.yml'
                    }
                }
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

    post{
        success{
            withCredentials([string(credentialsId: 'discord-webhook', variable: 'DISCORD_WEBHOOK')]){
                sh """
                curl -X POST \
                    -H "Content-Type: application/json" \
                    -d '{
                            "username": "Jenkins",
                            "embeds":[{
                                "description": "✅ ** 배포 성공 🎉**\\n프로젝트: Board-Server\\n빌드 번호: #${BUILD_NUMBER}\\n**소요 시간**: ${elapsedTime()}초\\n[서비스 바로가기](http://urzor.shop)\\n",
                                "color": 5763719
                            }]
                        }' \
                    ${DISCORD_WEBHOOK}
                """
            }
        }
        failure{
            withCredentials([string(credentialsId: 'discord-webhook', variable: 'DISCORD_WEBHOOK')]){
                sh """
                curl -X POST \
                    -H "Content-Type: application/json" \
                    -d '{
                            "username": "Jenkins",
                            "embeds":[{
                                "description": "❌ ** 배포 실패 💦**\\n프로젝트: Board-Server\\n빌드 번호: #${BUILD_NUMBER}\\n[로그 보기](${BUILD_URL})\\n",
                                "color": 15548997
                            }]
                        }' \
                    ${DISCORD_WEBHOOK}
                """
            }
        }
    }
}

def elapsedTime() {
    def diff = System.currentTimeMillis() - env.START_TIME.toLong()
    return String.format("%.1f", diff / 1000.0)
}