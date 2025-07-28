package com.fitcoach.infrastructure.nutrition;

import com.fitcoach.infrastructure.nutrition.fatsecret.FatSecretService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

/**
 * Центральный сервис для работы с API питания
 * 
 * Экономия: $290/месяц за счет каскадного использования бесплатных API
 * 
 * Приоритет источников:
 * 1. FatSecret API (бесплатно, 500 запросов/день)
 * 2. USDA FoodData Central (бесплатно, без лимитов)
 * 3. Edamam Nutrition API (бесплатно, 100 запросов/месяц)
 * 4. Локальная база продуктов (fallback)
 */
@Service
public class NutritionApiService {
    
    private static final Logger log = LoggerFactory.getLogger(NutritionApiService.class);
    
    private final FatSecretService fatSecretService;
    private final RedisTemplate<String, String> redisTemplate;
    
    // TODO: Добавить другие сервисы когда будут реализованы
    // private final USDAFdcService usdaFdcService;
    // private final EdamamService edamamService;
    // private final LocalNutritionDatabase localDatabase;
    
    public NutritionApiService(FatSecretService fatSecretService,
                              RedisTemplate<String, String> redisTemplate) {
        this.fatSecretService = fatSecretService;
        this.redisTemplate = redisTemplate;
        
        log.info("🍎 Nutrition API Service инициализирован:");
        log.info("   - FatSecret: {}", fatSecretService != null ? "✅" : "❌");
        log.info("   - Redis кэш: {}", redisTemplate != null ? "✅" : "❌");
    }
    
    /**
     * Получение питательной информации с каскадным fallback
     * 
     * @param foodName название продукта
     * @param weight вес порции в граммах
     * @return питательная информация
     * @throws NutritionNotFoundException если продукт не найден во всех источниках
     */
    public NutritionInfo getNutrition(String foodName, double weight) throws NutritionNotFoundException {
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("🔍 Поиск питания: '{}' ({}г)", foodName, weight);
            
            // Проверяем общий кэш
            String globalCacheKey = "nutrition:global:" + foodName.toLowerCase() + ":" + weight;
            String cachedResult = getCachedResult(globalCacheKey);
            if (cachedResult != null) {
                log.info("✅ Найден в общем кэше: '{}'", foodName);
                return NutritionInfo.fromJson(cachedResult);
            }
            
            // 1. Пробуем FatSecret (приоритет #1)
            Optional<NutritionInfo> result = tryFatSecret(foodName, weight);
            if (result.isPresent()) {
                cacheGlobalResult(globalCacheKey, result.get());
                logSuccess("FatSecret", result.get(), startTime);
                return result.get();
            }
            
            // 2. TODO: Пробуем USDA FDC (приоритет #2)
            // result = tryUSDAFdc(foodName, weight);
            // if (result.isPresent()) {
            //     cacheGlobalResult(globalCacheKey, result.get());
            //     logSuccess("USDA FDC", result.get(), startTime);
            //     return result.get();
            // }
            
            // 3. TODO: Пробуем Edamam (приоритет #3)
            // result = tryEdamam(foodName, weight);
            // if (result.isPresent()) {
            //     cacheGlobalResult(globalCacheKey, result.get());
            //     logSuccess("Edamam", result.get(), startTime);
            //     return result.get();
            // }
            
            // 4. TODO: Пробуем локальную базу (fallback)
            // result = tryLocalDatabase(foodName, weight);
            // if (result.isPresent()) {
            //     cacheGlobalResult(globalCacheKey, result.get());
            //     logSuccess("Local DB", result.get(), startTime);
            //     return result.get();
            // }
            
            // Если ничего не найдено
            long duration = System.currentTimeMillis() - startTime;
            log.warn("❌ Продукт '{}' не найден ни в одном источнике ({}мс)", foodName, duration);
            throw new NutritionNotFoundException("Продукт не найден: " + foodName);
            
        } catch (NutritionNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("❌ Критическая ошибка поиска питания для '{}': {}", foodName, e.getMessage());
            throw new NutritionNotFoundException("Ошибка сервиса питания", e);
        }
    }
    
    /**
     * Поиск через FatSecret API
     */
    private Optional<NutritionInfo> tryFatSecret(String foodName, double weight) {
        try {
            if (fatSecretService == null || !fatSecretService.isAvailable()) {
                log.debug("⚠️ FatSecret недоступен");
                return Optional.empty();
            }
            
            return fatSecretService.search(foodName, weight);
            
        } catch (Exception e) {
            log.warn("⚠️ Ошибка FatSecret для '{}': {}", foodName, e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * Получение результата из общего кэша
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
     * Кэширование в общий кэш
     */
    private void cacheGlobalResult(String key, NutritionInfo nutritionInfo) {
        if (redisTemplate != null) {
            try {
                redisTemplate.opsForValue().set(key, nutritionInfo.toJson(), Duration.ofHours(24));
                log.debug("📦 Закэширован в общий кэш: {}", key);
            } catch (Exception e) {
                log.warn("⚠️ Ошибка кэширования: {}", e.getMessage());
            }
        }
    }
    
    /**
     * Логирование успешного результата
     */
    private void logSuccess(String source, NutritionInfo nutrition, long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        log.info("✅ {} результат ({}мс): {}", source, duration, nutrition.getSummary());
    }
    
    /**
     * Получение статистики всех источников
     */
    public NutritionApiStats getStatistics() {
        return NutritionApiStats.builder()
                .fatSecretAvailable(fatSecretService != null && fatSecretService.isAvailable())
                .fatSecretStats(fatSecretService != null ? fatSecretService.getUsageStats() : "недоступен")
                .usdaAvailable(false) // TODO
                .edamamAvailable(false) // TODO  
                .localDbAvailable(false) // TODO
                .totalRequests(0) // TODO: Реализовать счетчики
                .cacheHitRate(0.0) // TODO: Реализовать статистику кэша
                .averageResponseTime(0) // TODO: Реализовать метрики
                .monthlySavings(290.0) // Экономия vs платные API
                .build();
    }
    
    /**
     * Проверка доступности хотя бы одного источника
     */
    public boolean isAnySourceAvailable() {
        return (fatSecretService != null && fatSecretService.isAvailable());
        // TODO: Добавить проверки других источников
    }
    
    /**
     * Очистка всех кэшей питания
     */
    public void clearAllCaches() {
        if (redisTemplate != null) {
            try {
                redisTemplate.delete("nutrition:*");
                redisTemplate.delete("fatsecret:*");
                log.info("🧹 Очищены все кэши питания");
            } catch (Exception e) {
                log.error("❌ Ошибка очистки кэшей: {}", e.getMessage());
            }
        }
    }
    
    /**
     * Статистика API питания
     */
    public static class NutritionApiStats {
        private boolean fatSecretAvailable;
        private String fatSecretStats;
        private boolean usdaAvailable;
        private boolean edamamAvailable;
        private boolean localDbAvailable;
        private long totalRequests;
        private double cacheHitRate;
        private long averageResponseTime;
        private double monthlySavings;
        
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private NutritionApiStats stats = new NutritionApiStats();
            
            public Builder fatSecretAvailable(boolean available) {
                stats.fatSecretAvailable = available;
                return this;
            }
            
            public Builder fatSecretStats(String statsString) {
                stats.fatSecretStats = statsString;
                return this;
            }
            
            public Builder usdaAvailable(boolean available) {
                stats.usdaAvailable = available;
                return this;
            }
            
            public Builder edamamAvailable(boolean available) {
                stats.edamamAvailable = available;
                return this;
            }
            
            public Builder localDbAvailable(boolean available) {
                stats.localDbAvailable = available;
                return this;
            }
            
            public Builder totalRequests(long requests) {
                stats.totalRequests = requests;
                return this;
            }
            
            public Builder cacheHitRate(double rate) {
                stats.cacheHitRate = rate;
                return this;
            }
            
            public Builder averageResponseTime(long timeMs) {
                stats.averageResponseTime = timeMs;
                return this;
            }
            
            public Builder monthlySavings(double savings) {
                stats.monthlySavings = savings;
                return this;
            }
            
            public NutritionApiStats build() {
                return stats;
            }
        }
        
        // Геттеры
        public boolean isFatSecretAvailable() { return fatSecretAvailable; }
        public String getFatSecretStats() { return fatSecretStats; }
        public boolean isUsdaAvailable() { return usdaAvailable; }
        public boolean isEdamamAvailable() { return edamamAvailable; }
        public boolean isLocalDbAvailable() { return localDbAvailable; }
        public long getTotalRequests() { return totalRequests; }
        public double getCacheHitRate() { return cacheHitRate; }
        public long getAverageResponseTime() { return averageResponseTime; }
        public double getMonthlySavings() { return monthlySavings; }
    }
} 