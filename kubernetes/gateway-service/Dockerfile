FROM eclipse-temurin:21-jre
WORKDIR /app
COPY gateway-service.jar /app/gateway-service.jar
COPY application.properties /app/application.properties
COPY logback-spring.xml /app/logback-spring.xml


RUN mkdir -p /app/logs

EXPOSE 8080

# Команда запуска
ENTRYPOINT ["java", "-jar", "gateway-service.jar", \
    "--spring.config.location=file:./application.properties", \
    "--logging.config=file:./logback-spring.xml"]
