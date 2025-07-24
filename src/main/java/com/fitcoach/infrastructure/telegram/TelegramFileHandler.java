package com.fitcoach.infrastructure.telegram;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

@Service
public class TelegramFileHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(TelegramFileHandler.class);
    
    @Value("${telegram.bot.token}")
    private String botToken;
    
    private static final String TELEGRAM_FILE_URL = "https://api.telegram.org/file/bot%s/%s";
    
    /**
     * Скачивает фото из Telegram и конвертирует в MultipartFile
     */
    public MultipartFile downloadTelegramPhoto(String fileId) throws TelegramApiException, IOException {
        logger.info("Downloading Telegram photo with fileId: {}", fileId);
        
        // Получаем информацию о файле
        GetFile getFile = new GetFile();
        getFile.setFileId(fileId);
        
        // Здесь нужен доступ к Telegram Bot API, но у нас его нет в этом контексте
        // В реальной реализации нужно будет передать TelegramBot или создать отдельный HTTP клиент
        
        // Пока создаем заглушку для тестирования
        return createMockMultipartFile(fileId);
    }
    
    /**
     * Создает мок MultipartFile для тестирования
     */
    private MultipartFile createMockMultipartFile(String fileId) {
        // Создаем простую заглушку для тестирования
        byte[] mockImageData = "mock_image_data".getBytes();
        
        return new MultipartFile() {
            @Override
            public String getName() {
                return "photo";
            }
            
            @Override
            public String getOriginalFilename() {
                return fileId + ".jpg";
            }
            
            @Override
            public String getContentType() {
                return "image/jpeg";
            }
            
            @Override
            public boolean isEmpty() {
                return false;
            }
            
            @Override
            public long getSize() {
                return mockImageData.length;
            }
            
            @Override
            public byte[] getBytes() {
                return mockImageData;
            }
            
            @Override
            public InputStream getInputStream() {
                return new ByteArrayInputStream(mockImageData);
            }
            
            @Override
            public void transferTo(java.io.File dest) throws IOException, IllegalStateException {
                // Заглушка
            }
        };
    }
    
    /**
     * Скачивает файл по URL
     */
    private byte[] downloadFileFromUrl(String fileUrl) throws IOException {
        logger.info("Downloading file from URL: {}", fileUrl);
        
        try (InputStream inputStream = new URL(fileUrl).openStream()) {
            return inputStream.readAllBytes();
        }
    }
    
    /**
     * Проверяет, является ли файл изображением
     */
    public boolean isImageFile(String fileName, String contentType) {
        if (contentType != null) {
            return contentType.startsWith("image/");
        }
        
        if (fileName != null) {
            String lowerName = fileName.toLowerCase();
            return lowerName.endsWith(".jpg") || 
                   lowerName.endsWith(".jpeg") || 
                   lowerName.endsWith(".png") || 
                   lowerName.endsWith(".webp");
        }
        
        return false;
    }
    
    /**
     * Получает размер файла в человекочитаемом формате
     */
    public String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        }
    }
    
    /**
     * Проверяет, не превышает ли файл максимальный размер
     */
    public boolean isFileSizeAcceptable(long fileSize, long maxSizeBytes) {
        return fileSize <= maxSizeBytes;
    }
} 