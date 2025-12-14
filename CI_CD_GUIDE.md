# 🔄 Инструкция по настройке CI/CD (GitHub Actions)

Эта инструкция объясняет, как добавить автоматическую сборку и тестирование проекта через GitHub Actions.

---

## 📋 Почему CI/CD не был добавлен автоматически?

GitHub OAuth токен не имеет права `workflow` для создания файлов в `.github/workflows/`. Это ограничение безопасности GitHub.

---

## 🚀 Способ 1: Через веб-интерфейс GitHub (рекомендуется)

### Шаг 1: Откройте репозиторий

Перейдите на https://github.com/NiMv1/task-management-system

### Шаг 2: Создайте файл workflow

1. Нажмите **Add file** → **Create new file**
2. Введите путь: `.github/workflows/ci-cd.yml`
3. Вставьте содержимое ниже
4. Нажмите **Commit new file**

### Содержимое файла `ci-cd.yml`:

```yaml
name: CI/CD Pipeline

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  # Сборка и тестирование
  build:
    runs-on: ubuntu-latest
    
    services:
      postgres:
        image: postgres:15-alpine
        env:
          POSTGRES_DB: test_db
          POSTGRES_USER: postgres
          POSTGRES_PASSWORD: postgres
        ports:
          - 5432:5432
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5

    steps:
      - name: Checkout код
        uses: actions/checkout@v4

      - name: Установка Java 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: maven

      - name: Кэширование Maven зависимостей
        uses: actions/cache@v4
        with:
          path: ~/.m2/repository
          key: ${{ runner.os }}-maven-${{ hashFiles('**/pom.xml') }}
          restore-keys: |
            ${{ runner.os }}-maven-

      - name: Сборка проекта
        run: mvn clean compile -B

      - name: Запуск тестов
        run: mvn test -B
        env:
          SPRING_DATASOURCE_URL: jdbc:postgresql://localhost:5432/test_db
          SPRING_DATASOURCE_USERNAME: postgres
          SPRING_DATASOURCE_PASSWORD: postgres

      - name: Сборка JAR файлов
        run: mvn package -DskipTests -B

      - name: Загрузка артефактов
        uses: actions/upload-artifact@v4
        with:
          name: jar-files
          path: |
            auth-service/target/*.jar
            task-service/target/*.jar
            notification-service/target/*.jar
            api-gateway/target/*.jar

  # Сборка Docker образов (только для main ветки)
  docker:
    needs: build
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    
    steps:
      - name: Checkout код
        uses: actions/checkout@v4

      - name: Установка Java 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: maven

      - name: Сборка JAR файлов
        run: mvn package -DskipTests -B

      - name: Настройка Docker Buildx
        uses: docker/setup-buildx-action@v3

      - name: Логин в Docker Hub
        uses: docker/login-action@v3
        with:
          username: ${{ secrets.DOCKER_USERNAME }}
          password: ${{ secrets.DOCKER_PASSWORD }}
        # Пропустить если секреты не настроены
        continue-on-error: true

      - name: Сборка Docker образов
        run: |
          docker build -t task-management/auth-service:latest ./auth-service
          docker build -t task-management/task-service:latest ./task-service
          docker build -t task-management/notification-service:latest ./notification-service
          docker build -t task-management/api-gateway:latest ./api-gateway

      - name: Публикация в Docker Hub
        if: ${{ secrets.DOCKER_USERNAME != '' }}
        run: |
          docker push task-management/auth-service:latest
          docker push task-management/task-service:latest
          docker push task-management/notification-service:latest
          docker push task-management/api-gateway:latest
        continue-on-error: true

  # Анализ кода (опционально)
  code-quality:
    runs-on: ubuntu-latest
    
    steps:
      - name: Checkout код
        uses: actions/checkout@v4

      - name: Установка Java 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: maven

      - name: Проверка стиля кода (Checkstyle)
        run: mvn checkstyle:check -B
        continue-on-error: true

      - name: Анализ зависимостей
        run: mvn dependency:analyze -B
        continue-on-error: true
```

---

## 🔐 Настройка секретов (опционально)

Для публикации Docker образов в Docker Hub:

1. Перейдите в **Settings** → **Secrets and variables** → **Actions**
2. Нажмите **New repository secret**
3. Добавьте:
   - `DOCKER_USERNAME` — ваш логин Docker Hub
   - `DOCKER_PASSWORD` — ваш пароль или токен Docker Hub

---

## 🖥️ Способ 2: Через командную строку

### Шаг 1: Обновите права токена

```bash
# Переавторизуйтесь с правами workflow
gh auth login --scopes workflow
```

### Шаг 2: Создайте файл локально

```bash
mkdir -p .github/workflows
# Создайте файл ci-cd.yml с содержимым выше
```

### Шаг 3: Запушьте изменения

```bash
git add .github/workflows/ci-cd.yml
git commit -m "ci: добавлен GitHub Actions pipeline"
git push
```

---

## ✅ Проверка работы CI/CD

После добавления workflow:

1. Перейдите в репозиторий на GitHub
2. Откройте вкладку **Actions**
3. Вы увидите запущенный pipeline

### Статусы:
- 🟢 **Зелёная галочка** — сборка успешна
- 🔴 **Красный крестик** — ошибка (нажмите для деталей)
- 🟡 **Жёлтый круг** — сборка в процессе

---

## 📊 Что делает pipeline?

| Job | Описание | Когда запускается |
|-----|----------|-------------------|
| **build** | Компиляция + тесты | Каждый push/PR |
| **docker** | Сборка Docker образов | Только main ветка |
| **code-quality** | Анализ кода | Каждый push/PR |

---

## 🔧 Добавление бейджа в README

Добавьте в начало README.md:

```markdown
![CI/CD](https://github.com/NiMv1/task-management-system/actions/workflows/ci-cd.yml/badge.svg)
```

Это покажет текущий статус сборки.
