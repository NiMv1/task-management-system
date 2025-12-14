# 📦 Инструкция по публикации в GitHub

## Шаг 1: Создание репозитория на GitHub

1. Перейдите на https://github.com/NiMv1
2. Нажмите **New repository**
3. Название: `task-management-system`
4. Описание: `Микросервисная система управления задачами (Spring Boot, PostgreSQL, Kafka, Redis, Docker, K8s)`
5. **НЕ** добавляйте README, .gitignore или лицензию
6. Нажмите **Create repository**

## Шаг 2: Инициализация локального репозитория

```powershell
cd C:\Users\bnex4\CascadeProjects\task-management-system

git init
git remote add origin https://github.com/NiMv1/task-management-system.git
```

## Шаг 3: Последовательные коммиты

```powershell
# Коммит 1: Корневой проект
git add pom.xml README.md .gitignore
git commit -m "feat: инициализация multi-module Maven проекта"

# Коммит 2: Common модуль
git add common/
git commit -m "feat: добавлен common модуль с общими DTO и исключениями"

# Коммит 3: Auth Service - базовая структура
git add auth-service/pom.xml auth-service/src/main/resources/
git commit -m "feat(auth): инициализация Auth Service"

# Коммит 4: Auth Service - сущности и репозитории
git add auth-service/src/main/java/com/taskmanager/auth/entity/
git add auth-service/src/main/java/com/taskmanager/auth/repository/
git commit -m "feat(auth): добавлены сущности User, RefreshToken и репозитории"

# Коммит 5: Auth Service - безопасность
git add auth-service/src/main/java/com/taskmanager/auth/security/
git commit -m "feat(auth): добавлена JWT аутентификация"

# Коммит 6: Auth Service - сервисы и контроллеры
git add auth-service/src/main/java/com/taskmanager/auth/service/
git add auth-service/src/main/java/com/taskmanager/auth/controller/
git add auth-service/src/main/java/com/taskmanager/auth/dto/
git commit -m "feat(auth): добавлены сервисы и REST API"

# Коммит 7: Auth Service - Liquibase миграции
git add auth-service/src/main/resources/db/
git commit -m "feat(auth): добавлены Liquibase миграции"

# Коммит 8: Task Service - базовая структура
git add task-service/pom.xml task-service/src/main/resources/
git commit -m "feat(task): инициализация Task Service"

# Коммит 9: Task Service - сущности
git add task-service/src/main/java/com/taskmanager/task/entity/
git add task-service/src/main/java/com/taskmanager/task/repository/
git commit -m "feat(task): добавлены сущности Project, Task"

# Коммит 10: Task Service - сервисы и Kafka
git add task-service/src/main/java/com/taskmanager/task/service/
git add task-service/src/main/java/com/taskmanager/task/kafka/
git commit -m "feat(task): добавлены сервисы и Kafka producer"

# Коммит 11: Task Service - контроллеры
git add task-service/src/main/java/com/taskmanager/task/controller/
git add task-service/src/main/java/com/taskmanager/task/dto/
git commit -m "feat(task): добавлены REST контроллеры"

# Коммит 12: Task Service - Liquibase
git add task-service/src/main/resources/db/
git commit -m "feat(task): добавлены Liquibase миграции"

# Коммит 13: Notification Service
git add notification-service/
git commit -m "feat(notification): добавлен Notification Service с Kafka consumer"

# Коммит 14: API Gateway
git add api-gateway/
git commit -m "feat(gateway): добавлен API Gateway с Circuit Breaker"

# Коммит 15: Docker
git add */Dockerfile docker-compose.yml
git commit -m "feat: добавлена Docker конфигурация для всех сервисов"

# Коммит 16: Kubernetes
git add k8s/
git commit -m "feat: добавлены Kubernetes манифесты"

# Коммит 17: Мониторинг
git add monitoring/
git commit -m "feat: добавлена конфигурация Prometheus, Grafana, ELK"

# Коммит 18: CI/CD
git add .github/
git commit -m "ci: добавлен GitHub Actions CI/CD pipeline"

# Коммит 19: Тесты
git add */src/test/
git commit -m "test: добавлены unit и интеграционные тесты"
```

## Шаг 4: Публикация

```powershell
git branch -M main
git push -u origin main
```

## Шаг 5: Теги

```powershell
git tag -a v1.0.0 -m "Первый релиз Task Management System"
git push origin v1.0.0
```

---

## 🎯 Результат

- ✅ 19 логических коммитов с понятными сообщениями
- ✅ Conventional Commits формат (feat, fix, ci, test)
- ✅ Профессиональная история для code review
