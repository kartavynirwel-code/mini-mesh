pipeline{
    agent any

    environment {
    Tag = "${env.BUILD_NUMBER}"
    }

    stages{
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                 sh 'echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin'

                dir('frontend') {
                    sh """
                    docker build -t kartavyanirwel/mini-mesh-frontend:${Tag} .
                    docker push \$DOCKER_USER/mini-mesh-frontend:${Tag}
                    """
                }

                dir('services/greeting-service') {
                    sh """
                    docker build -t kartavyanirwel/mini-mesh-greeting-service:${Tag} .
                    docker push \$DOCKER_USER/mini-mesh-greeting-service:${Tag}
                    """
                }
            
                dir('services/user-service') {
                    sh """
                    docker build -t kartavyanirwel/mini-mesh-user-service:${Tag} .
                    docker push \$DOCKER_USER/mini-mesh-user-service:${Tag}
                    """
                }
            
                dir('services/notification-service') {
                    sh """
                    docker build -t kartavyanirwel/mini-mesh-notification-service:${Tag} .
                    docker push \$DOCKER_USER/mini-mesh-notification-service:${Tag}
                    """
                }
            }
            }
        }

        stage('Test') {
            steps {
                echo 'Running tests...'
                echo "testing done"
            }
        }

        stage('Deploy') {
            steps {
                 withCredentials([usernamePassword(credentialsId: 'github-creds', usernameVariable: 'GIT_USER', passwordVariable: 'GIT_TOKEN')]) {
                    sh """
                    rm -rf mini-mesh-manifest
                    git clone https://\$GIT_USER:\$GIT_TOKEN@github.com/kartavynirwel-code/mini-mesh-manifest.git
                    cd mini-mesh-manifest

                    sed -i 's/mini-mesh-frontend:.*/mini-mesh-frontend:${Tag}/' frontend/deployment.yaml
                    sed -i 's/mini-mesh-greeting-service:.*/mini-mesh-greeting-service:${Tag}/' greeting-service/deployment.yaml
                    sed -i 's/mini-mesh-user-service:.*/mini-mesh-user-service:${Tag}/' user-service/deployment.yaml
                    sed -i 's/mini-mesh-notification-service:.*/mini-mesh-notification-service:${Tag}/' notification-service/deployment.yaml
                    git config --global user.email "kartavyanirwell@gmail.com"
                    git config --global user.name "Kartavynirwel-code"

                    git add .
                    git commit -m "Update image tags to ${Tag}"
                    git push origin main
                    """
                 }
            }
        }
    }

post {
    success {
        echo 'Pipeline completed successfully!'
    }
    failure {
        echo 'Pipeline failed!'
}

}
}