package com.fitcoach.domain.user;

public enum Platform {
    TELEGRAM("Telegram"),
    ANDROID("Android"),
    IOS("iOS"),
    WEB("Web"),
    DESKTOP("Desktop");
    
    private final String displayName;
    
    Platform(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
} 