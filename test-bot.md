# 🤖 Тестирование Telegram Bot

## Ваш бот настроен!

**Токен:** `8375716243:AAHjRBrv3aTZztFQlFf2TPr2oMmav72lU_c`

## 🧪 Простое тестирование (без кода)

### 1. Найдите своего бота в Telegram:
- Перейдите к @BotFather
- Найдите созданного бота по токену
- Или поищите по имени пользователя

### 2. Отправьте команды:
```
/start - должен ответить приветствием
/help - показать помощь
/setcommands - настроить команды (через BotFather)
```

### 3. Настройте команды через @BotFather:
```
/setcommands
start - 🚀 Начать работу с ботом
profile - 👤 Мой профиль  
food - 📸 Анализ фото еды
stats - 📊 Статистика питания
coach - 👨‍🏫 Тренерская панель
help - ❓ Помощь
```

## 🚀 Запуск полного проекта

### Вариант 1: Docker (рекомендуемый)
```bash
# Запуск инфраструктуры
./run.sh

# После успешного запуска базы данных
docker-compose up -d app
```

### Вариант 2: Установка Maven и локальный запуск
```bash
# Установка Maven (macOS)
brew install maven

# Сборка и запуск
mvn spring-boot:run
```

### Вариант 3: IDE разработка
1. Откройте проект в IntelliJ IDEA
2. Дождитесь индексации Maven зависимостей
3. Запустите `FitnessApplication.main()`

## ⚠️ Что нужно для полной работы:

### 1. OpenAI API Key (для ИИ-анализа)
```bash
# Получите ключ на https://platform.openai.com/
# Замените в .env:
OPENAI_API_KEY=sk-your-real-openai-key-here
```

### 2. Настройка Mini App (опционально)
```bash
# Создайте Mini App через @BotFather
# Обновите URL в .env:
MINI_APP_URL=https://your-domain.com/app
```

## 🔧 Troubleshooting

### Бот не отвечает:
1. Проверьте токен в .env
2. Убедитесь что приложение запущено
3. Проверьте логи: `docker-compose logs -f app`

### Ошибки компиляции:
1. Проверьте Java версию: `java -version`
2. Установите Maven: `brew install maven`
3. Очистите кэш: `mvn clean`

### База данных недоступна:
1. Запустите: `docker-compose up -d postgres`
2. Проверьте статус: `docker-compose ps`
3. Подключитесь к pgAdmin: http://localhost:5050

## 📞 Следующие шаги:

1. **Протестируйте базовые команды** бота
2. **Получите OpenAI ключ** для ИИ-функций  
3. **Запустите полный проект** для разработки
4. **Настройте Mini App** для расширенного интерфейса

---

🎯 **Цель:** Создать работающий MVP за 1-2 недели! 