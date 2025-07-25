package com.fitcoach.infrastructure.ai;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Результат анализа питания от OpenAI API
 */
public class NutritionAnalysis {
    
    @JsonProperty("detected_foods")
    private List<DetectedFood> detectedFoods;
    
    @JsonProperty("total_calories")
    private Double totalCalories;
    
    @JsonProperty("total_proteins")
    private Double totalProteins;
    
    @JsonProperty("total_fats")
    private Double totalFats;
    
    @JsonProperty("total_carbs")
    private Double totalCarbs;
    
    @JsonProperty("confidence_level")
    private Double confidenceLevel;
    
    @JsonProperty("analysis_notes")
    private String analysisNotes;
    
    @JsonProperty("health_recommendations")
    private List<String> healthRecommendations;

    // Constructors
    public NutritionAnalysis() {}

    public NutritionAnalysis(List<DetectedFood> detectedFoods, Double totalCalories, 
                           Double totalProteins, Double totalFats, Double totalCarbs,
                           Double confidenceLevel, String analysisNotes, 
                           List<String> healthRecommendations) {
        this.detectedFoods = detectedFoods;
        this.totalCalories = totalCalories;
        this.totalProteins = totalProteins;
        this.totalFats = totalFats;
        this.totalCarbs = totalCarbs;
        this.confidenceLevel = confidenceLevel;
        this.analysisNotes = analysisNotes;
        this.healthRecommendations = healthRecommendations;
    }

    // Getters and Setters
    public List<DetectedFood> getDetectedFoods() {
        return detectedFoods;
    }

    public void setDetectedFoods(List<DetectedFood> detectedFoods) {
        this.detectedFoods = detectedFoods;
    }

    public Double getTotalCalories() {
        return totalCalories;
    }

    public void setTotalCalories(Double totalCalories) {
        this.totalCalories = totalCalories;
    }

    public Double getTotalProteins() {
        return totalProteins;
    }

    public void setTotalProteins(Double totalProteins) {
        this.totalProteins = totalProteins;
    }

    public Double getTotalFats() {
        return totalFats;
    }

    public void setTotalFats(Double totalFats) {
        this.totalFats = totalFats;
    }

    public Double getTotalCarbs() {
        return totalCarbs;
    }

    public void setTotalCarbs(Double totalCarbs) {
        this.totalCarbs = totalCarbs;
    }

    public Double getConfidenceLevel() {
        return confidenceLevel;
    }

    public void setConfidenceLevel(Double confidenceLevel) {
        this.confidenceLevel = confidenceLevel;
    }

    public String getAnalysisNotes() {
        return analysisNotes;
    }

    public void setAnalysisNotes(String analysisNotes) {
        this.analysisNotes = analysisNotes;
    }

    public List<String> getHealthRecommendations() {
        return healthRecommendations;
    }

    public void setHealthRecommendations(List<String> healthRecommendations) {
        this.healthRecommendations = healthRecommendations;
    }

    @Override
    public String toString() {
        return "NutritionAnalysis{" +
                "detectedFoods=" + detectedFoods +
                ", totalCalories=" + totalCalories +
                ", totalProteins=" + totalProteins +
                ", totalFats=" + totalFats +
                ", totalCarbs=" + totalCarbs +
                ", confidenceLevel=" + confidenceLevel +
                ", analysisNotes='" + analysisNotes + '\'' +
                ", healthRecommendations=" + healthRecommendations +
                '}';
    }

    /**
     * Внутренний класс для представления обнаруженной еды
     */
    public static class DetectedFood {
        @JsonProperty("food_name")
        private String foodName;
        
        @JsonProperty("quantity")
        private String quantity;
        
        @JsonProperty("calories")
        private Double calories;
        
        @JsonProperty("proteins")
        private Double proteins;
        
        @JsonProperty("fats")
        private Double fats;
        
        @JsonProperty("carbs")
        private Double carbs;
        
        @JsonProperty("confidence")
        private Double confidence;

        // Constructors
        public DetectedFood() {}

        public DetectedFood(String foodName, String quantity, Double calories, 
                          Double proteins, Double fats, Double carbs, Double confidence) {
            this.foodName = foodName;
            this.quantity = quantity;
            this.calories = calories;
            this.proteins = proteins;
            this.fats = fats;
            this.carbs = carbs;
            this.confidence = confidence;
        }

        // Getters and Setters
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

        @Override
        public String toString() {
            return "DetectedFood{" +
                    "foodName='" + foodName + '\'' +
                    ", quantity='" + quantity + '\'' +
                    ", calories=" + calories +
                    ", proteins=" + proteins +
                    ", fats=" + fats +
                    ", carbs=" + carbs +
                    ", confidence=" + confidence +
                    '}';
        }
    }
} 