#!/usr/bin/env bash
echo " ----------------------------------------- "
echo "|            Building Image               |"
echo " ----------------------------------------- "

REGISTRY=shoresea
IMAGE=internal-transfers-api
TAG=$(git rev-parse --short=6 HEAD)

echo ">> building the fat jar..."
./gradlew clean build

echo ">> building docker image..."
docker build \
  -t ${REGISTRY}/${IMAGE}:${TAG} \
  -t ${REGISTRY}/${IMAGE}:latest .

echo "Docker image built successfully!"

echo " ----------------------------------------- "
echo "|      Running Docker Container           |"
echo " ----------------------------------------- "

echo ">> starting container with ${IMAGE} image..."

PORT=8090

docker run -d \
  -p ${PORT}:8090 \
  -e DEPLOY_ENV=dev \
  ${REGISTRY}/${IMAGE}:latest

echo "Docker container started on port ${PORT} successfully!"
