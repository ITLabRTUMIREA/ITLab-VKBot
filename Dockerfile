FROM gradle:4.0.1-jre8-alpine as builder

ENV SERVICE_DIR /opt/service
WORKDIR ${SERVICE_DIR}

USER root
 RUN   apk update \
  &&   apk add ca-certificates wget \
  &&   update-ca-certificates \
  &&   gradle \
  &&   run gradle build

FROM openjdk:8-jdk-alpine
EXPOSE 8080
COPY /build/libs/vk_bot-0.0.1.jar vk_bot-0.0.1.jar
ENTRYPOINT ["java", "-jar", "vk_bot-0.0.1.jar"]