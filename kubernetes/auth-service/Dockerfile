FROM eclipse-temurin:21-jdk-alpine

# Создаем пользователя для безопасности
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Рабочая директория
WORKDIR /app

# Копируем JAR файл
COPY --chown=spring:spring auth-service.jar /app/auth-service.jar

# Копируем конфигурационные файлы
COPY --chown=spring:spring application.properties /app/application.properties
COPY --chown=spring:spring logback-spring.xml /app/logback-spring.xml

# Создаем директорию для логов
RUN mkdir -p /app/logs && chown spring:spring /app/logs

# Порт приложения
EXPOSE 8080

# Команда запуска
CMD ["java", "-jar", "auth-service.jar", \
    "--spring.config.location=file:./application.properties", \
    "--logging.config=file:./logback-spring.xml"]
