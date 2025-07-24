# FitCoach AI Platform

Умная фитнес-платформа с ИИ-анализом питания и системой тренер-ученик.

## 🚀 Быстрый старт

### Предварительные требования

- Java 21+
- Maven 3.9+
- Docker & Docker Compose
- PostgreSQL 15+ (для локальной разработки)
- Redis 7+ (для кэширования)

### Настройка окружения

1. **Клонируйте репозиторий:**
```bash
git clone <repository-url>
cd fitness-platform
```

2. **Создайте файл переменных окружения:**
```bash
cp .env.example .env
```

3. **Заполните переменные в `.env`:**
- `TELEGRAM_BOT_TOKEN` - токен от @BotFather
- `TELEGRAM_BOT_USERNAME` - имя пользователя бота
- `OPENAI_API_KEY` - API ключ OpenAI
- `JWT_SECRET` - секретный ключ для JWT токенов

### Запуск с Docker Compose (рекомендуемый)

```bash
# Запуск всех сервисов
docker-compose up -d

# Просмотр логов
docker-compose logs -f app

# Остановка
docker-compose down
```

Приложение будет доступно по адресу: http://localhost:8080

### Локальный запуск для разработки

1. **Запустите базу данных:**
```bash
docker-compose up -d postgres redis
```

2. **Запустите приложение:**
```bash
mvn spring-boot:run
```

## 📱 Архитектура

### Технологический стек

**Backend:**
- Spring Boot 3.x
- PostgreSQL + Redis
- OpenAI GPT-4V API
- Telegram Bot API
- Spring Security + JWT

**Клиенты:**
- Telegram Mini App (Web)
- Android (планируется)
- iOS (планируется)
- Web Dashboard (планируется)

### Структура проекта

```
src/main/java/com/fitcoach/
├── domain/              # Доменные модели и бизнес-логика
│   ├── user/           # Пользователи и тренеры
│   └── nutrition/      # Питание и анализ еды
├── infrastructure/     # Внешние интеграции
│   ├── ai/            # OpenAI сервисы
│   └── telegram/      # Telegram Bot
└── api/               # REST API контроллеры
```

## 🤖 Telegram Bot

### Основные команды

- `/start` - Регистрация и главное меню
- `/profile` - Профиль пользователя
- `/food` - Анализ фото еды
- `/stats` - Статистика питания
- `/coach` - Тренерская панель

### Анализ фото еды

1. Отправьте фото еды боту
2. ИИ анализирует изображение
3. Получите данные о калориях и БЖУ
4. Просмотрите детальную информацию в Mini App

## 🔧 API Documentation

После запуска приложения документация API доступна по адресу:
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api-docs

## 🏗️ База данных

### Основные таблицы

- `users` - Пользователи и тренеры
- `food_entries` - Записи о питании
- `detected_food_items` - Продукты, обнаруженные ИИ

### Миграции

Приложение использует Hibernate для автоматического создания схемы БД в режиме разработки.

## 🧪 Тестирование

```bash
# Запуск всех тестов
mvn test

# Запуск определенного теста
mvn test -Dtest=UserServiceTest

# Запуск интеграционных тестов
mvn test -Dtest=*IntegrationTest
```

## 📊 Мониторинг

### Actuator endpoints

- `/actuator/health` - Статус приложения
- `/actuator/metrics` - Метрики
- `/actuator/info` - Информация о приложении

### Доступ к инструментам

- **pgAdmin:** http://localhost:5050 (admin@fitcoach.ai / admin123)
- **Application logs:** `docker-compose logs -f app`

## 🚀 Деплоймент

### Production готовность

1. **Настройте переменные окружения:**
   - Используйте реальные секретные ключи
   - Настройте SSL сертификаты
   - Измените пароли по умолчанию

2. **Конфигурация производства:**
```yaml
# application-production.yml
spring:
  profiles:
    active: production
  jpa:
    hibernate:
      ddl-auto: validate
logging:
  level:
    com.fitcoach: INFO
```

3. **Деплой:**
```bash
docker-compose -f docker-compose.prod.yml up -d
```

## 💡 Roadmap

### MVP (текущая версия)
- ✅ Telegram Bot с базовыми командами
- ✅ ИИ-анализ фото еды
- ✅ Система тренер-ученик
- ✅ Mini App интерфейс

### Версия 1.0
- 📱 Android приложение
- 🍎 iOS приложение
- 💰 Система платежей
- 📊 Расширенная аналитика

### Версия 2.0
- 🏋️ Трекинг тренировок
- 👥 Групповые занятия
- 🤝 Интеграции с фитнес-устройствами
- 🎯 ML рекомендации

## 🤝 Вклад в проект

1. Форкните репозиторий
2. Создайте feature branch
3. Зафиксируйте изменения
4. Создайте Pull Request

## 📄 Лицензия

Этот проект лицензирован под MIT License - смотрите [LICENSE](LICENSE) файл для деталей.

## 📞 Поддержка

- 📧 Email: support@fitcoach.ai
- 💬 Telegram: @fitcoach_support
- 🐛 Issues: [GitHub Issues](https://github.com/your-repo/issues) 