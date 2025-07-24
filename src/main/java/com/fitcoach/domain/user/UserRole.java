package com.fitcoach.domain.user;

public enum UserRole {
    STUDENT("Ученик"),
    COACH("Тренер"),
    ADMIN("Администратор");
    
    private final String displayName;
    
    UserRole(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
} 