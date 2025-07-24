package com.fitcoach.domain.user;

public enum UserStatus {
    ACTIVE("Активный"),
    INACTIVE("Неактивный"),
    BLOCKED("Заблокированный"),
    PENDING("Ожидает подтверждения");
    
    private final String displayName;
    
    UserStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
} 