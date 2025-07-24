package com.fitcoach.infrastructure.telegram;

import com.fitcoach.domain.user.User;
import com.fitcoach.domain.user.UserService;
import com.fitcoach.infrastructure.ai.OpenAIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.api.objects.webapp.WebApp;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class FitnessTelegramBot extends TelegramLongPollingBot {
    
    private static final Logger logger = LoggerFactory.getLogger(FitnessTelegramBot.class);
    
    private final UserService userService;
    private final OpenAIService openAIService;
    private final TelegramMessageHandler messageHandler;
    private final TelegramKeyboardFactory keyboardFactory;
    
    @Value("${telegram.bot.token}")
    private String botToken;
    
    @Value("${telegram.bot.username}")
    private String botUsername;
    
    @Value("${app.mini-app.url:https://t.me/fitcoach_bot/app}")
    private String miniAppUrl;
    
    public FitnessTelegramBot(UserService userService, 
                             OpenAIService openAIService,
                             TelegramMessageHandler messageHandler,
                             TelegramKeyboardFactory keyboardFactory) {
        this.userService = userService;
        this.openAIService = openAIService;
        this.messageHandler = messageHandler;
        this.keyboardFactory = keyboardFactory;
    }
    
    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage()) {
                handleMessage(update);
            } else if (update.hasCallbackQuery()) {
                handleCallbackQuery(update);
            }
        } catch (Exception e) {
            logger.error("Error processing update: {}", update, e);
            sendErrorMessage(getChatId(update), "Произошла ошибка. Попробуйте позже.");
        }
    }
    
    private void handleMessage(Update update) {
        String chatId = update.getMessage().getChatId().toString();
        String messageText = update.getMessage().getText();
        String telegramId = update.getMessage().getFrom().getId().toString();
        
        logger.info("Received message from {}: {}", telegramId, messageText);
        
        // Получаем или создаем пользователя
        User user = getOrCreateUser(update.getMessage().getFrom());
        
        if (messageText != null) {
            handleTextMessage(chatId, messageText, user);
        } else if (update.getMessage().hasPhoto()) {
            handlePhotoMessage(chatId, update, user);
        }
    }
    
    private void handleTextMessage(String chatId, String messageText, User user) {
        try {
            switch (messageText) {
                case "/start" -> handleStartCommand(chatId, user);
                case "/profile" -> handleProfileCommand(chatId, user);
                case "/food" -> handleFoodCommand(chatId, user);
                case "/stats" -> handleStatsCommand(chatId, user);
                case "/coach" -> handleCoachCommand(chatId, user);
                case "/help" -> handleHelpCommand(chatId);
                default -> handleDefaultMessage(chatId, messageText, user);
            }
        } catch (Exception e) {
            logger.error("Error handling text message", e);
            sendErrorMessage(chatId, "Не удалось обработать команду.");
        }
    }
    
    private void handleStartCommand(String chatId, User user) {
        String welcomeMessage = String.format(
            "Привет, %s! 👋\n\n" +
            "Я твой ИИ-помощник по фитнесу и питанию! 🤖\n\n" +
            "Что я умею:\n" +
            "📸 Анализировать фото еды и считать калории\n" +
            "📊 Вести статистику питания и тренировок\n" +
            "💪 Давать персональные рекомендации\n" +
            "👨‍🏫 Помогать тренерам работать с учениками\n\n" +
            "Для начала отправь мне фото своей еды или используй команды ниже:",
            user.getFirstName()
        );
        
        SendMessage message = SendMessage.builder()
            .chatId(chatId)
            .text(welcomeMessage)
            .replyMarkup(keyboardFactory.createMainMenuKeyboard())
            .build();
            
        executeMessage(message);
        
        // Если профиль не заполнен, предлагаем его заполнить
        if (user.getAge() == null || user.getHeightCm() == null) {
            sendProfileSetupPrompt(chatId);
        }
    }
    
    private void handleProfileCommand(String chatId, User user) {
        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
            .keyboardRow(List.of(
                InlineKeyboardButton.builder()
                    .text("📱 Открыть профиль")
                    .webApp(WebApp.builder().url(miniAppUrl + "?page=profile").build())
                    .build()
            ))
            .build();
            
        String profileText = messageHandler.formatUserProfile(user);
        
        SendMessage message = SendMessage.builder()
            .chatId(chatId)
            .text(profileText)
            .replyMarkup(keyboard)
            .build();
            
        executeMessage(message);
    }
    
    private void handleFoodCommand(String chatId, User user) {
        String message = "📸 Отправь мне фото своей еды, и я проанализирую её питательную ценность!\n\n" +
                        "Я смогу определить:\n" +
                        "• Калории\n" +
                        "• Белки, жиры, углеводы\n" +
                        "• Размер порции\n" +
                        "• Дать рекомендации\n\n" +
                        "Просто сделай фото и отправь мне! 📱";
                        
        SendMessage sendMessage = SendMessage.builder()
            .chatId(chatId)
            .text(message)
            .build();
            
        executeMessage(sendMessage);
    }
    
    private void handleStatsCommand(String chatId, User user) {
        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
            .keyboardRow(List.of(
                InlineKeyboardButton.builder()
                    .text("📊 Открыть статистику")
                    .webApp(WebApp.builder().url(miniAppUrl + "?page=stats").build())
                    .build()
            ))
            .keyboardRow(List.of(
                InlineKeyboardButton.builder()
                    .text("📈 Сегодня")
                    .callbackData("stats_today")
                    .build(),
                InlineKeyboardButton.builder()
                    .text("📅 За неделю")
                    .callbackData("stats_week")
                    .build()
            ))
            .build();
            
        SendMessage message = SendMessage.builder()
            .chatId(chatId)
            .text("📊 Выбери период для просмотра статистики:")
            .replyMarkup(keyboard)
            .build();
            
        executeMessage(message);
    }
    
    private void handleCoachCommand(String chatId, User user) {
        if (user.isCoach()) {
            handleCoachDashboard(chatId, user);
        } else {
            handleBecomeCoachPrompt(chatId, user);
        }
    }
    
    private void handleCoachDashboard(String chatId, User user) {
        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
            .keyboardRow(List.of(
                InlineKeyboardButton.builder()
                    .text("👥 Мои ученики")
                    .webApp(WebApp.builder().url(miniAppUrl + "?page=students").build())
                    .build()
            ))
            .keyboardRow(List.of(
                InlineKeyboardButton.builder()
                    .text("💰 Доходы")
                    .callbackData("coach_earnings")
                    .build(),
                InlineKeyboardButton.builder()
                    .text("🔗 Реф. ссылка")
                    .callbackData("coach_referral")
                    .build()
            ))
            .build();
            
        String coachStats = messageHandler.formatCoachStats(user);
        
        SendMessage message = SendMessage.builder()
            .chatId(chatId)
            .text("👨‍🏫 Панель тренера\n\n" + coachStats)
            .replyMarkup(keyboard)
            .build();
            
        executeMessage(message);
    }
    
    private void handleBecomeCoachPrompt(String chatId, User user) {
        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
            .keyboardRow(List.of(
                InlineKeyboardButton.builder()
                    .text("💪 Стать тренером")
                    .callbackData("become_coach")
                    .build()
            ))
            .keyboardRow(List.of(
                InlineKeyboardButton.builder()
                    .text("🔍 Найти тренера")
                    .callbackData("find_coach")
                    .build()
            ))
            .build();
            
        String message = "👨‍🏫 Хочешь стать тренером?\n\n" +
                        "Преимущества:\n" +
                        "💰 Получай 30% с каждого ученика\n" +
                        "📈 Увеличивай доход привлекая новых\n" +
                        "🎯 Помогай людям достигать целей\n" +
                        "📊 Удобные инструменты аналитики\n\n" +
                        "Или найди тренера для себя!";
                        
        SendMessage sendMessage = SendMessage.builder()
            .chatId(chatId)
            .text(message)
            .replyMarkup(keyboard)
            .build();
            
        executeMessage(sendMessage);
    }
    
    private void handleHelpCommand(String chatId) {
        String helpText = """
            🤖 FitCoach AI - твой персональный помощник по фитнесу
            
            📋 Доступные команды:
            /start - Главное меню
            /profile - Мой профиль  
            /food - Анализ питания
            /stats - Статистика
            /coach - Тренерская панель
            /help - Эта справка
            
            📸 Отправь фото еды для анализа калорий!
            
            💬 Задавай вопросы о питании и тренировках - я отвечу!
            """;
            
        SendMessage message = SendMessage.builder()
            .chatId(chatId)
            .text(helpText)
            .replyMarkup(keyboardFactory.createMainMenuKeyboard())
            .build();
            
        executeMessage(message);
    }
    
    private void handlePhotoMessage(String chatId, Update update, User user) {
        if (!user.canAnalyzeFood()) {
            sendSubscriptionPrompt(chatId);
            return;
        }
        
        // Отправляем сообщение о начале анализа
        SendMessage analyzing = SendMessage.builder()
            .chatId(chatId)
            .text("🔍 Анализирую фото... Это может занять несколько секунд.")
            .build();
            
        executeMessage(analyzing);
        
        // Обрабатываем фото асинхронно
        messageHandler.handleFoodPhotoAnalysis(chatId, update, user)
            .thenAccept(result -> {
                InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                    .keyboardRow(List.of(
                        InlineKeyboardButton.builder()
                            .text("📊 Подробнее")
                            .webApp(WebApp.builder()
                                .url(miniAppUrl + "?page=food&id=" + result.getFoodEntryId())
                                .build())
                            .build()
                    ))
                    .keyboardRow(List.of(
                        InlineKeyboardButton.builder()
                            .text("✏️ Исправить")
                            .callbackData("correct_" + result.getFoodEntryId())
                            .build(),
                        InlineKeyboardButton.builder()
                            .text("📸 Еще фото")
                            .callbackData("add_photo")
                            .build()
                    ))
                    .build();
                    
                SendMessage resultMessage = SendMessage.builder()
                    .chatId(chatId)
                    .text(result.getFormattedMessage())
                    .replyMarkup(keyboard)
                    .build();
                    
                executeMessage(resultMessage);
            })
            .exceptionally(throwable -> {
                logger.error("Error analyzing food photo", throwable);
                sendErrorMessage(chatId, "Не удалось проанализировать фото. Попробуйте другое изображение.");
                return null;
            });
    }
    
    private void handleDefaultMessage(String chatId, String messageText, User user) {
        // Обрабатываем как вопрос к ИИ-ассистенту
        messageHandler.handleAIChatMessage(chatId, messageText, user)
            .thenAccept(response -> {
                SendMessage message = SendMessage.builder()
                    .chatId(chatId)
                    .text(response)
                    .build();
                    
                executeMessage(message);
            })
            .exceptionally(throwable -> {
                logger.error("Error processing AI chat message", throwable);
                sendErrorMessage(chatId, "Не понял вопрос. Попробуйте переформулировать или используйте команды из меню.");
                return null;
            });
    }
    
    private void handleCallbackQuery(Update update) {
        String callbackData = update.getCallbackQuery().getData();
        String chatId = update.getCallbackQuery().getMessage().getChatId().toString();
        String telegramId = update.getCallbackQuery().getFrom().getId().toString();
        
        User user = userService.findByTelegramId(telegramId).orElse(null);
        if (user == null) {
            sendErrorMessage(chatId, "Пользователь не найден. Используйте /start");
            return;
        }
        
        messageHandler.handleCallbackQuery(callbackData, chatId, user);
    }
    
    private User getOrCreateUser(org.telegram.telegrambots.meta.api.objects.User telegramUser) {
        return userService.findByTelegramId(telegramUser.getId().toString())
            .orElseGet(() -> userService.createFromTelegram(telegramUser));
    }
    
    private void sendProfileSetupPrompt(String chatId) {
        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
            .keyboardRow(List.of(
                InlineKeyboardButton.builder()
                    .text("✏️ Заполнить профиль")
                    .webApp(WebApp.builder().url(miniAppUrl + "?page=setup").build())
                    .build()
            ))
            .build();
            
        SendMessage message = SendMessage.builder()
            .chatId(chatId)
            .text("👤 Для точных рекомендаций заполни свой профиль - это займет 2 минуты!")
            .replyMarkup(keyboard)
            .build();
            
        executeMessage(message);
    }
    
    private void sendSubscriptionPrompt(String chatId) {
        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
            .keyboardRow(List.of(
                InlineKeyboardButton.builder()
                    .text("⭐ Оформить подписку")
                    .callbackData("subscribe")
                    .build()
            ))
            .build();
            
        SendMessage message = SendMessage.builder()
            .chatId(chatId)
            .text("🔒 Анализ фото еды доступен только с подпиской.\n\n" +
                  "💎 Подписка включает:\n" +
                  "• Безлимитный анализ фото\n" +
                  "• Подсчет калорий и БЖУ\n" +
                  "• Персональные рекомендации\n" +
                  "• Статистика прогресса\n\n" +
                  "💰 Всего 490₽/месяц")
            .replyMarkup(keyboard)
            .build();
            
        executeMessage(message);
    }
    
    private void sendErrorMessage(String chatId, String errorText) {
        SendMessage message = SendMessage.builder()
            .chatId(chatId)
            .text("❌ " + errorText)
            .build();
            
        executeMessage(message);
    }
    
    private void executeMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            logger.error("Error sending message", e);
        }
    }
    
    private String getChatId(Update update) {
        if (update.hasMessage()) {
            return update.getMessage().getChatId().toString();
        } else if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getMessage().getChatId().toString();
        }
        return null;
    }
    
    @Override
    public String getBotUsername() {
        return botUsername;
    }
    
    @Override
    public String getBotToken() {
        return botToken;
    }
} 