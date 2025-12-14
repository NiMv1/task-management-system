@echo off
chcp 65001 >nul
title Task Management System
color 0B

echo ╔══════════════════════════════════════════════════════════════╗
echo ║         TASK MANAGEMENT SYSTEM - ЗАПУСК ПРОЕКТА             ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

:: Переход в директорию проекта
cd /d "%~dp0"

:: Остановка предыдущих Java процессов на портах
echo [0/6] Остановка предыдущих процессов...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8080 ^| findstr LISTENING 2^>nul') do taskkill /F /PID %%a 2>nul
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8081 ^| findstr LISTENING 2^>nul') do taskkill /F /PID %%a 2>nul
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8082 ^| findstr LISTENING 2^>nul') do taskkill /F /PID %%a 2>nul
echo [OK] Порты освобождены
echo.

:: Настройка Java 17
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot"
set "PATH=%JAVA_HOME%\bin;%PATH%"
echo [INFO] Используется Java 17

:: Проверка Java
echo [1/6] Проверка Java...
java -version 2>nul
if errorlevel 1 (
    echo [ОШИБКА] Java не найдена!
    pause
    exit /b 1
)
echo [OK] Java найдена
echo.

:: Проверка Docker
echo [2/6] Проверка Docker...
docker info >nul 2>&1
if errorlevel 1 (
    echo [ОШИБКА] Docker не запущен! Запустите Docker Desktop.
    pause
    exit /b 1
)
echo [OK] Docker запущен
echo.

:: Запуск инфраструктуры
echo [3/6] Запуск инфраструктуры Docker...
docker-compose up -d
if errorlevel 1 (
    echo [ОШИБКА] Не удалось запустить контейнеры!
    pause
    exit /b 1
)
echo [OK] Контейнеры запущены
echo.

:: Ожидание готовности
echo [4/6] Ожидание готовности сервисов (25 сек)...
timeout /t 25 /nobreak >nul
echo [OK] Сервисы готовы
echo.

:: Установка Maven модулей
echo [5/6] Установка Maven модулей...
call mvn install -N -DskipTests -q 2>nul
call mvn install -pl common -DskipTests -q 2>nul
echo [OK] Модули установлены
echo.

:: Настройка переменных окружения
set SPRING_DATASOURCE_USERNAME=postgres
set SPRING_DATASOURCE_PASSWORD=postgres
set SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092
set SPRING_DATA_REDIS_HOST=localhost
set SPRING_DATA_REDIS_PORT=6379

echo [6/6] Запуск сервисов (в этом окне)...
echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║  Auth Swagger:  http://localhost:8081/swagger-ui.html       ║
echo ║  Task Swagger:  http://localhost:8082/swagger-ui.html       ║
echo ║  Prometheus:    http://localhost:9090                       ║
echo ║  Grafana:       http://localhost:3000 (admin/admin)         ║
echo ╠══════════════════════════════════════════════════════════════╣
echo ║  Для остановки: закройте это окно или нажмите Ctrl+C        ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

:: Открытие браузера
start "" "http://localhost:8081/swagger-ui.html"

:: Запуск Auth Service в фоне
echo Запуск Auth Service на порту 8081...
set SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/auth_db
start /B "" mvn spring-boot:run -pl auth-service -DskipTests

:: Ожидание запуска Auth Service
timeout /t 30 /nobreak >nul

:: Запуск Task Service в текущем окне (блокирующий)
echo Запуск Task Service на порту 8082...
set SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5434/task_db
mvn spring-boot:run -pl task-service -DskipTests

:: После остановки (Ctrl+C или закрытие окна) - остановить всё
echo.
echo Остановка сервисов...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8081 ^| findstr LISTENING 2^>nul') do taskkill /F /PID %%a 2>nul
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8082 ^| findstr LISTENING 2^>nul') do taskkill /F /PID %%a 2>nul
echo Остановка контейнеров Docker...
docker-compose down
echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                    ВСЕ СЕРВИСЫ ОСТАНОВЛЕНЫ                   ║
echo ╚══════════════════════════════════════════════════════════════╝
pause
