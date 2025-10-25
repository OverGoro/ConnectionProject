FROM openjdk:21-jdk-slim
WORKDIR /app
COPY device-auth-service.jar app.jar

COPY device-auth-service.jar /app/device-auth-service.jar
COPY application.properties /app/application.properties
COPY logback-spring.xml /app/logback-spring.xml


RUN mkdir -p /app/logs

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "device-auth-service.jar", \
    "--spring.config.location=file:./application.properties", \
    "--logging.config=file:./logback-spring.xml"]
