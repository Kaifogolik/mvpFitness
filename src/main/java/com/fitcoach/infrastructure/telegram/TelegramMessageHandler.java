package com.fitcoach.infrastructure.telegram;

import com.fitcoach.domain.nutrition.FoodEntry;
import com.fitcoach.domain.nutrition.FoodEntryService;
import com.fitcoach.domain.user.User;
import com.fitcoach.domain.user.UserService;
import com.fitcoach.infrastructure.ai.NutritionAnalysis;
import com.fitcoach.infrastructure.ai.OpenAIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class TelegramMessageHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(TelegramMessageHandler.class);
    
    private final OpenAIService openAIService;
    private final FoodEntryService foodEntryService;
    private final UserService userService;
    private final TelegramFileHandler fileHandler;
    
    public TelegramMessageHandler(OpenAIService openAIService,
                                 FoodEntryService foodEntryService,
                                 UserService userService,
                                 TelegramFileHandler fileHandler) {
        this.openAIService = openAIService;
        this.foodEntryService = foodEntryService;
        this.userService = userService;
        this.fileHandler = fileHandler;
    }
    
    /**
     * Обрабатывает фото еды и возвращает результат анализа
     */
    public CompletableFuture<FoodAnalysisResult> handleFoodPhotoAnalysis(String chatId, Update update, User user) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Получаем фото наилучшего качества
                PhotoSize photo = update.getMessage().getPhoto().stream()
                    .max(Comparator.comparing(PhotoSize::getFileSize))
                    .orElseThrow(() -> new RuntimeException("Не удалось получить фото"));
                
                // Скачиваем файл
                MultipartFile photoFile = fileHandler.downloadTelegramPhoto(photo.getFileId());
                
                // Анализируем через OpenAI
                NutritionAnalysis analysis = openAIService.analyzeFoodPhoto(photoFile).get();
                
                // Сохраняем в базу данных
                FoodEntry foodEntry = foodEntryService.createFromAnalysis(user, analysis, photoFile);
                
                // Форматируем ответ
                String formattedMessage = formatFoodAnalysisMessage(analysis, foodEntry);
                
                return new FoodAnalysisResult(foodEntry.getId(), formattedMessage, analysis);
                
            } catch (Exception e) {
                logger.error("Error processing food photo analysis", e);
                throw new RuntimeException("Ошибка анализа фото: " + e.getMessage());
            }
        });
    }
    
    /**
     * Обрабатывает сообщения пользователей как вопросы к ИИ
     */
    public CompletableFuture<String> handleAIChatMessage(String chatId, String message, User user) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Получаем контекст пользователя (последние приемы пищи, цели и т.д.)
                String userContext = buildUserContext(user);
                
                // Формируем запрос к ИИ с контекстом
                String aiPrompt = String.format(
                    "Пользователь %s спрашивает: %s\n\nКонтекст пользователя:\n%s\n\n" +
                    "Дай краткий и полезный ответ по фитнесу/питанию на русском языке:",
                    user.getFirstName(), message, userContext
                );
                
                // Запрос к OpenAI (используем простой текстовый метод)
                return generateAIResponse(aiPrompt);
                
            } catch (Exception e) {
                logger.error("Error processing AI chat message", e);
                return "Извини, не смог обработать вопрос. Попробуй переформулировать или используй команды из меню.";
            }
        });
    }
    
    /**
     * Обрабатывает callback query от inline кнопок
     */
    public void handleCallbackQuery(String callbackData, String chatId, User user) {
        try {
            String[] parts = callbackData.split("_");
            String action = parts[0];
            
            switch (action) {
                case "stats" -> handleStatsCallback(parts[1], chatId, user);
                case "coach" -> handleCoachCallback(parts[1], chatId, user);
                case "become" -> handleBecomeCoachCallback(chatId, user);
                case "subscribe" -> handleSubscriptionCallback(chatId, user);
                case "correct" -> handleFoodCorrectionCallback(parts[1], chatId, user);
                default -> logger.warn("Unknown callback action: {}", action);
            }
        } catch (Exception e) {
            logger.error("Error handling callback query: {}", callbackData, e);
        }
    }
    
    /**
     * Форматирует профиль пользователя для отображения
     */
    public String formatUserProfile(User user) {
        StringBuilder profile = new StringBuilder();
        profile.append("👤 **Мой профиль**\n\n");
        profile.append("📛 Имя: ").append(user.getFullName()).append("\n");
        profile.append("🎯 Роль: ").append(user.getRole().getDisplayName()).append("\n");
        
        if (user.getAge() != null) {
            profile.append("🎂 Возраст: ").append(user.getAge()).append(" лет\n");
        }
        
        if (user.getHeightCm() != null && user.getWeightKg() != null) {
            profile.append("📏 Рост: ").append(user.getHeightCm()).append(" см\n");
            profile.append("⚖️ Вес: ").append(user.getWeightKg()).append(" кг\n");
            
            // Расчет ИМТ
            double bmi = user.getWeightKg() / Math.pow(user.getHeightCm() / 100.0, 2);
            profile.append("📊 ИМТ: ").append(String.format("%.1f", bmi)).append("\n");
        }
        
        if (user.getFitnessGoal() != null) {
            profile.append("🎯 Цель: ").append(user.getFitnessGoal().getDisplayName()).append("\n");
        }
        
        if (user.getActivityLevel() != null) {
            profile.append("💪 Активность: ").append(user.getActivityLevel().getDisplayName()).append("\n");
        }
        
        profile.append("\n💎 Подписка: ").append(user.getSubscriptionType().getDisplayName());
        
        if (user.hasActiveSubscription()) {
            profile.append(" (до ").append(user.getSubscriptionEndDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))).append(")");
        }
        
        return profile.toString();
    }
    
    /**
     * Форматирует статистику тренера
     */
    public String formatCoachStats(User coach) {
        // TODO: Получить реальную статистику из сервисов
        List<User> students = userService.getCoachStudents(coach.getId());
        long activeSubscriptions = students.stream()
            .filter(User::hasActiveSubscription)
            .count();
        
        StringBuilder stats = new StringBuilder();
        stats.append("👥 Учеников: ").append(students.size()).append("\n");
        stats.append("💎 Активных подписок: ").append(activeSubscriptions).append("\n");
        stats.append("💰 Доход за месяц: ").append(calculateMonthlyEarnings(coach)).append("₽\n");
        stats.append("📈 Рост за неделю: +").append(calculateWeeklyGrowth(coach)).append(" учеников");
        
        return stats.toString();
    }
    
    // Private helper methods
    
    private String formatFoodAnalysisMessage(NutritionAnalysis analysis, FoodEntry foodEntry) {
        StringBuilder message = new StringBuilder();
        
        // Основная информация
        message.append("🍽 **Анализ питания**\n\n");
        
        if (analysis.getFoodItems() != null && !analysis.getFoodItems().isEmpty()) {
            message.append("📋 Обнаружено: ").append(String.join(", ", analysis.getFoodItems())).append("\n\n");
        }
        
        // Калории и макросы
        message.append("🔥 Калории: **").append(analysis.getTotalCalories()).append(" ккал**\n");
        
        if (analysis.getProteins() != null) {
            message.append("🥩 Белки: ").append(String.format("%.1f", analysis.getProteins())).append("г (")
                   .append(analysis.getProteinCalories()).append(" ккал)\n");
        }
        
        if (analysis.getCarbs() != null) {
            message.append("🍞 Углеводы: ").append(String.format("%.1f", analysis.getCarbs())).append("г (")
                   .append(analysis.getCarbCalories()).append(" ккал)\n");
        }
        
        if (analysis.getFats() != null) {
            message.append("🥑 Жиры: ").append(String.format("%.1f", analysis.getFats())).append("г (")
                   .append(analysis.getFatCalories()).append(" ккал)\n");
        }
        
        // Соотношение БЖУ
        message.append("\n📊 ").append(analysis.getMacroBreakdown()).append("\n");
        
        // Уверенность ИИ
        if (analysis.getConfidence() != null) {
            int confidencePercent = (int) (analysis.getConfidence() * 100);
            String confidenceEmoji = confidencePercent >= 80 ? "✅" : confidencePercent >= 60 ? "⚠️" : "❓";
            message.append("\n").append(confidenceEmoji).append(" Точность: ").append(confidencePercent).append("%");
            
            if (confidencePercent < 70) {
                message.append("\n💡 Рекомендую проверить данные и при необходимости скорректировать");
            }
        }
        
        // Рекомендации ИИ
        if (analysis.getRecommendations() != null && !analysis.getRecommendations().isEmpty()) {
            message.append("\n\n💡 **Рекомендации:**\n");
            for (String recommendation : analysis.getRecommendations()) {
                message.append("• ").append(recommendation).append("\n");
            }
        }
        
        // Время добавления
        message.append("\n🕐 Добавлено: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        
        return message.toString();
    }
    
    private String buildUserContext(User user) {
        StringBuilder context = new StringBuilder();
        
        context.append("Пользователь: ").append(user.getFullName()).append("\n");
        
        if (user.getFitnessGoal() != null) {
            context.append("Цель: ").append(user.getFitnessGoal().getDisplayName()).append("\n");
        }
        
        if (user.getActivityLevel() != null) {
            context.append("Уровень активности: ").append(user.getActivityLevel().getDisplayName()).append("\n");
        }
        
        // TODO: Добавить информацию о последних приемах пищи, прогрессе и т.д.
        
        return context.toString();
    }
    
    private String generateAIResponse(String prompt) {
        // TODO: Интегрировать с OpenAI для текстовых запросов
        // Пока возвращаем заглушку
        return "Спасибо за вопрос! Функция ИИ-помощника находится в разработке. " +
               "Используйте команды из меню для анализа питания и просмотра статистики.";
    }
    
    private void handleStatsCallback(String period, String chatId, User user) {
        // TODO: Реализовать показ статистики за период
        logger.info("Stats callback for period: {} from user: {}", period, user.getId());
    }
    
    private void handleCoachCallback(String action, String chatId, User user) {
        // TODO: Реализовать коучинговые действия
        logger.info("Coach callback action: {} from user: {}", action, user.getId());
    }
    
    private void handleBecomeCoachCallback(String chatId, User user) {
        // TODO: Реализовать процесс становления тренером
        logger.info("Become coach callback from user: {}", user.getId());
    }
    
    private void handleSubscriptionCallback(String chatId, User user) {
        // TODO: Реализовать процесс подписки
        logger.info("Subscription callback from user: {}", user.getId());
    }
    
    private void handleFoodCorrectionCallback(String foodEntryId, String chatId, User user) {
        // TODO: Реализовать коррекцию данных о еде
        logger.info("Food correction callback for entry: {} from user: {}", foodEntryId, user.getId());
    }
    
    private int calculateMonthlyEarnings(User coach) {
        // TODO: Реальный расчет доходов
        return 15000; // Заглушка
    }
    
    private int calculateWeeklyGrowth(User coach) {
        // TODO: Реальный расчет роста
        return 3; // Заглушка
    }
    
    // Result class for food analysis
    public static class FoodAnalysisResult {
        private final Long foodEntryId;
        private final String formattedMessage;
        private final NutritionAnalysis analysis;
        
        public FoodAnalysisResult(Long foodEntryId, String formattedMessage, NutritionAnalysis analysis) {
            this.foodEntryId = foodEntryId;
            this.formattedMessage = formattedMessage;
            this.analysis = analysis;
        }
        
        public Long getFoodEntryId() { return foodEntryId; }
        public String getFormattedMessage() { return formattedMessage; }
        public NutritionAnalysis getAnalysis() { return analysis; }
    }
} 