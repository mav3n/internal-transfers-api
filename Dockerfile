#FROM openjdk:8-jre-alpine
FROM openjdk:11-jre-slim

MAINTAINER shoresea

ENV TARGET_PATH=/apps
ENV DEPLOY_ENV=dev
ENV SWAGGER_PORT=8091

WORKDIR $TARGET_PATH

RUN mkdir -p /apps/logs

COPY ./build/libs/internal-transfers-api-1.0.0-all.jar  /apps/internal-transfers-api-all.jar

CMD ["sh", "-c", "java -jar /apps/internal-transfers-api-all.jar"]

VOLUME /apps/logs

