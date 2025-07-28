package com.fitcoach.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "FitCoach AI Platform работает!");
        response.put("timestamp", LocalDateTime.now());
        response.put("version", "MVP 1.0");
        return response;
    }

    @GetMapping("/info")
    public Map<String, Object> info() {
        Map<String, Object> response = new HashMap<>();
        response.put("name", "FitCoach AI Platform");
        response.put("description", "Умная фитнес-платформа с ИИ-анализом питания");
        response.put("technologies", new String[]{"Spring Boot", "Telegram Bot", "OpenAI", "H2 Database"});
        response.put("features", new String[]{
            "Telegram Bot интеграция",
            "REST API",
            "Swagger документация",
            "H2 база данных в памяти"
        });
        response.put("github", "https://github.com/Kaifogolik/mvpFitness");
        return response;
    }

    @GetMapping("/telegram")
    public Map<String, Object> telegramInfo() {
        Map<String, Object> response = new HashMap<>();
        response.put("bot_username", "fitcoach_ai_bot");
        response.put("commands", new String[]{"/start", "/help", "/about", "/status"});
        response.put("status", "Активен");
        response.put("description", "Найдите бота в Telegram: @fitcoach_ai_bot");
        return response;
    }
} 