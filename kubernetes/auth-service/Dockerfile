FROM openjdk:21-jdk-slim
WORKDIR /app
COPY auth-service.jar app.jar

COPY auth-service.jar /app/auth-service.jar
COPY application.properties /app/application.properties
COPY logback-spring.xml /app/logback-spring.xml

RUN mkdir -p /app/logs

EXPOSE 8080

# Команда запуска
ENTRYPOINT ["java", "-jar", "auth-service.jar", \
    "--spring.config.location=file:./application.properties", \
    "--logging.config=file:./logback-spring.xml"]
