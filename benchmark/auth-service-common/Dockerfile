FROM eclipse-temurin:21-jre
WORKDIR /app
COPY auth-service-1.0.0.jar /app/auth-service-1.0.0.jar
COPY application.properties /app/application.properties
COPY logback-spring.xml /app/logback-spring.xml

RUN mkdir -p /app/logs

EXPOSE 8081

# Добавляем JMX и Prometheus метрики
ENTRYPOINT ["java", "-jar", "auth-service-1.0.0.jar", \
    "--spring.config.location=file:./application.properties", \
    "--logging.config=file:./logback-spring.xml", \
    "--management.endpoints.web.exposure.include=health,metrics,prometheus", \
    "--management.metrics.export.prometheus.enabled=true"]
