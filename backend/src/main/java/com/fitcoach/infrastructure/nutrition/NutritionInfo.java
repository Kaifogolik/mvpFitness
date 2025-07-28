package com.fitcoach.infrastructure.nutrition;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

/**
 * Унифицированная модель питательной информации
 * Поддерживает данные от FatSecret, USDA FDC, Edamam APIs
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class NutritionInfo {
    
    private String name;
    private double calories;
    private double protein;      // граммы
    private double carbohydrates; // граммы
    private double fat;          // граммы
    private Double fiber;        // граммы
    private Double sugar;        // граммы
    private Double sodium;       // миллиграммы
    private double weight;       // граммы порции
    private String source;       // API источник
    private LocalDateTime fetchedAt;
    
    // Дополнительные витамины и минералы
    private Double vitaminC;     // мг
    private Double calcium;      // мг
    private Double iron;         // мг
    private Double potassium;    // мг
    
    public NutritionInfo() {
        this.fetchedAt = LocalDateTime.now();
    }
    
    public NutritionInfo(String name, double calories, double protein, double carbohydrates, 
                        double fat, double weight, String source) {
        this();
        this.name = name;
        this.calories = calories;
        this.protein = protein;
        this.carbohydrates = carbohydrates;
        this.fat = fat;
        this.weight = weight;
        this.source = source;
    }
    
    /**
     * Создание Builder для удобного конструирования
     */
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private NutritionInfo info = new NutritionInfo();
        
        public Builder name(String name) {
            info.name = name;
            return this;
        }
        
        public Builder calories(double calories) {
            info.calories = calories;
            return this;
        }
        
        public Builder protein(double protein) {
            info.protein = protein;
            return this;
        }
        
        public Builder carbohydrates(double carbohydrates) {
            info.carbohydrates = carbohydrates;
            return this;
        }
        
        public Builder fat(double fat) {
            info.fat = fat;
            return this;
        }
        
        public Builder fiber(Double fiber) {
            info.fiber = fiber;
            return this;
        }
        
        public Builder sugar(Double sugar) {
            info.sugar = sugar;
            return this;
        }
        
        public Builder sodium(Double sodium) {
            info.sodium = sodium;
            return this;
        }
        
        public Builder weight(double weight) {
            info.weight = weight;
            return this;
        }
        
        public Builder source(String source) {
            info.source = source;
            return this;
        }
        
        public Builder vitaminC(double vitaminC) {
            info.vitaminC = vitaminC;
            return this;
        }
        
        public Builder calcium(double calcium) {
            info.calcium = calcium;
            return this;
        }
        
        public Builder iron(double iron) {
            info.iron = iron;
            return this;
        }
        
        public Builder potassium(double potassium) {
            info.potassium = potassium;
            return this;
        }
        
        public NutritionInfo build() {
            return info;
        }
    }
    
    /**
     * Пересчет для указанного веса порции
     */
    public NutritionInfo scaleToWeight(double targetWeight) {
        if (this.weight <= 0) {
            return this; // Не можем масштабировать без исходного веса
        }
        
        double scaleFactor = targetWeight / this.weight;
        
        return NutritionInfo.builder()
                .name(this.name)
                .calories(this.calories * scaleFactor)
                .protein(this.protein * scaleFactor)
                .carbohydrates(this.carbohydrates * scaleFactor)
                .fat(this.fat * scaleFactor)
                .fiber(this.fiber != null ? this.fiber * scaleFactor : null)
                .sugar(this.sugar != null ? this.sugar * scaleFactor : null)
                .sodium(this.sodium != null ? this.sodium * scaleFactor : null)
                .weight(targetWeight)
                .source(this.source + " (scaled)")
                .vitaminC(this.vitaminC != null ? this.vitaminC * scaleFactor : null)
                .calcium(this.calcium != null ? this.calcium * scaleFactor : null)
                .iron(this.iron != null ? this.iron * scaleFactor : null)
                .potassium(this.potassium != null ? this.potassium * scaleFactor : null)
                .build();
    }
    
    /**
     * Сериализация в JSON для кэширования
     */
    public String toJson() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "{\"error\":\"Serialization failed\"}";
        }
    }
    
    /**
     * Десериализация из JSON
     */
    public static NutritionInfo fromJson(String json) {
        try {
            return new ObjectMapper().readValue(json, NutritionInfo.class);
        } catch (Exception e) {
            return new NutritionInfo("Error", 0, 0, 0, 0, 0, "error");
        }
    }
    
    /**
     * Проверка валидности данных
     */
    public boolean isValid() {
        return name != null && !name.trim().isEmpty() && 
               calories >= 0 && protein >= 0 && carbohydrates >= 0 && fat >= 0;
    }
    
    /**
     * Краткое описание для логирования
     */
    public String getSummary() {
        return String.format("%s: %.1f ккал, %.1f/%.1f/%.1f БЖУ (%.1fg) [%s]",
                name, calories, protein, fat, carbohydrates, weight, source);
    }
    
    // Геттеры и сеттеры
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public double getCalories() { return calories; }
    public void setCalories(double calories) { this.calories = calories; }
    
    public double getProtein() { return protein; }
    public void setProtein(double protein) { this.protein = protein; }
    
    public double getCarbohydrates() { return carbohydrates; }
    public void setCarbohydrates(double carbohydrates) { this.carbohydrates = carbohydrates; }
    
    public double getFat() { return fat; }
    public void setFat(double fat) { this.fat = fat; }
    
    public double getFiber() { return fiber != null ? fiber : 0; }
    public void setFiber(Double fiber) { this.fiber = fiber; }
    
    public double getSugar() { return sugar != null ? sugar : 0; }
    public void setSugar(Double sugar) { this.sugar = sugar; }
    
    public double getSodium() { return sodium != null ? sodium : 0; }
    public void setSodium(Double sodium) { this.sodium = sodium; }
    
    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }
    
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    
    public LocalDateTime getFetchedAt() { return fetchedAt; }
    public void setFetchedAt(LocalDateTime fetchedAt) { this.fetchedAt = fetchedAt; }
    
    public Double getVitaminC() { return vitaminC; }
    public void setVitaminC(Double vitaminC) { this.vitaminC = vitaminC; }
    
    public Double getCalcium() { return calcium; }
    public void setCalcium(Double calcium) { this.calcium = calcium; }
    
    public Double getIron() { return iron; }
    public void setIron(Double iron) { this.iron = iron; }
    
    public Double getPotassium() { return potassium; }
    public void setPotassium(Double potassium) { this.potassium = potassium; }
} 