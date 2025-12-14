# Task Management System - Запуск проекта
# Для остановки нажмите любую клавишу

$Host.UI.RawUI.WindowTitle = "Task Management System"
$ErrorActionPreference = "SilentlyContinue"

# Переход в директорию проекта
Set-Location $PSScriptRoot

Write-Host "╔══════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║         TASK MANAGEMENT SYSTEM - ЗАПУСК ПРОЕКТА             ║" -ForegroundColor Cyan
Write-Host "╚══════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

# Остановка предыдущих процессов на портах
Write-Host "[0/5] Остановка предыдущих процессов..." -ForegroundColor Yellow
foreach ($port in @(8080, 8081, 8082)) {
    $connections = netstat -ano | Select-String ":$port.*LISTENING"
    foreach ($conn in $connections) {
        $processId = ($conn -split '\s+')[-1]
        if ($processId -match '^\d+$') {
            Write-Host "Останавливаю процесс на порту $port, PID: $processId"
            Stop-Process -Id $processId -Force -ErrorAction SilentlyContinue
        }
    }
}
Start-Sleep -Seconds 3
Write-Host "[OK] Порты освобождены" -ForegroundColor Green
Write-Host ""

# Настройка Java 17
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
Write-Host "[INFO] Используется Java 17" -ForegroundColor Cyan

# Проверка Java
Write-Host "[1/5] Проверка Java..." -ForegroundColor Yellow
$null = java -version 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "[ОШИБКА] Java не найдена" -ForegroundColor Red
    Read-Host "Нажмите Enter для выхода"
    exit 1
}
Write-Host "[OK] Java найдена" -ForegroundColor Green
Write-Host ""

# Проверка Docker
Write-Host "[2/5] Проверка Docker..." -ForegroundColor Yellow
docker info 2>&1 | Out-Null
if ($LASTEXITCODE -ne 0) {
    Write-Host "[ОШИБКА] Docker не запущен. Запустите Docker Desktop." -ForegroundColor Red
    Read-Host "Нажмите Enter для выхода"
    exit 1
}
Write-Host "[OK] Docker запущен" -ForegroundColor Green
Write-Host ""

# Сборка сервисов
Write-Host "[3/5] Сборка сервисов и пересборка Docker образов..." -ForegroundColor Yellow
mvn -q clean package -pl auth-service,task-service -DskipTests 2>&1 | Out-Null
if ($LASTEXITCODE -ne 0) {
    Write-Host "[ОШИБКА] Не удалось собрать сервисы" -ForegroundColor Red
    Read-Host "Нажмите Enter для выхода"
    exit 1
}
docker-compose build --no-cache auth-service task-service 2>&1 | Out-Null
Write-Host "[OK] Образы пересобраны" -ForegroundColor Green
Write-Host ""

# Запуск инфраструктуры
Write-Host "[4/5] Запуск инфраструктуры Docker..." -ForegroundColor Yellow
docker-compose up -d
if ($LASTEXITCODE -ne 0) {
    Write-Host "[ОШИБКА] Не удалось запустить контейнеры" -ForegroundColor Red
    Read-Host "Нажмите Enter для выхода"
    exit 1
}
Write-Host "[OK] Контейнеры запущены" -ForegroundColor Green
Write-Host ""

# Ожидание готовности
Write-Host "[5/5] Ожидание готовности сервисов (40 сек)..." -ForegroundColor Yellow
Start-Sleep -Seconds 40
Write-Host "[OK] Сервисы запущены" -ForegroundColor Green
Write-Host ""

Write-Host "╔══════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║              TASK MANAGEMENT SYSTEM ЗАПУЩЕН                 ║" -ForegroundColor Cyan
Write-Host "╠══════════════════════════════════════════════════════════════╣" -ForegroundColor Cyan
Write-Host "║  Сервисы (запущены в Docker):                               ║" -ForegroundColor Cyan
Write-Host "║  - Auth Service:    http://localhost:8081                   ║" -ForegroundColor Cyan
Write-Host "║  - Task Service:    http://localhost:8082                   ║" -ForegroundColor Cyan
Write-Host "║  - API Gateway:     http://localhost:8080                   ║" -ForegroundColor Cyan
Write-Host "╠══════════════════════════════════════════════════════════════╣" -ForegroundColor Cyan
Write-Host "║  Swagger UI:                                                 ║" -ForegroundColor Cyan
Write-Host "║  - Auth Swagger:    http://localhost:8081/swagger-ui.html   ║" -ForegroundColor Cyan
Write-Host "║  - Task Swagger:    http://localhost:8082/swagger-ui.html   ║" -ForegroundColor Cyan
Write-Host "╠══════════════════════════════════════════════════════════════╣" -ForegroundColor Cyan
Write-Host "║  Мониторинг:                                                 ║" -ForegroundColor Cyan
Write-Host "║  - Prometheus:      http://localhost:9090                   ║" -ForegroundColor Cyan
Write-Host "║  - Grafana:         http://localhost:3000 (admin/admin)     ║" -ForegroundColor Cyan
Write-Host "║  - Kibana:          http://localhost:5601                   ║" -ForegroundColor Cyan
Write-Host "╠══════════════════════════════════════════════════════════════╣" -ForegroundColor Cyan
Write-Host "║  Для остановки: нажмите любую клавишу                       ║" -ForegroundColor Cyan
Write-Host "╚══════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

# Открытие браузера
Start-Process "http://localhost:8081/swagger-ui.html"
Start-Sleep -Seconds 1
Start-Process "http://localhost:8082/swagger-ui.html"
Start-Sleep -Seconds 1
Start-Process "http://localhost:9090"
Start-Sleep -Seconds 1
Start-Process "http://localhost:3000"
Start-Sleep -Seconds 1
Start-Process "http://localhost:5601"

# Ожидание нажатия клавиши
Write-Host ""
Write-Host "Нажмите любую клавишу для остановки сервисов..." -ForegroundColor Yellow
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

# Cleanup
Write-Host ""
Write-Host "╔══════════════════════════════════════════════════════════════╗" -ForegroundColor Yellow
Write-Host "║              ОСТАНОВКА СЕРВИСОВ...                          ║" -ForegroundColor Yellow
Write-Host "╚══════════════════════════════════════════════════════════════╝" -ForegroundColor Yellow
Write-Host ""
Write-Host "Остановка контейнеров Docker..." -ForegroundColor Yellow
Set-Location $PSScriptRoot
docker-compose down
Write-Host ""
Write-Host "╔══════════════════════════════════════════════════════════════╗" -ForegroundColor Green
Write-Host "║                    ВСЕ СЕРВИСЫ ОСТАНОВЛЕНЫ                   ║" -ForegroundColor Green
Write-Host "╚══════════════════════════════════════════════════════════════╝" -ForegroundColor Green
Write-Host ""
Write-Host "Нажмите Enter для закрытия окна..."
Read-Host
