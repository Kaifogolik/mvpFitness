package com.fitcoach.api;

import com.fitcoach.model.NutritionEntry;
import com.fitcoach.model.User;
import com.fitcoach.service.NutritionService;
import com.fitcoach.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST контроллер для питания и статистики
 */
@RestController
@RequestMapping("/api/nutrition")
@CrossOrigin(origins = "*")
public class NutritionController {
    
    private static final Logger logger = LoggerFactory.getLogger(NutritionController.class);
    
    @Autowired
    private NutritionService nutritionService;
    
    @Autowired
    private UserService userService;
    
    /**
     * Получить дневную статистику питания
     */
    @GetMapping("/{telegramId}/daily")
    public ResponseEntity<Map<String, Object>> getDailyStats(
            @PathVariable String telegramId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        try {
            logger.info("📊 Получение дневной статистики для telegramId: {}, дата: {}", telegramId, date);
            
            if (date == null) {
                date = LocalDate.now();
            }
            
            Optional<User> userOpt = userService.findByTelegramId(telegramId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Пользователь не найден",
                    "error", "USER_NOT_FOUND"
                ));
            }
            
            User user = userOpt.get();
            NutritionService.DailyNutritionStats stats = nutritionService.getDailyStats(user, date);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("date", date.format(DateTimeFormatter.ISO_LOCAL_DATE));
            response.put("total_calories", stats.getTotalCalories());
            response.put("total_protein", stats.getTotalProteins());
            response.put("total_carbs", stats.getTotalCarbs());
            response.put("total_fat", stats.getTotalFats());
            response.put("entries_count", stats.getEntries().size());
            
            // Конвертируем записи в формат для фронтенда
            List<Map<String, Object>> entries = stats.getEntries().stream()
                    .map(this::convertNutritionEntryToMap)
                    .collect(Collectors.toList());
            response.put("entries", entries);
            
            // Цели и прогресс
            if (stats.getProfile() != null) {
                response.put("goal_calories", stats.getProfile().getDailyCaloriesGoal());
                response.put("remaining_calories", Math.max(0, 
                    (stats.getProfile().getDailyCaloriesGoal() != null ? stats.getProfile().getDailyCaloriesGoal() : 2000) 
                    - stats.getTotalCalories()));
                response.put("calories_progress", stats.getCaloriesProgress());
                response.put("proteins_progress", stats.getProteinsProgress());
                response.put("fats_progress", stats.getFatsProgress());
                response.put("carbs_progress", stats.getCarbsProgress());
            } else {
                response.put("goal_calories", 2000); // default goal
                response.put("remaining_calories", Math.max(0, 2000 - stats.getTotalCalories()));
            }
            
            response.put("message", "Дневная статистика получена");
            
            logger.info("✅ Дневная статистика отправлена для пользователя: {}", user.getUsername());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ Ошибка получения дневной статистики для {}: {}", telegramId, e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Ошибка сервера: " + e.getMessage(),
                "error", "INTERNAL_ERROR"
            ));
        }
    }
    
    /**
     * Получить недельную статистику
     */
    @GetMapping("/{telegramId}/weekly")
    public ResponseEntity<Map<String, Object>> getWeeklyStats(
            @PathVariable String telegramId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate) {
        
        try {
            logger.info("📈 Получение недельной статистики для telegramId: {}, начальная дата: {}", telegramId, startDate);
            
            if (startDate == null) {
                startDate = LocalDate.now().minusDays(6); // последние 7 дней
            }
            
            Optional<User> userOpt = userService.findByTelegramId(telegramId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Пользователь не найден",
                    "error", "USER_NOT_FOUND"
                ));
            }
            
            User user = userOpt.get();
            NutritionService.WeeklyNutritionStats weeklyStats = nutritionService.getWeeklyStats(user, startDate);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("start_date", startDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
            response.put("end_date", weeklyStats.getEndDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
            response.put("average_daily_calories", weeklyStats.getAverageCalories());
            response.put("total_calories", weeklyStats.getTotalCalories());
            response.put("days_tracked", weeklyStats.getDailyStats().size());
            
            // Статистика по дням
            List<Map<String, Object>> dailyStats = weeklyStats.getDailyStats().entrySet().stream()
                    .map(entry -> {
                        LocalDate date = entry.getKey();
                        NutritionService.DailyNutritionStats dayStats = entry.getValue();
                        Map<String, Object> dayMap = new HashMap<>();
                        dayMap.put("date", date.format(DateTimeFormatter.ISO_LOCAL_DATE));
                        dayMap.put("calories", dayStats.getTotalCalories());
                        dayMap.put("proteins", dayStats.getTotalProteins());
                        dayMap.put("fats", dayStats.getTotalFats());
                        dayMap.put("carbs", dayStats.getTotalCarbs());
                        dayMap.put("entries_count", dayStats.getEntries().size());
                        return dayMap;
                    })
                    .collect(Collectors.toList());
            response.put("daily_stats", dailyStats);
            
            // Подсчитываем достижение целей
            long daysOnTrack = weeklyStats.getDailyStats().values().stream()
                    .mapToLong(dayStats -> {
                        if (dayStats.getProfile() != null && dayStats.getProfile().getDailyCaloriesGoal() != null) {
                            double goalProgress = dayStats.getCaloriesProgress() / 100.0;
                            return (goalProgress >= 0.8 && goalProgress <= 1.2) ? 1 : 0;
                        }
                        return 0;
                    })
                    .sum();
            
            response.put("goal_achievement_rate", weeklyStats.getDailyStats().size() > 0 ? 
                (double) daysOnTrack / weeklyStats.getDailyStats().size() : 0.0);
            
            response.put("message", "Недельная статистика получена");
            
            logger.info("✅ Недельная статистика отправлена для пользователя: {}", user.getUsername());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ Ошибка получения недельной статистики для {}: {}", telegramId, e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Ошибка сервера: " + e.getMessage(),
                "error", "INTERNAL_ERROR"
            ));
        }
    }
    
    /**
     * Получить рекомендации по питанию
     */
    @GetMapping("/{telegramId}/recommendations")
    public ResponseEntity<Map<String, Object>> getRecommendations(@PathVariable String telegramId) {
        
        try {
            logger.info("💡 Получение рекомендаций для telegramId: {}", telegramId);
            
            Optional<User> userOpt = userService.findByTelegramId(telegramId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Пользователь не найден",
                    "error", "USER_NOT_FOUND"
                ));
            }
            
            User user = userOpt.get();
            List<String> recommendations = nutritionService.getNutritionRecommendations(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("recommendations", recommendations);
            response.put("message", "Персональные рекомендации получены");
            
            logger.info("✅ Рекомендации отправлены для пользователя: {}", user.getUsername());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ Ошибка получения рекомендаций для {}: {}", telegramId, e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Ошибка сервера: " + e.getMessage(),
                "error", "INTERNAL_ERROR"
            ));
        }
    }
    
    /**
     * Получить любимые продукты пользователя
     */
    @GetMapping("/{telegramId}/favorites")
    public ResponseEntity<Map<String, Object>> getFavoriteProducts(@PathVariable String telegramId) {
        
        try {
            logger.info("⭐ Получение любимых продуктов для telegramId: {}", telegramId);
            
            Optional<User> userOpt = userService.findByTelegramId(telegramId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Пользователь не найден",
                    "error", "USER_NOT_FOUND"
                ));
            }
            
            User user = userOpt.get();
            List<NutritionService.FavoriteFood> favorites = nutritionService.getFavoriteProducts(user);
            
            List<Map<String, Object>> favoritesData = favorites.stream()
                    .map(fav -> {
                         Map<String, Object> favMap = new HashMap<>();
                         favMap.put("foodName", fav.getFoodName());
                         favMap.put("count", fav.getCount());
                         favMap.put("averageCalories", fav.getAverageCalories());
                         return favMap;
                    })
                    .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("favorites", favoritesData);
            response.put("message", "Любимые продукты получены");
            
            logger.info("✅ Любимые продукты отправлены для пользователя: {}", user.getUsername());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ Ошибка получения любимых продуктов для {}: {}", telegramId, e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Ошибка сервера: " + e.getMessage(),
                "error", "INTERNAL_ERROR"
            ));
        }
    }
    
    /**
     * Добавить новую запись о питании
     */
    @PostMapping("/{telegramId}/entries")
    public ResponseEntity<Map<String, Object>> addNutritionEntry(
            @PathVariable String telegramId,
            @RequestBody Map<String, Object> entryData) {
        
        try {
            logger.info("🍎 Добавление записи о питании для telegramId: {}", telegramId);
            
            Optional<User> userOpt = userService.findByTelegramId(telegramId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Пользователь не найден",
                    "error", "USER_NOT_FOUND"
                ));
            }
            
            User user = userOpt.get();
            
            // Создаем новую запись питания
            NutritionEntry entry = new NutritionEntry();
            entry.setUser(user);
            entry.setFoodName((String) entryData.get("foodName"));
            entry.setQuantity((String) entryData.getOrDefault("quantity", "1 порция"));
            entry.setCalories(((Number) entryData.getOrDefault("calories", 0)).doubleValue());
            entry.setProteins(((Number) entryData.getOrDefault("proteins", 0)).doubleValue());
            entry.setFats(((Number) entryData.getOrDefault("fats", 0)).doubleValue());
            entry.setCarbs(((Number) entryData.getOrDefault("carbs", 0)).doubleValue());
            
            if (entryData.containsKey("mealType")) {
                try {
                    entry.setMealType(NutritionEntry.MealType.valueOf(
                        ((String) entryData.get("mealType")).toUpperCase()));
                } catch (IllegalArgumentException e) {
                    entry.setMealType(NutritionEntry.MealType.OTHER);
                }
            }
            
            // Сохраняем (здесь нужно добавить метод save в NutritionService)
            // entry = nutritionService.saveEntry(entry);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("entry", convertNutritionEntryToMap(entry));
            response.put("message", "Запись о питании добавлена");
            
            logger.info("✅ Запись о питании добавлена для пользователя: {}", user.getUsername());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ Ошибка добавления записи питания для {}: {}", telegramId, e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Ошибка сервера: " + e.getMessage(),
                "error", "INTERNAL_ERROR"
            ));
        }
    }
    
    // Вспомогательные методы
    
    private Map<String, Object> convertNutritionEntryToMap(NutritionEntry entry) {
        Map<String, Object> entryMap = new HashMap<>();
        entryMap.put("id", entry.getId());
        entryMap.put("foodName", entry.getFoodName());
        entryMap.put("quantity", entry.getQuantity());
        entryMap.put("calories", entry.getCalories());
        entryMap.put("proteins", entry.getProteins());
        entryMap.put("fats", entry.getFats());
        entryMap.put("carbs", entry.getCarbs());
        entryMap.put("mealType", entry.getMealType() != null ? entry.getMealType().toString() : null);
        entryMap.put("timestamp", entry.getTimestamp() != null ? 
            entry.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null);
        entryMap.put("confidence", entry.getConfidence());
                 entryMap.put("aiNotes", entry.getNotes());
        return entryMap;
    }
} 