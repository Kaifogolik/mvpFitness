package com.fitcoach.infrastructure.ai.deepseek;

import com.fitcoach.infrastructure.ai.common.AIResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Временная заглушка для DeepSeek R1 API
 * TODO: Реализовать полную интеграцию с DeepSeek API
 */
@Service
public class DeepSeekService {
    
    private static final Logger log = LoggerFactory.getLogger(DeepSeekService.class);
    
    public AIResponse analyze(String content, String context) {
        log.info("DeepSeek заглушка: анализ контента длиной {} символов", content.length());
        
        // Временная имитация ответа DeepSeek
        String mockResponse = "Это временный ответ от DeepSeek. " +
                "Контекст: " + context + ". " +
                "Анализируемый контент обработан успешно.";
        
        return AIResponse.success(mockResponse, "deepseek", "deepseek-r1")
                .withUsage(100, 0.014); // Примерные значения для DeepSeek R1
    }
} 