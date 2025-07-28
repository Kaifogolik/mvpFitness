package com.fitcoach.service;

import com.fitcoach.infrastructure.ai.NutritionAnalysis;
import com.fitcoach.infrastructure.ai.NutritionAnalysis.DetectedFood;
import com.fitcoach.model.NutritionEntry;
import com.fitcoach.model.User;
import com.fitcoach.model.UserProfile;
import com.fitcoach.repository.NutritionEntryRepository;
import com.fitcoach.repository.UserProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Сервис для работы с питанием и историей
 */
@Service
@Transactional
public class NutritionService {
    
    private static final Logger logger = LoggerFactory.getLogger(NutritionService.class);
    
    @Autowired
    private NutritionEntryRepository nutritionEntryRepository;
    
    @Autowired
    private UserProfileRepository userProfileRepository;
    
    /**
     * Сохранить анализ питания от ИИ
     */
    public List<NutritionEntry> saveNutritionAnalysis(User user, NutritionAnalysis analysis, String imageBase64) {
        List<NutritionEntry> entries = new ArrayList<>();
        
        if (analysis.getDetectedFoods() != null && !analysis.getDetectedFoods().isEmpty()) {
            for (DetectedFood food : analysis.getDetectedFoods()) {
                NutritionEntry entry = NutritionEntry.fromAIAnalysis(
                    user,
                    food.getFoodName(),
                    food.getQuantity(),
                    food.getCalories(),
                    food.getProteins(),
                    food.getFats(),
                    food.getCarbs(),
                    food.getConfidence(),
                    analysis.getAnalysisNotes()
                );
                
                // Сохраняем изображение только для первого продукта
                if (entries.isEmpty() && imageBase64 != null) {
                    entry.setImageBase64(imageBase64);
                }
                
                entries.add(nutritionEntryRepository.save(entry));
            }
        } else {
            // Если ИИ ничего не нашел, создаем запись с нулевыми значениями
            NutritionEntry entry = NutritionEntry.fromAIAnalysis(
                user,
                "Неопознанная еда",
                "неизвестно",
                0.0, 0.0, 0.0, 0.0,
                0.0,
                analysis.getAnalysisNotes() != null ? analysis.getAnalysisNotes() : "ИИ не смог распознать еду на изображении"
            );
            entries.add(nutritionEntryRepository.save(entry));
        }
        
        logger.info("💾 Сохранено {} записей о питании для пользователя: {}", 
                   entries.size(), user.getUsername());
        
        return entries;
    }
    
    /**
     * Получить дневную статистику питания
     */
    @Transactional(readOnly = true)
    public DailyNutritionStats getDailyStats(User user, LocalDate date) {
        List<NutritionEntry> entries = nutritionEntryRepository.findByUserAndDateOrderByTimestamp(user, date);
        
        double totalCalories = entries.stream().mapToDouble(NutritionEntry::getCalories).sum();
        double totalProteins = entries.stream().mapToDouble(NutritionEntry::getProteins).sum();
        double totalFats = entries.stream().mapToDouble(NutritionEntry::getFats).sum();
        double totalCarbs = entries.stream().mapToDouble(NutritionEntry::getCarbs).sum();
        
        // Получаем цели пользователя
        Optional<UserProfile> profileOpt = userProfileRepository.findByUser(user);
        UserProfile profile = profileOpt.orElse(null);
        
        return new DailyNutritionStats(
            date, entries, totalCalories, totalProteins, totalFats, totalCarbs, profile
        );
    }
    
    /**
     * Получить недельную статистику
     */
    @Transactional(readOnly = true)
    public WeeklyNutritionStats getWeeklyStats(User user, LocalDate startDate) {
        LocalDate endDate = startDate.plusDays(6);
        List<Object[]> dailyStats = nutritionEntryRepository.getDailyStatistics(user, startDate, endDate);
        
        Map<LocalDate, DailyNutritionStats> statsMap = new HashMap<>();
        
        for (Object[] row : dailyStats) {
            LocalDate date = (LocalDate) row[0];
            double calories = ((Number) row[1]).doubleValue();
            double proteins = ((Number) row[2]).doubleValue();
            double fats = ((Number) row[3]).doubleValue();
            double carbs = ((Number) row[4]).doubleValue();
            long entriesCount = ((Number) row[5]).longValue();
            
            // Получаем записи для этого дня
            List<NutritionEntry> entries = nutritionEntryRepository.findByUserAndDateOrderByTimestamp(user, date);
            
            Optional<UserProfile> profileOpt = userProfileRepository.findByUser(user);
            UserProfile profile = profileOpt.orElse(null);
            
            statsMap.put(date, new DailyNutritionStats(date, entries, calories, proteins, fats, carbs, profile));
        }
        
        return new WeeklyNutritionStats(startDate, endDate, statsMap);
    }
    
    /**
     * Получить последние записи пользователя
     */
    @Transactional(readOnly = true)
    public List<NutritionEntry> getRecentEntries(User user, int limit) {
        return nutritionEntryRepository.findRecentEntries(user, PageRequest.of(0, limit));
    }
    
    /**
     * Получить любимые продукты пользователя
     */
    @Transactional(readOnly = true)
    public List<FavoriteFood> getFavoriteProducts(User user) {
        List<Object[]> results = nutritionEntryRepository.getFavoriteProducts(user);
        
        return results.stream()
                .map(row -> new FavoriteFood(
                    (String) row[0], 
                    ((Number) row[1]).intValue(),
                    ((Number) row[2]).doubleValue()
                ))
                .collect(Collectors.toList());
    }
    
    /**
     * Проверить прогресс к целям
     */
    @Transactional(readOnly = true)
    public ProgressReport getProgressReport(User user, LocalDate date) {
        DailyNutritionStats todayStats = getDailyStats(user, date);
        
        Optional<UserProfile> profileOpt = userProfileRepository.findByUser(user);
        if (profileOpt.isEmpty()) {
            return new ProgressReport(todayStats, null, false);
        }
        
        UserProfile profile = profileOpt.get();
        
        // Рассчитываем прогресс к целям
        boolean onTrack = true;
        StringBuilder feedback = new StringBuilder();
        
        if (profile.getDailyCaloriesGoal() != null) {
            double caloriesProgress = todayStats.getTotalCalories() / profile.getDailyCaloriesGoal();
            if (caloriesProgress < 0.8) {
                onTrack = false;
                feedback.append("Недостаточно калорий. ");
            } else if (caloriesProgress > 1.2) {
                onTrack = false;
                feedback.append("Превышение калорий. ");
            }
        }
        
        if (profile.getDailyProteinsGoal() != null) {
            double proteinsProgress = todayStats.getTotalProteins() / profile.getDailyProteinsGoal();
            if (proteinsProgress < 0.8) {
                feedback.append("Мало белка. ");
            }
        }
        
        return new ProgressReport(todayStats, profile, onTrack, feedback.toString());
    }
    
    /**
     * Получить рекомендации на основе истории питания
     */
    @Transactional(readOnly = true)
    public List<String> getNutritionRecommendations(User user) {
        List<String> recommendations = new ArrayList<>();
        
        // Анализируем последние 7 дней
        LocalDate weekAgo = LocalDate.now().minusDays(7);
        List<Object[]> weeklyStats = nutritionEntryRepository.getDailyStatistics(user, weekAgo, LocalDate.now());
        
        if (weeklyStats.isEmpty()) {
            recommendations.add("Начните отслеживать питание для получения персональных рекомендаций");
            return recommendations;
        }
        
        // Анализ средних значений
        double avgCalories = weeklyStats.stream()
                .mapToDouble(row -> ((Number) row[1]).doubleValue())
                .average().orElse(0);
        
        double avgProteins = weeklyStats.stream()
                .mapToDouble(row -> ((Number) row[2]).doubleValue())
                .average().orElse(0);
        
        // Получаем профиль для сравнения с целями
        Optional<UserProfile> profileOpt = userProfileRepository.findByUser(user);
        if (profileOpt.isPresent()) {
            UserProfile profile = profileOpt.get();
            
            if (profile.getDailyCaloriesGoal() != null && avgCalories < profile.getDailyCaloriesGoal() * 0.8) {
                recommendations.add("Увеличьте калорийность рациона - вы недоедаете");
            }
            
            if (profile.getDailyProteinsGoal() != null && avgProteins < profile.getDailyProteinsGoal() * 0.8) {
                recommendations.add("Добавьте больше белковых продуктов в рацион");
            }
            
            if (profile.getFitnessGoal() == UserProfile.FitnessGoal.LOSE_WEIGHT) {
                recommendations.add("Для похудения сосредоточьтесь на овощах и нежирных белках");
            } else if (profile.getFitnessGoal() == UserProfile.FitnessGoal.BUILD_MUSCLE) {
                recommendations.add("Для набора мышечной массы увеличьте потребление белка и сложных углеводов");
            }
        }
        
        // Анализ любимых продуктов
        List<FavoriteFood> favorites = getFavoriteProducts(user);
        if (!favorites.isEmpty()) {
            double avgCaloriesPerFood = favorites.stream()
                    .mapToDouble(FavoriteFood::getAverageCalories)
                    .average().orElse(0);
            
            if (avgCaloriesPerFood > 400) {
                recommendations.add("Попробуйте заменить высококалорийные продукты более здоровыми альтернативами");
            }
        }
        
        return recommendations;
    }
    
    // Вспомогательные классы
    
    /**
     * Дневная статистика питания
     */
    public static class DailyNutritionStats {
        private final LocalDate date;
        private final List<NutritionEntry> entries;
        private final double totalCalories;
        private final double totalProteins;
        private final double totalFats;
        private final double totalCarbs;
        private final UserProfile profile;
        
        public DailyNutritionStats(LocalDate date, List<NutritionEntry> entries,
                                  double totalCalories, double totalProteins, 
                                  double totalFats, double totalCarbs, UserProfile profile) {
            this.date = date;
            this.entries = entries;
            this.totalCalories = totalCalories;
            this.totalProteins = totalProteins;
            this.totalFats = totalFats;
            this.totalCarbs = totalCarbs;
            this.profile = profile;
        }
        
        // Getters
        public LocalDate getDate() { return date; }
        public List<NutritionEntry> getEntries() { return entries; }
        public double getTotalCalories() { return totalCalories; }
        public double getTotalProteins() { return totalProteins; }
        public double getTotalFats() { return totalFats; }
        public double getTotalCarbs() { return totalCarbs; }
        public UserProfile getProfile() { return profile; }
        
        // Прогресс к целям (в процентах)
        public double getCaloriesProgress() {
            return profile != null && profile.getDailyCaloriesGoal() != null ? 
                   (totalCalories / profile.getDailyCaloriesGoal()) * 100 : 0;
        }
        
        public double getProteinsProgress() {
            return profile != null && profile.getDailyProteinsGoal() != null ? 
                   (totalProteins / profile.getDailyProteinsGoal()) * 100 : 0;
        }
        
        public double getFatsProgress() {
            return profile != null && profile.getDailyFatsGoal() != null ? 
                   (totalFats / profile.getDailyFatsGoal()) * 100 : 0;
        }
        
        public double getCarbsProgress() {
            return profile != null && profile.getDailyCarbsGoal() != null ? 
                   (totalCarbs / profile.getDailyCarbsGoal()) * 100 : 0;
        }
    }
    
    /**
     * Недельная статистика питания
     */
    public static class WeeklyNutritionStats {
        private final LocalDate startDate;
        private final LocalDate endDate;
        private final Map<LocalDate, DailyNutritionStats> dailyStats;
        
        public WeeklyNutritionStats(LocalDate startDate, LocalDate endDate, 
                                   Map<LocalDate, DailyNutritionStats> dailyStats) {
            this.startDate = startDate;
            this.endDate = endDate;
            this.dailyStats = dailyStats;
        }
        
        public LocalDate getStartDate() { return startDate; }
        public LocalDate getEndDate() { return endDate; }
        public Map<LocalDate, DailyNutritionStats> getDailyStats() { return dailyStats; }
        
        public double getAverageCalories() {
            return dailyStats.values().stream()
                    .mapToDouble(DailyNutritionStats::getTotalCalories)
                    .average().orElse(0);
        }
        
        public double getTotalCalories() {
            return dailyStats.values().stream()
                    .mapToDouble(DailyNutritionStats::getTotalCalories)
                    .sum();
        }
    }
    
    /**
     * Любимая еда пользователя
     */
    public static class FavoriteFood {
        private final String foodName;
        private final int count;
        private final double averageCalories;
        
        public FavoriteFood(String foodName, int count, double averageCalories) {
            this.foodName = foodName;
            this.count = count;
            this.averageCalories = averageCalories;
        }
        
        public String getFoodName() { return foodName; }
        public int getCount() { return count; }
        public double getAverageCalories() { return averageCalories; }
    }
    
    /**
     * Отчет о прогрессе
     */
    public static class ProgressReport {
        private final DailyNutritionStats todayStats;
        private final UserProfile profile;
        private final boolean onTrack;
        private final String feedback;
        
        public ProgressReport(DailyNutritionStats todayStats, UserProfile profile, boolean onTrack) {
            this(todayStats, profile, onTrack, "");
        }
        
        public ProgressReport(DailyNutritionStats todayStats, UserProfile profile, boolean onTrack, String feedback) {
            this.todayStats = todayStats;
            this.profile = profile;
            this.onTrack = onTrack;
            this.feedback = feedback;
        }
        
        public DailyNutritionStats getTodayStats() { return todayStats; }
        public UserProfile getProfile() { return profile; }
        public boolean isOnTrack() { return onTrack; }
        public String getFeedback() { return feedback; }
    }
} 