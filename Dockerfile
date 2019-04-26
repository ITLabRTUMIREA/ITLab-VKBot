FROM maven:3.5.4-jdk-8 as builder

ENV SERVICE_DIR /opt/service
WORKDIR ${SERVICE_DIR}
COPY . .
RUN mvn package


FROM openjdk:8-jdk
EXPOSE 8080
ENV SERVICE_DIR /opt/service
ENV SERVICE_JAR ItLabMirea-1.0-SNAPSHOT.jar
RUN mkdir -p ${SERVICE_DIR}
WORKDIR ${SERVICE_DIR}
COPY --from=builder ${SERVICE_DIR}/target/${SERVICE_JAR} .
COPY src/htmlPageForEmail/notification.html src/htmlPageForEmail/notification.html
CMD java ${JAVA_OPTS} \
    -server -Xms128m -Xmx128m \
    -Djava.security.egd=file:/dev/./urandom \
    -jar ${SERVICE_DIR}/${SERVICE_JAR}
