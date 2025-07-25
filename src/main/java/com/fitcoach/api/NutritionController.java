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
import java.util.*;
import java.util.stream.Collectors;

/**
 * REST API для работы с питанием и статистикой
 */
@RestController
@RequestMapping("/api/nutrition")
@CrossOrigin(origins = "*")
public class NutritionController {
    
    private static final Logger logger = LoggerFactory.getLogger(NutritionController.class);
    
    /**
     * Mock версия дневной статистики для демонстрации Mini App
     */
    @GetMapping("/{telegramId}/daily")
    public ResponseEntity<Map<String, Object>> getDailyStatsMock(@PathVariable String telegramId) {
        Map<String, Object> response = new HashMap<>();
        
        // Создаем mock данные дневной статистики
        response.put("success", true);
        response.put("date", java.time.LocalDate.now().toString());
        response.put("total_calories", 1250.5);
        response.put("total_protein", 85.2);
        response.put("total_carbs", 140.8);
        response.put("total_fat", 45.3);
        response.put("entries_count", 4);
        response.put("goal_calories", 2000);
        response.put("remaining_calories", 749.5);
        
        // Mock записи о питании
        List<Map<String, Object>> entries = new ArrayList<>();
        
        Map<String, Object> entry1 = new HashMap<>();
        entry1.put("id", 1);
        entry1.put("foodName", "Овсянка с ягодами");
        entry1.put("calories", 320.0);
        entry1.put("proteins", 12.5);
        entry1.put("carbs", 58.0);
        entry1.put("fats", 6.2);
        entry1.put("mealType", "BREAKFAST");
        entry1.put("timestamp", "2025-07-25T08:30:00");
        entries.add(entry1);
        
        Map<String, Object> entry2 = new HashMap<>();
        entry2.put("id", 2);
        entry2.put("foodName", "Куриная грудка с рисом");
        entry2.put("calories", 450.0);
        entry2.put("proteins", 35.2);
        entry2.put("carbs", 42.8);
        entry2.put("fats", 12.1);
        entry2.put("mealType", "LUNCH");
        entry2.put("timestamp", "2025-07-25T13:15:00");
        entries.add(entry2);
        
        Map<String, Object> entry3 = new HashMap<>();
        entry3.put("id", 3);
        entry3.put("foodName", "Греческий салат");
        entry3.put("calories", 280.5);
        entry3.put("proteins", 15.5);
        entry3.put("carbs", 15.0);
        entry3.put("fats", 18.0);
        entry3.put("mealType", "DINNER");
        entry3.put("timestamp", "2025-07-25T19:45:00");
        entries.add(entry3);
        
        response.put("entries", entries);
        response.put("message", "Mock дневная статистика");
        
        logger.info("📊 Mock дневная статистика отправлена для telegramId: {}", telegramId);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Mock версия недельной статистики для демонстрации Mini App
     */
    @GetMapping("/{telegramId}/weekly")
    public ResponseEntity<Map<String, Object>> getWeeklyStatsMock(@PathVariable String telegramId) {
        Map<String, Object> response = new HashMap<>();
        
        response.put("success", true);
        response.put("average_daily_calories", 1450.2);
        response.put("total_entries", 28);
        response.put("days_tracked", 7);
        response.put("goal_achievement_rate", 0.72);
        
        // Mock статистика по дням
        List<Map<String, Object>> dailyStats = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            Map<String, Object> dayStats = new HashMap<>();
            dayStats.put("date", java.time.LocalDate.now().minusDays(i).toString());
            dayStats.put("calories", 1200 + Math.random() * 400);
            dayStats.put("entries_count", (int)(Math.random() * 5) + 2);
            dailyStats.add(dayStats);
        }
        
        response.put("daily_stats", dailyStats);
        response.put("message", "Mock недельная статистика");
        
        logger.info("📈 Mock недельная статистика отправлена для telegramId: {}", telegramId);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Mock версия рекомендаций для демонстрации Mini App
     */
    @GetMapping("/{telegramId}/recommendations")
    public ResponseEntity<Map<String, Object>> getRecommendationsMock(@PathVariable String telegramId) {
        Map<String, Object> response = new HashMap<>();
        
        response.put("success", true);
        
        List<String> recommendations = Arrays.asList(
            "🥗 Добавьте больше овощей в рацион для получения клетчатки",
            "💪 Увеличьте потребление белка для поддержания мышечной массы",
            "💧 Не забывайте пить достаточно воды - минимум 2 литра в день",
            "🕐 Старайтесь есть в одно и то же время каждый день",
            "🏃‍♂️ Сочетайте правильное питание с регулярными тренировками"
        );
        
        response.put("recommendations", recommendations);
        response.put("message", "Mock персональные рекомендации");
        
        logger.info("💡 Mock рекомендации отправлены для telegramId: {}", telegramId);
        
        return ResponseEntity.ok(response);
    }
    
    @Autowired
    private NutritionService nutritionService;
    
    @Autowired
    private UserService userService;
    
    // УДАЛЕН оригинальный getDailyStats для избежания конфликта маппинга с Mock версией
    
    // УДАЛЕН оригинальный getWeeklyStats для избежания конфликта маппинга с Mock версией
    
    /**
     * Получить отчет о прогрессе к целям
     */
    @GetMapping("/{telegramId}/progress")
    public ResponseEntity<Map<String, Object>> getProgressReport(
            @PathVariable String telegramId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<User> userOpt = userService.findByTelegramId(telegramId);
            if (userOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Пользователь не найден");
                return ResponseEntity.notFound().build();
            }
            
            User user = userOpt.get();
            LocalDate targetDate = date != null ? date : LocalDate.now();
            
            NutritionService.ProgressReport report = nutritionService.getProgressReport(user, targetDate);
            
            Map<String, Object> progressData = new HashMap<>();
            progressData.put("date", targetDate);
            progressData.put("onTrack", report.isOnTrack());
            progressData.put("feedback", report.getFeedback());
            
            // Сегодняшняя статистика
            NutritionService.DailyNutritionStats todayStats = report.getTodayStats();
            Map<String, Object> todayData = new HashMap<>();
            todayData.put("totalCalories", todayStats.getTotalCalories());
            todayData.put("totalProteins", todayStats.getTotalProteins());
            todayData.put("totalFats", todayStats.getTotalFats());
            todayData.put("totalCarbs", todayStats.getTotalCarbs());
            
            if (todayStats.getProfile() != null) {
                todayData.put("caloriesProgress", todayStats.getCaloriesProgress());
                todayData.put("proteinsProgress", todayStats.getProteinsProgress());
                todayData.put("fatsProgress", todayStats.getFatsProgress());
                todayData.put("carbsProgress", todayStats.getCarbsProgress());
            }
            
            progressData.put("todayStats", todayData);
            
            // Профиль с целями
            if (report.getProfile() != null) {
                Map<String, Object> profileData = new HashMap<>();
                profileData.put("fitnessGoal", report.getProfile().getFitnessGoal());
                profileData.put("dailyCaloriesGoal", report.getProfile().getDailyCaloriesGoal());
                profileData.put("dailyProteinsGoal", report.getProfile().getDailyProteinsGoal());
                profileData.put("dailyFatsGoal", report.getProfile().getDailyFatsGoal());
                profileData.put("dailyCarbsGoal", report.getProfile().getDailyCarbsGoal());
                
                progressData.put("profile", profileData);
            }
            
            response.put("success", true);
            response.put("progressReport", progressData);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Ошибка получения отчета о прогрессе для {}: {}", telegramId, e.getMessage());
            response.put("success", false);
            response.put("message", "Внутренняя ошибка сервера");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    // УДАЛЕН оригинальный getNutritionRecommendations для избежания конфликта маппинга с Mock версией
    
    /**
     * Получить любимые продукты пользователя
     */
    @GetMapping("/{telegramId}/favorites")
    public ResponseEntity<Map<String, Object>> getFavoriteProducts(@PathVariable String telegramId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<User> userOpt = userService.findByTelegramId(telegramId);
            if (userOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Пользователь не найден");
                return ResponseEntity.notFound().build();
            }
            
            User user = userOpt.get();
            List<NutritionService.FavoriteFood> favorites = nutritionService.getFavoriteProducts(user);
            
            List<Map<String, Object>> favoritesData = favorites.stream()
                    .map(fav -> {
                        Map<String, Object> favData = new HashMap<>();
                        favData.put("foodName", fav.getFoodName());
                        favData.put("count", fav.getCount());
                        favData.put("averageCalories", fav.getAverageCalories());
                        return favData;
                    })
                    .collect(Collectors.toList());
            
            response.put("success", true);
            response.put("favorites", favoritesData);
            response.put("count", favoritesData.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Ошибка получения любимых продуктов для {}: {}", telegramId, e.getMessage());
            response.put("success", false);
            response.put("message", "Внутренняя ошибка сервера");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Получить последние записи о питании
     */
    @GetMapping("/{telegramId}/recent")
    public ResponseEntity<Map<String, Object>> getRecentEntries(
            @PathVariable String telegramId,
            @RequestParam(defaultValue = "10") int limit) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<User> userOpt = userService.findByTelegramId(telegramId);
            if (userOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Пользователь не найден");
                return ResponseEntity.notFound().build();
            }
            
            User user = userOpt.get();
            List<NutritionEntry> recentEntries = nutritionService.getRecentEntries(user, Math.min(limit, 50));
            
            List<Map<String, Object>> entriesData = recentEntries.stream()
                    .map(this::entryToMap)
                    .collect(Collectors.toList());
            
            response.put("success", true);
            response.put("entries", entriesData);
            response.put("count", entriesData.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Ошибка получения последних записей для {}: {}", telegramId, e.getMessage());
            response.put("success", false);
            response.put("message", "Внутренняя ошибка сервера");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Получить статистику по типам приемов пищи
     */
    @GetMapping("/{telegramId}/meal-types")
    public ResponseEntity<Map<String, Object>> getMealTypeStatistics(
            @PathVariable String telegramId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<User> userOpt = userService.findByTelegramId(telegramId);
            if (userOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Пользователь не найден");
                return ResponseEntity.notFound().build();
            }
            
            User user = userOpt.get();
            LocalDate start = startDate != null ? startDate : LocalDate.now().minusDays(7);
            LocalDate end = endDate != null ? endDate : LocalDate.now();
            
            // Получаем статистику по типам приемов пищи
            // Это требует добавления метода в NutritionService, пока возвращаем базовую информацию
            Map<String, Object> mealTypeStats = new HashMap<>();
            mealTypeStats.put("period", Map.of("startDate", start, "endDate", end));
            mealTypeStats.put("message", "Статистика по типам приемов пищи будет доступна в следующей версии");
            
            response.put("success", true);
            response.put("mealTypeStats", mealTypeStats);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Ошибка получения статистики по типам приемов пищи для {}: {}", telegramId, e.getMessage());
            response.put("success", false);
            response.put("message", "Внутренняя ошибка сервера");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Преобразовать NutritionEntry в Map для JSON ответа
     */
    private Map<String, Object> entryToMap(NutritionEntry entry) {
        Map<String, Object> entryData = new HashMap<>();
        entryData.put("id", entry.getId());
        entryData.put("date", entry.getDate());
        entryData.put("timestamp", entry.getTimestamp());
        entryData.put("foodName", entry.getFoodName());
        entryData.put("quantity", entry.getQuantity());
        entryData.put("calories", entry.getCalories());
        entryData.put("proteins", entry.getProteins());
        entryData.put("fats", entry.getFats());
        entryData.put("carbs", entry.getCarbs());
        entryData.put("confidence", entry.getConfidence());
        entryData.put("mealType", entry.getMealType());
        entryData.put("notes", entry.getNotes());
        entryData.put("dataSource", entry.getDataSource());
        
        return entryData;
    }
} 