package com.fitcoach.infrastructure.telegram;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class SimpleTelegramBot extends TelegramLongPollingBot {
    
    @Value("${telegram.bot.token}")
    private String botToken;
    
    @Value("${telegram.bot.username}")
    private String botUsername;

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
                case "/start":
                    responseText = "🎯 Привет, " + userName + "!\n\n" +
                                 "Добро пожаловать в FitCoach AI! 🤖\n\n" +
                                 "Доступные команды:\n" +
                                 "/help - Помощь\n" +
                                 "/about - О проекте\n" +
                                 "/status - Статус системы";
                    break;

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
} 