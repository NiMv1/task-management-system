@echo off
chcp 65001 >nul
title Task Management System - Запуск проекта
color 0B

echo ╔══════════════════════════════════════════════════════════════╗
echo ║         TASK MANAGEMENT SYSTEM - ЗАПУСК ПРОЕКТА             ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

:: Переход в директорию проекта
cd /d "%~dp0"

:: Остановка предыдущих Java процессов на портах
echo [0/8] Остановка предыдущих процессов...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8080 ^| findstr LISTENING 2^>nul') do taskkill /F /PID %%a 2>nul
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8081 ^| findstr LISTENING 2^>nul') do taskkill /F /PID %%a 2>nul
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8082 ^| findstr LISTENING 2^>nul') do taskkill /F /PID %%a 2>nul
echo [OK] Порты освобождены
echo.

:: Настройка Java
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.9.10-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%

:: Проверка Java
echo [1/8] Проверка Java...
java -version 2>nul
if errorlevel 1 (
    echo [ОШИБКА] Java не найдена! Установите Java 21.
    pause
    exit /b 1
)
echo [OK] Java найдена
echo.

:: Проверка Docker
echo [2/8] Проверка Docker...
docker info >nul 2>&1
if errorlevel 1 (
    echo [ОШИБКА] Docker не запущен! Запустите Docker Desktop.
    pause
    exit /b 1
)
echo [OK] Docker запущен
echo.

:: Запуск ВСЕЙ инфраструктуры (включая Prometheus, Grafana, ELK)
echo [3/8] Запуск всей инфраструктуры Docker...
docker-compose up -d
if errorlevel 1 (
    echo [ОШИБКА] Не удалось запустить контейнеры!
    pause
    exit /b 1
)
echo [OK] Все контейнеры запущены
echo.

:: Ожидание готовности контейнеров
echo [4/8] Ожидание готовности сервисов (30 сек)...
timeout /t 30 /nobreak >nul
echo [OK] Сервисы готовы
echo.

:: Установка Maven модулей
echo [5/8] Установка Maven модулей...
call mvn install -N -DskipTests -Dmaven.test.skip=true -q 2>nul
call mvn install -pl common -DskipTests -Dmaven.test.skip=true -q 2>nul
echo [OK] Модули установлены
echo.

:: Настройка переменных окружения для сервисов
set SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/auth_db
set SPRING_DATASOURCE_USERNAME=postgres
set SPRING_DATASOURCE_PASSWORD=postgres
set SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092
set SPRING_DATA_REDIS_HOST=localhost
set SPRING_DATA_REDIS_PORT=6379

echo [6/8] Запуск Auth Service...
start "Auth Service (8081)" cmd /c "cd /d "%~dp0" && set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.9.10-hotspot && set SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/auth_db && set SPRING_DATASOURCE_USERNAME=postgres && set SPRING_DATASOURCE_PASSWORD=postgres && mvn spring-boot:run -pl auth-service -DskipTests"

:: Ожидание запуска Auth Service
timeout /t 20 /nobreak >nul

echo [7/8] Запуск Task Service...
start "Task Service (8082)" cmd /c "cd /d "%~dp0" && set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.9.10-hotspot && set SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5434/task_db && set SPRING_DATASOURCE_USERNAME=postgres && set SPRING_DATASOURCE_PASSWORD=postgres && set SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092 && mvn spring-boot:run -pl task-service -DskipTests"

:: Ожидание запуска Task Service
timeout /t 15 /nobreak >nul

:: Открытие браузера с вкладками
echo [8/8] Открытие браузера...
timeout /t 5 /nobreak >nul
start "" "http://localhost:8081/swagger-ui.html"
timeout /t 1 /nobreak >nul
start "" "http://localhost:8082/swagger-ui.html"
timeout /t 1 /nobreak >nul
start "" "http://localhost:9090"
timeout /t 1 /nobreak >nul
start "" "http://localhost:3000"
timeout /t 1 /nobreak >nul
start "" "http://localhost:5601"
echo [OK] Вкладки браузера открыты
echo.

echo ╔══════════════════════════════════════════════════════════════╗
echo ║              TASK MANAGEMENT SYSTEM ЗАПУЩЕН!                ║
echo ╠══════════════════════════════════════════════════════════════╣
echo ║  Сервисы:                                                    ║
echo ║  • API Gateway:     http://localhost:8080                   ║
echo ║  • Auth Service:    http://localhost:8081                   ║
echo ║  • Task Service:    http://localhost:8082                   ║
echo ╠══════════════════════════════════════════════════════════════╣
echo ║  Swagger UI:                                                 ║
echo ║  • Auth Swagger:    http://localhost:8081/swagger-ui.html   ║
echo ║  • Task Swagger:    http://localhost:8082/swagger-ui.html   ║
echo ╠══════════════════════════════════════════════════════════════╣
echo ║  Мониторинг:                                                 ║
echo ║  • Prometheus:      http://localhost:9090                   ║
echo ║  • Grafana:         http://localhost:3000 (admin/admin)     ║
echo ║  • Kibana:          http://localhost:5601                   ║
echo ╠══════════════════════════════════════════════════════════════╣
echo ║  Health Check:                                               ║
echo ║  • Auth Health:     http://localhost:8081/actuator/health   ║
echo ║  • Task Health:     http://localhost:8082/actuator/health   ║
echo ╠══════════════════════════════════════════════════════════════╣
echo ║  Для остановки: нажмите любую клавишу в этом окне           ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

pause

:: После остановки - остановить всё
echo.
echo Остановка сервисов...
taskkill /FI "WINDOWTITLE eq Auth Service*" /F 2>nul
taskkill /FI "WINDOWTITLE eq Task Service*" /F 2>nul
echo Остановка контейнеров...
docker-compose down
echo Готово!
pause
