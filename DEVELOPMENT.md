# 🚀 Документ разработки FitCoach AI Platform

## 📋 Обзор проекта

**FitCoach AI Platform** - это умная фитнес-платформа с интеграцией искусственного интеллекта для анализа питания и системой тренер-ученик. Проект реализован как MVP в виде Telegram Mini App с мощным backend на Spring Boot.

### 🎯 Основные возможности

- **🤖 ИИ-анализ фото еды** через OpenAI GPT-4V с определением калорий и БЖУ
- **💬 AI чат-консультант** по вопросам питания и здорового образа жизни
- **📱 Telegram Bot** с интуитивным интерфейсом и клавиатурой
- **🌐 REST API** для интеграции с веб-приложениями
- **📊 Swagger UI** для документации и тестирования API
- **🔄 Кэширование** результатов анализа для оптимизации

## 🏗️ Архитектура системы

### Технологический стек

```
Backend:
├── Java 21
├── Spring Boot 3.2.1
├── Spring Security + JWT
├── PostgreSQL (производство) / H2 (разработка)
├── Redis (кэширование)
├── OpenAI GPT-4V API
├── Telegram Bot API
└── Docker + Docker Compose

AI & Интеграции:
├── OpenAI GPT-4o (анализ изображений)
├── OpenAI GPT-4o-mini (чат-бот)
├── Image Processing (сжатие и оптимизация)
└── Nutrition Analysis (анализ питания)

Инфраструктура:
├── Maven (сборка проекта)
├── Swagger/OpenAPI 3 (документация)
├── Spring Boot Actuator (мониторинг)
└── Логирование (SLF4J + Logback)
```

### Архитектурные слои

```
src/main/java/com/fitcoach/
├── api/                    # REST API контроллеры
│   ├── AiController.java          # AI функции (анализ, чат)
│   └── TestController.java        # Тестовые endpoints
├── config/                 # Конфигурация Spring
│   └── SecurityConfig.java        # Настройки безопасности
├── infrastructure/         # Внешние интеграции
│   ├── ai/                        # OpenAI сервисы
│   │   ├── OpenAIService.java     # Основной AI сервис
│   │   ├── ImageProcessor.java    # Обработка изображений
│   │   ├── NutritionAnalysis.java # Модель анализа питания
│   │   └── ProductsDatabase.java  # База продуктов (fallback)
│   └── telegram/                  # Telegram Bot
│       ├── SimpleTelegramBot.java      # Основная логика бота
│       └── SimpleTelegramBotConfig.java # Конфигурация бота
└── FitnessApplication.java # Главный класс приложения
```

## 🔧 Ключевые компоненты

### 1. OpenAI Сервис (`OpenAIService.java`)

**Назначение:** Центральный компонент для всех AI функций

**Основные методы:**
- `analyzeFoodImage(String base64)` - анализ фото еды через GPT-4V
- `analyzeFoodImageFromBytes(byte[], String)` - анализ с предобработкой
- `chatWithNutritionBot(String, String)` - AI чат-консультант
- `generateNutritionRecommendations()` - персональные рекомендации
- `isApiHealthy()` - проверка состояния OpenAI API

**Особенности реализации:**
```java
// Анализ изображения с оптимизацией токенов
@Cacheable(value = "nutritionAnalysis")
public NutritionAnalysis analyzeFoodImage(String imageBase64) {
    // Прямой HTTP запрос к OpenAI GPT-4V
    String httpResponse = sendImageToOpenAI(imageBase64);
    // Парсинг JSON ответа с валидацией
    return parseAndValidateAnalysis(httpResponse);
}
```

### 2. Telegram Bot (`SimpleTelegramBot.java`)

**Назначение:** Интерфейс взаимодействия с пользователями через Telegram

**Основные команды:**
- `/start` - регистрация и главное меню
- `/food` - режим анализа фото еды
- `/chat` - активация AI чата
- `/help` - справка по командам
- `/status` - статус системы

**Интерактивная клавиатура:**
```java
private ReplyKeyboardMarkup createMainKeyboard() {
    // Три ряда кнопок:
    // 📸 Анализ еды | 🤖 AI Чат
    // 📱 Открыть приложение | ℹ️ Помощь  
    // 📊 Статус | ℹ️ О проекте
}
```

### 3. AI API Controller (`AiController.java`)

**Назначение:** REST API для AI функций

**Endpoints:**
- `POST /api/ai/analyze-food-photo` - загрузка и анализ фото
- `POST /api/ai/analyze-food-base64` - анализ base64 изображения
- `POST /api/ai/chat` - чат с AI консультантом
- `POST /api/ai/recommendations` - персональные рекомендации
- `GET /api/ai/status` - статус AI сервисов
- `POST /api/ai/quick-calories` - быстрый анализ калорий

### 4. Image Processor (`ImageProcessor.java`)

**Назначение:** Оптимизация изображений для экономии токенов OpenAI

**Функции:**
- Валидация изображений (формат, размер)
- Сжатие для экономии токенов (до 75% экономии)
- Конвертация в оптимальный формат для AI
- Контроль качества обработки

## 📊 Модель данных

### NutritionAnalysis (Анализ питания)

```java
public class NutritionAnalysis {
    private List<DetectedFood> detectedFoods;    // Обнаруженные продукты
    private Double totalCalories;                // Общие калории
    private Double totalProteins;                // Белки (г)
    private Double totalFats;                    // Жиры (г) 
    private Double totalCarbs;                   // Углеводы (г)
    private Double confidenceLevel;              // Уровень уверенности (0-1)
    private String analysisNotes;                // Заметки анализа
    private List<String> healthRecommendations;  // Рекомендации
}

public static class DetectedFood {
    private String foodName;      // Название продукта
    private String quantity;      // Количество/порция
    private Double calories;      // Калории продукта
    private Double proteins;      // Белки (г)
    private Double fats;         // Жиры (г)
    private Double carbs;        // Углеводы (г)
    private Double confidence;   // Уверенность распознавания
}
```

## 🛠️ Инструкции по разработке

### Настройка среды разработки

1. **Требования:**
   ```bash
   Java 21+
   Maven 3.9+
   Docker & Docker Compose
   PostgreSQL 15+ (опционально)
   Redis 7+ (опционально)
   ```

2. **Переменные окружения:**
   ```bash
   # Обязательные
   TELEGRAM_BOT_TOKEN=ваш_токен_от_BotFather
   TELEGRAM_BOT_USERNAME=имя_вашего_бота
   OPENAI_API_KEY=ваш_ключ_OpenAI
   
   # Опциональные
   JWT_SECRET=секретный_ключ_для_JWT
   ```

3. **Запуск для разработки:**
   ```bash
   # 1. Клонирование репозитория
   git clone <repository-url>
   cd Fitnes
   
   # 2. Настройка переменных в config.properties
   cp config.properties.example config.properties
   # Отредактировать значения
   
   # 3. Запуск с Docker Compose
   docker-compose up -d
   
   # 4. Или локальный запуск
   mvn spring-boot:run
   ```

### Структура конфигурации

#### application.yml
```yaml
spring:
  profiles:
    active: development
  datasource:
    url: jdbc:postgresql://localhost:5432/fitness_app
  jpa:
    hibernate:
      ddl-auto: update

telegram:
  bot:
    token: ${TELEGRAM_BOT_TOKEN:}
    username: ${TELEGRAM_BOT_USERNAME:}

openai:
  api-key: ${OPENAI_API_KEY:}
  model: gpt-4-vision-preview
  max-tokens: 1000
```

#### Docker Compose
```yaml
services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - TELEGRAM_BOT_TOKEN=${TELEGRAM_BOT_TOKEN}
      - OPENAI_API_KEY=${OPENAI_API_KEY}
    depends_on:
      - postgres
      - redis
```

### Добавление новых функций

#### 1. Новый AI метод

```java
// В OpenAIService.java
public String analyzeNewFeature(String input) {
    ChatCompletionRequest request = ChatCompletionRequest.builder()
        .model("gpt-4o-mini")
        .messages(createMessages(input))
        .maxTokens(300)
        .build();
    
    return openAiService.createChatCompletion(request)
        .getChoices().get(0).getMessage().getContent();
}
```

#### 2. Новый API endpoint

```java
// В AiController.java
@PostMapping("/new-feature")
@Operation(summary = "Новая функция")
public ResponseEntity<Map<String, Object>> newFeature(
    @RequestBody Map<String, String> request) {
    
    String result = openAIService.analyzeNewFeature(
        request.get("input"));
    
    return ResponseEntity.ok(Map.of(
        "success", true,
        "result", result
    ));
}
```

#### 3. Новая команда Telegram бота

```java
// В SimpleTelegramBot.java
case "/newcommand":
    responseText = "Описание новой команды";
    // Дополнительная логика
    break;
```

## 🧪 Тестирование

### API тестирование

**Swagger UI:** `http://localhost:8080/swagger-ui.html`

**Примеры запросов:**

1. **Анализ фото еды:**
   ```bash
   curl -X POST "http://localhost:8080/api/ai/analyze-food-photo" \
        -H "Content-Type: multipart/form-data" \
        -F "photo=@food.jpg"
   ```

2. **AI чат:**
   ```bash
   curl -X POST "http://localhost:8080/api/ai/chat" \
        -H "Content-Type: application/json" \
        -d '{"message": "Сколько калорий в яблоке?"}'
   ```

3. **Проверка статуса:**
   ```bash
   curl "http://localhost:8080/api/ai/status"
   ```

### Telegram Bot тестирование

1. Найдите вашего бота в Telegram
2. Отправьте `/start` для инициализации
3. Используйте кнопки меню или команды:
   - Отправьте фото еды для анализа
   - Напишите вопрос для AI чата
   - Попробуйте команды `/help`, `/status`

### Unit тесты

```bash
# Запуск всех тестов
mvn test

# Запуск конкретного теста
mvn test -Dtest=OpenAIServiceTest

# Интеграционные тесты
mvn test -Dtest=*IntegrationTest
```

## 📈 Мониторинг и логирование

### Actuator endpoints

- `/actuator/health` - состояние приложения
- `/actuator/metrics` - метрики производительности
- `/actuator/info` - информация о приложении

### Логирование

**Конфигурация в application.yml:**
```yaml
logging:
  level:
    com.fitcoach: DEBUG
    org.springframework.security: DEBUG
    org.telegram: INFO
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
```

**Структура логов:**
```
2024-01-20 10:30:15 - 🔍 Анализ изображения: food.jpg, размер: 245760 bytes
2024-01-20 10:30:16 - ✅ Изображение обработано: 512x384, 89342 bytes, сжатие: 63.6%
2024-01-20 10:30:20 - ✅ Успешный анализ: 450 ккал, уверенность: 0.89
```

## 🔒 Безопасность

### Конфигурация Spring Security

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        return http
            .cors().and()
            .csrf().disable()
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/ai/**").permitAll()
                .requestMatchers("/swagger-ui/**").permitAll()
                .anyRequest().authenticated()
            )
            .build();
    }
}
```

### Защита API ключей

- Используйте переменные окружения для секретов
- Никогда не коммитьте API ключи в Git
- Используйте отдельные ключи для dev/prod сред
- Мониторьте использование токенов OpenAI

## 🚀 Деплоймент

### Production готовность

1. **Переменные окружения production:**
   ```bash
   TELEGRAM_BOT_TOKEN=prod_bot_token
   OPENAI_API_KEY=prod_openai_key
   JWT_SECRET=secure_256_bit_key
   SPRING_PROFILES_ACTIVE=production
   ```

2. **Docker образ:**
   ```bash
   # Сборка образа
   docker build -t fitcoach-ai:latest .
   
   # Запуск в production
   docker-compose -f docker-compose.prod.yml up -d
   ```

3. **База данных:**
   ```yaml
   # production настройки
   spring:
     jpa:
       hibernate:
         ddl-auto: validate  # НЕ update в production!
     datasource:
       url: jdbc:postgresql://prod-db:5432/fitness_app
   ```

### CI/CD Pipeline

```yaml
# .github/workflows/deploy.yml
name: Deploy FitCoach AI
on:
  push:
    branches: [main]
jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Setup Java 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
      - name: Run tests
        run: mvn test
      - name: Build Docker image
        run: docker build -t fitcoach-ai .
      - name: Deploy to production
        run: docker-compose up -d
```

## 📊 Производительность и оптимизация

### Кэширование

```java
// Кэширование результатов анализа
@Cacheable(value = "nutritionAnalysis", key = "#imageBase64.hashCode()")
public NutritionAnalysis analyzeFoodImage(String imageBase64) {
    // Результат кэшируется автоматически
}
```

### Оптимизация токенов OpenAI

1. **Сжатие изображений** до 75% экономии токенов
2. **Использование GPT-4o-mini** для чата (в 4 раза дешевле)
3. **Кэширование** повторных запросов
4. **Оптимизированные промпты** для точных ответов

### Мониторинг токенов

```java
// Логирование использования токенов
if (jsonResponse.has("usage")) {
    JsonNode usage = jsonResponse.get("usage");
    logger.info("Использовано токенов - Входящие: {}, Исходящие: {}, Всего: {}", 
        usage.get("prompt_tokens").asInt(),
        usage.get("completion_tokens").asInt(), 
        usage.get("total_tokens").asInt());
}
```

## 🔄 Планы развития

### Версия 1.1 (следующий спринт)
- [ ] Персистентное хранение истории анализов
- [ ] Пользовательские профили с целями
- [ ] Статистика прогресса по дням/неделям
- [ ] Экспорт данных в CSV/PDF

### Версия 1.2 (через месяц)
- [ ] Система тренер-ученик
- [ ] Платежные интеграции (Telegram Payments)
- [ ] Групповые чаты и вызовы
- [ ] Уведомления и напоминания

### Версия 2.0 (долгосрочно)
- [ ] Мобильные приложения (Android/iOS)
- [ ] Интеграция с фитнес-трекерами
- [ ] ML рекомендации на основе истории
- [ ] Социальные функции и сообщества

## 🤝 Участие в разработке

### Стандарты кода

1. **Java Code Style:**
   - Используйте Java 21 features
   - Следуйте Google Java Style Guide
   - Документируйте public методы JavaDoc

2. **Git Flow:**
   - `main` - production ready код
   - `develop` - интеграционная ветка
   - `feature/*` - новые функции
   - `hotfix/*` - критичные исправления

3. **Commit сообщения:**
   ```
   feat: добавлен анализ БЖУ в фото еды
   fix: исправлена ошибка парсинга OpenAI ответа
   docs: обновлена документация API
   test: добавлены тесты для ImageProcessor
   ```

### Pull Request процесс

1. Создайте feature branch
2. Напишите тесты для новой функциональности
3. Убедитесь что все тесты проходят
4. Создайте PR с подробным описанием
5. Пройдите code review

## 📞 Поддержка и контакты

- **Issues:** [GitHub Issues](https://github.com/your-repo/issues)
- **Email:** support@fitcoach.ai
- **Telegram:** @fitcoach_support
- **Документация:** Swagger UI `/swagger-ui.html`

---

**© 2024 FitCoach AI Platform - Умное питание с искусственным интеллектом** 🚀🤖 