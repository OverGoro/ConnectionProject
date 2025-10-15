FROM openjdk:21-jdk-slim
WORKDIR /app
COPY device-service.jar app.jar

COPY device-service.jar /app/device-service.jar
COPY application.properties /app/application.properties
COPY logback-spring.xml /app/logback-spring.xml


RUN mkdir -p /app/logs

EXPOSE 8080

# Команда запуска
ENTRYPOINT ["java", "-jar", "device-service.jar", \
    "--spring.config.location=file:./application.properties", \
    "--logging.config=file:./logback-spring.xml"]
