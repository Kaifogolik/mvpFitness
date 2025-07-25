package com.fitcoach.api;

import com.fitcoach.model.User;
import com.fitcoach.model.UserProfile;
import com.fitcoach.service.UserService;
import com.fitcoach.repository.UserProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * REST API для управления профилями пользователей
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    /**
     * Простой тест эндпоинт без зависимостей
     */
    @GetMapping("/test")
    public Map<String, String> testEndpoint() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "UserController работает!");
        response.put("timestamp", new java.util.Date().toString());
        return response;
    }
    
    /**
     * Mock версия профиля пользователя для демонстрации Mini App
     */
    @GetMapping("/{telegramId}/profile")
    public ResponseEntity<Map<String, Object>> getUserProfileMock(@PathVariable String telegramId) {
        Map<String, Object> response = new HashMap<>();
        
        // Создаем mock данные
        Map<String, Object> user = new HashMap<>();
        user.put("id", 1);
        user.put("telegramId", telegramId);
        user.put("firstName", "Test");
        user.put("lastName", "User");
        user.put("registrationDate", "2025-07-25");
        
        Map<String, Object> profile = new HashMap<>();
        profile.put("goal", "WEIGHT_LOSS");
        profile.put("currentWeight", 75.0);
        profile.put("targetWeight", 70.0);
        profile.put("height", 175.0);
        profile.put("age", 25);
        profile.put("gender", "MALE");
        profile.put("activityLevel", "MODERATE");
        profile.put("dailyCalorieGoal", 2000);
        
        response.put("success", true);
        response.put("user", user);
        response.put("profile", profile);
        response.put("message", "Mock профиль для демонстрации");
        
        logger.info("📱 Mock профиль отправлен для telegramId: {}", telegramId);
        
        return ResponseEntity.ok(response);
    }
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserProfileRepository userProfileRepository;
    
    /**
     * Получить профиль пользователя по Telegram ID
     */
    @GetMapping("/{telegramId}/profile")
    public ResponseEntity<Map<String, Object>> getUserProfile(@PathVariable String telegramId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<UserService.UserWithProfile> userWithProfile = userService.getUserWithProfile(telegramId);
            
            if (userWithProfile.isEmpty()) {
                response.put("success", false);
                response.put("message", "Пользователь не найден");
                return ResponseEntity.notFound().build();
            }
            
            UserService.UserWithProfile uwp = userWithProfile.get();
            
            // Информация о пользователе
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", uwp.getUser().getId());
            userData.put("telegramId", uwp.getUser().getTelegramId());
            userData.put("username", uwp.getUser().getUsername());
            userData.put("firstName", uwp.getUser().getFirstName());
            userData.put("lastName", uwp.getUser().getLastName());
            userData.put("createdAt", uwp.getUser().getCreatedAt());
            userData.put("lastActiveAt", uwp.getUser().getLastActiveAt());
            
            response.put("success", true);
            response.put("user", userData);
            response.put("hasProfile", uwp.hasProfile());
            
            if (uwp.hasProfile()) {
                UserProfile profile = uwp.getProfile();
                
                Map<String, Object> profileData = new HashMap<>();
                profileData.put("id", profile.getId());
                profileData.put("age", profile.getAge());
                profileData.put("weight", profile.getWeight());
                profileData.put("height", profile.getHeight());
                profileData.put("gender", profile.getGender());
                profileData.put("activityLevel", profile.getActivityLevel());
                profileData.put("fitnessGoal", profile.getFitnessGoal());
                
                // Целевые показатели
                profileData.put("dailyCaloriesGoal", profile.getDailyCaloriesGoal());
                profileData.put("dailyProteinsGoal", profile.getDailyProteinsGoal());
                profileData.put("dailyFatsGoal", profile.getDailyFatsGoal());
                profileData.put("dailyCarbsGoal", profile.getDailyCarbsGoal());
                
                // Настройки
                profileData.put("notificationsEnabled", profile.getNotificationsEnabled());
                profileData.put("trackingEnabled", profile.getTrackingEnabled());
                
                profileData.put("createdAt", profile.getCreatedAt());
                profileData.put("updatedAt", profile.getUpdatedAt());
                
                response.put("profile", profileData);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Ошибка получения профиля пользователя {}: {}", telegramId, e.getMessage());
            response.put("success", false);
            response.put("message", "Внутренняя ошибка сервера");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Создать или обновить профиль пользователя
     */
    @PostMapping("/{telegramId}/profile")
    public ResponseEntity<Map<String, Object>> createOrUpdateProfile(
            @PathVariable String telegramId, 
            @RequestBody Map<String, Object> profileData) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Находим или создаем пользователя
            Optional<User> userOpt = userService.findByTelegramId(telegramId);
            if (userOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Пользователь не найден. Сначала необходимо зарегистрироваться через Telegram бота.");
                return ResponseEntity.badRequest().body(response);
            }
            
            User user = userOpt.get();
            
            // Находим существующий профиль или создаем новый
            Optional<UserProfile> existingProfileOpt = userProfileRepository.findByUser(user);
            UserProfile profile = existingProfileOpt.orElse(new UserProfile());
            
            if (profile.getUser() == null) {
                profile.setUser(user);
            }
            
            // Обновляем данные профиля
            if (profileData.containsKey("age")) {
                profile.setAge(((Number) profileData.get("age")).intValue());
            }
            
            if (profileData.containsKey("weight")) {
                profile.setWeight(((Number) profileData.get("weight")).doubleValue());
            }
            
            if (profileData.containsKey("height")) {
                profile.setHeight(((Number) profileData.get("height")).intValue());
            }
            
            if (profileData.containsKey("gender")) {
                String genderStr = (String) profileData.get("gender");
                try {
                    profile.setGender(UserProfile.Gender.valueOf(genderStr.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    response.put("success", false);
                    response.put("message", "Неверное значение пола: " + genderStr);
                    return ResponseEntity.badRequest().body(response);
                }
            }
            
            if (profileData.containsKey("activityLevel")) {
                String activityStr = (String) profileData.get("activityLevel");
                try {
                    profile.setActivityLevel(UserProfile.ActivityLevel.valueOf(activityStr.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    response.put("success", false);
                    response.put("message", "Неверное значение уровня активности: " + activityStr);
                    return ResponseEntity.badRequest().body(response);
                }
            }
            
            if (profileData.containsKey("fitnessGoal")) {
                String goalStr = (String) profileData.get("fitnessGoal");
                try {
                    profile.setFitnessGoal(UserProfile.FitnessGoal.valueOf(goalStr.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    response.put("success", false);
                    response.put("message", "Неверное значение цели фитнеса: " + goalStr);
                    return ResponseEntity.badRequest().body(response);
                }
            }
            
            if (profileData.containsKey("notificationsEnabled")) {
                profile.setNotificationsEnabled((Boolean) profileData.get("notificationsEnabled"));
            }
            
            if (profileData.containsKey("trackingEnabled")) {
                profile.setTrackingEnabled((Boolean) profileData.get("trackingEnabled"));
            }
            
            // Рассчитываем цели питания
            profile.updateNutritionGoals();
            
            // Сохраняем профиль
            profile = userProfileRepository.save(profile);
            
            logger.info("📝 {} профиль для пользователя: {} ({})", 
                       existingProfileOpt.isPresent() ? "Обновлен" : "Создан", 
                       user.getUsername(), telegramId);
            
            // Возвращаем обновленный профиль
            Map<String, Object> profileResponse = new HashMap<>();
            profileResponse.put("id", profile.getId());
            profileResponse.put("age", profile.getAge());
            profileResponse.put("weight", profile.getWeight());
            profileResponse.put("height", profile.getHeight());
            profileResponse.put("gender", profile.getGender());
            profileResponse.put("activityLevel", profile.getActivityLevel());
            profileResponse.put("fitnessGoal", profile.getFitnessGoal());
            
            // Рассчитанные цели
            profileResponse.put("bmr", profile.calculateBMR());
            profileResponse.put("dailyCaloriesGoal", profile.getDailyCaloriesGoal());
            profileResponse.put("dailyProteinsGoal", profile.getDailyProteinsGoal());
            profileResponse.put("dailyFatsGoal", profile.getDailyFatsGoal());
            profileResponse.put("dailyCarbsGoal", profile.getDailyCarbsGoal());
            
            profileResponse.put("notificationsEnabled", profile.getNotificationsEnabled());
            profileResponse.put("trackingEnabled", profile.getTrackingEnabled());
            profileResponse.put("updatedAt", profile.getUpdatedAt());
            
            response.put("success", true);
            response.put("message", existingProfileOpt.isPresent() ? "Профиль обновлен" : "Профиль создан");
            response.put("profile", profileResponse);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Ошибка создания/обновления профиля для {}: {}", telegramId, e.getMessage());
            response.put("success", false);
            response.put("message", "Внутренняя ошибка сервера: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Получить доступные опции для профиля
     */
    @GetMapping("/profile/options")
    public ResponseEntity<Map<String, Object>> getProfileOptions() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> options = new HashMap<>();
            
            // Доступные значения пола
            Map<String, String> genders = new HashMap<>();
            for (UserProfile.Gender gender : UserProfile.Gender.values()) {
                genders.put(gender.name(), gender.getDisplayName());
            }
            options.put("genders", genders);
            
            // Доступные уровни активности
            Map<String, Map<String, Object>> activityLevels = new HashMap<>();
            for (UserProfile.ActivityLevel level : UserProfile.ActivityLevel.values()) {
                Map<String, Object> levelData = new HashMap<>();
                levelData.put("displayName", level.getDisplayName());
                levelData.put("multiplier", level.getMultiplier());
                activityLevels.put(level.name(), levelData);
            }
            options.put("activityLevels", activityLevels);
            
            // Доступные цели фитнеса
            Map<String, Map<String, Object>> fitnessGoals = new HashMap<>();
            for (UserProfile.FitnessGoal goal : UserProfile.FitnessGoal.values()) {
                Map<String, Object> goalData = new HashMap<>();
                goalData.put("displayName", goal.getDisplayName());
                goalData.put("calorieAdjustment", goal.getCalorieAdjustment());
                fitnessGoals.put(goal.name(), goalData);
            }
            options.put("fitnessGoals", fitnessGoals);
            
            response.put("success", true);
            response.put("options", options);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Ошибка получения опций профиля: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "Внутренняя ошибка сервера");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Получить статистику пользователей (для админки)
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getUserStatistics() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            UserService.UserStatistics stats = userService.getUserStatistics();
            
            Map<String, Object> statisticsData = new HashMap<>();
            statisticsData.put("totalUsers", stats.getTotalUsers());
            statisticsData.put("activeUsers7Days", stats.getActiveUsers7Days());
            statisticsData.put("activeUsers30Days", stats.getActiveUsers30Days());
            statisticsData.put("newUsers7Days", stats.getNewUsers7Days());
            statisticsData.put("newUsers30Days", stats.getNewUsers30Days());
            
            response.put("success", true);
            response.put("statistics", statisticsData);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Ошибка получения статистики пользователей: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "Внутренняя ошибка сервера");
            return ResponseEntity.internalServerError().body(response);
        }
    }
} 