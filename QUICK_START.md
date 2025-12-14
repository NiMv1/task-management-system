# 🚀 Быстрый старт — Task Management System

Эта инструкция поможет вам запустить микросервисную систему управления задачами на своём компьютере.

---

## 📋 Требования

| Компонент | Версия | Ссылка |
|-----------|--------|--------|
| **Java** | 17+ (рекомендуется 21) | [Eclipse Temurin](https://adoptium.net/) |
| **Maven** | 3.8+ | [Apache Maven](https://maven.apache.org/download.cgi) |
| **Docker** | 20+ | [Docker Desktop](https://www.docker.com/products/docker-desktop/) |
| **Docker Compose** | 2.0+ | Включён в Docker Desktop |

---

## 🏗️ Архитектура системы

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│  Frontend   │────▶│ API Gateway │────▶│   Services  │
└─────────────┘     └─────────────┘     └─────────────┘
                           │
         ┌─────────────────┼─────────────────┐
         │                 │                 │
         ▼                 ▼                 ▼
   ┌───────────┐    ┌───────────┐    ┌───────────────┐
   │   Auth    │    │   Task    │    │ Notification  │
   │  Service  │    │  Service  │    │   Service     │
   └─────┬─────┘    └─────┬─────┘    └───────┬───────┘
         │                │                  │
         ▼                ▼                  ▼
   ┌───────────┐    ┌───────────┐    ┌───────────┐
   │PostgreSQL │    │PostgreSQL │    │   Kafka   │
   │  (Auth)   │    │  (Tasks)  │    │           │
   └───────────┘    └───────────┘    └───────────┘
```

---

## 🐳 Способ 1: Запуск через Docker Compose (рекомендуется)

```bash
# 1. Клонируйте репозиторий
git clone https://github.com/NiMv1/task-management-system.git
cd task-management-system

# 2. Запустите все сервисы
docker-compose up -d

# 3. Проверьте статус (подождите 1-2 минуты для инициализации)
docker-compose ps
```

### Доступные сервисы:

| Сервис | URL | Описание |
|--------|-----|----------|
| **API Gateway** | http://localhost:8080 | Единая точка входа |
| **Auth Service** | http://localhost:8081 | Аутентификация |
| **Task Service** | http://localhost:8082 | Управление задачами |
| **Notification Service** | http://localhost:8083 | Уведомления |
| **PostgreSQL (Auth)** | localhost:5432 | БД аутентификации |
| **PostgreSQL (Tasks)** | localhost:5433 | БД задач |
| **Kafka** | localhost:9092 | Очередь сообщений |
| **Redis** | localhost:6379 | Кэш |
| **Prometheus** | http://localhost:9090 | Метрики |
| **Grafana** | http://localhost:3000 | Дашборды (admin/admin) |

### Остановка:

```bash
docker-compose down

# С удалением данных:
docker-compose down -v
```

---

## 💻 Способ 2: Локальный запуск (для разработки)

### Шаг 1: Запустите инфраструктуру

```bash
docker-compose up -d postgres-auth postgres-task kafka redis zookeeper
```

### Шаг 2: Соберите все модули

```bash
mvn clean package -DskipTests
```

### Шаг 3: Запустите сервисы (в отдельных терминалах)

```bash
# Терминал 1: Auth Service
cd auth-service
mvn spring-boot:run

# Терминал 2: Task Service
cd task-service
mvn spring-boot:run

# Терминал 3: Notification Service
cd notification-service
mvn spring-boot:run

# Терминал 4: API Gateway
cd api-gateway
mvn spring-boot:run
```

---

## 🧪 Проверка работоспособности

### 1. Health Check всех сервисов

```bash
# API Gateway
curl http://localhost:8080/actuator/health

# Auth Service
curl http://localhost:8081/actuator/health

# Task Service
curl http://localhost:8082/actuator/health
```

### 2. Регистрация пользователя

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123"
  }'
```

Ожидаемый ответ:
```json
{
  "success": true,
  "message": "Пользователь успешно зарегистрирован",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "..."
  }
}
```

### 3. Авторизация

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

### 4. Создание проекта (с токеном)

```bash
# Замените YOUR_TOKEN на полученный accessToken
curl -X POST http://localhost:8080/api/projects \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "name": "Мой первый проект",
    "description": "Описание проекта"
  }'
```

### 5. Создание задачи

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "title": "Первая задача",
    "description": "Описание задачи",
    "projectId": 1,
    "priority": "HIGH"
  }'
```

### 6. Получение списка задач

```bash
curl http://localhost:8080/api/tasks \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## 📖 API Endpoints

### Auth Service (`/api/auth`)

| Метод | Endpoint | Описание |
|-------|----------|----------|
| `POST` | `/register` | Регистрация |
| `POST` | `/login` | Авторизация |
| `POST` | `/refresh` | Обновление токена |
| `POST` | `/logout` | Выход |

### Task Service (`/api/projects`, `/api/tasks`)

| Метод | Endpoint | Описание |
|-------|----------|----------|
| `GET` | `/api/projects` | Список проектов |
| `POST` | `/api/projects` | Создать проект |
| `GET` | `/api/projects/{id}` | Получить проект |
| `PUT` | `/api/projects/{id}` | Обновить проект |
| `DELETE` | `/api/projects/{id}` | Удалить проект |
| `GET` | `/api/tasks` | Список задач |
| `POST` | `/api/tasks` | Создать задачу |
| `GET` | `/api/tasks/{id}` | Получить задачу |
| `PUT` | `/api/tasks/{id}` | Обновить задачу |
| `DELETE` | `/api/tasks/{id}` | Удалить задачу |
| `PATCH` | `/api/tasks/{id}/status` | Изменить статус |

---

## 🔧 Конфигурация

### Переменные окружения

```bash
# Auth Service
AUTH_DB_HOST=localhost
AUTH_DB_PORT=5432
AUTH_DB_NAME=auth_db
JWT_SECRET=your-secret-key

# Task Service
TASK_DB_HOST=localhost
TASK_DB_PORT=5433
TASK_DB_NAME=task_db
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

---

## ❓ Частые проблемы

### Kafka не запускается
```bash
# Убедитесь, что Zookeeper запущен первым
docker-compose up -d zookeeper
sleep 10
docker-compose up -d kafka
```

### Порты заняты
```bash
# Проверьте занятые порты
netstat -ano | findstr :8080
netstat -ano | findstr :5432
```

### База данных не инициализируется
```bash
# Пересоздайте контейнеры
docker-compose down -v
docker-compose up -d
```

### Ошибка "Connection refused"
Подождите 1-2 минуты после запуска — сервисам нужно время для инициализации.

---

## 📊 Мониторинг

### Prometheus
- URL: http://localhost:9090
- Метрики: `http_server_requests_seconds_count`, `jvm_memory_used_bytes`

### Grafana
- URL: http://localhost:3000
- Логин: `admin` / `admin`
- Импортируйте дашборд: ID `4701` (JVM Micrometer)

---

## 🧪 Запуск тестов

```bash
# Все тесты
mvn test

# Тесты конкретного модуля
mvn test -pl auth-service
mvn test -pl task-service

# Интеграционные тесты (требуют Docker)
mvn verify -pl task-service
```

---

## 📚 Структура проекта

```
task-management-system/
├── common/                 # Общие DTO и исключения
├── auth-service/           # Сервис аутентификации
├── task-service/           # Сервис управления задачами
├── notification-service/   # Сервис уведомлений
├── api-gateway/            # API Gateway
├── k8s/                    # Kubernetes манифесты
├── monitoring/             # Конфигурация мониторинга
└── docker-compose.yml      # Docker Compose конфигурация
```

---

**Автор**: [NiMv1](https://github.com/NiMv1)
