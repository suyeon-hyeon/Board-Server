pipeline {
    agent any

    environment {
        DISCORD_WEBHOOK = credentials('discord-webhook-url')
        APP_NAME = 'Board-Server'
        IMAGE = 'urzor/board-gcp-be:latest'
        CONTAINER = 'BE'
        PORT = '8080'
    }

    stages {
        stage("CI/CD start") {
            steps {
                script {
                    def AUTHOR_NAME = sh(script: "git show -s --pretty=%an", returnStdout: true).trim()
                    def AUTHOR_EMAIL = sh(script: "git show -s --pretty=%ae", returnStdout: true).trim()

                    withEnv(["AUTHOR_NAME=${AUTHOR_NAME}", "AUTHOR_EMAIL=${AUTHOR_EMAIL}"]) {
                        sh(label: 'discord notify start', script: '''
                            set -e
                            payload=$(printf '{"content":"📢 CI/CD 시작\\n- App: %s\\n- Job: %s\\n- Build: #%s\\n- Author: %s <%s>\\n- URL: %s"}' \
                                "$APP_NAME" "$JOB_NAME" "$BUILD_NUMBER" "$AUTHOR_NAME" "$AUTHOR_EMAIL" "$BUILD_URL")
                            curl -sS -o /dev/null -w "discord http=%{http_code}\n" \
                                -H "Content-Type: application/json" -X POST -d "$payload" "$DISCORD_WEBHOOK"
                        ''')
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
                sh(label: 'discord notify deploy start', script: '''
                    set -e
                    payload=$(printf '{"content":"🚀 배포 시작 (BE)\\n- App: %s\\n- Job: %s\\n- Build: #%s\\n- Image: %s\\n- URL: %s"}' \
                        "$APP_NAME" "$JOB_NAME" "$BUILD_NUMBER" "$IMAGE" "$BUILD_URL")
                    curl -sS -o /dev/null -w "discord http=%{http_code}\n" \
                        -H "Content-Type: application/json" -X POST -d "$payload" "$DISCORD_WEBHOOK"
                ''')

                echo '백엔드 E2에 배포 시작!'
                // 여기에서는 SSH 플러그인이나 SSH 스크립트를 사용하여 E2로 연결하고 Docker 컨테이너 실행
                
                sh "docker rm -f BE"
                sh "docker rmi urzor/board-gcp-be:latest"
                sh "docker image prune -f"
                sh "docker pull urzor/board-gcp-be:latest && docker run -d -p 8080:8080 --name BE urzor/board-gcp-be:latest"
                
                echo '백엔드 E2에 배포 완료!'
                sh(label: 'discord notify deploy success', script: '''
                    set -e
                    payload=$(printf '{"content":"🎉 배포 성공 (BE)\\n- App: %s\\n- Container: %s\\n- Port: %s\\n- Image: %s\\n- Build: #%s\\n- URL: %s"}' \
                        "$APP_NAME" "$CONTAINER" "$PORT" "$IMAGE" "$BUILD_NUMBER" "$BUILD_URL")
                    curl -sS -o /dev/null -w "discord http=%{http_code}\n" \
                        -H "Content-Type: application/json" -X POST -d "$payload" "$DISCORD_WEBHOOK"
                ''')
            }
        }
    }

    post {
        failure {
            sh(label: 'discord notify failure', script: '''
                set -e
                payload=$(printf '{"content":"❌ 파이프라인 실패\\n- App: %s\\n- Job: %s\\n- Build: #%s\\n- URL: %s\\n- Console에서 실패 지점 확인 ㄱㄱ"}' \
                    "$APP_NAME" "$JOB_NAME" "$BUILD_NUMBER" "$BUILD_URL")
                curl -sS -o /dev/null -w "discord http=%{http_code}\n" \
                     -H "Content-Type: application/json" -X POST -d "$payload" "$DISCORD_WEBHOOK"
            ''')
        }
        success {
            sh(label: 'discord notify success', script: '''
                set -e
                payload=$(printf '{"content":"✅ 파이프라인 전체 성공\\n- App: %s\\n- Job: %s\\n- Build: #%s\\n- URL: %s"}' \
                    "$APP_NAME" "$JOB_NAME" "$BUILD_NUMBER" "$BUILD_URL")
                curl -sS -o /dev/null -w "discord http=%{http_code}\n" \
                    -H "Content-Type: application/json" -X POST -d "$payload" "$DISCORD_WEBHOOK"
            ''')
        }
    }
}
