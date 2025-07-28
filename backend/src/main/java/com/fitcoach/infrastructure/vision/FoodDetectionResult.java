package com.fitcoach.infrastructure.vision;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Результат анализа изображения еды через EfficientNet
 * 
 * Экономия: $1,200/месяц vs GPT-4V Vision API
 * Локальная обработка изображений без внешних API вызовов
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FoodDetectionResult {
    
    private String foodName;
    private double confidence;
    private double estimatedWeight;  // граммы
    private String category;         // "fruits", "vegetables", "meat", etc.
    private List<DetectedFood> alternativeDetections;
    private BoundingBox boundingBox;
    private ImageMetadata imageMetadata;
    private LocalDateTime analyzedAt;
    private String modelVersion;
    
    public FoodDetectionResult() {
        this.analyzedAt = LocalDateTime.now();
        this.modelVersion = "EfficientNet-B0-Food-v1.0";
    }
    
    public FoodDetectionResult(String foodName, double confidence, double estimatedWeight) {
        this();
        this.foodName = foodName;
        this.confidence = confidence;
        this.estimatedWeight = estimatedWeight;
    }
    
    /**
     * Альтернативные детекции с меньшей уверенностью
     */
    public static class DetectedFood {
        private String name;
        private double confidence;
        private String category;
        
        public DetectedFood() {}
        
        public DetectedFood(String name, double confidence, String category) {
            this.name = name;
            this.confidence = confidence;
            this.category = category;
        }
        
        // Геттеры и сеттеры
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }
        
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
    }
    
    /**
     * Координаты обнаруженного объекта на изображении
     */
    public static class BoundingBox {
        private int x;
        private int y;
        private int width;
        private int height;
        
        public BoundingBox() {}
        
        public BoundingBox(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
        
        // Геттеры и сеттеры
        public int getX() { return x; }
        public void setX(int x) { this.x = x; }
        
        public int getY() { return y; }
        public void setY(int y) { this.y = y; }
        
        public int getWidth() { return width; }
        public void setWidth(int width) { this.width = width; }
        
        public int getHeight() { return height; }
        public void setHeight(int height) { this.height = height; }
        
        /**
         * Площадь bounding box в пикселях
         */
        public int getArea() {
            return width * height;
        }
        
        /**
         * Относительная площадь от общего размера изображения
         */
        public double getRelativeArea(int imageWidth, int imageHeight) {
            int totalArea = imageWidth * imageHeight;
            return totalArea > 0 ? (double) getArea() / totalArea : 0;
        }
    }
    
    /**
     * Метаданные изображения
     */
    public static class ImageMetadata {
        private int width;
        private int height;
        private String format;          // "JPEG", "PNG", etc.
        private long fileSizeBytes;
        private String colorSpace;      // "RGB", "GRAYSCALE", etc.
        private Double brightness;      // 0.0 - 1.0
        private Double contrast;        // 0.0 - 1.0
        private Double sharpness;       // 0.0 - 1.0
        
        public ImageMetadata() {}
        
        public ImageMetadata(int width, int height, String format, long fileSizeBytes) {
            this.width = width;
            this.height = height;
            this.format = format;
            this.fileSizeBytes = fileSizeBytes;
        }
        
        // Геттеры и сеттеры
        public int getWidth() { return width; }
        public void setWidth(int width) { this.width = width; }
        
        public int getHeight() { return height; }
        public void setHeight(int height) { this.height = height; }
        
        public String getFormat() { return format; }
        public void setFormat(String format) { this.format = format; }
        
        public long getFileSizeBytes() { return fileSizeBytes; }
        public void setFileSizeBytes(long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }
        
        public String getColorSpace() { return colorSpace; }
        public void setColorSpace(String colorSpace) { this.colorSpace = colorSpace; }
        
        public Double getBrightness() { return brightness; }
        public void setBrightness(Double brightness) { this.brightness = brightness; }
        
        public Double getContrast() { return contrast; }
        public void setContrast(Double contrast) { this.contrast = contrast; }
        
        public Double getSharpness() { return sharpness; }
        public void setSharpness(Double sharpness) { this.sharpness = sharpness; }
        
        /**
         * Проверка качества изображения для анализа
         */
        public boolean isGoodQuality() {
            // Минимальное разрешение 224x224 (размер входа EfficientNet)
            boolean goodResolution = width >= 224 && height >= 224;
            
            // Разумный размер файла (не слишком маленький/большой)
            boolean goodFileSize = fileSizeBytes >= 10_000 && fileSizeBytes <= 10_000_000;
            
            // Достаточная яркость и контрастность
            boolean goodBrightness = brightness == null || (brightness > 0.1 && brightness < 0.9);
            boolean goodContrast = contrast == null || contrast > 0.3;
            
            return goodResolution && goodFileSize && goodBrightness && goodContrast;
        }
    }
    
    /**
     * Проверка валидности результата
     */
    public boolean isValid() {
        return foodName != null && !foodName.trim().isEmpty() && 
               confidence >= 0.0 && confidence <= 1.0 &&
               estimatedWeight > 0;
    }
    
    /**
     * Проверка высокой уверенности в результате
     */
    public boolean isHighConfidence() {
        return confidence >= 0.7; // 70% или выше
    }
    
    /**
     * Краткое описание результата
     */
    public String getSummary() {
        return String.format("%s (%.1f%% уверенность, ~%.0fг)", 
                foodName, confidence * 100, estimatedWeight);
    }
    
    /**
     * Получение лучшей альтернативы если основной результат имеет низкую уверенность
     */
    public DetectedFood getBestAlternative() {
        if (alternativeDetections == null || alternativeDetections.isEmpty()) {
            return null;
        }
        
        return alternativeDetections.stream()
                .max((a, b) -> Double.compare(a.getConfidence(), b.getConfidence()))
                .orElse(null);
    }
    
    // Основные геттеры и сеттеры
    
    public String getFoodName() { return foodName; }
    public void setFoodName(String foodName) { this.foodName = foodName; }
    
    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }
    
    public double getEstimatedWeight() { return estimatedWeight; }
    public void setEstimatedWeight(double estimatedWeight) { this.estimatedWeight = estimatedWeight; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public List<DetectedFood> getAlternativeDetections() { return alternativeDetections; }
    public void setAlternativeDetections(List<DetectedFood> alternativeDetections) { 
        this.alternativeDetections = alternativeDetections; 
    }
    
    public BoundingBox getBoundingBox() { return boundingBox; }
    public void setBoundingBox(BoundingBox boundingBox) { this.boundingBox = boundingBox; }
    
    public ImageMetadata getImageMetadata() { return imageMetadata; }
    public void setImageMetadata(ImageMetadata imageMetadata) { this.imageMetadata = imageMetadata; }
    
    public LocalDateTime getAnalyzedAt() { return analyzedAt; }
    public void setAnalyzedAt(LocalDateTime analyzedAt) { this.analyzedAt = analyzedAt; }
    
    public String getModelVersion() { return modelVersion; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }
    
    @Override
    public String toString() {
        return String.format("FoodDetectionResult{food='%s', confidence=%.3f, weight=%.1fg, model='%s'}",
                foodName, confidence, estimatedWeight, modelVersion);
    }
} 