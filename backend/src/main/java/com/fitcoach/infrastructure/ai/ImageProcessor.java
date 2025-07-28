package com.fitcoach.infrastructure.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

/**
 * Сервис для обработки изображений перед анализом
 * Включает сжатие, валидацию и оптимизацию для OpenAI API
 */
@Service
public class ImageProcessor {
    
    private static final Logger logger = LoggerFactory.getLogger(ImageProcessor.class);
    
    // Максимальные параметры для OpenAI (оптимизировано для экономии токенов)
    private static final int MAX_WIDTH = 512;  // Уменьшено с 2048 для экономии токенов
    private static final int MAX_HEIGHT = 512; // Уменьшено с 2048 для экономии токенов  
    private static final long MAX_FILE_SIZE = 512 * 1024; // 512KB вместо 4MB
    private static final float JPEG_QUALITY = 0.6f; // Больше сжатие для экономии
    
    /**
     * Обрабатывает изображение для оптимального анализа в OpenAI
     */
    public ProcessedImage processImageForAI(byte[] imageBytes, String originalFormat) {
        try {
            logger.info("🖼️ Начинаю обработку изображения: {} bytes, формат: {}", 
                imageBytes.length, originalFormat);
            
            // Загружаем изображение
            BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (originalImage == null) {
                throw new IllegalArgumentException("Не удалось загрузить изображение");
            }
            
            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();
            logger.debug("Исходный размер: {}x{}", originalWidth, originalHeight);
            
            // Проверяем нужно ли изменение размера
            BufferedImage processedImage = originalImage;
            boolean wasResized = false;
            
            if (originalWidth > MAX_WIDTH || originalHeight > MAX_HEIGHT) {
                processedImage = resizeImage(originalImage, MAX_WIDTH, MAX_HEIGHT);
                wasResized = true;
                logger.info("📏 Изображение изменено до: {}x{}", 
                    processedImage.getWidth(), processedImage.getHeight());
            }
            
            // Конвертируем в JPEG для лучшего сжатия
            byte[] processedBytes = convertToJPEG(processedImage, JPEG_QUALITY);
            
            // Проверяем размер после обработки
            if (processedBytes.length > MAX_FILE_SIZE) {
                logger.warn("⚠️ Файл все еще большой после сжатия: {} bytes", processedBytes.length);
                // Дополнительное сжатие
                processedBytes = convertToJPEG(processedImage, 0.6f);
            }
            
            // Конвертируем в base64
            String base64Image = Base64.getEncoder().encodeToString(processedBytes);
            
            ProcessedImage result = new ProcessedImage(
                base64Image,
                processedImage.getWidth(),
                processedImage.getHeight(),
                processedBytes.length,
                wasResized,
                calculateCompressionRatio(imageBytes.length, processedBytes.length)
            );
            
            logger.info("✅ Обработка завершена: {}x{}, {} bytes, сжатие: {:.1f}%", 
                result.getWidth(), result.getHeight(), result.getFileSize(), result.getCompressionRatio());
            
            return result;
            
        } catch (Exception e) {
            logger.error("❌ Ошибка при обработке изображения: {}", e.getMessage(), e);
            throw new RuntimeException("Ошибка обработки изображения: " + e.getMessage(), e);
        }
    }
    
    /**
     * Изменяет размер изображения с сохранением пропорций
     */
    private BufferedImage resizeImage(BufferedImage original, int maxWidth, int maxHeight) {
        int originalWidth = original.getWidth();
        int originalHeight = original.getHeight();
        
        // Вычисляем новые размеры с сохранением пропорций
        double widthRatio = (double) maxWidth / originalWidth;
        double heightRatio = (double) maxHeight / originalHeight;
        double ratio = Math.min(widthRatio, heightRatio);
        
        int newWidth = (int) (originalWidth * ratio);
        int newHeight = (int) (originalHeight * ratio);
        
        BufferedImage resized = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resized.createGraphics();
        
        // Настройки для качественного ресайза
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2d.drawImage(original, 0, 0, newWidth, newHeight, null);
        g2d.dispose();
        
        return resized;
    }
    
    /**
     * Конвертирует изображение в JPEG с заданным качеством
     */
    private byte[] convertToJPEG(BufferedImage image, float quality) throws IOException {
        // Создаем RGB изображение (JPEG не поддерживает прозрачность)
        BufferedImage rgbImage = new BufferedImage(
            image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        
        Graphics2D g2d = rgbImage.createGraphics();
        g2d.setColor(Color.WHITE); // Белый фон вместо прозрачности
        g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(rgbImage, "jpg", baos);
        
        return baos.toByteArray();
    }
    
    /**
     * Проводит предварительную валидацию изображения
     */
    public ImageValidationResult validateImage(byte[] imageBytes, String fileName) {
        try {
            if (imageBytes == null || imageBytes.length == 0) {
                return new ImageValidationResult(false, "Файл пустой");
            }
            
            if (imageBytes.length > 20 * 1024 * 1024) { // 20MB лимит на загрузку
                return new ImageValidationResult(false, 
                    String.format("Файл слишком большой: %.1f MB (максимум 20 MB)", 
                        imageBytes.length / (1024.0 * 1024.0)));
            }
            
            // Проверяем что это изображение
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (image == null) {
                return new ImageValidationResult(false, "Файл не является изображением");
            }
            
            int width = image.getWidth();
            int height = image.getHeight();
            
            if (width < 100 || height < 100) {
                return new ImageValidationResult(false, 
                    String.format("Изображение слишком маленькое: %dx%d (минимум 100x100)", width, height));
            }
            
            if (width > 4096 || height > 4096) {
                logger.info("📐 Большое изображение: {}x{}, будет сжато", width, height);
            }
            
            return new ImageValidationResult(true, "Изображение корректно");
            
        } catch (Exception e) {
            logger.error("Ошибка валидации изображения: {}", e.getMessage());
            return new ImageValidationResult(false, "Ошибка при проверке файла: " + e.getMessage());
        }
    }
    
    /**
     * Вычисляет коэффициент сжатия
     */
    private double calculateCompressionRatio(long originalSize, long compressedSize) {
        if (originalSize == 0) return 0.0;
        return ((double) (originalSize - compressedSize) / originalSize) * 100.0;
    }
    
    /**
     * Результат обработки изображения
     */
    public static class ProcessedImage {
        private final String base64Data;
        private final int width;
        private final int height;
        private final long fileSize;
        private final boolean wasResized;
        private final double compressionRatio;
        
        public ProcessedImage(String base64Data, int width, int height, long fileSize, 
                            boolean wasResized, double compressionRatio) {
            this.base64Data = base64Data;
            this.width = width;
            this.height = height;
            this.fileSize = fileSize;
            this.wasResized = wasResized;
            this.compressionRatio = compressionRatio;
        }
        
        // Getters
        public String getBase64Data() { return base64Data; }
        public int getWidth() { return width; }
        public int getHeight() { return height; }
        public long getFileSize() { return fileSize; }
        public boolean wasResized() { return wasResized; }
        public double getCompressionRatio() { return compressionRatio; }
    }
    
    /**
     * Результат валидации изображения
     */
    public static class ImageValidationResult {
        private final boolean valid;
        private final String message;
        
        public ImageValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }
        
        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
    }
} 