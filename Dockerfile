FROM gradle:5.6.4-r0-jdk8-alpine

ENV SERVICE_DIR /opt/service
WORKDIR ${SERVICE_DIR}
COPY . .

RUN ./gradlew build --stacktrace