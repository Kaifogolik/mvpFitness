#!/bin/bash

echo "🚀 Запуск FitCoach AI приложения..."

# Проверяем что VPN включен
echo "🔍 Проверяю OpenAI API подключение..."
if curl -s "https://api.openai.com/v1/models" -H "Authorization: Bearer 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" >/dev/null 2>&1; then
    echo "✅ OpenAI API доступен"
else 
    echo "⚠️ OpenAI API недоступен - проверьте VPN"
fi

# Останавливаем предыдущий процесс если есть
echo "🛑 Останавливаю предыдущие процессы..."
pkill -f "FitnessApplication" 2>/dev/null

# Ждем немного
sleep 2

# Запускаем приложение
echo "▶️ Запускаю Spring Boot приложение..."
java -jar target/fitness-0.0.1-SNAPSHOT.jar --spring.profiles.active=development > application.log 2>&1 &

# Сохраняем PID
JAVA_PID=$!
echo $JAVA_PID > app.pid
echo "🆔 PID приложения: $JAVA_PID"

# Ждем запуска
echo "⏳ Жду запуска приложения..."
for i in {1..20}; do
    if curl -s http://localhost:8080/api/test/health >/dev/null 2>&1; then
        echo "✅ Приложение запущено на http://localhost:8080"
        echo ""
        echo "🧪 Тестирование:"
        echo "📱 Mini App: http://localhost:8080"
        echo "📊 API Status: http://localhost:8080/api/ai/status"
        echo "📚 Swagger: http://localhost:8080/swagger-ui.html"
        echo ""
        echo "🤖 Telegram Bot: @mvpfitness_bot"
        exit 0
    fi
    echo "   Попытка $i/20..."
    sleep 3
done

echo "❌ Приложение не запустилось. Проверьте логи:"
echo "tail -f application.log" 