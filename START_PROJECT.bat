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

:: Остановка предыдущих Java процессов на портах 8081 и 8082
echo [0/7] Остановка предыдущих процессов...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8081 ^| findstr LISTENING 2^>nul') do taskkill /F /PID %%a 2>nul
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8082 ^| findstr LISTENING 2^>nul') do taskkill /F /PID %%a 2>nul
echo [OK] Порты освобождены
echo.

:: Настройка Java
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.9.10-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%

:: Проверка Java
echo [1/7] Проверка Java...
java -version 2>nul
if errorlevel 1 (
    echo [ОШИБКА] Java не найдена! Установите Java 21.
    pause
    exit /b 1
)
echo [OK] Java найдена
echo.

:: Проверка Docker
echo [2/7] Проверка Docker...
docker info >nul 2>&1
if errorlevel 1 (
    echo [ОШИБКА] Docker не запущен! Запустите Docker Desktop.
    pause
    exit /b 1
)
echo [OK] Docker запущен
echo.

:: Запуск инфраструктуры
echo [3/7] Запуск PostgreSQL, Redis, Kafka...
docker-compose up -d auth-db task-db redis zookeeper kafka
if errorlevel 1 (
    echo [ОШИБКА] Не удалось запустить контейнеры!
    pause
    exit /b 1
)
echo [OK] Контейнеры запущены
echo.

:: Ожидание готовности контейнеров
echo [4/7] Ожидание готовности сервисов (20 сек)...
timeout /t 20 /nobreak >nul
echo [OK] Сервисы готовы
echo.

:: Установка Maven модулей
echo [5/7] Установка Maven модулей...
call mvn install -N -DskipTests -Dmaven.test.skip=true -q 2>nul
call mvn install -pl common -DskipTests -Dmaven.test.skip=true -q 2>nul
echo [OK] Модули установлены
echo.

:: Открытие браузера с вкладками
echo [6/7] Открытие браузера...
timeout /t 2 /nobreak >nul
start "" "http://localhost:8081/actuator/health"
timeout /t 1 /nobreak >nul
start "" "http://localhost:8082/actuator/health"
timeout /t 1 /nobreak >nul
start "" "http://localhost:9090"
timeout /t 1 /nobreak >nul
start "" "http://localhost:3000"
echo [OK] Вкладки браузера открыты
echo.

echo ╔══════════════════════════════════════════════════════════════╗
echo ║                    ЗАПУСК СЕРВИСОВ                          ║
echo ║                                                              ║
echo ║  Ссылки для проверки:                                       ║
echo ║  • Auth Service:  http://localhost:8081/actuator/health     ║
echo ║  • Task Service:  http://localhost:8082/actuator/health     ║
echo ║  • Prometheus:    http://localhost:9090                     ║
echo ║  • Grafana:       http://localhost:3000 (admin/admin)       ║
echo ║                                                              ║
echo ║  API Endpoints:                                              ║
echo ║  • Регистрация:   POST http://localhost:8081/api/v1/auth/register ║
echo ║  • Вход:          POST http://localhost:8081/api/v1/auth/login    ║
echo ║  • Проекты:       GET  http://localhost:8082/api/projects   ║
echo ║                                                              ║
echo ║  Для остановки: закройте это окно или нажмите Ctrl+C        ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

:: Настройка переменных окружения для сервисов
set DB_PORT=5433
set KAFKA_SERVERS=localhost:9092

echo [7/7] Запуск Auth Service и Task Service...
echo.

:: Запуск Auth Service в фоне
start "Auth Service (8081)" cmd /c "cd /d "%~dp0" && set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.9.10-hotspot && set DB_PORT=5433 && mvn spring-boot:run -pl auth-service -DskipTests -Dmaven.test.skip=true"

:: Небольшая задержка перед запуском второго сервиса
timeout /t 10 /nobreak >nul

:: Запуск Task Service в текущем окне (при закрытии - всё остановится)
set DB_PORT=5434
set KAFKA_SERVERS=localhost:9092
call mvn spring-boot:run -pl task-service -DskipTests -Dmaven.test.skip=true

:: После остановки - остановить всё
echo.
echo Остановка сервисов...
taskkill /FI "WINDOWTITLE eq Auth Service*" /F 2>nul
echo Остановка контейнеров...
docker-compose down
echo Готово!
pause
