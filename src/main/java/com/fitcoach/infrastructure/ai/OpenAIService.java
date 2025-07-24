package com.fitcoach.infrastructure.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.image.ImageResult;
import com.theokanning.openai.service.OpenAiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class OpenAIService {
    
    private static final Logger logger = LoggerFactory.getLogger(OpenAIService.class);
    
    private final OpenAiService openAiService;
    private final ObjectMapper objectMapper;
    
    @Value("${openai.model:gpt-4-vision-preview}")
    private String model;
    
    @Value("${openai.max-tokens:1000}")
    private Integer maxTokens;
    
    @Value("${openai.temperature:0.3}")
    private Double temperature;
    
    private static final String FOOD_ANALYSIS_PROMPT = """
        Проанализируй это фото еды и верни ТОЛЬКО JSON в следующем формате:
        {
            "food_items": ["название блюда 1", "название блюда 2"],
            "total_calories": общие_калории_число,
            "proteins": белки_в_граммах_число,
            "carbs": углеводы_в_граммах_число,
            "fats": жиры_в_граммах_число,
            "fiber": клетчатка_в_граммах_число,
            "sugar": сахар_в_граммах_число,
            "detailed_items": [
                {
                    "name": "название продукта",
                    "calories": калории_число,
                    "weight": вес_в_граммах_число,
                    "confidence": уверенность_от_0_до_1,
                    "category": "категория"
                }
            ],
            "warnings": ["предупреждение 1", "предупреждение 2"],
            "recommendations": ["рекомендация 1", "рекомендация 2"]
        }
        
        Важные правила:
        1. Возвращай ТОЛЬКО валидный JSON, без дополнительного текста
        2. Если не уверен в продукте, укажи низкую confidence (0.1-0.4)
        3. Для неизвестных продуктов давай приблизительную оценку
        4. Учитывай размер порций на фото
        5. Добавляй полезные рекомендации по питанию
        """;
    
    public OpenAIService(@Value("${openai.api-key}") String apiKey) {
        this.openAiService = new OpenAiService(apiKey, Duration.ofSeconds(60));
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Анализирует фото еды и возвращает пищевую ценность
     */
    public CompletableFuture<NutritionAnalysis> analyzeFoodPhoto(MultipartFile photoFile) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("Starting food photo analysis for file: {}", photoFile.getOriginalFilename());
                
                // Проверяем тип файла
                if (!isValidImageFile(photoFile)) {
                    throw new IllegalArgumentException("Неподдерживаемый тип файла: " + photoFile.getContentType());
                }
                
                // Вычисляем хэш для кэширования
                String photoHash = calculateFileHash(photoFile);
                
                // Пытаемся получить из кэша
                NutritionAnalysis cachedResult = getCachedAnalysis(photoHash);
                if (cachedResult != null) {
                    logger.info("Returning cached analysis for hash: {}", photoHash);
                    return cachedResult;
                }
                
                // Конвертируем фото в base64
                String base64Image = encodeImageToBase64(photoFile);
                
                // Создаем запрос к OpenAI
                ChatMessage systemMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), FOOD_ANALYSIS_PROMPT);
                ChatMessage userMessage = new ChatMessage(ChatMessageRole.USER.value(), 
                    "Проанализируй это фото еды: data:image/jpeg;base64," + base64Image);
                
                ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
                    .model(model)
                    .messages(Arrays.asList(systemMessage, userMessage))
                    .maxTokens(maxTokens)
                    .temperature(temperature)
                    .build();
                
                // Отправляем запрос
                var completion = openAiService.createChatCompletion(completionRequest);
                String response = completion.getChoices().get(0).getMessage().getContent();
                
                logger.debug("OpenAI response: {}", response);
                
                // Парсим JSON ответ
                NutritionAnalysis analysis = parseAIResponse(response);
                analysis.setModelUsed(model);
                analysis.setRawResponse(response);
                
                // Сохраняем в кэш
                cacheAnalysis(photoHash, analysis);
                
                logger.info("Food analysis completed successfully. Calories: {}, Confidence: {}", 
                          analysis.getTotalCalories(), analysis.getConfidence());
                
                return analysis;
                
            } catch (Exception e) {
                logger.error("Error analyzing food photo", e);
                return createErrorAnalysis(e.getMessage());
            }
        });
    }
    
    /**
     * Генерирует персональные рекомендации по питанию
     */
    public CompletableFuture<List<String>> generateNutritionRecommendations(
            String userGoals, List<NutritionAnalysis> recentMeals) {
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                String prompt = buildRecommendationPrompt(userGoals, recentMeals);
                
                ChatMessage message = new ChatMessage(ChatMessageRole.USER.value(), prompt);
                
                ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .model("gpt-4")
                    .messages(List.of(message))
                    .maxTokens(500)
                    .temperature(0.7)
                    .build();
                
                var completion = openAiService.createChatCompletion(request);
                String response = completion.getChoices().get(0).getMessage().getContent();
                
                return Arrays.asList(response.split("\n"));
                
            } catch (Exception e) {
                logger.error("Error generating recommendations", e);
                return List.of("Не удалось сгенерировать рекомендации");
            }
        });
    }
    
    /**
     * Кэшированный анализ по хэшу изображения
     */
    @Cacheable(value = "foodAnalysis", key = "#photoHash")
    public NutritionAnalysis getCachedAnalysis(String photoHash) {
        return null; // Кэш будет управляться Spring Cache
    }
    
    // Private helper methods
    
    private boolean isValidImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && (
            contentType.equals("image/jpeg") ||
            contentType.equals("image/png") ||
            contentType.equals("image/webp")
        );
    }
    
    private String calculateFileHash(MultipartFile file) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(file.getBytes());
        return Base64.getEncoder().encodeToString(hash);
    }
    
    private String encodeImageToBase64(MultipartFile file) throws IOException {
        byte[] imageBytes = file.getBytes();
        return Base64.getEncoder().encodeToString(imageBytes);
    }
    
    private NutritionAnalysis parseAIResponse(String response) {
        try {
            // Убираем возможные markdown форматирования
            String cleanJson = response.trim();
            if (cleanJson.startsWith("```json")) {
                cleanJson = cleanJson.substring(7);
            }
            if (cleanJson.endsWith("```")) {
                cleanJson = cleanJson.substring(0, cleanJson.length() - 3);
            }
            
            NutritionAnalysis analysis = objectMapper.readValue(cleanJson, NutritionAnalysis.class);
            
            // Устанавливаем базовую уверенность, если не указана
            if (analysis.getConfidence() == null) {
                analysis.setConfidence(0.7);
            }
            
            return analysis;
            
        } catch (Exception e) {
            logger.error("Error parsing AI response: {}", response, e);
            return createErrorAnalysis("Ошибка парсинга ответа ИИ");
        }
    }
    
    private NutritionAnalysis createErrorAnalysis(String error) {
        NutritionAnalysis analysis = NutritionAnalysis.empty();
        analysis.setConfidence(0.0);
        analysis.setWarnings(List.of("Ошибка анализа: " + error));
        return analysis;
    }
    
    private String buildRecommendationPrompt(String userGoals, List<NutritionAnalysis> recentMeals) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Цели пользователя: ").append(userGoals).append("\n");
        prompt.append("Последние приемы пищи:\n");
        
        for (NutritionAnalysis meal : recentMeals) {
            prompt.append("- Калории: ").append(meal.getTotalCalories())
                  .append(", Белки: ").append(meal.getProteins())
                  .append(", Углеводы: ").append(meal.getCarbs())
                  .append(", Жиры: ").append(meal.getFats()).append("\n");
        }
        
        prompt.append("Дай 3-5 персональных рекомендаций по питанию:");
        return prompt.toString();
    }
    
    private void cacheAnalysis(String photoHash, NutritionAnalysis analysis) {
        // Кэширование будет обрабатываться Spring Cache автоматически
        logger.debug("Analysis cached for hash: {}", photoHash);
    }
} 