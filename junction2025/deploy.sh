#!/bin/bash

# ====== 설정 ======
APP_NAME="keonheehan/junction2025:1.0.0"
VERSION="1.0.0"
DOCKER_USER="keonheehan"
EC2_HOST="ec2-user@16.171.179.85"    # Ubuntu면 ubuntu@<EC2-IP>
PEM_PATH="/Users/keonheehan/Downloads/junction2025-key.pem"
PORT=8080

# ====== 1. jar 파일 빌드 ======
echo "🧱 Building Spring Boot jar..."
./gradlew clean build -x test

# ====== 2. Docker 이미지 빌드 ======
echo "🐳 Building Docker image..."
docker build --platform linux/amd64 --build-arg DEPENDENCY=build/dependency --tag $APP_NAME .

# ====== 3. DockerHub 푸시 ======
echo "📦 Pushing image to DockerHub..."
docker push $APP_NAME

# ====== 4. EC2 접속 및 배포 ======
echo "🚀 Deploying on EC2..."
ssh -i $PEM_PATH $EC2_HOST << EOF
  echo "🛑 Stopping old container..."
  docker stop $APP_NAME || true
  docker rm $APP_NAME || true

  echo "🧹 Removing old image..."
  docker rmi $APP_NAME || true

  echo "📥 Pulling new image..."
  docker pull $APP_NAME

  echo "🔧 Running new container..."
  docker run -i -t -p 8080:8080 $APP_NAME &

  echo "✅ Deployment complete!"
EOF

# 16.171.179.85
# teamgo.store
