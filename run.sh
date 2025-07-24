#!/bin/bash

# FitCoach AI Platform - Запуск скрипт
echo "🚀 Запуск FitCoach AI Platform..."

# Загружаем переменные окружения
if [ -f .env ]; then
    echo "📋 Загружаем переменные окружения из .env"
    export $(cat .env | grep -v '^#' | xargs)
else
    echo "⚠️  Файл .env не найден. Создайте его на основе .env.example"
    exit 1
fi

# Проверяем наличие Docker
if ! command -v docker &> /dev/null; then
    echo "❌ Docker не установлен. Установите Docker для запуска проекта."
    exit 1
fi

# Проверяем наличие Docker Compose
if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose не установлен."
    exit 1
fi

echo "🐳 Запускаем сервисы через Docker Compose..."

# Останавливаем предыдущие контейнеры если есть
docker-compose down

# Запускаем все сервисы
docker-compose up -d postgres redis

echo "⏳ Ждем запуска базы данных..."
sleep 10

# Запускаем приложение (если есть собранный образ)
echo "🔧 Для запуска приложения выполните:"
echo "   docker-compose up -d app"
echo ""
echo "📊 Доступные сервисы:"
echo "   - PostgreSQL: localhost:5432"
echo "   - Redis: localhost:6379"
echo "   - pgAdmin: http://localhost:5050"
echo ""
echo "🤖 Telegram Bot Token настроен: ${TELEGRAM_BOT_TOKEN:0:10}..."
echo ""
echo "✅ Готово! Теперь можете разрабатывать и тестировать бота." 