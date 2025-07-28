package com.fitcoach.infrastructure.nutrition.fatsecret;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Модель продукта из FatSecret API
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FatSecretFood {
    
    @JsonProperty("food_id")
    private String foodId;
    
    @JsonProperty("food_name")
    private String foodName;
    
    @JsonProperty("food_type")
    private String foodType;
    
    @JsonProperty("food_description")
    private String foodDescription;
    
    @JsonProperty("food_url")
    private String foodUrl;
    
    // Конструкторы
    public FatSecretFood() {}
    
    public FatSecretFood(String foodId, String foodName, String foodDescription) {
        this.foodId = foodId;
        this.foodName = foodName;
        this.foodDescription = foodDescription;
    }
    
    /**
     * Извлекает калории из описания FatSecret
     * Формат: "Per 100g - Calories: 250kcal | Fat: 15.00g | Carbs: 20.00g | Protein: 10.00g"
     */
    public double extractCalories() {
        if (foodDescription == null) return 0;
        
        try {
            // Ищем паттерн "Calories: XXXkcal"
            String[] parts = foodDescription.split("\\|");
            for (String part : parts) {
                if (part.toLowerCase().contains("calories:")) {
                    String caloriesPart = part.trim().toLowerCase();
                    String calories = caloriesPart.replaceAll(".*calories:\\s*", "")
                                                 .replaceAll("kcal.*", "")
                                                 .trim();
                    return Double.parseDouble(calories);
                }
            }
        } catch (Exception e) {
            // Если не удалось распарсить, возвращаем 0
        }
        return 0;
    }
    
    /**
     * Извлекает белки из описания
     */
    public double extractProtein() {
        return extractNutrient("protein", "g");
    }
    
    /**
     * Извлекает жиры из описания
     */
    public double extractFat() {
        return extractNutrient("fat", "g");
    }
    
    /**
     * Извлекает углеводы из описания
     */
    public double extractCarbs() {
        return extractNutrient("carbs", "g");
    }
    
    /**
     * Универсальный метод для извлечения питательных веществ
     */
    private double extractNutrient(String nutrientName, String unit) {
        if (foodDescription == null) return 0;
        
        try {
            String[] parts = foodDescription.split("\\|");
            for (String part : parts) {
                if (part.toLowerCase().contains(nutrientName.toLowerCase() + ":")) {
                    String nutrientPart = part.trim().toLowerCase();
                    String value = nutrientPart.replaceAll(".*" + nutrientName.toLowerCase() + ":\\s*", "")
                                               .replaceAll(unit + ".*", "")
                                               .trim();
                    return Double.parseDouble(value);
                }
            }
        } catch (Exception e) {
            // Если не удалось распарсить, возвращаем 0
        }
        return 0;
    }
    
    /**
     * Определяет базовый вес порции (обычно 100г для FatSecret)
     */
    public double getBaseWeight() {
        if (foodDescription == null) return 100.0;
        
        // Ищем "Per XXXg" в начале описания
        try {
            String desc = foodDescription.toLowerCase();
            if (desc.startsWith("per ")) {
                String weightPart = desc.substring(4);
                String weight = weightPart.replaceAll("g.*", "").trim();
                return Double.parseDouble(weight);
            }
        } catch (Exception e) {
            // Если не удалось распарсить, возвращаем стандартные 100г
        }
        return 100.0;
    }
    
    /**
     * Проверяет валидность данных о продукте
     */
    public boolean isValid() {
        return foodId != null && !foodId.trim().isEmpty() &&
               foodName != null && !foodName.trim().isEmpty() &&
               foodDescription != null && !foodDescription.trim().isEmpty();
    }
    
    // Геттеры и сеттеры
    
    public String getFoodId() { return foodId; }
    public void setFoodId(String foodId) { this.foodId = foodId; }
    
    public String getFoodName() { return foodName; }
    public void setFoodName(String foodName) { this.foodName = foodName; }
    
    public String getFoodType() { return foodType; }
    public void setFoodType(String foodType) { this.foodType = foodType; }
    
    public String getFoodDescription() { return foodDescription; }
    public void setFoodDescription(String foodDescription) { this.foodDescription = foodDescription; }
    
    public String getFoodUrl() { return foodUrl; }
    public void setFoodUrl(String foodUrl) { this.foodUrl = foodUrl; }
    
    @Override
    public String toString() {
        return String.format("FatSecretFood{id='%s', name='%s', calories=%.1f}",
                foodId, foodName, extractCalories());
    }
} 