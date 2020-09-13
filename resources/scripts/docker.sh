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

API_SPEC_PATH=`pwd`/docs
SWAGGER_PORT=8091

echo ">> starting docker container for swagger..."

docker run -d \
  -p ${SWAGGER_PORT}:8080 \
  -e BASE_URL=/swagger \
  -e SWAGGER_JSON=/tmp/api-spec.yml \
  -v ${API_SPEC_PATH}:/tmp \
  swaggerapi/swagger-ui

echo "Docker container for swagger started on port ${SWAGGER_PORT} successfully!"