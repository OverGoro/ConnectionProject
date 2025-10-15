FROM openjdk:21-jdk-slim
WORKDIR /app
COPY connectionscheme-service.jar app.jar

COPY connectionscheme-service.jar /app/connectionscheme-service.jar
COPY application.properties /app/application.properties
COPY logback-spring.xml /app/logback-spring.xml


RUN mkdir -p /app/logs

EXPOSE 8080

# Команда запуска
ENTRYPOINT ["java", "-jar", "connectionscheme-service.jar", \
    "--spring.config.location=file:./application.properties", \
    "--logging.config=file:./logback-spring.xml"]
