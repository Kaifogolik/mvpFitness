package com.fitcoach.domain.user;

import java.math.BigDecimal;

public enum SubscriptionType {
    FREE("Бесплатная", BigDecimal.ZERO, 0, false),
    BASIC("Базовая", new BigDecimal("490.00"), 30, true),
    PREMIUM("Премиум", new BigDecimal("990.00"), 30, true),
    COACH_BASIC("Тренер Базовый", new BigDecimal("990.00"), 30, true),
    COACH_PREMIUM("Тренер Премиум", new BigDecimal("1990.00"), 30, true);
    
    private final String displayName;
    private final BigDecimal price;
    private final int durationDays;
    private final boolean aiAnalysisEnabled;
    
    SubscriptionType(String displayName, BigDecimal price, int durationDays, boolean aiAnalysisEnabled) {
        this.displayName = displayName;
        this.price = price;
        this.durationDays = durationDays;
        this.aiAnalysisEnabled = aiAnalysisEnabled;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public int getDurationDays() {
        return durationDays;
    }
    
    public boolean isAiAnalysisEnabled() {
        return aiAnalysisEnabled;
    }
    
    public boolean isFree() {
        return this == FREE;
    }
    
    public boolean isCoachSubscription() {
        return this == COACH_BASIC || this == COACH_PREMIUM;
    }
} 