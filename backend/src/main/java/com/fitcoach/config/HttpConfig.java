package com.fitcoach.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Конфигурация HTTP клиентов и сериализации
 */
@Configuration
public class HttpConfig {
    
    /**
     * RestTemplate для HTTP запросов к внешним API
     */
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        
        // Таймауты для внешних API
        factory.setConnectTimeout((int) Duration.ofSeconds(10).toMillis());
        factory.setReadTimeout((int) Duration.ofSeconds(30).toMillis());
        
        RestTemplate restTemplate = new RestTemplate(factory);
        
        return restTemplate;
    }
    
    /**
     * ObjectMapper для JSON сериализации/десериализации
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // Поддержка Java 8 времени
        mapper.registerModule(new JavaTimeModule());
        
        return mapper;
    }
} 