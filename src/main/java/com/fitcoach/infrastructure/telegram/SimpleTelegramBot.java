package com.fitcoach.infrastructure.telegram;

import com.fitcoach.infrastructure.ai.NutritionAnalysis;
import com.fitcoach.infrastructure.ai.OpenAIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;

@Component
public class SimpleTelegramBot extends TelegramLongPollingBot {
    
    private static final Logger logger = LoggerFactory.getLogger(SimpleTelegramBot.class);
    
    @Value("${telegram.bot.token}")
    private String botToken;
    
    @Value("${telegram.bot.username}")
    private String botUsername;
    
    @Value("${app.mini-app.url:http://localhost:8080}")
    private String miniAppUrl;
    
    @Autowired
    private OpenAIService openAIService;

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage()) {
                Message message = update.getMessage();
                long chatId = message.getChatId();
                String userName = message.getFrom().getFirstName();

                // Обработка фото
                if (message.hasPhoto()) {
                    handlePhotoMessage(message);
                    return;
                }

                // Обработка текстовых сообщений
                if (message.hasText()) {
                    handleTextMessage(chatId, message.getText(), userName);
                }
            }
        } catch (Exception e) {
            logger.error("Ошибка при обработке сообщения: {}", e.getMessage(), e);
        }
    }

    /**
     * Обработка фото сообщений - анализ еды через OpenAI
     */
    private void handlePhotoMessage(Message message) {
        try {
            String firstName = message.getFrom().getFirstName();
            logger.info("Получено фото от пользователя: {}", firstName);

            // Получаем самое большое фото (лучшее качество)
            List<PhotoSize> photos = message.getPhoto();
            PhotoSize photo = photos.get(photos.size() - 1);
            
            // Проверяем размер файла (ужесточили лимиты)
            if (photo.getFileSize() != null && photo.getFileSize() > 500 * 1024) { // 500KB вместо 1MB
                sendMessage(message.getChatId(), 
                    "⚠️ Изображение слишком большое (" + (photo.getFileSize() / 1024) + " КБ).\n\n" +
                    "📸 Для работы ИИ нужны компактные файлы до 500 КБ:\n" +
                    "• Сжать фото в галерее\n" +
                    "• Уменьшить разрешение камеры\n" +
                    "• Сфотографировать только еду без фона\n\n" +
                    "💡 Простые фото работают лучше!");
                return;
            }

            String fileId = photo.getFileId();
            
            // Отправляем сообщение о начале анализа
            sendMessage(message.getChatId(), "📸 Отлично! Анализирую ваше фото еды...\n⏳ Оптимизирую изображение для экономии токенов.");
            
            // Скачиваем файл и конвертируем в base64
            String imageBase64 = downloadAndConvertToBase64(fileId);
            
            if (imageBase64 != null) {
                // СУПЕР агрессивная проверка размера base64
                if (imageBase64.length() > 200_000) { // ~150KB оригинального изображения (экономия 75% токенов!)
                    sendMessage(message.getChatId(), 
                        "⚠️ Изображение все еще слишком детализированное для экономии токенов.\n\n" +
                        "📸 Нужны ОЧЕНЬ компактные изображения (до 150KB):\n" +
                        "• Максимально простое фото блюда\n" +
                        "• Без фона, рук, столовых приборов\n" +
                        "• Близко к еде, четкий фокус\n" +
                        "• Сжатие JPEG максимальное\n\n" +
                        "🔄 Используем базу данных продуктов для анализа...");
                }
                
                // Анализируем изображение (с fallback в случае ошибок)
                NutritionAnalysis analysis = openAIService.analyzeFoodImage(imageBase64);
                
                // Отправляем результат
                String response = formatNutritionAnalysis(analysis, firstName);
                sendMessage(message.getChatId(), response);
            } else {
                sendMessage(message.getChatId(), "❌ Не удалось скачать изображение. Попробуйте еще раз.");
            }
            
        } catch (Exception e) {
            logger.error("Ошибка при обработке фото", e);
            sendMessage(message.getChatId(), 
                "❌ Произошла ошибка при анализе фото.\n\n" +
                "🔄 Попробуйте:\n" +
                "• Отправить другое фото\n" +
                "• Описать блюдо текстом\n" +
                "• Обратиться к AI чату");
        }
        
        logger.info("Анализ фото завершен для пользователя: {}", message.getFrom().getFirstName());
    }

    /**
     * Обработка текстовых сообщений
     */
    private void handleTextMessage(long chatId, String messageText, String userName) {
        String responseText = "";

        switch (messageText) {
            case "📸 Анализ еды":
                responseText = "📸 Анализ фото еды с ИИ!\n\n" +
                             "🍎 Просто отправьте мне фото вашей еды,\n" +
                             "и я определю:\n" +
                             "• Все продукты на фото\n" +
                             "• Калории и БЖУ\n" +
                             "• Рекомендации по питанию\n\n" +
                             "📱 Сделайте фото и отправьте прямо в чат!";
                break;

            case "🤖 AI Чат":
                responseText = "🤖 AI Консультант по питанию!\n\n" +
                             "Задайте мне любой вопрос о:\n" +
                             "• Правильном питании\n" +
                             "• Подсчете калорий\n" +
                             "• Диетах и здоровье\n" +
                             "• Планировании рациона\n\n" +
                             "💬 Просто напишите свой вопрос!";
                break;

            case "📱 Открыть приложение":
                responseText = "📱 FitCoach AI приложение!\n\n" +
                             "🌐 Адрес: " + miniAppUrl + "\n\n" +
                             "💡 В приложении вы найдете:\n" +
                             "📊 Дашборд с вашей статистикой\n" +
                             "🍎 Трекинг питания и калорий\n" +
                             "📈 Прогресс и достижения\n" +
                             "👤 Персональный профиль\n\n" +
                             "⚠️ Пока доступен только локально\n" +
                             "🚀 Скоро будет доступен онлайн!";
                break;
                
            case "ℹ️ Помощь":
                responseText = "📋 Помощь по FitCoach AI:\n\n" +
                             "🤖 Возможности бота:\n" +
                             "📸 Анализ фото еды с ИИ\n" +
                             "💬 AI консультант по питанию\n" +
                             "🍎 Подсчет калорий и БЖУ\n" +
                             "📈 Рекомендации по здоровью\n\n" +
                             "Команды:\n" +
                             "📸 Анализ еды - отправьте фото\n" +
                             "🤖 AI Чат - задайте вопрос\n" +
                             "📱 Открыть приложение\n" +
                             "📊 Статус системы";
                break;
                
            case "📊 Статус":
                boolean openAiHealthy = openAIService.isApiHealthy();
                responseText = "✅ Статус системы FitCoach AI:\n\n" +
                             "🤖 Telegram Bot: Активен\n" +
                             "🗄️ База данных: H2 (в памяти)\n" +
                             "🔧 Spring Boot: Запущен\n" +
                             "📡 API: Доступен\n" +
                             "🧠 OpenAI API: " + (openAiHealthy ? "✅ Подключен" : "❌ Недоступен") + "\n\n" +
                             "Swagger UI: " + miniAppUrl + "/swagger-ui.html";
                break;
                
            case "ℹ️ О проекте":
                responseText = "🚀 FitCoach AI Platform\n\n" +
                             "Умная фитнес-платформа с ИИ-анализом питания\n" +
                             "и системой тренер-ученик.\n\n" +
                             "🔧 Версия: MVP 1.0 с AI\n" +
                             "🛠️ Технологии: Spring Boot + OpenAI GPT-4V\n" +
                             "📱 Платформа: Telegram Mini App\n\n" +
                             "🌟 Новые возможности:\n" +
                             "📸 Анализ фото еды с GPT-4V\n" +
                             "🤖 AI чат-консультант\n" +
                             "📊 Умные рекомендации\n\n" +
                             "GitHub: https://github.com/Kaifogolik/mvpFitness";
                break;

            case "/start":
                responseText = "🎯 Привет, " + userName + "!\n\n" +
                             "Добро пожаловать в FitCoach AI! 🤖\n\n" +
                             "🆕 Теперь с ИИ-анализом питания!\n\n" +
                             "🌟 Новые возможности:\n" +
                             "📸 Анализ фото еды с GPT-4V\n" +
                             "🤖 AI консультант по питанию\n" +
                             "📊 Умные рекомендации\n\n" +
                             "Выберите действие из меню ниже ⬇️";
                sendMessageWithKeyboard(chatId, responseText);
                return;
                
            case "/food":
                responseText = "📸 Отправьте фото вашей еды!\n\n" +
                             "🧠 ИИ определит:\n" +
                             "• Все продукты на фото\n" +
                             "• Калории и питательность\n" +
                             "• Рекомендации по здоровью\n\n" +
                             "📱 Просто сделайте фото и отправьте!";
                break;

            case "/chat":
                responseText = "🤖 Режим AI чата активирован!\n\n" +
                             "Задайте любой вопрос о питании, диетах,\n" +
                             "калориях или здоровом образе жизни.\n\n" +
                             "💬 Напишите ваш вопрос:";
                break;

            case "/help":
                responseText = "📋 Команды FitCoach AI:\n\n" +
                             "🆕 /food - Анализ фото еды\n" +
                             "🤖 /chat - AI консультант\n" +
                             "📱 /app - Открыть приложение\n" +
                             "📊 /status - Статус системы\n" +
                             "ℹ️ /about - О проекте\n\n" +
                             "📸 Или просто отправьте фото еды!";
                break;

            case "/app":
                responseText = "📱 FitCoach AI приложение!\n\n" +
                             "🌐 Адрес: " + miniAppUrl + "\n\n" +
                             "💡 Возможности веб-версии:\n" +
                             "📊 Дашборд с аналитикой\n" +
                             "🍎 История анализов питания\n" +
                             "📈 Графики прогресса\n" +
                             "👤 Персональный профиль\n\n" +
                             "⚠️ Пока localhost, скоро онлайн!";
                break;

            case "/about":
                responseText = "🚀 FitCoach AI Platform\n\n" +
                             "MVP фитнес-платформы с ИИ анализом.\n\n" +
                             "🔧 Версия: 1.0 + OpenAI\n" +
                             "🛠️ Stack: Spring Boot + GPT-4V\n" +
                             "📱 Интерфейс: Telegram + Mini App\n\n" +
                             "GitHub: https://github.com/Kaifogolik/mvpFitness";
                break;

            case "/status":
                boolean apiHealthy = openAIService.isApiHealthy();
                responseText = "✅ Статус FitCoach AI:\n\n" +
                             "🤖 Bot: Активен\n" +
                             "🔧 Backend: Запущен\n" +
                             "🧠 OpenAI: " + (apiHealthy ? "✅ OK" : "❌ Ошибка") + "\n" +
                             "📡 API: Доступен\n\n" +
                             "Docs: " + miniAppUrl + "/swagger-ui.html";
                break;

            default:
                // Если это не команда, пробуем обработать как вопрос к AI
                if (!messageText.startsWith("/")) {
                    handleAiChatMessage(chatId, messageText, userName);
                    return;
                } else {
                    responseText = "🤔 Неизвестная команда: " + messageText + "\n\n" +
                                 "Попробуйте:\n" +
                                 "/help - Список команд\n" +
                                 "/start - Главное меню\n" +
                                 "📸 Или отправьте фото еды!";
                }
                break;
        }

        sendMessage(chatId, responseText);
    }

    /**
     * Обработка сообщений для AI чата
     */
    private void handleAiChatMessage(long chatId, String messageText, String userName) {
        try {
            logger.info("AI чат запрос от {}: {}", userName, messageText);
            
            sendMessage(chatId, "🤖 Думаю над вашим вопросом...");
            
            String userContext = "Пользователь: " + userName + ", Платформа: Telegram";
            String aiResponse = openAIService.chatWithNutritionBot(messageText, userContext);
            
            String formattedResponse = "🤖 AI Консультант:\n\n" + aiResponse + 
                                     "\n\n💡 Есть еще вопросы? Просто напишите!";
            
            sendMessage(chatId, formattedResponse);
            
        } catch (Exception e) {
            logger.error("Ошибка в AI чате: {}", e.getMessage(), e);
            sendMessage(chatId, "🤖 Извините, сейчас я немного занят. Попробуйте позже или проверьте настройки AI.");
        }
    }

    /**
     * Скачивает фото и конвертирует в base64
     */
    private String downloadAndConvertToBase64(String fileId) {
        try {
            // Получаем информацию о файле
            GetFile getFile = new GetFile();
            getFile.setFileId(fileId);
            File file = execute(getFile);
            
            // Формируем URL для скачивания
            String fileUrl = "https://api.telegram.org/file/bot" + botToken + "/" + file.getFilePath();
            
            // Скачиваем и конвертируем в base64
            try (InputStream inputStream = new URL(fileUrl).openStream()) {
                byte[] imageBytes = inputStream.readAllBytes();
                return Base64.getEncoder().encodeToString(imageBytes);
            }
            
        } catch (TelegramApiException | IOException e) {
            logger.error("Ошибка при скачивании фото: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Форматирует результат анализа питания
     */
    private String formatNutritionAnalysis(NutritionAnalysis analysis, String userName) {
        StringBuilder result = new StringBuilder();
        
        result.append("🍽️ Анализ вашей еды, ").append(userName).append("!\n\n");
        
        // Общая информация
        result.append("📊 Общая питательность:\n");
        result.append("🔥 Калории: ").append(String.format("%.0f", analysis.getTotalCalories())).append(" ккал\n");
        result.append("🥩 Белки: ").append(String.format("%.1f", analysis.getTotalProteins())).append(" г\n");
        result.append("🧈 Жиры: ").append(String.format("%.1f", analysis.getTotalFats())).append(" г\n");
        result.append("🍞 Углеводы: ").append(String.format("%.1f", analysis.getTotalCarbs())).append(" г\n\n");
        
        // Обнаруженные продукты
        if (analysis.getDetectedFoods() != null && !analysis.getDetectedFoods().isEmpty()) {
            result.append("🔍 Обнаруженные продукты:\n");
            for (NutritionAnalysis.DetectedFood food : analysis.getDetectedFoods()) {
                result.append("• ").append(food.getFoodName())
                      .append(" (").append(food.getQuantity()).append(") - ")
                      .append(String.format("%.0f", food.getCalories())).append(" ккал\n");
            }
            result.append("\n");
        }
        
        // Уровень уверенности
        if (analysis.getConfidenceLevel() != null) {
            result.append("🎯 Точность анализа: ")
                  .append(String.format("%.0f", analysis.getConfidenceLevel() * 100))
                  .append("%\n\n");
        }
        
        // Заметки
        if (analysis.getAnalysisNotes() != null && !analysis.getAnalysisNotes().isEmpty()) {
            result.append("📝 Заметки: ").append(analysis.getAnalysisNotes()).append("\n\n");
        }
        
        // Рекомендации
        if (analysis.getHealthRecommendations() != null && !analysis.getHealthRecommendations().isEmpty()) {
            result.append("💡 Рекомендации:\n");
            for (String recommendation : analysis.getHealthRecommendations()) {
                if (recommendation != null && !recommendation.trim().isEmpty()) {
                    result.append("• ").append(recommendation.trim()).append("\n");
                }
            }
            result.append("\n");
        }
        
        result.append("📸 Отправьте еще фото для нового анализа!");
        
        return result.toString();
    }

    private void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            logger.error("Ошибка отправки сообщения: {}", e.getMessage(), e);
        }
    }
    
    private void sendMessageWithKeyboard(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        message.setReplyMarkup(createMainKeyboard());

        try {
            execute(message);
        } catch (TelegramApiException e) {
            logger.error("Ошибка отправки сообщения с клавиатурой: {}", e.getMessage(), e);
        }
    }
    
    private ReplyKeyboardMarkup createMainKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboardRows = new ArrayList<>();

        // Первая строка - AI функции
        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("📸 Анализ еды"));
        row1.add(new KeyboardButton("🤖 AI Чат"));

        // Вторая строка - приложение и помощь
        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton("📱 Открыть приложение"));
        row2.add(new KeyboardButton("ℹ️ Помощь"));

        // Третья строка - статус и о проекте
        KeyboardRow row3 = new KeyboardRow();
        row3.add(new KeyboardButton("📊 Статус"));
        row3.add(new KeyboardButton("ℹ️ О проекте"));

        keyboardRows.add(row1);
        keyboardRows.add(row2);
        keyboardRows.add(row3);
        keyboardMarkup.setKeyboard(keyboardRows);

        return keyboardMarkup;
    }
} 