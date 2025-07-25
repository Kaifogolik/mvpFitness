package com.fitcoach.infrastructure.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.image.ImageResult;
import com.theokanning.openai.service.OpenAiService;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Сервис для работы с OpenAI API
 * Включает анализ фото еды и чат-бот функциональность
 */
@Service
public class OpenAIService {
    
    private static final Logger logger = LoggerFactory.getLogger(OpenAIService.class);
    
    private final OpenAiService openAiService;
    private final ObjectMapper objectMapper;
    private final ImageProcessor imageProcessor;
    private final String apiKey;
    
    // Точный русский промпт для OpenAI (v7.0)
    private static final String NUTRITION_ANALYSIS_PROMPT = """
        Ты эксперт по питанию. Внимательно проанализируй фото еды и дай точную оценку калорийности.
        
        Если видишь еду, отвечай JSON:
        {"detected_foods":[{"food_name":"точное название","quantity":"размер","calories":реальное_число,"proteins":граммы,"fats":граммы,"carbs":граммы,"confidence":0.9}],"total_calories":точные_калории,"total_proteins":граммы,"total_fats":граммы,"total_carbs":граммы,"confidence_level":0.9,"analysis_notes":"что вижу на фото","health_recommendations":["совет"]}
        
        Если еды не видно:
        {"total_calories":0,"total_proteins":0,"total_fats":0,"total_carbs":0,"confidence_level":0,"analysis_notes":"Не могу четко определить еду на изображении","health_recommendations":["Загрузите более четкое фото"]}
        
        ВАЖНО: Используй реальные калории из базы данных, не завышай. ТОЛЬКО JSON без пояснений!
        """;

    public OpenAIService(@Value("${openai.api-key}") String apiKey, ImageProcessor imageProcessor) {
        this.apiKey = apiKey;
        this.openAiService = new OpenAiService(apiKey, Duration.ofSeconds(60));
        this.objectMapper = new ObjectMapper();
        this.imageProcessor = imageProcessor;
    }

    /**
     * Анализирует фото еды и возвращает питательную информацию
     * Результат кэшируется для повышения производительности
     */
    @Cacheable(value = "nutritionAnalysis", key = "#imageBase64.hashCode()")
    public NutritionAnalysis analyzeFoodImage(String imageBase64) {
        try {
            logger.info("Начинаю анализ изображения еды через OpenAI GPT-4V");
            
            // Используем прямой HTTP запрос для правильной отправки изображений
            String httpResponse = sendImageToOpenAI(imageBase64);
            
            if (httpResponse == null || httpResponse.trim().isEmpty()) {
                logger.warn("Получен пустой ответ от OpenAI");
                return createErrorAnalysis("OpenAI вернул пустой ответ. Попробуйте еще раз.");
            }
            
            // Парсим ответ как ChatCompletion результат
            JsonNode jsonResponse = objectMapper.readTree(httpResponse);
            
            // Логируем использование токенов если есть
            if (jsonResponse.has("usage")) {
                JsonNode usage = jsonResponse.get("usage");
                logger.info("Использовано токенов - Входящие: {}, Исходящие: {}, Всего: {}", 
                    usage.get("prompt_tokens").asInt(),
                    usage.get("completion_tokens").asInt(),
                    usage.get("total_tokens").asInt());
            }
            
            if (jsonResponse.has("choices") && jsonResponse.get("choices").size() > 0) {
                String content = jsonResponse.get("choices").get(0).get("message").get("content").asText();
                logger.info("Получен ответ от OpenAI: {}", content);
                
                // Проверяем на отказ OpenAI анализировать изображение
                if (content.contains("I'm sorry") || 
                    content.contains("I can't") || 
                    content.contains("cannot") ||
                    content.contains("unable to") ||
                    content.contains("Извините") ||
                    content.contains("не могу") ||
                    content.contains("не смогу")) {
                    logger.warn("OpenAI отказался анализировать изображение: {}", content.substring(0, Math.min(100, content.length())));
                    return createErrorAnalysis("OpenAI не смог проанализировать изображение. Попробуйте другое фото.");
                }
                
                // Улучшенный парсинг JSON ответа
                try {
                    // Извлекаем чистый JSON из ответа
                    String cleanJson = extractJsonFromResponse(content);
                    logger.info("🔍 Ответ OpenAI (первые 300 символов): {}", content.substring(0, Math.min(300, content.length())));
                    logger.debug("Извлеченный JSON: {}", cleanJson);
                    
                    // Парсим JSON
                    NutritionAnalysis analysis = objectMapper.readValue(cleanJson, NutritionAnalysis.class);
                    
                    // Валидация результата
                    if (isValidAnalysis(analysis)) {
                        logger.info("✅ Успешный анализ: {} ккал, уверенность: {}", 
                            analysis.getTotalCalories(), analysis.getConfidenceLevel());
                        return analysis;
                    } else {
                        logger.warn("⚠️ Некорректные данные в анализе");
                        return createErrorAnalysis("Получены некорректные данные от ИИ. Попробуйте еще раз.");
                    }
                    
                } catch (JsonProcessingException e) {
                    logger.error("❌ Ошибка парсинга JSON: {}", e.getMessage());
                    logger.debug("Проблемный ответ (первые 300 символов): {}", 
                        content.substring(0, Math.min(300, content.length())));
                    return createErrorAnalysis("Ошибка обработки ответа ИИ. Попробуйте еще раз.");
                }
                
            } else {
                logger.warn("OpenAI не вернул ответ для анализа изображения.");
                return createErrorAnalysis("OpenAI не вернул ответ. Проверьте подключение и попробуйте снова.");
            }
            
        } catch (Exception e) {
            logger.error("Ошибка при анализе изображения еды: {}", e.getMessage(), e);
            
            // FALLBACK: Возвращаем демо-данные если OpenAI недоступен или изображение слишком большое
            if (e.getMessage().contains("unsupported_country") || 
                e.getMessage().contains("region") ||
                e.getMessage().contains("Request too large") ||
                e.getMessage().contains("tokens per min") ||
                e.getMessage().contains("maximum context length") ||
                e.getMessage().contains("resulted in") && e.getMessage().contains("tokens") ||
                e.getMessage().contains("Limit") && e.getMessage().contains("Requested") ||
                e.getMessage().contains("input or output tokens must be reduced") ||
                e.getMessage().contains("не могу анализировать изображения") ||
                e.getMessage().contains("can't analyze") ||
                e.getMessage().contains("can't interpret images") ||
                e.getMessage().contains("Rate limit reached") ||
                e.getMessage().contains("Unrecognized token")) {
                
                logger.warn("⚠️ OpenAI лимит токенов: {}", 
                    e.getMessage().substring(0, Math.min(150, e.getMessage().length())));
                return createErrorAnalysis("🔧 Изображение слишком детальное. Попробуйте более простое фото или уменьшите размер файла.");
            }
            
            return createErrorAnalysis("Ошибка при анализе изображения: " + e.getMessage());
        }
    }

    /**
     * Анализирует фото еды из файла с обработкой изображения
     */
    public NutritionAnalysis analyzeFoodImageFromFile(File imageFile) {
        try {
            // Читаем файл
            byte[] imageBytes = java.nio.file.Files.readAllBytes(imageFile.toPath());
            
            return analyzeFoodImageFromBytes(imageBytes, imageFile.getName());
            
        } catch (IOException e) {
            logger.error("Ошибка при чтении файла изображения: {}", e.getMessage(), e);
            return createErrorAnalysis("Ошибка при чтении файла: " + e.getMessage());
        }
    }
    
    /**
     * Анализирует фото еды из массива байт с предварительной обработкой
     */
    public NutritionAnalysis analyzeFoodImageFromBytes(byte[] imageBytes, String fileName) {
        try {
            logger.info("🔍 Анализ изображения: {}, размер: {} bytes", fileName, imageBytes.length);
            
            // Валидируем изображение
            ImageProcessor.ImageValidationResult validation = imageProcessor.validateImage(imageBytes, fileName);
            if (!validation.isValid()) {
                logger.warn("❌ Валидация изображения не пройдена: {}", validation.getMessage());
                return createErrorAnalysis("Некорректное изображение: " + validation.getMessage());
            }
            
            // Обрабатываем изображение для OpenAI
            ImageProcessor.ProcessedImage processedImage = imageProcessor.processImageForAI(
                imageBytes, getFileExtension(fileName));
            
            logger.info("✅ Изображение обработано: {}x{}, {} bytes, сжатие: {:.1f}%", 
                processedImage.getWidth(), processedImage.getHeight(), 
                processedImage.getFileSize(), processedImage.getCompressionRatio());
            
            // Анализируем обработанное изображение
            return analyzeFoodImage(processedImage.getBase64Data());
            
        } catch (Exception e) {
            logger.error("❌ Ошибка при обработке изображения: {}", e.getMessage(), e);
            return createErrorAnalysis("Ошибка обработки изображения: " + e.getMessage());
        }
    }
    
    /**
     * Извлекает расширение файла
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "jpg";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * Чат-бот для вопросов о питании и фитнесе
     */
    public String chatWithNutritionBot(String userMessage, String userContext) {
        try {
            logger.info("Обработка сообщения пользователя: {}", userMessage);
            
            String systemPrompt = """
                Ты - персональный фитнес тренер и нутрициолог в приложении FitCoach. Обращайся к пользователю на "ты".
                
                📋 СТРУКТУРА ОТВЕТОВ:
                
                🎯 **Главное:** [краткий ответ]
                
                📊 **Детали:**
                • Пункт 1
                • Пункт 2  
                • Пункт 3
                
                💡 **Рекомендации:**
                ✅ Что делать
                ❌ Чего избегать
                
                🚀 **Следующий шаг:** [конкретное действие]
                
                ТВОЯ РОЛЬ КАК ТРЕНЕРА:
                - Мотивируй и поддерживай 
                - Давай конкретные планы действий
                - Объясняй "почему" за каждым советом
                - Адаптируй советы под цели пользователя
                - Отслеживай прогресс и корректируй подход
                
                Контекст пользователя: """ + (userContext != null ? userContext : "Новый подопечный") + """
                
                Пиши по-русски, будь мотивирующим но реалистичным. Используй эмодзи для структуры.
                """;

            List<ChatMessage> messages = Arrays.asList(
                new ChatMessage(ChatMessageRole.SYSTEM.value(), systemPrompt),
                new ChatMessage(ChatMessageRole.USER.value(), userMessage)
            );

            ChatCompletionRequest chatRequest = ChatCompletionRequest.builder()
                    .model("gpt-4o-mini") // GPT-4o-mini для чата - в 4 раза дешевле GPT-4
                    .messages(messages)
                    .maxTokens(400) // Оптимизировано для экономии
                    .temperature(0.7) // Умеренная креативность
                    .build();

            var completion = openAiService.createChatCompletion(chatRequest);
            String response = completion.getChoices().get(0).getMessage().getContent();
            
            logger.info("Ответ чат-бота: {}", response);
            return response;
            
        } catch (Exception e) {
            logger.error("Ошибка в чат-боте: {}", e.getMessage(), e);
            return "Извините, произошла ошибка при обработке вашего запроса. Попробуйте позже.";
        }
    }

    /**
     * Генерирует персональные рекомендации по питанию
     */
    public List<String> generateNutritionRecommendations(String userGoals, String currentDiet, String restrictions) {
        try {
            String prompt = String.format("""
                Создай 3-5 персональных рекомендаций по питанию для пользователя:
                
                Цели: %s
                Текущий рацион: %s  
                Ограничения: %s
                
                Верни результат в виде списка коротких, практичных советов на русском языке.
                Каждый совет должен быть конкретным и выполнимым.
                """, 
                userGoals != null ? userGoals : "Общее улучшение здоровья",
                currentDiet != null ? currentDiet : "Стандартный рацион",
                restrictions != null ? restrictions : "Нет ограничений"
            );

            List<ChatMessage> messages = Arrays.asList(
                new ChatMessage(ChatMessageRole.USER.value(), prompt)
            );

            ChatCompletionRequest chatRequest = ChatCompletionRequest.builder()
                    .model("gpt-4o-mini") // Экономная модель для рекомендаций
                    .messages(messages)
                    .maxTokens(300) // Короткие, но полезные советы
                    .temperature(0.8)
                    .build();

            var completion = openAiService.createChatCompletion(chatRequest);
            String response = completion.getChoices().get(0).getMessage().getContent();
            
            // Парсим ответ в список рекомендаций
            return Arrays.asList(response.split("\n"));
            
        } catch (Exception e) {
            logger.error("Ошибка при генерации рекомендаций: {}", e.getMessage(), e);
            return Arrays.asList("Пейте больше воды", "Ешьте больше овощей", "Контролируйте размер порций");
        }
    }

    /**
     * Извлекает JSON из ответа OpenAI (улучшенная версия)
     */
    private String extractJsonFromResponse(String response) {
        // Удаляем лишние символы и пробелы
        response = response.trim();
        
        // Ищем JSON блок в ответе
        int startIndex = response.indexOf("{");
        int endIndex = response.lastIndexOf("}");
        
        if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            String json = response.substring(startIndex, endIndex + 1);
            
            // Очищаем JSON от возможных markdown блоков
            json = json.replace("```json", "").replace("```", "").trim();
            
            return json;
        }
        
        // Если не нашли JSON, возвращаем весь ответ
        return response.replace("```json", "").replace("```", "").trim();
    }
    
    /**
     * Валидирует результат анализа питания
     */
    private boolean isValidAnalysis(NutritionAnalysis analysis) {
        if (analysis == null) {
            return false;
        }
        
        // Проверяем что основные поля заполнены
        if (analysis.getTotalCalories() == null || analysis.getTotalCalories() < 0) {
            logger.warn("Некорректные калории: {}", analysis.getTotalCalories());
            return false;
        }
        
        if (analysis.getTotalCalories() > 5000) {
            logger.warn("Слишком много калорий: {}", analysis.getTotalCalories());
            return false;
        }
        
        if (analysis.getConfidenceLevel() == null || 
            analysis.getConfidenceLevel() < 0 || analysis.getConfidenceLevel() > 1) {
            logger.warn("Некорректный уровень уверенности: {}", analysis.getConfidenceLevel());
            return false;
        }
        
        if (analysis.getDetectedFoods() == null || analysis.getDetectedFoods().isEmpty()) {
            logger.warn("Не обнаружено продуктов в анализе");
            return false;
        }
        
        // Проверяем БЖУ
        if (analysis.getTotalProteins() == null || analysis.getTotalProteins() < 0 ||
            analysis.getTotalFats() == null || analysis.getTotalFats() < 0 ||
            analysis.getTotalCarbs() == null || analysis.getTotalCarbs() < 0) {
            logger.warn("Некорректные БЖУ: P={}, F={}, C={}", 
                analysis.getTotalProteins(), analysis.getTotalFats(), analysis.getTotalCarbs());
            return false;
        }
        
        return true;
    }

    /**
     * Создает объект анализа с ошибкой
     */
    private NutritionAnalysis createErrorAnalysis(String errorMessage) {
        NutritionAnalysis analysis = new NutritionAnalysis();
        analysis.setTotalCalories(0.0);
        analysis.setTotalProteins(0.0);
        analysis.setTotalFats(0.0);
        analysis.setTotalCarbs(0.0);
        analysis.setConfidenceLevel(0.0);
        analysis.setAnalysisNotes(errorMessage);
        analysis.setDetectedFoods(new ArrayList<>());
        analysis.setHealthRecommendations(Arrays.asList("Попробуйте загрузить изображение еще раз"));
        return analysis;
    }



    /**
     * Проверяет работоспособность OpenAI API
     */
    public boolean isApiHealthy() {
        try {
            List<ChatMessage> messages = Arrays.asList(
                new ChatMessage(ChatMessageRole.USER.value(), "Hello")
            );

            ChatCompletionRequest chatRequest = ChatCompletionRequest.builder()
                    .model("gpt-3.5-turbo") // Самая дешевая модель для проверки подключения
                    .messages(messages)
                    .maxTokens(5) // Минимум токенов для теста
                    .build();

            openAiService.createChatCompletion(chatRequest);
            return true;
            
        } catch (Exception e) {
            logger.warn("OpenAI API недоступен: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Отправляет изображение в OpenAI GPT-4V через HTTP API (правильный формат)
     */
    private String sendImageToOpenAI(String base64Image) {
        try {
            // Создаем JSON вручную для надежности
            String cleanPrompt = NUTRITION_ANALYSIS_PROMPT.replace("\"", "\\\"").replace("\n", " ");
            
            String requestBody = String.format("""
                {
                  "model": "gpt-4o",
                  "messages": [
                    {
                      "role": "user",
                      "content": [
                        {
                          "type": "text",
                          "text": "%s"
                        },
                        {
                          "type": "image_url",
                          "image_url": {
                            "url": "data:image/jpeg;base64,%s"
                          }
                        }
                      ]
                    }
                  ],
                  "max_tokens": 800,
                  "temperature": 0.1
                }
                """, cleanPrompt, base64Image);
            
            logger.debug("📤 Отправляемый JSON размер: {} символов", requestBody.length());
            
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
            
            logger.info("Отправляю изображение в OpenAI GPT-4V через HTTP API...");
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                logger.info("✅ Успешный ответ от OpenAI GPT-4V");
                logger.debug("🔍 Полный ответ OpenAI: {}", response.body().substring(0, Math.min(500, response.body().length())));
                return response.body();
            } else {
                logger.error("❌ OpenAI API вернул ошибку: {} - {}", response.statusCode(), response.body());
                return null;
            }
            
        } catch (Exception e) {
            logger.error("❌ Ошибка при отправке HTTP запроса в OpenAI: {}", e.getMessage(), e);
            return null;
        }
    }

} 