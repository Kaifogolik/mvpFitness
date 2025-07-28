package com.fitcoach.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Профиль пользователя с целями и параметрами
 */
@Entity
@Table(name = "user_profiles")
public class UserProfile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    // Физические параметры
    private Integer age;
    private Double weight; // кг
    private Integer height; // см
    
    @Enumerated(EnumType.STRING)
    private Gender gender;
    
    @Enumerated(EnumType.STRING)
    private ActivityLevel activityLevel;
    
    @Enumerated(EnumType.STRING)
    private FitnessGoal fitnessGoal;
    
    // Целевые параметры питания (рассчитываются автоматически)
    private Double dailyCaloriesGoal;
    private Double dailyProteinsGoal; // граммы
    private Double dailyFatsGoal; // граммы
    private Double dailyCarbsGoal; // граммы
    
    // Настройки
    private Boolean notificationsEnabled = true;
    private Boolean trackingEnabled = true;
    
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    private LocalDateTime updatedAt;
    
    // Конструкторы
    public UserProfile() {}
    
    public UserProfile(User user, FitnessGoal fitnessGoal) {
        this.user = user;
        this.fitnessGoal = fitnessGoal;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Enums
    public enum Gender {
        MALE("Мужской"),
        FEMALE("Женский");
        
        private final String displayName;
        
        Gender(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum ActivityLevel {
        SEDENTARY("Сидячий образ жизни", 1.2),
        LIGHTLY_ACTIVE("Легкая активность", 1.375),
        MODERATELY_ACTIVE("Умеренная активность", 1.55),
        VERY_ACTIVE("Высокая активность", 1.725),
        SUPER_ACTIVE("Очень высокая активность", 1.9);
        
        private final String displayName;
        private final double multiplier;
        
        ActivityLevel(String displayName, double multiplier) {
            this.displayName = displayName;
            this.multiplier = multiplier;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public double getMultiplier() {
            return multiplier;
        }
    }
    
    public enum FitnessGoal {
        LOSE_WEIGHT("Похудение", -500),
        MAINTAIN_WEIGHT("Поддержание веса", 0),
        GAIN_WEIGHT("Набор веса", 500),
        BUILD_MUSCLE("Набор мышечной массы", 300);
        
        private final String displayName;
        private final int calorieAdjustment; // ккал в день
        
        FitnessGoal(String displayName, int calorieAdjustment) {
            this.displayName = displayName;
            this.calorieAdjustment = calorieAdjustment;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public int getCalorieAdjustment() {
            return calorieAdjustment;
        }
    }
    
    // Методы расчета
    /**
     * Рассчитывает базовый метаболизм (BMR) по формуле Миффлина-Сан Жеора
     */
    public double calculateBMR() {
        if (weight == null || height == null || age == null || gender == null) {
            return 0;
        }
        
        if (gender == Gender.MALE) {
            return 10 * weight + 6.25 * height - 5 * age + 5;
        } else {
            return 10 * weight + 6.25 * height - 5 * age - 161;
        }
    }
    
    /**
     * Рассчитывает суточную норму калорий
     */
    public double calculateDailyCalories() {
        if (activityLevel == null) return 0;
        double bmr = calculateBMR();
        double maintenance = bmr * activityLevel.getMultiplier();
        int adjustment = fitnessGoal != null ? fitnessGoal.getCalorieAdjustment() : 0;
        return maintenance + adjustment;
    }
    
    /**
     * Обновляет целевые показатели питания на основе параметров
     */
    public void updateNutritionGoals() {
        this.dailyCaloriesGoal = calculateDailyCalories();
        
        if (weight != null && dailyCaloriesGoal > 0) {
            // Белки: 1.6-2.2г на кг веса (берем 2г)
            this.dailyProteinsGoal = weight * 2.0;
            
            // Жиры: 20-30% от калорий (берем 25%)
            this.dailyFatsGoal = (dailyCaloriesGoal * 0.25) / 9; // 9 ккал в грамме жира
            
            // Углеводы: оставшиеся калории
            double remainingCalories = dailyCaloriesGoal - (dailyProteinsGoal * 4) - (dailyFatsGoal * 9);
            this.dailyCarbsGoal = remainingCalories / 4; // 4 ккал в грамме углеводов
        }
        
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters и Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public Integer getAge() {
        return age;
    }
    
    public void setAge(Integer age) {
        this.age = age;
    }
    
    public Double getWeight() {
        return weight;
    }
    
    public void setWeight(Double weight) {
        this.weight = weight;
    }
    
    public Integer getHeight() {
        return height;
    }
    
    public void setHeight(Integer height) {
        this.height = height;
    }
    
    public Gender getGender() {
        return gender;
    }
    
    public void setGender(Gender gender) {
        this.gender = gender;
    }
    
    public ActivityLevel getActivityLevel() {
        return activityLevel;
    }
    
    public void setActivityLevel(ActivityLevel activityLevel) {
        this.activityLevel = activityLevel;
    }
    
    public FitnessGoal getFitnessGoal() {
        return fitnessGoal;
    }
    
    public void setFitnessGoal(FitnessGoal fitnessGoal) {
        this.fitnessGoal = fitnessGoal;
    }
    
    public Double getDailyCaloriesGoal() {
        return dailyCaloriesGoal;
    }
    
    public void setDailyCaloriesGoal(Double dailyCaloriesGoal) {
        this.dailyCaloriesGoal = dailyCaloriesGoal;
    }
    
    public Double getDailyProteinsGoal() {
        return dailyProteinsGoal;
    }
    
    public void setDailyProteinsGoal(Double dailyProteinsGoal) {
        this.dailyProteinsGoal = dailyProteinsGoal;
    }
    
    public Double getDailyFatsGoal() {
        return dailyFatsGoal;
    }
    
    public void setDailyFatsGoal(Double dailyFatsGoal) {
        this.dailyFatsGoal = dailyFatsGoal;
    }
    
    public Double getDailyCarbsGoal() {
        return dailyCarbsGoal;
    }
    
    public void setDailyCarbsGoal(Double dailyCarbsGoal) {
        this.dailyCarbsGoal = dailyCarbsGoal;
    }
    
    public Boolean getNotificationsEnabled() {
        return notificationsEnabled;
    }
    
    public void setNotificationsEnabled(Boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }
    
    public Boolean getTrackingEnabled() {
        return trackingEnabled;
    }
    
    public void setTrackingEnabled(Boolean trackingEnabled) {
        this.trackingEnabled = trackingEnabled;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public String toString() {
        return String.format("UserProfile{userId=%d, goal=%s, calories=%.0f}", 
                           user != null ? user.getId() : null, 
                           fitnessGoal, dailyCaloriesGoal);
    }
} 