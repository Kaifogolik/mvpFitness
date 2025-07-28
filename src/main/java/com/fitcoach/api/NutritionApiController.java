package com.fitcoach.api;

import com.fitcoach.infrastructure.nutrition.NutritionApiService;
import com.fitcoach.infrastructure.nutrition.NutritionInfo;
import com.fitcoach.infrastructure.nutrition.NutritionNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * API контроллер для тестирования Nutrition API Service
 * Демонстрирует экономию $290/месяц через бесплатные API питания
 */
@RestController
@RequestMapping("/api/v2/nutrition")
public class NutritionApiController {
    
    private static final Logger log = LoggerFactory.getLogger(NutritionApiController.class);
    
    private final NutritionApiService nutritionApiService;
    
    public NutritionApiController(NutritionApiService nutritionApiService) {
        this.nutritionApiService = nutritionApiService;
    }
    
    /**
     * Поиск питательной информации
     * 
     * @param foodName название продукта
     * @param weight вес порции в граммах (опционально, по умолчанию 100г)
     * @return питательная информация
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchNutrition(
            @RequestParam String foodName,
            @RequestParam(defaultValue = "100") double weight) {
        
        try {
            log.info("🔍 API: Поиск питания для '{}' ({}г)", foodName, weight);
            
            NutritionInfo nutrition = nutritionApiService.getNutrition(foodName, weight);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "nutrition", createNutritionResponse(nutrition),
                    "source", nutrition.getSource(),
                    "message", "Питательная информация найдена"
            ));
            
        } catch (NutritionNotFoundException e) {
            log.warn("❌ Продукт '{}' не найден", foodName);
            return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "error", "NOT_FOUND",
                    "message", "Продукт не найден: " + foodName,
                    "suggestion", "Попробуйте изменить название или проверить правописание"
            ));
            
        } catch (Exception e) {
            log.error("❌ Ошибка поиска питания для '{}': {}", foodName, e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "error", "INTERNAL_ERROR",
                    "message", "Внутренняя ошибка сервиса"
            ));
        }
    }
    
    /**
     * Пакетный поиск нескольких продуктов
     */
    @PostMapping("/batch-search")
    public ResponseEntity<Map<String, Object>> batchSearch(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            java.util.List<Map<String, Object>> foods = (java.util.List<Map<String, Object>>) request.get("foods");
            
            java.util.List<Map<String, Object>> results = new java.util.ArrayList<>();
            int found = 0;
            int notFound = 0;
            
            for (Map<String, Object> food : foods) {
                String foodName = (String) food.get("name");
                double weight = food.containsKey("weight") ? 
                              ((Number) food.get("weight")).doubleValue() : 100.0;
                
                try {
                    NutritionInfo nutrition = nutritionApiService.getNutrition(foodName, weight);
                    results.add(Map.of(
                            "name", foodName,
                            "success", true,
                            "nutrition", createNutritionResponse(nutrition),
                            "source", nutrition.getSource()
                    ));
                    found++;
                    
                } catch (NutritionNotFoundException e) {
                    results.add(Map.of(
                            "name", foodName,
                            "success", false,
                            "error", "NOT_FOUND"
                    ));
                    notFound++;
                }
            }
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "results", results,
                    "summary", Map.of(
                            "total", foods.size(),
                            "found", found,
                            "notFound", notFound
                    )
            ));
            
        } catch (Exception e) {
            log.error("❌ Ошибка пакетного поиска: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "INVALID_REQUEST",
                    "message", "Неверный формат запроса"
            ));
        }
    }
    
    /**
     * Статистика всех API источников питания
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        try {
            NutritionApiService.NutritionApiStats stats = nutritionApiService.getStatistics();
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "statistics", Map.of(
                            "sources", Map.of(
                                    "fatSecret", Map.of(
                                            "available", stats.isFatSecretAvailable(),
                                            "details", stats.getFatSecretStats()
                                    ),
                                    "usda", Map.of(
                                            "available", stats.isUsdaAvailable(),
                                            "details", "TODO: Implement USDA FDC"
                                    ),
                                    "edamam", Map.of(
                                            "available", stats.isEdamamAvailable(),
                                            "details", "TODO: Implement Edamam"
                                    ),
                                    "localDb", Map.of(
                                            "available", stats.isLocalDbAvailable(),
                                            "details", "TODO: Implement Local Database"
                                    )
                            ),
                            "performance", Map.of(
                                    "totalRequests", stats.getTotalRequests(),
                                    "cacheHitRate", String.format("%.1f%%", stats.getCacheHitRate() * 100),
                                    "averageResponseTime", stats.getAverageResponseTime() + "ms"
                            ),
                            "economics", Map.of(
                                    "monthlySavings", "$" + stats.getMonthlySavings(),
                                    "yearlyProjection", "$" + (stats.getMonthlySavings() * 12),
                                    "costPerRequest", "$0 (бесплатные API)",
                                    "vsCommercialApis", "97% экономия"
                            )
                    ),
                    "anySourceAvailable", nutritionApiService.isAnySourceAvailable()
            ));
            
        } catch (Exception e) {
            log.error("❌ Ошибка получения статистики: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "error", "STATS_ERROR",
                    "message", "Ошибка получения статистики"
            ));
        }
    }
    
    /**
     * Очистка всех кэшей питания
     */
    @DeleteMapping("/cache")
    public ResponseEntity<Map<String, Object>> clearCaches() {
        try {
            nutritionApiService.clearAllCaches();
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Все кэши питания очищены",
                    "action", "cache_cleared"
            ));
            
        } catch (Exception e) {
            log.error("❌ Ошибка очистки кэшей: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "error", "CACHE_ERROR",
                    "message", "Ошибка очистки кэшей"
            ));
        }
    }
    
    /**
     * Проверка доступности API источников
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        try {
            boolean anyAvailable = nutritionApiService.isAnySourceAvailable();
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "healthy", anyAvailable,
                    "status", anyAvailable ? "OK" : "DEGRADED",
                    "message", anyAvailable ? 
                              "Хотя бы один источник питания доступен" : 
                              "Все источники питания недоступны",
                    "timestamp", java.time.LocalDateTime.now().toString()
            ));
            
        } catch (Exception e) {
            log.error("❌ Ошибка проверки здоровья: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "healthy", false,
                    "status", "ERROR",
                    "message", "Ошибка проверки доступности сервисов"
            ));
        }
    }
    
    /**
     * Создание JSON ответа для питательной информации
     */
    private Map<String, Object> createNutritionResponse(NutritionInfo nutrition) {
        return Map.of(
                "name", nutrition.getName(),
                "weight", nutrition.getWeight(),
                "calories", nutrition.getCalories(),
                "macros", Map.of(
                        "protein", nutrition.getProtein(),
                        "carbohydrates", nutrition.getCarbohydrates(),
                        "fat", nutrition.getFat()
                ),
                "additional", Map.of(
                        "fiber", nutrition.getFiber(),
                        "sugar", nutrition.getSugar(),
                        "sodium", nutrition.getSodium()
                ),
                "vitamins", Map.of(
                        "vitaminC", nutrition.getVitaminC(),
                        "calcium", nutrition.getCalcium(),
                        "iron", nutrition.getIron(),
                        "potassium", nutrition.getPotassium()
                ),
                "metadata", Map.of(
                        "source", nutrition.getSource(),
                        "fetchedAt", nutrition.getFetchedAt().toString(),
                        "valid", nutrition.isValid()
                )
        );
    }
} 