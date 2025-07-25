package com.fitcoach.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Запись о приеме пищи пользователя
 */
@Entity
@Table(name = "nutrition_entries", indexes = {
    @Index(name = "idx_user_date", columnList = "user_id, date")
})
public class NutritionEntry {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    private LocalDate date;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    // Информация о еде
    @Column(nullable = false)
    private String foodName;
    
    private String quantity;
    
    // Пищевая ценность
    @Column(nullable = false)
    private Double calories;
    
    @Column(nullable = false)
    private Double proteins; // граммы
    
    @Column(nullable = false)
    private Double fats; // граммы
    
    @Column(nullable = false)
    private Double carbs; // граммы
    
    // Дополнительная информация
    private Double confidence; // уверенность ИИ (0-1)
    
    @Enumerated(EnumType.STRING)
    private MealType mealType;
    
    @Column(length = 1000)
    private String notes; // заметки от анализа ИИ
    
    // Источник данных
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DataSource dataSource;
    
    private String imageBase64; // сохраненное изображение (опционально)
    
    // Конструкторы
    public NutritionEntry() {}
    
    public NutritionEntry(User user, String foodName, Double calories, 
                         Double proteins, Double fats, Double carbs) {
        this.user = user;
        this.foodName = foodName;
        this.calories = calories;
        this.proteins = proteins;
        this.fats = fats;
        this.carbs = carbs;
        this.date = LocalDate.now();
        this.timestamp = LocalDateTime.now();
        this.dataSource = DataSource.AI_ANALYSIS;
        this.mealType = MealType.OTHER;
    }
    
    // Enums
    public enum MealType {
        BREAKFAST("Завтрак"),
        LUNCH("Обед"),
        DINNER("Ужин"),
        SNACK("Перекус"),
        OTHER("Другое");
        
        private final String displayName;
        
        MealType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum DataSource {
        AI_ANALYSIS("Анализ ИИ"),
        MANUAL_INPUT("Ручной ввод"),
        PRODUCTS_DATABASE("База продуктов"),
        BARCODE_SCAN("Сканирование штрихкода");
        
        private final String displayName;
        
        DataSource(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // Utility методы
    /**
     * Автоматически определяет тип приема пищи по времени
     */
    public void autoDetectMealType() {
        int hour = timestamp.getHour();
        
        if (hour >= 6 && hour < 11) {
            this.mealType = MealType.BREAKFAST;
        } else if (hour >= 11 && hour < 16) {
            this.mealType = MealType.LUNCH;
        } else if (hour >= 16 && hour < 22) {
            this.mealType = MealType.DINNER;
        } else {
            this.mealType = MealType.SNACK;
        }
    }
    
    /**
     * Создает запись из анализа ИИ
     */
    public static NutritionEntry fromAIAnalysis(User user, String foodName, String quantity,
                                              Double calories, Double proteins, Double fats, 
                                              Double carbs, Double confidence, String notes) {
        NutritionEntry entry = new NutritionEntry(user, foodName, calories, proteins, fats, carbs);
        entry.setQuantity(quantity);
        entry.setConfidence(confidence);
        entry.setNotes(notes);
        entry.setDataSource(DataSource.AI_ANALYSIS);
        entry.autoDetectMealType();
        return entry;
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
    
    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(LocalDate date) {
        this.date = date;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getFoodName() {
        return foodName;
    }
    
    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }
    
    public String getQuantity() {
        return quantity;
    }
    
    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }
    
    public Double getCalories() {
        return calories;
    }
    
    public void setCalories(Double calories) {
        this.calories = calories;
    }
    
    public Double getProteins() {
        return proteins;
    }
    
    public void setProteins(Double proteins) {
        this.proteins = proteins;
    }
    
    public Double getFats() {
        return fats;
    }
    
    public void setFats(Double fats) {
        this.fats = fats;
    }
    
    public Double getCarbs() {
        return carbs;
    }
    
    public void setCarbs(Double carbs) {
        this.carbs = carbs;
    }
    
    public Double getConfidence() {
        return confidence;
    }
    
    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }
    
    public MealType getMealType() {
        return mealType;
    }
    
    public void setMealType(MealType mealType) {
        this.mealType = mealType;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public DataSource getDataSource() {
        return dataSource;
    }
    
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    public String getImageBase64() {
        return imageBase64;
    }
    
    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }
    
    @Override
    public String toString() {
        return String.format("NutritionEntry{id=%d, user=%s, food='%s', calories=%.0f, date=%s}", 
                           id, user != null ? user.getUsername() : null, 
                           foodName, calories, date);
    }
} 