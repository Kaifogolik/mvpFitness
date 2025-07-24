package com.fitcoach.domain.nutrition;

public enum MealType {
    BREAKFAST("Завтрак", "🌅"),
    LUNCH("Обед", "🌞"), 
    DINNER("Ужин", "🌙"),
    SNACK("Перекус", "🍎"),
    PRE_WORKOUT("До тренировки", "💪"),
    POST_WORKOUT("После тренировки", "🏋️");
    
    private final String displayName;
    private final String emoji;
    
    MealType(String displayName, String emoji) {
        this.displayName = displayName;
        this.emoji = emoji;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getEmoji() {
        return emoji;
    }
    
    public String getDisplayNameWithEmoji() {
        return emoji + " " + displayName;
    }
} 