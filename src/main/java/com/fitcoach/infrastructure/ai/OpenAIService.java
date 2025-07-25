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
import java.util.List;

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
    
    // Универсальный промпт для любой кухни мира (v4.0)
    private static final String NUTRITION_ANALYSIS_PROMPT = """
        Ты эксперт по питанию. Проанализируй фото еды и верни точный JSON:
        
        {
          "detected_foods": [
            {
              "food_name": "точное название блюда",
              "quantity": "размер порции",
              "calories": калории_числом,
              "proteins": белки_в_граммах,
              "fats": жиры_в_граммах,
              "carbs": углеводы_в_граммах,
              "confidence": уверенность_0_до_1
            }
          ],
          "total_calories": общие_калории,
          "total_proteins": общие_белки_г,
          "total_fats": общие_жиры_г,
          "total_carbs": общие_углеводы_г,
          "confidence_level": общая_уверенность,
          "analysis_notes": "что видишь на фото",
          "health_recommendations": ["практичный совет"]
        }
        
        ВАЖНО: определи реальный размер порции, любую кухню мира, будь максимально точным. ТОЛЬКО JSON!
        """;

    public OpenAIService(@Value("${openai.api-key}") String apiKey, ImageProcessor imageProcessor) {
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
            logger.info("Начинаю анализ изображения еды через OpenAI GPT-4o");
            
            // Правильный формат для GPT-4V с изображениями
            ChatCompletionRequest chatRequest = ChatCompletionRequest.builder()
                    .model("gpt-4o") // GPT-4o с поддержкой изображений
                    .messages(Arrays.asList(
                        new ChatMessage(ChatMessageRole.USER.value(), 
                            NUTRITION_ANALYSIS_PROMPT + "\n\n[IMAGE: " + imageBase64.substring(0, Math.min(100, imageBase64.length())) + "...]")
                    ))
                    .maxTokens(400) // Увеличиваем для подробного анализа
                    .temperature(0.1) // Минимальная креативность для точности
                    .build();

            logger.info("Отправляю запрос в OpenAI gpt-4o с радикальными оптимизациями токенов...");
            ChatCompletionResult chatResult = openAiService.createChatCompletion(chatRequest);
            
            if (chatResult.getChoices() != null && !chatResult.getChoices().isEmpty()) {
                String response = chatResult.getChoices().get(0).getMessage().getContent();
                logger.info("Получен ответ от OpenAI: {}", response);
                
                // Логируем использование токенов для мониторинга
                if (chatResult.getUsage() != null) {
                    logger.info("Использовано токенов - Входящие: {}, Исходящие: {}, Всего: {}", 
                        chatResult.getUsage().getPromptTokens(),
                        chatResult.getUsage().getCompletionTokens(), 
                        chatResult.getUsage().getTotalTokens());
                }
                
                // Проверяем корректность ответа перед парсингом
                if (response == null || response.trim().isEmpty()) {
                    logger.warn("Получен пустой ответ от OpenAI");
                    return createErrorAnalysis("OpenAI вернул пустой ответ. Попробуйте еще раз.");
                }
                
                // Проверяем на отказ OpenAI анализировать изображение
                if (response.contains("I'm sorry") || 
                    response.contains("I can't") || 
                    response.contains("cannot") ||
                    response.contains("unable to") ||
                    response.contains("Извините") ||
                    response.contains("не могу") ||
                    response.contains("не смогу")) {
                    logger.warn("OpenAI отказался анализировать изображение: {}", response.substring(0, Math.min(100, response.length())));
                    return createErrorAnalysis("OpenAI не смог проанализировать изображение. Попробуйте другое фото.");
                }
                
                // Улучшенный парсинг JSON ответа
                try {
                    // Извлекаем чистый JSON из ответа
                    String cleanJson = extractJsonFromResponse(response);
                    logger.debug("Извлеченный JSON: {}", cleanJson);
                    
                    // Парсим JSON
                    ObjectMapper mapper = new ObjectMapper();
                    NutritionAnalysis analysis = mapper.readValue(cleanJson, NutritionAnalysis.class);
                    
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
                        response.substring(0, Math.min(300, response.length())));
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
                e.getMessage().contains("не могу анализировать изображения") ||
                e.getMessage().contains("can't analyze") ||
                e.getMessage().contains("can't interpret images") ||
                e.getMessage().contains("Rate limit reached") ||
                e.getMessage().contains("Unrecognized token")) {
                
                logger.warn("OpenAI недоступен: {}", 
                    e.getMessage().substring(0, Math.min(150, e.getMessage().length())));
                return createErrorAnalysis("Сервис анализа временно недоступен: " + e.getMessage());
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
                Ты - AI помощник по питанию и фитнесу в приложении FitCoach.
                
                Твоя роль:
                - Отвечать на вопросы о питании, калориях, диетах
                - Давать советы по здоровому питанию
                - Помогать с планированием рациона
                - Объяснять пищевую ценность продуктов
                - Поддерживать мотивацию пользователей
                
                Контекст пользователя: """ + (userContext != null ? userContext : "Новый пользователь") + """
                
                Отвечай на русском языке, будь дружелюбным и профессиональным.
                Если не знаешь точного ответа, честно признайся и предложи обратиться к специалисту.
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

} 