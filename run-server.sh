#!/bin/bash

echo "🚀 Запуск FitCoach AI Server..."

# Остановка предыдущих процессов
pkill -f "FitnessApplication" 2>/dev/null && echo "🛑 Остановил предыдущие процессы" || echo "📋 Предыдущих процессов нет"

# Проверяем VPN и OpenAI
echo "🔍 Проверяю OpenAI API..."
if curl -s -o /dev/null --max-time 5 "https://api.openai.com/v1/models" -H "Authorization: Bearer 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"; then
    echo "✅ OpenAI API доступен (VPN работает)"
else
    echo "⚠️  OpenAI API недоступен - включите VPN или будет использован fallback режим"
fi

# Создаем класспаф с основными зависимостями
echo "📦 Подготавливаю classpath..."

# Основной classpath
CP="target/classes"

# Добавляем Spring Boot зависимости
for jar in ~/.m2/repository/org/springframework/boot/spring-boot-starter-*/*.jar \
           ~/.m2/repository/org/springframework/boot/spring-boot/*.jar \
           ~/.m2/repository/org/springframework/boot/spring-boot-autoconfigure/*.jar \
           ~/.m2/repository/org/springframework/*/*.jar \
           ~/.m2/repository/org/apache/tomcat/embed/*/*.jar \
           ~/.m2/repository/com/fasterxml/jackson/core/*/*.jar \
           ~/.m2/repository/org/slf4j/slf4j-api/*/slf4j-api-*.jar \
           ~/.m2/repository/ch/qos/logback/logback-classic/*/logback-classic-*.jar \
           ~/.m2/repository/ch/qos/logback/logback-core/*/logback-core-*.jar \
           ~/.m2/repository/org/telegram/telegrambots/*/telegrambots-*.jar \
           ~/.m2/repository/org/telegram/telegrambots-meta/*/telegrambots-meta-*.jar \
           ~/.m2/repository/com/theokanning/openai-gpt3-java/service/*/service-*.jar \
           ~/.m2/repository/com/theokanning/openai-gpt3-java/client/*/client-*.jar \
           ~/.m2/repository/com/theokanning/openai-gpt3-java/api/*/api-*.jar \
           ~/.m2/repository/com/h2database/h2/*/h2-*.jar \
           ~/.m2/repository/jakarta/annotation/jakarta.annotation-api/*/jakarta.annotation-api-*.jar; do
    if [[ -f "$jar" ]]; then
        CP="$CP:$jar"
    fi
done

echo "▶️  Запускаю сервер с оптимизированным classpath..."

# Запуск с правильными параметрами JVM
nohup java \
    -server \
    -Xms256m \
    -Xmx1024m \
    -Dspring.profiles.active=development \
    -Dlogging.level.root=INFO \
    -Djava.awt.headless=true \
    -cp "$CP" \
    com.fitcoach.FitnessApplication > app-server.log 2>&1 &

JAVA_PID=$!
echo $JAVA_PID > server.pid
echo "🆔 Server PID: $JAVA_PID"

# Ожидание запуска с таймаутом
echo "⏳ Ожидаю запуска сервера..."
for i in {1..30}; do
    if curl -s -o /dev/null --max-time 2 "http://localhost:8080/api/test/health" 2>/dev/null; then
        echo ""
        echo "🎉 ✅ СЕРВЕР ЗАПУЩЕН УСПЕШНО!"
        echo ""
        echo "🌐 Mini App:      http://localhost:8080"
        echo "📊 API Status:    http://localhost:8080/api/ai/status" 
        echo "📚 Swagger UI:    http://localhost:8080/swagger-ui.html"
        echo "🗄️  H2 Console:    http://localhost:8080/h2-console"
        echo ""
        echo "🤖 Telegram Bot:  @mvpfitness_bot"
        echo ""
        echo "📋 Логи: tail -f app-server.log"
        echo "🛑 Остановка: kill $(cat server.pid)"
        echo ""
        echo "🧪 Готов к тестированию!"
        exit 0
    fi
    echo -n "."
    sleep 2
done

echo ""
echo "❌ Сервер не запустился за 60 секунд"
echo "📋 Проверьте логи: tail -20 app-server.log"
kill $JAVA_PID 2>/dev/null
exit 1 