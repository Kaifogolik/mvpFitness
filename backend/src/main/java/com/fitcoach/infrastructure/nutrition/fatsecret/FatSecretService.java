package com.fitcoach.infrastructure.nutrition.fatsecret;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitcoach.infrastructure.nutrition.NutritionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

/**
 * Сервис для работы с FatSecret API
 * 
 * Экономия: $290/месяц vs платные Nutrition APIs
 * 
 * FatSecret предоставляет бесплатный доступ к базе данных питания
 * до 500 запросов в день с OAuth 2.0 Client Credentials flow
 */
@Service
public class FatSecretService {
    
    private static final Logger log = LoggerFactory.getLogger(FatSecretService.class);
    
    private static final String FATSECRET_BASE_URL = "https://platform.fatsecret.com/rest/server.api";
    private static final String OAUTH_TOKEN_URL = "https://oauth.fatsecret.com/connect/token";
    
    @Value("${app.nutrition.fatsecret.client-id:}")
    private String clientId;
    
    @Value("${app.nutrition.fatsecret.client-secret:}")
    private String clientSecret;
    
    private final RestTemplate restTemplate;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    
    public FatSecretService(RestTemplate restTemplate, 
                           RedisTemplate<String, String> redisTemplate,
                           ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Поиск продукта и получение питательной информации
     * 
     * @param foodName название продукта
     * @param weight вес порции в граммах
     * @return питательная информация или пустой Optional
     */
    public Optional<NutritionInfo> search(String foodName, double weight) {
        try {
            log.info("🔍 FatSecret поиск: '{}' ({}г)", foodName, weight);
            
            // Проверяем кэш
            String cacheKey = "fatsecret:search:" + foodName.toLowerCase();
            String cachedResult = getCachedResult(cacheKey);
            if (cachedResult != null) {
                log.info("✅ Найден кэш для '{}'", foodName);
                NutritionInfo cached = NutritionInfo.fromJson(cachedResult);
                return Optional.of(cached.scaleToWeight(weight));
            }
            
            // Получаем токен доступа
            String accessToken = getAccessToken();
            if (accessToken == null) {
                log.error("❌ Не удалось получить токен FatSecret");
                return Optional.empty();
            }
            
            // Выполняем поиск
            Optional<FatSecretFood> foodResult = searchFood(accessToken, foodName);
            if (foodResult.isEmpty()) {
                log.warn("⚠️ Продукт '{}' не найден в FatSecret", foodName);
                return Optional.empty();
            }
            
            // Конвертируем в NutritionInfo
            FatSecretFood food = foodResult.get();
            NutritionInfo nutritionInfo = convertToNutritionInfo(food, weight);
            
            // Кэшируем результат на 24 часа
            cacheResult(cacheKey, nutritionInfo.scaleToWeight(100)); // Кэшируем базовую порцию 100г
            
            log.info("✅ FatSecret результат: {}", nutritionInfo.getSummary());
            return Optional.of(nutritionInfo);
            
        } catch (Exception e) {
            log.error("❌ Ошибка FatSecret API для '{}': {}", foodName, e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * Поиск продукта через FatSecret API
     */
    private Optional<FatSecretFood> searchFood(String accessToken, String foodName) {
        try {
            String url = FATSECRET_BASE_URL + "?method=foods.search&search_expression=" + 
                        foodName.replace(" ", "%20") + "&format=json&max_results=1";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            log.debug("🌐 FatSecret запрос: {}", url);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                FatSecretSearchResponse searchResponse = objectMapper.readValue(
                    response.getBody(), FatSecretSearchResponse.class);
                
                return searchResponse.getFirstFood();
            }
            
        } catch (Exception e) {
            log.error("❌ Ошибка поиска в FatSecret: {}", e.getMessage());
        }
        
        return Optional.empty();
    }
    
    /**
     * Получение OAuth 2.0 токена доступа
     */
    private String getAccessToken() {
        // Проверяем кэш токена
        String cachedToken = getCachedResult("fatsecret:token");
        if (cachedToken != null) {
            return cachedToken;
        }
        
        try {
            // Подготавливаем Basic Auth заголовок
            String credentials = clientId + ":" + clientSecret;
            String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Authorization", "Basic " + encodedCredentials);
            
            // Подготавливаем тело запроса
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "client_credentials");
            body.add("scope", "basic");
            
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);
            
            log.debug("🔐 Получение токена FatSecret OAuth 2.0");
            
            ResponseEntity<String> response = restTemplate.postForEntity(OAUTH_TOKEN_URL, entity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode tokenResponse = objectMapper.readTree(response.getBody());
                String accessToken = tokenResponse.get("access_token").asText();
                int expiresIn = tokenResponse.get("expires_in").asInt();
                
                // Кэшируем токен на время его жизни (минус 5 минут для безопасности)
                cacheResult("fatsecret:token", accessToken, Duration.ofSeconds(expiresIn - 300));
                
                log.info("✅ Получен токен FatSecret, действителен {} секунд", expiresIn);
                return accessToken;
            }
            
        } catch (Exception e) {
            log.error("❌ Ошибка получения токена FatSecret: {}", e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Конвертация FatSecretFood в NutritionInfo
     */
    private NutritionInfo convertToNutritionInfo(FatSecretFood food, double weight) {
        double baseWeight = food.getBaseWeight(); // Обычно 100г
        double calories = food.extractCalories();
        double protein = food.extractProtein();
        double fat = food.extractFat();
        double carbs = food.extractCarbs();
        
        // Создаем базовую информацию на 100г, затем масштабируем
        NutritionInfo baseInfo = NutritionInfo.builder()
                .name(food.getFoodName())
                .calories(calories)
                .protein(protein)
                .carbohydrates(carbs)
                .fat(fat)
                .weight(baseWeight)
                .source("FatSecret")
                .build();
        
        // Масштабируем на нужный вес
        return baseInfo.scaleToWeight(weight);
    }
    
    /**
     * Получение результата из кэша
     */
    private String getCachedResult(String key) {
        if (redisTemplate != null) {
            try {
                return redisTemplate.opsForValue().get(key);
            } catch (Exception e) {
                log.warn("⚠️ Ошибка доступа к кэшу: {}", e.getMessage());
            }
        }
        return null;
    }
    
    /**
     * Кэширование результата
     */
    private void cacheResult(String key, Object value) {
        cacheResult(key, value, Duration.ofHours(24));
    }
    
    private void cacheResult(String key, Object value, Duration ttl) {
        if (redisTemplate != null) {
            try {
                String json = value instanceof String ? (String) value : 
                             value instanceof NutritionInfo ? ((NutritionInfo) value).toJson() : 
                             objectMapper.writeValueAsString(value);
                redisTemplate.opsForValue().set(key, json, ttl);
                log.debug("📦 Закэширован результат: {} на {}", key, ttl);
            } catch (Exception e) {
                log.warn("⚠️ Ошибка кэширования: {}", e.getMessage());
            }
        }
    }
    
    /**
     * Проверка доступности FatSecret API
     */
    public boolean isAvailable() {
        if (clientId == null || clientId.trim().isEmpty() || 
            clientSecret == null || clientSecret.trim().isEmpty()) {
            log.warn("⚠️ FatSecret API не настроен (отсутствуют клиентские данные)");
            return false;
        }
        
        try {
            String token = getAccessToken();
            boolean available = token != null;
            log.info("🔍 FatSecret API доступность: {}", available ? "✅" : "❌");
            return available;
        } catch (Exception e) {
            log.error("❌ Ошибка проверки доступности FatSecret: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Получение статистики использования
     */
    public String getUsageStats() {
        try {
            // TODO: Реализовать сбор статистики
            return String.format("FatSecret API: статус=%s, requests_today=unknown", 
                    isAvailable() ? "активен" : "недоступен");
        } catch (Exception e) {
            return "FatSecret API: ошибка получения статистики";
        }
    }
} 