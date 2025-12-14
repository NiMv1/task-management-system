# Task Management System

Микросервисная система управления задачами, демонстрирующая навыки Middle Java Backend разработчика.

## 🏗️ Архитектура

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│   API Gateway   │────▶│  Auth Service   │────▶│   PostgreSQL    │
│   (Port 8080)   │     │   (Port 8081)   │     │   (auth_db)     │
└────────┬────────┘     └────────┬────────┘     └─────────────────┘
         │                       │
         │              ┌────────▼────────┐     ┌─────────────────┐
         │              │     Redis       │     │   PostgreSQL    │
         │              │   (Cache/JWT)   │     │   (task_db)     │
         │              └─────────────────┘     └────────▲────────┘
         │                                               │
         │              ┌─────────────────┐              │
         └─────────────▶│  Task Service   │──────────────┘
                        │   (Port 8082)   │
                        └────────┬────────┘
                                 │
                        ┌────────▼────────┐     ┌─────────────────┐
                        │     Kafka       │────▶│ Notification    │
                        │                 │     │    Service      │
                        └─────────────────┘     │   (Port 8083)   │
                                                └─────────────────┘
```

## 🛠️ Технологии

| Категория | Технологии |
|-----------|------------|
| **Backend** | Java 21, Spring Boot 3.2, Spring MVC, Spring Security, Spring Data JPA |
| **Базы данных** | PostgreSQL 15, Redis 7 |
| **Messaging** | Apache Kafka |
| **API Gateway** | Spring Cloud Gateway |
| **Безопасность** | JWT, BCrypt |
| **Мониторинг** | Prometheus, Grafana, ELK Stack |
| **Контейнеризация** | Docker, Docker Compose, Kubernetes |
| **CI/CD** | GitHub Actions |
| **Документация** | OpenAPI/Swagger |
| **Тестирование** | JUnit 5, Testcontainers |
| **Сборка** | Maven |

## 📦 Модули

- **api-gateway** - API Gateway с маршрутизацией и rate limiting
- **auth-service** - Сервис аутентификации и авторизации (JWT)
- **task-service** - Сервис управления задачами и проектами
- **notification-service** - Сервис уведомлений (Kafka consumer)
- **common** - Общие DTO и утилиты

## 🚀 Быстрый старт

### Требования
- Docker & Docker Compose
- Java 21+
- Maven 3.9+

### Запуск с Docker Compose

```bash
# Клонирование репозитория
git clone https://github.com/your-username/task-management-system.git
cd task-management-system

# Запуск всей инфраструктуры
docker-compose up -d

# Проверка статуса
docker-compose ps
```

### Доступные сервисы

| Сервис | URL |
|--------|-----|
| API Gateway | http://localhost:8080 |
| Auth Service | http://localhost:8081 |
| Task Service | http://localhost:8082 |
| Swagger UI (Auth) | http://localhost:8081/swagger-ui.html |
| Swagger UI (Task) | http://localhost:8082/swagger-ui.html |
| Prometheus | http://localhost:9090 |
| Grafana | http://localhost:3000 (admin/admin) |
| Kibana | http://localhost:5601 |

## 📝 API Endpoints

### Auth Service

```http
POST /api/v1/auth/register    # Регистрация
POST /api/v1/auth/login       # Вход
POST /api/v1/auth/refresh     # Обновление токена
POST /api/v1/auth/logout      # Выход
```

### Task Service

```http
# Проекты
GET    /api/v1/projects           # Список проектов
POST   /api/v1/projects           # Создание проекта
GET    /api/v1/projects/{id}      # Получение проекта
PUT    /api/v1/projects/{id}      # Обновление проекта
DELETE /api/v1/projects/{id}      # Удаление проекта

# Задачи
GET    /api/v1/tasks              # Список задач
POST   /api/v1/tasks              # Создание задачи
GET    /api/v1/tasks/{id}         # Получение задачи
PUT    /api/v1/tasks/{id}         # Обновление задачи
DELETE /api/v1/tasks/{id}         # Удаление задачи
PATCH  /api/v1/tasks/{id}/status  # Изменение статуса
```

## 🔧 Конфигурация

### Переменные окружения

```bash
# Database
DB_HOST=localhost
DB_PORT=5432
DB_USERNAME=postgres
DB_PASSWORD=postgres

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# Kafka
KAFKA_SERVERS=localhost:9092

# JWT
JWT_SECRET=your-secret-key
```

## 📊 Мониторинг

### Prometheus Metrics
Все сервисы экспортируют метрики через `/actuator/prometheus`:
- JVM метрики
- HTTP запросы (latency, count, errors)
- Kafka метрики
- Custom бизнес-метрики

### Grafana Dashboards
- JVM Dashboard
- Spring Boot Dashboard
- Kafka Dashboard
- Custom Application Dashboard

### ELK Stack
- Централизованное логирование
- Поиск и анализ логов в Kibana

## 🧪 Тестирование

```bash
# Unit тесты
mvn test

# Integration тесты
mvn verify -DskipUnitTests

# Все тесты с coverage
mvn verify jacoco:report
```

## 🚢 Деплой

### Kubernetes

```bash
# Применение конфигурации
kubectl apply -f k8s/base/ --recursive

# Проверка статуса
kubectl get pods -n task-manager
```

### CI/CD Pipeline
- **Build** - Сборка и тестирование
- **Code Analysis** - SonarQube анализ
- **Docker Build** - Сборка Docker образов
- **Deploy Staging** - Деплой в staging (develop branch)
- **Deploy Production** - Деплой в production (main branch)

## 📈 Высоконагруженность

Проект демонстрирует паттерны для высоконагруженных систем:

- **Горизонтальное масштабирование** - Kubernetes replicas
- **Кэширование** - Redis для сессий и данных
- **Асинхронная обработка** - Kafka для уведомлений
- **Circuit Breaker** - Resilience4j
- **Rate Limiting** - API Gateway
- **Connection Pooling** - HikariCP
- **Stateless архитектура** - JWT токены

## 📄 Лицензия

MIT License

## 👤 Автор

Middle Java Backend Developer Portfolio Project
