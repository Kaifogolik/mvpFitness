package com.fitcoach.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Модель пользователя системы
 */
@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String telegramId;
    
    @Column(nullable = false)
    private String username;
    
    private String firstName;
    private String lastName;
    
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    private LocalDateTime lastActiveAt;
    
    // Конструкторы
    public User() {}
    
    public User(String telegramId, String username) {
        this.telegramId = telegramId;
        this.username = username;
        this.createdAt = LocalDateTime.now();
        this.lastActiveAt = LocalDateTime.now();
    }
    
    // Getters и Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTelegramId() {
        return telegramId;
    }
    
    public void setTelegramId(String telegramId) {
        this.telegramId = telegramId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getLastActiveAt() {
        return lastActiveAt;
    }
    
    public void setLastActiveAt(LocalDateTime lastActiveAt) {
        this.lastActiveAt = lastActiveAt;
    }
    
    @Override
    public String toString() {
        return String.format("User{id=%d, telegramId='%s', username='%s'}", 
                           id, telegramId, username);
    }
} 