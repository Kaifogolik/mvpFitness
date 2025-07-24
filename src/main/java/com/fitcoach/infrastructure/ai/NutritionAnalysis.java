package com.fitcoach.infrastructure.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NutritionAnalysis {
    
    @JsonProperty("food_items")
    private List<String> foodItems;
    
    @JsonProperty("total_calories")
    private Integer totalCalories;
    
    @JsonProperty("proteins")
    private Double proteins;
    
    @JsonProperty("carbs")  
    private Double carbs;
    
    @JsonProperty("fats")
    private Double fats;
    
    @JsonProperty("fiber")
    private Double fiber;
    
    @JsonProperty("sugar")
    private Double sugar;
    
    // AI metadata
    private Double confidence;
    private String modelUsed;
    private String rawResponse;
    
    // Analysis details
    @JsonProperty("detailed_items")
    private List<DetectedItem> detailedItems;
    
    @JsonProperty("warnings")
    private List<String> warnings;
    
    @JsonProperty("recommendations")
    private List<String> recommendations;
    
    // Constructors
    public NutritionAnalysis() {}
    
    public NutritionAnalysis(Integer totalCalories, Double proteins, Double carbs, Double fats) {
        this.totalCalories = totalCalories;
        this.proteins = proteins;
        this.carbs = carbs;
        this.fats = fats;
    }
    
    // Business methods
    public boolean isReliable() {
        return confidence != null && confidence >= 0.7;
    }
    
    public boolean needsUserVerification() {
        return confidence != null && confidence < 0.5;
    }
    
    public int getProteinCalories() {
        return proteins != null ? (int) (proteins * 4) : 0;
    }
    
    public int getCarbCalories() {
        return carbs != null ? (int) (carbs * 4) : 0;
    }
    
    public int getFatCalories() {
        return fats != null ? (int) (fats * 9) : 0;
    }
    
    public String getMacroBreakdown() {
        if (totalCalories == null || totalCalories == 0) {
            return "Не удалось определить состав";
        }
        
        int proteinCal = getProteinCalories();
        int carbCal = getCarbCalories();
        int fatCal = getFatCalories();
        
        int proteinPercent = (proteinCal * 100) / totalCalories;
        int carbPercent = (carbCal * 100) / totalCalories;
        int fatPercent = (fatCal * 100) / totalCalories;
        
        return String.format("Б: %d%% | Ж: %d%% | У: %d%%", 
                           proteinPercent, fatPercent, carbPercent);
    }
    
    // Static factory methods
    public static NutritionAnalysis empty() {
        return new NutritionAnalysis(0, 0.0, 0.0, 0.0);
    }
    
    public static NutritionAnalysis fromCalories(int calories) {
        NutritionAnalysis analysis = new NutritionAnalysis();
        analysis.totalCalories = calories;
        analysis.confidence = 0.3; // Low confidence when only calories provided
        return analysis;
    }
    
    // Getters and Setters
    public List<String> getFoodItems() { return foodItems; }
    public void setFoodItems(List<String> foodItems) { this.foodItems = foodItems; }
    
    public Integer getTotalCalories() { return totalCalories; }
    public void setTotalCalories(Integer totalCalories) { this.totalCalories = totalCalories; }
    
    public Double getProteins() { return proteins; }
    public void setProteins(Double proteins) { this.proteins = proteins; }
    
    public Double getCarbs() { return carbs; }
    public void setCarbs(Double carbs) { this.carbs = carbs; }
    
    public Double getFats() { return fats; }
    public void setFats(Double fats) { this.fats = fats; }
    
    public Double getFiber() { return fiber; }
    public void setFiber(Double fiber) { this.fiber = fiber; }
    
    public Double getSugar() { return sugar; }
    public void setSugar(Double sugar) { this.sugar = sugar; }
    
    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }
    
    public String getModelUsed() { return modelUsed; }
    public void setModelUsed(String modelUsed) { this.modelUsed = modelUsed; }
    
    public String getRawResponse() { return rawResponse; }
    public void setRawResponse(String rawResponse) { this.rawResponse = rawResponse; }
    
    public List<DetectedItem> getDetailedItems() { return detailedItems; }
    public void setDetailedItems(List<DetectedItem> detailedItems) { this.detailedItems = detailedItems; }
    
    public List<String> getWarnings() { return warnings; }
    public void setWarnings(List<String> warnings) { this.warnings = warnings; }
    
    public List<String> getRecommendations() { return recommendations; }
    public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }
    
    @Override
    public String toString() {
        return "NutritionAnalysis{" +
                "totalCalories=" + totalCalories +
                ", proteins=" + proteins +
                ", carbs=" + carbs +
                ", fats=" + fats +
                ", confidence=" + confidence +
                '}';
    }
    
    // Inner class for detailed item analysis
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DetectedItem {
        private String name;
        private Integer calories;
        private Double weight;
        private Double confidence;
        private String category;
        
        // Constructors
        public DetectedItem() {}
        
        public DetectedItem(String name, Integer calories, Double weight) {
            this.name = name;
            this.calories = calories;
            this.weight = weight;
        }
        
        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public Integer getCalories() { return calories; }
        public void setCalories(Integer calories) { this.calories = calories; }
        
        public Double getWeight() { return weight; }
        public void setWeight(Double weight) { this.weight = weight; }
        
        public Double getConfidence() { return confidence; }
        public void setConfidence(Double confidence) { this.confidence = confidence; }
        
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
    }
} 