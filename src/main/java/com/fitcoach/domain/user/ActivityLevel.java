package com.fitcoach.domain.user;

public enum ActivityLevel {
    SEDENTARY("Сидячий образ жизни", 1.2),
    LIGHTLY_ACTIVE("Легкая активность", 1.375),
    MODERATELY_ACTIVE("Умеренная активность", 1.55),
    VERY_ACTIVE("Высокая активность", 1.725),
    EXTREMELY_ACTIVE("Очень высокая активность", 1.9);
    
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