package com.fitcoach.api;

import com.fitcoach.infrastructure.ai.NutritionAnalysis;
import com.fitcoach.infrastructure.ai.OpenAIService;
import com.fitcoach.model.NutritionEntry;
import com.fitcoach.model.User;
import com.fitcoach.service.NutritionService;
import com.fitcoach.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * REST API для AI функций
 */
@RestController
@RequestMapping("/api/ai")
@Tag(name = "AI Functions", description = "OpenAI интеграция для анализа питания и чат-бота")
@CrossOrigin(origins = "*")
public class AiController {
    
    private static final Logger logger = LoggerFactory.getLogger(AiController.class);
    
    @Autowired
    private OpenAIService openAIService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private NutritionService nutritionService;

    /**
     * Анализ фото еды через OpenAI GPT-4V
     */
    @PostMapping("/analyze-food-photo")
    @Operation(summary = "Анализ фото еды", description = "Загрузка и анализ фото еды с определением калорий и БЖУ")
    public ResponseEntity<Map<String, Object>> analyzeFoodPhoto(@RequestParam("photo") MultipartFile photo) {
        try {
            logger.info("Получен запрос на анализ фото еды, размер: {} bytes", photo.getSize());
            
            // Валидация файла
            if (photo.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Файл пустой", "success", false));
            }
            
            if (photo.getSize() > 10 * 1024 * 1024) { // 10MB
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Файл слишком большой (максимум 10MB)", "success", false));
            }
            
            String contentType = photo.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Файл должен быть изображением", "success", false));
            }
            
            // Анализируем с предварительной обработкой изображения
            byte[] imageBytes = photo.getBytes();
            NutritionAnalysis analysis = openAIService.analyzeFoodImageFromBytes(
                imageBytes, photo.getOriginalFilename());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("analysis", analysis);
            response.put("timestamp", new Date());
            response.put("filename", photo.getOriginalFilename());
            
            logger.info("Анализ фото завершен успешно");
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            logger.error("Ошибка при чтении файла: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Ошибка при обработке файла", "success", false));
        } catch (Exception e) {
            logger.error("Ошибка при анализе фото: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Ошибка при анализе изображения", "success", false));
        }
    }

    /**
     * Анализ фото еды через base64 (для Telegram Mini App)
     */
    @PostMapping("/analyze-food-base64")
    @Operation(summary = "Анализ фото еды (base64)", description = "Анализ фото еды переданного в base64 формате")
    public ResponseEntity<Map<String, Object>> analyzeFoodBase64(@RequestBody Map<String, String> request) {
        try {
            String base64Image = request.get("image");
            String telegramId = request.get("telegramId"); // опционально
            
            if (base64Image == null || base64Image.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Изображение не предоставлено", "success", false));
            }
            
            // Убираем data:image/...;base64, если присутствует
            if (base64Image.contains(",")) {
                base64Image = base64Image.split(",")[1];
            }
            
            logger.info("Анализируем изображение в base64 формате{}", 
                       telegramId != null ? " для пользователя: " + telegramId : "");
            
            // Анализируем через OpenAI
            NutritionAnalysis analysis = openAIService.analyzeFoodImage(base64Image);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("analysis", analysis);
            response.put("timestamp", new Date());
            
            // Сохраняем в историю питания если указан telegramId
            if (telegramId != null && !telegramId.trim().isEmpty()) {
                try {
                    Optional<User> userOpt = userService.findByTelegramId(telegramId);
                    if (userOpt.isPresent()) {
                        // Сохраняем анализ в историю питания
                        List<NutritionEntry> savedEntries = nutritionService.saveNutritionAnalysis(
                            userOpt.get(), analysis, base64Image);
                        
                        // Добавляем информацию о сохраненных записях
                        List<Map<String, Object>> entriesData = savedEntries.stream()
                            .map(entry -> {
                                Map<String, Object> entryData = new HashMap<>();
                                entryData.put("id", entry.getId());
                                entryData.put("foodName", entry.getFoodName());
                                entryData.put("calories", entry.getCalories());
                                entryData.put("proteins", entry.getProteins());
                                entryData.put("fats", entry.getFats());
                                entryData.put("carbs", entry.getCarbs());
                                entryData.put("mealType", entry.getMealType());
                                entryData.put("timestamp", entry.getTimestamp());
                                return entryData;
                            })
                            .collect(Collectors.toList());
                        
                        response.put("savedToHistory", true);
                        response.put("nutritionEntries", entriesData);
                        logger.info("💾 Сохранено {} записей в историю питания для пользователя: {}", 
                                   savedEntries.size(), telegramId);
                    } else {
                        response.put("savedToHistory", false);
                        response.put("message", "Пользователь не найден, анализ не сохранен в историю");
                        logger.warn("⚠️ Пользователь {} не найден для сохранения анализа", telegramId);
                    }
                } catch (Exception saveException) {
                    logger.error("Ошибка сохранения анализа в историю для {}: {}", 
                               telegramId, saveException.getMessage());
                    response.put("savedToHistory", false);
                    response.put("saveError", saveException.getMessage());
                }
            } else {
                response.put("savedToHistory", false);
                response.put("message", "TelegramId не указан, анализ не сохранен в историю");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Ошибка при анализе base64 изображения: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Ошибка при анализе изображения", "success", false));
        }
    }

    /**
     * AI чат-бот для вопросов о питании
     */
    @PostMapping("/chat")
    @Operation(summary = "AI чат-консультант", description = "Отправка вопроса AI консультанту по питанию")
    public ResponseEntity<Map<String, Object>> chatWithAi(@RequestBody Map<String, String> request) {
        try {
            String message = request.get("message");
            String userContext = request.get("context"); // опционально
            
            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Сообщение не может быть пустым", "success", false));
            }
            
            logger.info("AI чат запрос: {}", message);
            
            String aiResponse = openAIService.chatWithNutritionBot(message, userContext);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", aiResponse);
            response.put("timestamp", new Date());
            response.put("user_message", message);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Ошибка в AI чате: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Ошибка при обработке запроса", "success", false));
        }
    }

    /**
     * Генерация персональных рекомендаций по питанию
     */
    @PostMapping("/recommendations")
    @Operation(summary = "Персональные рекомендации", description = "Генерация рекомендаций по питанию на основе целей пользователя")
    public ResponseEntity<Map<String, Object>> getRecommendations(@RequestBody Map<String, String> request) {
        try {
            String userGoals = request.get("goals");
            String currentDiet = request.get("current_diet");
            String restrictions = request.get("restrictions");
            
            logger.info("Запрос рекомендаций для целей: {}", userGoals);
            
            List<String> recommendations = openAIService.generateNutritionRecommendations(
                userGoals, currentDiet, restrictions
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("recommendations", recommendations);
            response.put("timestamp", new Date());
            response.put("user_goals", userGoals);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Ошибка при генерации рекомендаций: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Ошибка при генерации рекомендаций", "success", false));
        }
    }

    /**
     * Проверка статуса OpenAI API
     */
    @GetMapping("/status")
    @Operation(summary = "Статус AI сервиса", description = "Проверка доступности OpenAI API")
    public ResponseEntity<Map<String, Object>> getAiStatus() {
        try {
            boolean isHealthy = openAIService.isApiHealthy();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("openai_available", isHealthy);
            response.put("status", isHealthy ? "connected" : "disconnected");
            response.put("timestamp", new Date());
            response.put("features", Map.of(
                "food_analysis", isHealthy,
                "ai_chat", isHealthy,
                "recommendations", isHealthy
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Ошибка при проверке статуса AI: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Ошибка при проверке статуса", "success", false));
        }
    }

    /**
     * Получение примеров для тестирования
     */
    @GetMapping("/examples")
    @Operation(summary = "Примеры использования", description = "Получение примеров запросов для тестирования AI функций")
    public ResponseEntity<Map<String, Object>> getExamples() {
        Map<String, Object> examples = new HashMap<>();
        
        examples.put("chat_examples", Arrays.asList(
            "Сколько калорий в яблоке?",
            "Как рассчитать суточную норму калорий?",
            "Какие продукты богаты белком?",
            "Можно ли есть углеводы на ночь?",
            "Как составить план питания для похудения?"
        ));
        
        examples.put("recommendation_goals", Arrays.asList(
            "Похудение",
            "Набор мышечной массы",
            "Поддержание веса",
            "Улучшение здоровья",
            "Спортивное питание"
        ));
        
        examples.put("food_analysis_tips", Arrays.asList(
            "Сфотографируйте еду на светлом фоне",
            "Убедитесь, что все продукты видны",
            "Используйте хорошее освещение",
            "Избегайте размытых фото",
            "Максимальный размер файла: 10MB"
        ));
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("examples", examples);
        response.put("timestamp", new Date());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Быстрый анализ калорий по названию продукта
     */
    @PostMapping("/quick-calories")
    @Operation(summary = "Быстрый анализ калорий", description = "Получение информации о калориях продукта по названию")
    public ResponseEntity<Map<String, Object>> getQuickCalories(@RequestBody Map<String, String> request) {
        try {
            String foodName = request.get("food_name");
            String quantity = request.get("quantity");
            
            if (foodName == null || foodName.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Название продукта не указано", "success", false));
            }
            
            String prompt = String.format(
                "Предоставь информацию о калориях и БЖУ для продукта: %s %s. " +
                "Ответь кратко в формате: Калории: X ккал, Белки: X г, Жиры: X г, Углеводы: X г",
                foodName, quantity != null ? "(" + quantity + ")" : "(100г)"
            );
            
            String aiResponse = openAIService.chatWithNutritionBot(prompt, "Быстрый запрос");
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("food_name", foodName);
            response.put("quantity", quantity != null ? quantity : "100г");
            response.put("nutrition_info", aiResponse);
            response.put("timestamp", new Date());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Ошибка при быстром анализе калорий: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Ошибка при получении информации", "success", false));
        }
    }
} 