package com.fitcoach.domain.nutrition;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "detected_food_items")
public class DetectedFoodItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "food_entry_id", nullable = false)
    private FoodEntry foodEntry;
    
    @Column(name = "item_name", nullable = false)
    private String itemName;
    
    @Column(name = "calories")
    private Integer calories;
    
    @Column(name = "weight_grams")
    private Double weightGrams;
    
    @Column(name = "confidence")
    private Double confidence; // Уверенность ИИ в распознавании этого продукта
    
    @Column(name = "category")
    private String category; // Категория продукта (фрукты, мясо, и т.д.)
    
    // Макронутриенты для конкретного продукта
    @Column(name = "protein_g")
    private Double proteinGrams;
    
    @Column(name = "carb_g")
    private Double carbGrams;
    
    @Column(name = "fat_g")
    private Double fatGrams;
    
    // Constructors
    public DetectedFoodItem() {}
    
    public DetectedFoodItem(FoodEntry foodEntry, String itemName, Integer calories) {
        this.foodEntry = foodEntry;
        this.itemName = itemName;
        this.calories = calories;
    }
    
    // Business methods
    public boolean isHighConfidence() {
        return confidence != null && confidence >= 0.8;
    }
    
    public boolean needsVerification() {
        return confidence != null && confidence < 0.6;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public FoodEntry getFoodEntry() { return foodEntry; }
    public void setFoodEntry(FoodEntry foodEntry) { this.foodEntry = foodEntry; }
    
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    
    public Integer getCalories() { return calories; }
    public void setCalories(Integer calories) { this.calories = calories; }
    
    public Double getWeightGrams() { return weightGrams; }
    public void setWeightGrams(Double weightGrams) { this.weightGrams = weightGrams; }
    
    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public Double getProteinGrams() { return proteinGrams; }
    public void setProteinGrams(Double proteinGrams) { this.proteinGrams = proteinGrams; }
    
    public Double getCarbGrams() { return carbGrams; }
    public void setCarbGrams(Double carbGrams) { this.carbGrams = carbGrams; }
    
    public Double getFatGrams() { return fatGrams; }
    public void setFatGrams(Double fatGrams) { this.fatGrams = fatGrams; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DetectedFoodItem that = (DetectedFoodItem) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "DetectedFoodItem{" +
                "id=" + id +
                ", itemName='" + itemName + '\'' +
                ", calories=" + calories +
                ", confidence=" + confidence +
                '}';
    }
} 