package com.fitcoach.domain.user;

public enum FitnessGoal {
    WEIGHT_LOSS("Снижение веса", -500),          // дефицит 500 ккал
    WEIGHT_GAIN("Набор веса", 500),              // профицит 500 ккал  
    MUSCLE_GAIN("Набор мышечной массы", 300),    // профицит 300 ккал
    MAINTENANCE("Поддержание веса", 0),          // без изменений
    BODY_RECOMPOSITION("Рекомпозиция тела", -200); // небольшой дефицит
    
    private final String displayName;
    private final int calorieAdjustment; // +/- к базовому метаболизму
    
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