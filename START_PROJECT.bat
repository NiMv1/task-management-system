@echo off
chcp 65001 >nul
title Task Management System
color 0B

:: Обработка закрытия консоли (Ctrl+C или закрытие окна)
if "%~1"=="cleanup" goto :cleanup

echo ╔══════════════════════════════════════════════════════════════╗
echo ║         TASK MANAGEMENT SYSTEM - ЗАПУСК ПРОЕКТА             ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

:: Переход в директорию проекта
cd /d "%~dp0"

:: Остановка предыдущих Java процессов на портах
echo [0/5] Остановка предыдущих процессов...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8080 ^| findstr LISTENING 2^>nul') do (
    echo Останавливаю процесс на порту 8080, PID: %%a
    taskkill /F /PID %%a >nul 2>&1
)
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8081 ^| findstr LISTENING 2^>nul') do (
    echo Останавливаю процесс на порту 8081, PID: %%a
    taskkill /F /PID %%a >nul 2>&1
)
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8082 ^| findstr LISTENING 2^>nul') do (
    echo Останавливаю процесс на порту 8082, PID: %%a
    taskkill /F /PID %%a >nul 2>&1
)
:: Ожидание освобождения портов
timeout /t 3 /nobreak >nul
echo [OK] Порты освобождены
echo.

:: Настройка Java 17
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot"
set "PATH=%JAVA_HOME%\bin;%PATH%"
echo [INFO] Используется Java 17

:: Проверка Java
echo [1/5] Проверка Java...
java -version 2>nul
if errorlevel 1 (
    echo [ОШИБКА] Java не найдена!
    pause
    exit /b 1
)
echo [OK] Java найдена
echo.

:: Проверка Docker
echo [2/5] Проверка Docker...
docker info >nul 2>&1
if errorlevel 1 (
    echo [ОШИБКА] Docker не запущен! Запустите Docker Desktop.
    pause
    exit /b 1
)
echo [OK] Docker запущен
echo.

echo [3/5] Сборка сервисов и пересборка Docker образов...
call mvn -q clean package -pl auth-service,task-service -DskipTests
if errorlevel 1 (
    echo [ОШИБКА] Не удалось собрать сервисы (mvn package)!
    pause
    exit /b 1
)
:: docker-compose build может писать в stderr даже при успехе, поэтому не проверяем errorlevel
docker-compose build --no-cache auth-service task-service 2>nul
echo [OK] Образы пересобраны
echo.

:: Запуск инфраструктуры
echo [4/5] Запуск инфраструктуры Docker...
docker-compose up -d
if errorlevel 1 (
    echo [ОШИБКА] Не удалось запустить контейнеры!
    pause
    exit /b 1
)
echo [OK] Контейнеры запущены
echo.

:: Ожидание готовности сервисов
echo [5/5] Ожидание готовности сервисов (40 сек)...
timeout /t 40 /nobreak >nul
echo [OK] Сервисы запущены
echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║              TASK MANAGEMENT SYSTEM ЗАПУЩЕН!                ║
echo ╠══════════════════════════════════════════════════════════════╣
echo ║  Сервисы (запущены в Docker):                               ║
echo ║  • Auth Service:    http://localhost:8081                   ║
echo ║  • Task Service:    http://localhost:8082                   ║
echo ║  • API Gateway:     http://localhost:8080                   ║
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
echo ║  Для остановки: нажмите любую клавишу                       ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

:: Открытие браузера - все страницы
start "" "http://localhost:8081/swagger-ui.html"
timeout /t 1 /nobreak >nul
start "" "http://localhost:8082/swagger-ui.html"
timeout /t 1 /nobreak >nul
start "" "http://localhost:9090"
timeout /t 1 /nobreak >nul
start "" "http://localhost:3000"
timeout /t 1 /nobreak >nul
start "" "http://localhost:5601"

:: Ожидание нажатия клавиши для остановки
echo.
echo Нажмите любую клавишу для остановки сервисов...
pause >nul

:cleanup
:: Остановка всех контейнеров
echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║              ОСТАНОВКА СЕРВИСОВ...                          ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.
echo Остановка контейнеров Docker...
cd /d "%~dp0"
docker-compose down
echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                    ВСЕ СЕРВИСЫ ОСТАНОВЛЕНЫ                   ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.
echo Нажмите любую клавишу для закрытия окна...
pause >nul
