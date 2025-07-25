#!/bin/bash

echo "🧪 Тестирование OpenAI API подключения..."
echo "🔑 Ключ: sk-proj-IDs_l8JM...TTVdAA (скрыт для безопасности)"
echo ""

API_KEY="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"

curl -s -X POST "https://api.openai.com/v1/chat/completions" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $API_KEY" \
  -d '{
    "model": "gpt-4o-mini",
    "messages": [{"role": "user", "content": "Привет! Это тест API."}],
    "max_tokens": 20
  }' | jq '.'

echo ""
echo "✅ Если видите JSON ответ с текстом - API работает!"
echo "❌ Если ошибка - проверьте баланс на платформе OpenAI"
echo ""
echo "🌐 Проверить баланс: https://platform.openai.com/account/billing" 