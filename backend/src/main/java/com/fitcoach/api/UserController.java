package com.fitcoach.api;

import com.fitcoach.model.User;
import com.fitcoach.model.UserProfile;
import com.fitcoach.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * REST контроллер для работы с пользователями
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    @Autowired
    private UserService userService;
    
    /**
     * Получить профиль пользователя
     */
    @GetMapping("/{telegramId}/profile")
    public ResponseEntity<Map<String, Object>> getUserProfile(@PathVariable String telegramId) {
        try {
            logger.info("📱 Получение профиля для telegramId: {}", telegramId);
        
            Optional<UserService.UserWithProfile> userWithProfileOpt = userService.getUserWithProfile(telegramId);
            
            if (userWithProfileOpt.isEmpty()) {
                // Создаем нового пользователя если не найден
                logger.info("👤 Пользователь не найден, создаем нового: {}", telegramId);
                User newUser = userService.findOrCreateUser(telegramId, "user_" + telegramId, "Новый", "Пользователь");
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("user", createUserResponse(newUser));
                response.put("profile", null);
                response.put("message", "Новый пользователь создан. Настройте профиль!");
                response.put("isNewUser", true);
                
                return ResponseEntity.ok(response);
            }
            
            UserService.UserWithProfile userWithProfile = userWithProfileOpt.get();
            User user = userWithProfile.getUser();
            UserProfile profile = userWithProfile.getProfile();
            
            // Обновляем время последней активности
            userService.updateLastActiveTime(telegramId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("user", createUserResponse(user));
            response.put("profile", profile != null ? createProfileResponse(profile) : null);
            response.put("message", profile != null ? "Профиль загружен" : "Настройте свой профиль для лучших рекомендаций");
            response.put("isNewUser", false);
            
            logger.info("✅ Профиль отправлен для пользователя: {}", user.getUsername());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ Ошибка получения профиля для {}: {}", telegramId, e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Ошибка сервера: " + e.getMessage(),
                "error", "INTERNAL_ERROR"
            ));
        }
    }
    
    /**
     * Создать или обновить пользователя
     */
    @PostMapping("/{telegramId}")
    public ResponseEntity<Map<String, Object>> createOrUpdateUser(
            @PathVariable String telegramId, 
            @RequestBody Map<String, Object> userData) {
        
        try {
            logger.info("👤 Создание/обновление пользователя: {}", telegramId);
            
            String username = (String) userData.getOrDefault("username", "user_" + telegramId);
            String firstName = (String) userData.get("firstName");
            String lastName = (String) userData.get("lastName");
            
            User user = userService.findOrCreateUser(telegramId, username, firstName, lastName);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("user", createUserResponse(user));
            response.put("message", "Пользователь успешно обновлен");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ Ошибка создания/обновления пользователя {}: {}", telegramId, e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Ошибка сервера: " + e.getMessage(),
                "error", "INTERNAL_ERROR"
            ));
        }
    }
    
    /**
     * Проверить существование пользователя
     */
    @GetMapping("/{telegramId}/exists")
    public ResponseEntity<Map<String, Object>> checkUserExists(@PathVariable String telegramId) {
        try {
            boolean exists = userService.userExists(telegramId);
            boolean hasProfile = exists ? userService.hasProfile(telegramId) : false;
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("exists", exists);
            response.put("hasProfile", hasProfile);
            response.put("message", exists ? "Пользователь найден" : "Пользователь не найден");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ Ошибка проверки пользователя {}: {}", telegramId, e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Ошибка сервера: " + e.getMessage(),
                "error", "INTERNAL_ERROR"
            ));
        }
    }
    
    /**
     * Статистика пользователей (для админки)
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getUserStatistics() {
        try {
            UserService.UserStatistics stats = userService.getUserStatistics();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("statistics", Map.of(
                "totalUsers", stats.getTotalUsers(),
                "activeUsers7Days", stats.getActiveUsers7Days(),
                "activeUsers30Days", stats.getActiveUsers30Days(),
                "newUsers7Days", stats.getNewUsers7Days(),
                "newUsers30Days", stats.getNewUsers30Days()
            ));
            response.put("message", "Статистика получена");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ Ошибка получения статистики: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Ошибка сервера: " + e.getMessage(),
                "error", "INTERNAL_ERROR"
            ));
        }
    }
    
    // Вспомогательные методы
    
    private Map<String, Object> createUserResponse(User user) {
        Map<String, Object> userResponse = new HashMap<>();
        userResponse.put("id", user.getId());
        userResponse.put("telegramId", user.getTelegramId());
        userResponse.put("username", user.getUsername());
        userResponse.put("firstName", user.getFirstName());
        userResponse.put("lastName", user.getLastName());
        userResponse.put("registrationDate", user.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE));
        userResponse.put("lastActiveAt", user.getLastActiveAt() != null ? 
            user.getLastActiveAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null);
        return userResponse;
    }
    
    private Map<String, Object> createProfileResponse(UserProfile profile) {
        Map<String, Object> profileResponse = new HashMap<>();
        profileResponse.put("id", profile.getId());
        profileResponse.put("age", profile.getAge());
        profileResponse.put("gender", profile.getGender() != null ? profile.getGender().toString() : null);
        profileResponse.put("height", profile.getHeight());
        profileResponse.put("currentWeight", profile.getWeight());
        // targetWeight поле не существует в модели, убираем
        profileResponse.put("activityLevel", profile.getActivityLevel() != null ? profile.getActivityLevel().toString() : null);
        profileResponse.put("goal", profile.getFitnessGoal() != null ? profile.getFitnessGoal().toString() : null);
        profileResponse.put("dailyCalorieGoal", profile.getDailyCaloriesGoal());
        profileResponse.put("dailyProteinsGoal", profile.getDailyProteinsGoal());
        profileResponse.put("dailyFatsGoal", profile.getDailyFatsGoal());
        profileResponse.put("dailyCarbsGoal", profile.getDailyCarbsGoal());
        profileResponse.put("createdAt", profile.getCreatedAt() != null ? 
            profile.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null);
        profileResponse.put("updatedAt", profile.getUpdatedAt() != null ? 
            profile.getUpdatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null);
        return profileResponse;
    }
} 