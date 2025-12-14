# Этап запуска (сборка выполняется локально через mvn package)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Создаём пользователя для безопасности
RUN addgroup -g 1001 -S appgroup && adduser -u 1001 -S appuser -G appgroup
USER appuser

# Копируем исполняемый jar файл (собранный локально через mvn package)
COPY target/notification-service-1.0.0.jar app.jar

# Настройки JVM для контейнера
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

# Healthcheck
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8083/actuator/health || exit 1

EXPOSE 8083

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
