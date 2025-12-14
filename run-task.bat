@echo off
:: Вспомогательный скрипт для запуска Task Service
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot"
set "PATH=%JAVA_HOME%\bin;%PATH%"
set SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5434/task_db
set SPRING_DATASOURCE_USERNAME=postgres
set SPRING_DATASOURCE_PASSWORD=postgres
set SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092
cd /d "%~dp0"
mvn spring-boot:run -pl task-service -DskipTests
