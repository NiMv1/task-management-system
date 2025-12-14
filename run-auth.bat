@echo off
:: Вспомогательный скрипт для запуска Auth Service
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot"
set "PATH=%JAVA_HOME%\bin;%PATH%"
set SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/auth_db
set SPRING_DATASOURCE_USERNAME=postgres
set SPRING_DATASOURCE_PASSWORD=postgres
cd /d "%~dp0"
mvn spring-boot:run -pl auth-service -DskipTests
