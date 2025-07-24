package com.fitcoach.infrastructure.telegram;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
// import org.telegram.telegrambots.meta.api.objects.webapp.WebApp; // Может быть недоступен в текущей версии
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Component
public class SimpleTelegramBot extends TelegramLongPollingBot {
    
    @Value("${telegram.bot.token}")
    private String botToken;
    
    @Value("${telegram.bot.username}")
    private String botUsername;
    
    @Value("${app.mini-app.url:http://localhost:8080}")
    private String miniAppUrl;

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
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            String userName = update.getMessage().getFrom().getFirstName();

            String responseText = "";

            switch (messageText) {
                case "📱 Открыть приложение":
                    responseText = "📱 Открываем FitCoach AI приложение!\n\n" +
                                 "🔗 Нажмите кнопку ниже для перехода к приложению\n\n" +
                                 "💡 В приложении вы найдете:\n" +
                                 "📊 Дашборд с вашей статистикой\n" +
                                 "🍎 Трекинг питания и калорий\n" +
                                 "📈 Прогресс и достижения\n" +
                                 "👤 Персональный профиль";
                    sendMessageWithAppButton(chatId, responseText);
                    return;
                    
                case "ℹ️ Помощь":
                case "📊 Статус":
                case "ℹ️ О проекте":
                    // Обрабатываем как обычные текстовые команды
                    if (messageText.equals("ℹ️ Помощь")) messageText = "/help";
                    else if (messageText.equals("📊 Статус")) messageText = "/status";
                    else if (messageText.equals("ℹ️ О проекте")) messageText = "/about";
                    // Продолжаем в switch для обработки
                    break;
                case "/start":
                    responseText = "🎯 Привет, " + userName + "!\n\n" +
                                 "Добро пожаловать в FitCoach AI! 🤖\n\n" +
                                 "🚀 Ваш умный фитнес-помощник готов к работе!\n\n" +
                                 "📱 Нажмите кнопку ниже, чтобы открыть приложение,\n" +
                                 "или используйте команды:\n\n" +
                                 "/app - Открыть приложение\n" +
                                 "/help - Помощь\n" +
                                 "/about - О проекте\n" +
                                 "/status - Статус системы";
                    sendMessageWithKeyboard(chatId, responseText);
                    return;
                    
                case "/app":
                    responseText = "📱 Открываем FitCoach AI приложение!\n\n" +
                                 "🔗 Ссылка: " + miniAppUrl + "\n\n" +
                                 "💡 В приложении вы найдете:\n" +
                                 "📊 Дашборд с вашей статистикой\n" +
                                 "🍎 Трекинг питания и калорий\n" +
                                 "📈 Прогресс и достижения\n" +
                                 "👤 Персональный профиль";
                    sendMessageWithAppButton(chatId, responseText);
                    return;

                case "/help":
                    responseText = "📋 Помощь по FitCoach AI:\n\n" +
                                 "🤖 Это MVP версия фитнес-платформы\n" +
                                 "📸 Планируется: анализ фото еды\n" +
                                 "👨‍🏫 Планируется: система тренер-ученик\n" +
                                 "💰 Планируется: подписки\n\n" +
                                 "Команды:\n" +
                                 "/start - Начало работы\n" +
                                 "/about - О проекте\n" +
                                 "/status - Проверить статус";
                    break;

                case "/about":
                    responseText = "🚀 FitCoach AI Platform\n\n" +
                                 "Умная фитнес-платформа с ИИ-анализом питания\n" +
                                 "и системой тренер-ученик.\n\n" +
                                 "🔧 Версия: MVP 1.0\n" +
                                 "🛠️ Технологии: Spring Boot + OpenAI\n" +
                                 "📱 Платформа: Telegram Mini App\n\n" +
                                 "GitHub: https://github.com/Kaifogolik/mvpFitness";
                    break;

                case "/status":
                    responseText = "✅ Система работает!\n\n" +
                                 "🤖 Telegram Bot: Активен\n" +
                                 "🗄️ База данных: H2 (в памяти)\n" +
                                 "🔧 Spring Boot: Запущен\n" +
                                 "📡 API: Доступен\n\n" +
                                 "Swagger UI: http://localhost:8080/swagger-ui.html";
                    break;

                default:
                    responseText = "🤔 Не понял команду: " + messageText + "\n\n" +
                                 "Попробуйте:\n" +
                                 "/help - Список команд\n" +
                                 "/start - Начало работы";
                    break;
            }

            sendMessage(chatId, responseText);
        }
    }

    private void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            System.err.println("Ошибка отправки сообщения: " + e.getMessage());
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
            System.err.println("Ошибка отправки сообщения с клавиатурой: " + e.getMessage());
        }
    }
    
    private void sendMessageWithAppButton(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        message.setReplyMarkup(createAppInlineKeyboard());

        try {
            execute(message);
        } catch (TelegramApiException e) {
            System.err.println("Ошибка отправки сообщения с inline клавиатурой: " + e.getMessage());
        }
    }
    
    private ReplyKeyboardMarkup createMainKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboardRows = new ArrayList<>();

        // Первая строка - команды
        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("📱 Открыть приложение"));
        row1.add(new KeyboardButton("ℹ️ Помощь"));

        // Вторая строка - статус и о проекте
        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton("📊 Статус"));
        row2.add(new KeyboardButton("ℹ️ О проекте"));

        keyboardRows.add(row1);
        keyboardRows.add(row2);
        keyboardMarkup.setKeyboard(keyboardRows);

        return keyboardMarkup;
    }
    
    private InlineKeyboardMarkup createAppInlineKeyboard() {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        // Кнопка открытия приложения
        List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
        InlineKeyboardButton appButton = new InlineKeyboardButton();
        appButton.setText("🚀 Открыть FitCoach AI приложение");
        appButton.setUrl(miniAppUrl);
        rowInline1.add(appButton);

        // Кнопка поделиться
        List<InlineKeyboardButton> rowInline2 = new ArrayList<>();
        InlineKeyboardButton shareButton = new InlineKeyboardButton();
        shareButton.setText("📤 Поделиться с друзьями");
        shareButton.setUrl("https://t.me/share/url?url=" + miniAppUrl + "&text=🤖 Попробуйте FitCoach AI - умного фитнес-помощника!");
        rowInline2.add(shareButton);

        rowsInline.add(rowInline1);
        rowsInline.add(rowInline2);
        markupInline.setKeyboard(rowsInline);

        return markupInline;
    }
} 