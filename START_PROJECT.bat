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
echo [0/4] Остановка предыдущих процессов...
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
echo [1/4] Проверка Java...
java -version 2>nul
if errorlevel 1 (
    echo [ОШИБКА] Java не найдена!
    pause
    exit /b 1
)
echo [OK] Java найдена
echo.

:: Проверка Docker
echo [2/4] Проверка Docker...
docker info >nul 2>&1
if errorlevel 1 (
    echo [ОШИБКА] Docker не запущен! Запустите Docker Desktop.
    pause
    exit /b 1
)
echo [OK] Docker запущен
echo.

:: Запуск инфраструктуры
echo [3/4] Запуск инфраструктуры Docker...
docker-compose up -d
if errorlevel 1 (
    echo [ОШИБКА] Не удалось запустить контейнеры!
    pause
    exit /b 1
)
echo [OK] Контейнеры запущены
echo.

:: Ожидание готовности сервисов
echo [4/4] Ожидание готовности сервисов (40 сек)...
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

:: Открытие браузера
start "" "http://localhost:8081/swagger-ui.html"
start "" "http://localhost:8082/swagger-ui.html"

:: Ожидание нажатия клавиши для остановки
pause

:: Остановка всех контейнеров
echo.
echo Остановка контейнеров Docker...
docker-compose down
echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                    ВСЕ СЕРВИСЫ ОСТАНОВЛЕНЫ                   ║
echo ╚══════════════════════════════════════════════════════════════╝
pause
