# Этап сборки
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Копируем родительский pom
COPY ../pom.xml ../pom.xml

# Копируем pom и исходники сервиса
COPY pom.xml .
COPY src ./src

# Собираем проект
RUN mvn clean package -DskipTests

# Этап запуска
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Создаём пользователя для безопасности
RUN addgroup -g 1001 -S appgroup && adduser -u 1001 -S appuser -G appgroup
USER appuser

# Копируем jar файл
COPY --from=build /app/target/*.jar app.jar

# Настройки JVM для контейнера
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

# Healthcheck
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
