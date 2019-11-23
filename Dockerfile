FROM openjdk:8-jdk-alpine
EXPOSE 8080
ADD /build/libs/vk_bot-0.0.1.jar vk_bot-0.0.1.jar
ENTRYPOINT ["java", "-jar", "vk_bot-0.0.1.jar"]