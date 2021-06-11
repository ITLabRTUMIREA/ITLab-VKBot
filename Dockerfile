FROM gradle:7.0.2-jdk8 as build

ENV SERVICE_DIR /opt/service
#WORKDIR ${SERVICE_DIR}
#COPY . .

USER root
# RUN   apk update \
#  &&   apk add ca-certificates wget \
#  &&   update-ca-certificates

COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --stacktrace

FROM openjdk:8-jdk-alpine
EXPOSE 8080
COPY --from=build /home/gradle/src/build/libs/notify-service-0.0.1.jar notify-service-0.0.1.jar
ENTRYPOINT ["java", "-jar", "notify-service-0.0.1.jar"]