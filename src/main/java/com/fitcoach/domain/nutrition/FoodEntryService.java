package com.fitcoach.domain.nutrition;

import com.fitcoach.domain.user.User;
import com.fitcoach.infrastructure.ai.NutritionAnalysis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class FoodEntryService {
    
    private static final Logger logger = LoggerFactory.getLogger(FoodEntryService.class);
    
    private final FoodEntryRepository foodEntryRepository;
    
    public FoodEntryService(FoodEntryRepository foodEntryRepository) {
        this.foodEntryRepository = foodEntryRepository;
    }
    
    /**
     * Создает запись о еде на основе анализа ИИ
     */
    public FoodEntry createFromAnalysis(User user, NutritionAnalysis analysis, MultipartFile photoFile) {
        try {
            String photoHash = calculatePhotoHash(photoFile);
            
            // Проверяем, не анализировали ли мы уже это фото
            Optional<FoodEntry> existing = foodEntryRepository.findByPhotoHash(photoHash);
            if (existing.isPresent()) {
                logger.info("Food entry with hash {} already exists, returning existing", photoHash);
                return existing.get();
            }
            
            FoodEntry foodEntry = new FoodEntry();
            foodEntry.setUser(user);
            foodEntry.setPhotoHash(photoHash);
            foodEntry.setMealDate(LocalDateTime.now());
            foodEntry.setMealType(determineMealType());
            
            // Обновляем данные из анализа ИИ
            foodEntry.updateNutritionFromAI(analysis);
            
            // Добавляем детальные продукты если есть
            if (analysis.getDetailedItems() != null) {
                for (NutritionAnalysis.DetectedItem item : analysis.getDetailedItems()) {
                    foodEntry.addDetectedItem(item.getName(), item.getCalories(), item.getWeight());
                }
            }
            
            FoodEntry saved = foodEntryRepository.save(foodEntry);
            
            logger.info("Created food entry {} for user {} with {} calories", 
                       saved.getId(), user.getId(), saved.getTotalCalories());
            
            return saved;
            
        } catch (Exception e) {
            logger.error("Error creating food entry from analysis", e);
            throw new RuntimeException("Ошибка сохранения анализа питания: " + e.getMessage());
        }
    }
    
    /**
     * Обновляет запись пользователем (коррекция данных ИИ)
     */
    public FoodEntry updateByUser(Long foodEntryId, Long userId, FoodEntryUpdateRequest request) {
        FoodEntry foodEntry = foodEntryRepository.findById(foodEntryId)
            .orElseThrow(() -> new RuntimeException("Запись о еде не найдена"));
        
        if (!foodEntry.getUser().getId().equals(userId)) {
            throw new RuntimeException("Нет доступа к этой записи");
        }
        
        // Обновляем данные
        if (request.getTotalCalories() != null) {
            foodEntry.setTotalCalories(request.getTotalCalories());
        }
        
        if (request.getProteins() != null) {
            foodEntry.setProteinsGrams(request.getProteins());
        }
        
        if (request.getCarbs() != null) {
            foodEntry.setCarbsGrams(request.getCarbs());
        }
        
        if (request.getFats() != null) {
            foodEntry.setFatsGrams(request.getFats());
        }
        
        if (request.getMealType() != null) {
            foodEntry.setMealType(request.getMealType());
        }
        
        if (request.getUserNotes() != null) {
            foodEntry.setUserNotes(request.getUserNotes());
        }
        
        foodEntry.setUserCorrected(true);
        
        FoodEntry updated = foodEntryRepository.save(foodEntry);
        
        logger.info("Food entry {} updated by user {}", foodEntryId, userId);
        
        return updated;
    }
    
    /**
     * Получает записи пользователя за день
     */
    @Transactional(readOnly = true)
    public List<FoodEntry> getTodaysEntries(User user) {
        return foodEntryRepository.findTodaysEntries(user);
    }
    
    /**
     * Получает записи пользователя за период
     */
    @Transactional(readOnly = true)
    public List<FoodEntry> getEntriesInDateRange(User user, LocalDateTime start, LocalDateTime end) {
        return foodEntryRepository.findByUserAndDateRange(user, start, end);
    }
    
    /**
     * Получает общие калории за сегодня
     */
    @Transactional(readOnly = true)
    public int getTodaysTotalCalories(User user) {
        return foodEntryRepository.getTodaysTotalCalories(user).orElse(0);
    }
    
    /**
     * Получает макронутриенты за сегодня
     */
    @Transactional(readOnly = true)
    public MacroNutrients getTodaysMacros(User user) {
        Object[] result = foodEntryRepository.getTodaysMacros(user);
        
        if (result == null || result[0] == null) {
            return new MacroNutrients(0.0, 0.0, 0.0);
        }
        
        Double proteins = (Double) result[0];
        Double carbs = (Double) result[1];
        Double fats = (Double) result[2];
        
        return new MacroNutrients(
            proteins != null ? proteins : 0.0,
            carbs != null ? carbs : 0.0,
            fats != null ? fats : 0.0
        );
    }
    
    /**
     * Получает статистику калорий за неделю
     */
    @Transactional(readOnly = true)
    public List<DailyCalorieStats> getWeeklyStats(User user) {
        LocalDateTime weekStart = LocalDateTime.now().minusDays(7);
        List<Object[]> results = foodEntryRepository.getWeeklyCalorieStats(user, weekStart);
        
        return results.stream()
            .map(row -> new DailyCalorieStats(
                (java.sql.Date) row[0],
                ((Number) row[1]).intValue()
            ))
            .toList();
    }
    
    /**
     * Получает последние записи пользователя
     */
    @Transactional(readOnly = true)
    public Page<FoodEntry> getRecentEntries(User user, Pageable pageable) {
        return foodEntryRepository.findByUserOrderByMealDateDesc(user, pageable);
    }
    
    /**
     * Получает записи с низкой точностью ИИ
     */
    @Transactional(readOnly = true)
    public List<FoodEntry> getLowConfidenceEntries(User user, double threshold) {
        return foodEntryRepository.findLowConfidenceEntries(user, threshold);
    }
    
    /**
     * Получает наиболее частые продукты пользователя
     */
    @Transactional(readOnly = true)
    public List<FoodFrequency> getMostFrequentFoods(User user, Pageable pageable) {
        List<Object[]> results = foodEntryRepository.findMostFrequentFoods(user, pageable);
        
        return results.stream()
            .map(row -> new FoodFrequency(
                (String) row[0],
                ((Number) row[1]).longValue()
            ))
            .toList();
    }
    
    /**
     * Расчет среднего потребления калорий за период
     */
    @Transactional(readOnly = true)
    public double getAverageDailyCalories(User user, LocalDateTime start, LocalDateTime end) {
        return foodEntryRepository.getAverageDailyCalories(user, start, end).orElse(0.0);
    }
    
    /**
     * Расчет стрика (дней подряд с записями)
     */
    @Transactional(readOnly = true)
    public int calculateCurrentStreak(User user) {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<Object[]> loggedDays = foodEntryRepository.findLoggedDays(user, thirtyDaysAgo);
        
        if (loggedDays.isEmpty()) {
            return 0;
        }
        
        int streak = 0;
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1).toLocalDate().atStartOfDay();
        
        for (Object[] row : loggedDays) {
            java.sql.Date sqlDate = (java.sql.Date) row[0];
            LocalDateTime loggedDate = sqlDate.toLocalDate().atStartOfDay();
            
            if (loggedDate.equals(yesterday) || loggedDate.equals(LocalDateTime.now().toLocalDate().atStartOfDay())) {
                streak++;
                yesterday = yesterday.minusDays(1);
            } else {
                break;
            }
        }
        
        return streak;
    }
    
    /**
     * Удаляет запись о еде
     */
    public void deleteEntry(Long foodEntryId, Long userId) {
        FoodEntry foodEntry = foodEntryRepository.findById(foodEntryId)
            .orElseThrow(() -> new RuntimeException("Запись о еде не найдена"));
        
        if (!foodEntry.getUser().getId().equals(userId)) {
            throw new RuntimeException("Нет доступа к этой записи");
        }
        
        foodEntryRepository.delete(foodEntry);
        
        logger.info("Food entry {} deleted by user {}", foodEntryId, userId);
    }
    
    /**
     * Получает прогресс к цели по калориям
     */
    @Transactional(readOnly = true)
    public CalorieProgress getTodaysProgress(User user, int targetCalories) {
        int consumed = getTodaysTotalCalories(user);
        int remaining = Math.max(0, targetCalories - consumed);
        double percentage = targetCalories > 0 ? (double) consumed / targetCalories * 100 : 0;
        
        return new CalorieProgress(consumed, targetCalories, remaining, percentage);
    }
    
    // Private helper methods
    
    private String calculatePhotoHash(MultipartFile file) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(file.getBytes());
        return Base64.getEncoder().encodeToString(hash);
    }
    
    private MealType determineMealType() {
        LocalTime now = LocalTime.now();
        
        if (now.isBefore(LocalTime.of(10, 0))) {
            return MealType.BREAKFAST;
        } else if (now.isBefore(LocalTime.of(15, 0))) {
            return MealType.LUNCH;
        } else if (now.isBefore(LocalTime.of(21, 0))) {
            return MealType.DINNER;
        } else {
            return MealType.SNACK;
        }
    }
    
    // Inner classes for responses and requests
    
    public static class FoodEntryUpdateRequest {
        private Integer totalCalories;
        private Double proteins;
        private Double carbs;
        private Double fats;
        private MealType mealType;
        private String userNotes;
        
        // Getters and Setters
        public Integer getTotalCalories() { return totalCalories; }
        public void setTotalCalories(Integer totalCalories) { this.totalCalories = totalCalories; }
        
        public Double getProteins() { return proteins; }
        public void setProteins(Double proteins) { this.proteins = proteins; }
        
        public Double getCarbs() { return carbs; }
        public void setCarbs(Double carbs) { this.carbs = carbs; }
        
        public Double getFats() { return fats; }
        public void setFats(Double fats) { this.fats = fats; }
        
        public MealType getMealType() { return mealType; }
        public void setMealType(MealType mealType) { this.mealType = mealType; }
        
        public String getUserNotes() { return userNotes; }
        public void setUserNotes(String userNotes) { this.userNotes = userNotes; }
    }
    
    public static class MacroNutrients {
        private final double proteins;
        private final double carbs;
        private final double fats;
        
        public MacroNutrients(double proteins, double carbs, double fats) {
            this.proteins = proteins;
            this.carbs = carbs;
            this.fats = fats;
        }
        
        public double getProteins() { return proteins; }
        public double getCarbs() { return carbs; }
        public double getFats() { return fats; }
        
        public int getProteinCalories() { return (int) (proteins * 4); }
        public int getCarbCalories() { return (int) (carbs * 4); }
        public int getFatCalories() { return (int) (fats * 9); }
        
        public int getTotalCalories() {
            return getProteinCalories() + getCarbCalories() + getFatCalories();
        }
    }
    
    public static class DailyCalorieStats {
        private final java.sql.Date date;
        private final int calories;
        
        public DailyCalorieStats(java.sql.Date date, int calories) {
            this.date = date;
            this.calories = calories;
        }
        
        public java.sql.Date getDate() { return date; }
        public int getCalories() { return calories; }
    }
    
    public static class FoodFrequency {
        private final String foodName;
        private final long frequency;
        
        public FoodFrequency(String foodName, long frequency) {
            this.foodName = foodName;
            this.frequency = frequency;
        }
        
        public String getFoodName() { return foodName; }
        public long getFrequency() { return frequency; }
    }
    
    public static class CalorieProgress {
        private final int consumed;
        private final int target;
        private final int remaining;
        private final double percentage;
        
        public CalorieProgress(int consumed, int target, int remaining, double percentage) {
            this.consumed = consumed;
            this.target = target;
            this.remaining = remaining;
            this.percentage = percentage;
        }
        
        public int getConsumed() { return consumed; }
        public int getTarget() { return target; }
        public int getRemaining() { return remaining; }
        public double getPercentage() { return percentage; }
        
        public boolean isOverTarget() { return consumed > target; }
        public boolean isOnTrack() { return percentage >= 80 && percentage <= 110; }
    }
} 