FROM gradle:4.7.0-jdk8-alpine as build

ENV SERVICE_DIR /opt/service
#WORKDIR ${SERVICE_DIR}
#COPY . .

USER root
 RUN   apk update \
  &&   apk add ca-certificates wget \
  &&   update-ca-certificates

COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --stacktrace

FROM openjdk:8-jdk-alpine
EXPOSE 8080
COPY --from=build /home/gradle/src/build/libs/vk_bot-0.0.1.jar vk_bot-0.0.1.jar
ENTRYPOINT ["java", "-jar", "vk_bot-0.0.1.jar"]