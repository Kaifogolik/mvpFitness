# 🧪 Руководство по локальному тестированию FitCoach AI

## 🚀 **Способ 1: IntelliJ IDEA (Рекомендуется)**

### Запуск проекта

1. **Откройте проект:**
   ```bash
   open -a "IntelliJ IDEA" /Users/nikita/IdeaProjects/Fitnes
   ```

2. **Дождитесь индексации** проекта (несколько минут)

3. **Настройте запуск:**
   - Найдите файл `src/main/java/com/fitcoach/FitnessApplication.java`
   - Нажмите зеленую кнопку ▶️ рядом с `main` методом
   - Или используйте комбинацию `Ctrl+Shift+F10`

4. **Проверьте консоль** - должны увидеть:
   ```
   Started FitnessApplication in X.XXX seconds
   Tomcat started on port(s): 8080 (http)
   ```

### Тестирование API

После запуска приложения:

1. **Swagger UI:** http://localhost:8080/swagger-ui.html
2. **API Docs:** http://localhost:8080/v3/api-docs
3. **Actuator Health:** http://localhost:8080/actuator/health

---

## 🚀 **Способ 2: Командная строка (Maven)**

### Установка Maven через Homebrew

```bash
# Установка Homebrew (если не установлен)
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# Добавление в PATH
echo 'eval "$(/opt/homebrew/bin/brew shellenv)"' >> ~/.zshrc
source ~/.zshrc

# Установка Maven
brew install maven

# Проверка установки
mvn --version
```

### Запуск через Maven

```bash
cd /Users/nikita/IdeaProjects/Fitnes

# Компиляция
mvn clean compile

# Запуск в development режиме
mvn spring-boot:run -Dspring-boot.run.profiles=development

# Или сборка и запуск JAR
mvn clean package -DskipTests
java -jar target/fitness-app-1.0.0.jar --spring.profiles.active=development
```

---

## 🤖 **Тестирование Telegram Bot**

### Подготовка окружения

1. **Проверьте конфигурацию** в `src/main/resources/application-development.yml`:
   ```yaml
   telegram:
     bot:
       token: 8375716243:AAHjRBrv3aTZztFQlFf2TPr2oMmav72lU_c
       username: fitcoach_ai_bot
   ```

2. **Получите OpenAI API ключ** (опционально для полного тестирования):
   - Перейдите: https://platform.openai.com/api-keys
   - Создайте новый ключ
   - Добавьте в `.env`: `OPENAI_API_KEY=your-key`

### Тестирование бота

1. **Запустите приложение** (любым способом выше)

2. **Найдите бота в Telegram:** @fitcoach_ai_bot

3. **Протестируйте команды:**
   ```
   /start - Регистрация и приветствие
   /profile - Профиль пользователя  
   /food - Анализ фото еды (нужен OpenAI ключ)
   /stats - Статистика калорий
   /help - Помощь
   ```

4. **Отправьте фото еды** для тестирования AI анализа

---

## 🐳 **Способ 3: Docker (После установки)**

### Установка Docker

```bash
# Через Homebrew
brew install --cask docker

# Запуск Docker Desktop
open -a Docker
```

### Запуск через Docker Compose

```bash
cd /Users/nikita/IdeaProjects/Fitnes

# Запуск всей инфраструктуры
docker-compose up -d

# Проверка статуса
docker-compose ps

# Просмотр логов
docker-compose logs -f app

# Остановка
docker-compose down
```

---

## 🧪 **Тестовые сценарии**

### 1. Регистрация пользователя
- Отправьте `/start` боту
- Проверьте создание записи в БД

### 2. Анализ фото еды
- Отправьте фото еды боту
- Проверьте ответ с калориями и макросами

### 3. Профиль пользователя
- Отправьте `/profile`
- Проверьте отображение данных

### 4. API тестирование
- Откройте Swagger UI
- Протестируйте эндпоинты

---

## 🔍 **Возможные проблемы и решения**

### Порт уже занят
```bash
# Найти процесс на порту 8080
lsof -i :8080

# Убить процесс
kill -9 PID
```

### OpenAI API не работает
- Проверьте ключ в `.env`
- Убедитесь что есть средства на счету OpenAI
- Временно отключите AI функции для базового тестирования

### База данных не подключается
- Проект использует H2 в development режиме (в памяти)
- Консоль H2: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:testdb`

---

## ✅ **Критерии успешного тестирования**

- [ ] Приложение запускается без ошибок
- [ ] Swagger UI доступен
- [ ] Telegram Bot отвечает на команды
- [ ] База данных H2 работает
- [ ] API эндпоинты возвращают корректные ответы
- [ ] AI анализ фото работает (при наличии OpenAI ключа)

**Готов помочь с любым из способов!** 🚀 