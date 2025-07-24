# 🚀 Настройка GitHub репозитория для FitCoach AI Platform

## 📋 Шаги для создания репозитория на GitHub

### 1. Создайте новый репозиторий на GitHub

1. Перейдите на https://github.com/Kaifogolik
2. Нажмите **"New"** или **"+"** → **"New repository"**
3. Заполните форму:
   ```
   Repository name: fitcoach-ai-platform
   Description: 🤖 Умная фитнес-платформа с ИИ-анализом питания и системой тренер-ученик
   Visibility: Public (или Private)
   ✅ Initialize this repository with: НЕ ВЫБИРАЙТЕ (у нас уже есть файлы)
   ```
4. Нажмите **"Create repository"**

### 2. Подключите локальный репозиторий к GitHub

После создания репозитория на GitHub выполните команды:

```bash
# Добавляем удаленный репозиторий
git remote add origin https://github.com/Kaifogolik/fitcoach-ai-platform.git

# Устанавливаем основную ветку
git branch -M main

# Загружаем код на GitHub
git push -u origin main
```

### 3. Настройте переменные окружения на GitHub

В настройках репозитория создайте **GitHub Secrets** для CI/CD:

1. Перейдите в репозиторий → **Settings** → **Secrets and variables** → **Actions**
2. Добавьте секреты:
   ```
   TELEGRAM_BOT_TOKEN: 8375716243:AAHjRBrv3aTZztFQlFf2TPr2oMmav72lU_c
   OPENAI_API_KEY: your-openai-api-key
   JWT_SECRET: fitcoach-super-secret-key-2024
   ```

### 4. Активируйте GitHub Actions (опционально)

Создайте файл `.github/workflows/ci.yml` для автоматического тестирования:

```yaml
name: CI/CD Pipeline

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v4
    
    - name: Set up JDK 21
      uses: actions/setup-java@v3
      with:
        java-version: '21'
        distribution: 'openjdk'
        
    - name: Cache Maven packages
      uses: actions/cache@v3
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
        
    - name: Run tests
      run: mvn clean test
      
    - name: Build application
      run: mvn clean package -DskipTests
```

## 🔧 Полезные команды Git

```bash
# Проверить статус
git status

# Добавить новые файлы
git add .

# Зафиксировать изменения
git commit -m "✨ Добавлена новая функция"

# Отправить на GitHub
git push

# Синхронизировать с GitHub
git pull

# Создать новую ветку для функции
git checkout -b feature/new-feature

# Переключиться на основную ветку
git checkout main

# Слить ветку
git merge feature/new-feature
```

## 📁 Структура проекта на GitHub

После загрузки ваш репозиторий будет содержать:

```
fitcoach-ai-platform/
├── 📄 README.md                 # Описание проекта
├── 🐳 docker-compose.yml        # Docker конфигурация
├── 🚀 Dockerfile               # Контейнер приложения
├── ⚙️ pom.xml                   # Maven зависимости
├── 🔧 run.sh                   # Скрипт запуска
├── 📋 .gitignore               # Игнорируемые файлы
├── 📖 fitness-app-plan.md      # План проекта
├── 🏗️ scalable-architecture-plan.md # Архитектура
├── 🤖 telegram-mvp-plan.md     # Telegram MVP план
└── src/main/java/com/fitcoach/  # Исходный код
    ├── domain/                  # Доменные модели
    ├── infrastructure/         # Внешние интеграции
    └── FitnessApplication.java # Главный класс
```

## 🎯 Следующие шаги

1. ✅ Создайте репозиторий на GitHub
2. ✅ Загрузите код командами выше
3. 🔧 Настройте GitHub Actions для CI/CD
4. 📝 Создайте Issues для планирования задач
5. 🌟 Пригласите других разработчиков в проект
6. 📊 Настройте GitHub Projects для управления

---

## 🚀 Готово к командной разработке!

После настройки вы сможете:
- 👥 Работать в команде через Pull Requests
- 🐛 Отслеживать баги через Issues  
- 📈 Видеть прогресс через GitHub Projects
- 🔄 Автоматически тестировать код через Actions
- 🚀 Деплоить в продакшн через GitHub Pages/Actions

**Удачи в разработке!** 💪 