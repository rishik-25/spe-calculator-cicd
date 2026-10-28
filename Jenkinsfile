pipeline{
    agent any

    options {
        disableConcurrentBuilds()
        timeout(time: 20, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '15'))
    }

    environment {
        // Pin Java 17 (the Jenkins service itself runs on Java 21).
        JAVA_HOME      = '/usr/lib/jvm/java-17-openjdk-amd64'
        PATH           = "${JAVA_HOME}/bin:${env.PATH}"

        // Image naming: shared with docker-compose.yml via these variables.
        DOCKERHUB_USER = 'rishik0425'
        IMAGE_REPO     = "${DOCKERHUB_USER}/spe-calculator"
        IMAGE_TAG      = "${env.BUILD_NUMBER}"

        HOST_PORT      = '8081'
        NOTIFY_EMAIL   = 'rishikpatha17@gmail.com'
    }
  stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    env.GIT_SHORT = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
                }
                sh 'git log -1 --pretty=format:"%h %s (%an)"'
            }
        }

        stage('Build') {
            steps {
                sh './mvnw -B clean package -DskipTests'
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/calculator-api.jar', fingerprint: true
                }
            }
        }

      stage('Test') {
            steps {
                // A failing test makes Maven exit non-zero, which fails the build.
                sh './mvnw -B test'
            }
            post {
                always {
                    // Publish the JUnit XML reports even when tests fail.
                    junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: false
                }
            }
        }

        stage('Docker Build') {
            steps {
                sh '''
                    docker build -t "$IMAGE_REPO:$IMAGE_TAG" -t "$IMAGE_REPO:latest" .
                    docker image ls "$IMAGE_REPO"
                '''
            }
        }
      stage('Push') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials',
                                                  usernameVariable: 'DH_USER',
                                                  passwordVariable: 'DH_TOKEN')]) {
                    sh '''
                        echo "$DH_TOKEN" | docker login -u "$DH_USER" --password-stdin
                        docker push "$IMAGE_REPO:$IMAGE_TAG"
                        docker push "$IMAGE_REPO:latest"
                    '''
                }
            }
            post {
                always {
                    sh 'docker logout || true'
                }
            }
        }
      stage('Deploy') {
            steps {
                sh '''
                    docker compose config -q
                    docker compose pull calculator-api
                    docker compose up -d --remove-orphans
                    docker compose ps
                '''
                sh '''
                    set +x
                    api=missing; ngx=missing
                    for i in $(seq 1 30); do
                        api=$(docker inspect --format '{{.State.Health.Status}}' calculator-api 2>/dev/null || echo missing)
                        ngx=$(docker inspect --format '{{.State.Health.Status}}' calculator-nginx 2>/dev/null || echo missing)
                        echo "attempt $i/30: api=$api nginx=$ngx"
                        if [ "$api" = "healthy" ] && [ "$ngx" = "healthy" ]; then break; fi
                        sleep 5
                    done
                    if [ "$api" != "healthy" ] || [ "$ngx" != "healthy" ]; then
                        echo "Containers did not become healthy"
                        docker compose ps
                        docker compose logs --tail 50
                        exit 1
                    fi
                    curl -fsS "http://localhost:$HOST_PORT/actuator/health" | grep -q '"status":"UP"'
                    curl -fsS "http://localhost:$HOST_PORT/api/v1/add?a=2&b=3" | grep -q '"result":5'
                    echo "Deployment verified through Nginx on port $HOST_PORT"
                '''
            }
        }
    }
  post {
        success {
            mail to: env.NOTIFY_EMAIL,
                 subject: "[SPE Calculator] SUCCESS - build #${env.BUILD_NUMBER}",
                 body: """Build #${env.BUILD_NUMBER} of ${env.JOB_NAME} succeeded.

Commit:   ${env.GIT_SHORT}
Image:    ${env.IMAGE_REPO}:${env.IMAGE_TAG}
Running:  behind Nginx on http://localhost:${env.HOST_PORT}
Details:  ${env.BUILD_URL}
"""
        }
        failure {
            mail to: env.NOTIFY_EMAIL,
                 subject: "[SPE Calculator] FAILED - build #${env.BUILD_NUMBER}",
                 body: """Build #${env.BUILD_NUMBER} of ${env.JOB_NAME} FAILED.

Commit:   ${env.GIT_SHORT}
Console:  ${env.BUILD_URL}console
Tests:    ${env.BUILD_URL}testReport
"""
        }
        always {
            sh 'docker logout || true'
            sh 'docker image prune -f || true'
        }
    }
}
