package com.fitcoach.infrastructure.ai.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Универсальный ответ от AI сервисов
 * Поддерживает все типы провайдеров: OpenAI, DeepSeek, Gemini
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AIResponse {
    
    private String content;
    private boolean success;
    private String errorMessage;
    private String provider;
    private String model;
    private Integer tokensUsed;
    private Double costUsd;
    private LocalDateTime processedAt;
    private Long processingTimeMs;
    private Map<String, Object> metadata;
    private LocalDateTime createdAt;
    
    public AIResponse() {
        this.createdAt = LocalDateTime.now();
    }
    
    public AIResponse(String content, boolean success, String errorMessage, String provider, 
                     String model, Integer tokensUsed, Double costUsd, LocalDateTime processedAt,
                     Long processingTimeMs, Map<String, Object> metadata) {
        this();
        this.content = content;
        this.success = success;
        this.errorMessage = errorMessage;
        this.provider = provider;
        this.model = model;
        this.tokensUsed = tokensUsed;
        this.costUsd = costUsd;
        this.processedAt = processedAt;
        this.processingTimeMs = processingTimeMs;
        this.metadata = metadata;
    }
    
    /**
     * Создает успешный ответ
     */
    public static AIResponse success(String content, String provider, String model) {
        AIResponse response = new AIResponse();
        response.content = content;
        response.success = true;
        response.provider = provider;
        response.model = model;
        response.processedAt = LocalDateTime.now();
        return response;
    }
    
    /**
     * Создает ответ с ошибкой
     */
    public static AIResponse error(String errorMessage, String provider) {
        AIResponse response = new AIResponse();
        response.success = false;
        response.errorMessage = errorMessage;
        response.provider = provider;
        response.processedAt = LocalDateTime.now();
        return response;
    }
    
    /**
     * Создает ответ с ошибкой (без провайдера)
     */
    public static AIResponse error(String errorMessage) {
        return error(errorMessage, "unknown");
    }
    
    /**
     * Устанавливает информацию о токенах и стоимости
     */
    public AIResponse withUsage(int tokensUsed, double costUsd) {
        this.tokensUsed = tokensUsed;
        this.costUsd = costUsd;
        return this;
    }
    
    /**
     * Устанавливает время обработки
     */
    public AIResponse withProcessingTime(long processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
        return this;
    }
    
    /**
     * Добавляет дополнительные метаданные
     */
    public AIResponse withMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
        return this;
    }
    
    /**
     * Проверяет успешность ответа
     */
    public boolean isSuccessful() {
        return success && content != null && !content.trim().isEmpty();
    }
    
    /**
     * Сериализация в JSON
     */
    public String toJson() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "{\"error\":\"Serialization failed\"}";
        }
    }
    
    /**
     * Десериализация из JSON
     */
    public static AIResponse fromJson(String json) {
        try {
            return new ObjectMapper().readValue(json, AIResponse.class);
        } catch (Exception e) {
            return AIResponse.error("Deserialization failed");
        }
    }
    
    /**
     * Возвращает краткую информацию для логирования
     */
    public String getLogSummary() {
        return String.format("Provider: %s, Model: %s, Success: %s, Tokens: %d, Cost: $%.4f, Time: %dms",
                provider, model, success, 
                tokensUsed != null ? tokensUsed : 0,
                costUsd != null ? costUsd : 0.0,
                processingTimeMs != null ? processingTimeMs : 0);
    }
    
    // Геттеры и сеттеры
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    
    public Integer getTokensUsed() { return tokensUsed; }
    public void setTokensUsed(Integer tokensUsed) { this.tokensUsed = tokensUsed; }
    
    public Double getCostUsd() { return costUsd; }
    public void setCostUsd(Double costUsd) { this.costUsd = costUsd; }
    
    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
    
    public Long getProcessingTimeMs() { return processingTimeMs; }
    public void setProcessingTimeMs(Long processingTimeMs) { this.processingTimeMs = processingTimeMs; }
    
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
} 