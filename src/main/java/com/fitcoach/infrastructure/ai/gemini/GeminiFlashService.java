package com.fitcoach.infrastructure.ai.gemini;

import com.fitcoach.infrastructure.ai.common.AIResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Временная заглушка для Gemini 2.5 Flash API
 * TODO: Реализовать полную интеграцию с Google Gemini API
 */
@Service
public class GeminiFlashService {
    
    private static final Logger log = LoggerFactory.getLogger(GeminiFlashService.class);
    
    public AIResponse generateAdvice(String content, String context) {
        log.info("Gemini Flash заглушка: генерация советов для контента длиной {} символов", content.length());
        
        // Временная имитация ответа Gemini
        String mockResponse = "Это временный ответ от Gemini 2.5 Flash. " +
                "Контекст: " + context + ". " +
                "Персонализированные советы сгенерированы на основе вашего запроса.";
        
        return AIResponse.success(mockResponse, "gemini", "gemini-2.5-flash")
                .withUsage(150, 0.011); // Примерные значения для Gemini Flash
    }
} 