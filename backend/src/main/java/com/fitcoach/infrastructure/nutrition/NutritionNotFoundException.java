package com.fitcoach.infrastructure.nutrition;

/**
 * Исключение когда продукт не найден ни в одном источнике питания
 */
public class NutritionNotFoundException extends Exception {
    
    public NutritionNotFoundException(String message) {
        super(message);
    }
    
    public NutritionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
} 