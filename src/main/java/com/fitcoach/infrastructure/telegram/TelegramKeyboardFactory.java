package com.fitcoach.infrastructure.telegram;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Component
public class TelegramKeyboardFactory {
    
    /**
     * Создает основную клавиатуру меню
     */
    public ReplyKeyboardMarkup createMainMenuKeyboard() {
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setResizeKeyboard(true);
        keyboard.setOneTimeKeyboard(false);
        
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        
        // Первая строка - основные функции
        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("📸 Анализ еды"));
        row1.add(new KeyboardButton("📊 Статистика"));
        keyboardRows.add(row1);
        
        // Вторая строка - профиль и тренер
        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton("👤 Профиль"));
        row2.add(new KeyboardButton("👨‍🏫 Тренер"));
        keyboardRows.add(row2);
        
        // Третья строка - помощь
        KeyboardRow row3 = new KeyboardRow();
        row3.add(new KeyboardButton("❓ Помощь"));
        keyboardRows.add(row3);
        
        keyboard.setKeyboard(keyboardRows);
        return keyboard;
    }
    
    /**
     * Создает клавиатуру для тренеров
     */
    public ReplyKeyboardMarkup createCoachKeyboard() {
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setResizeKeyboard(true);
        keyboard.setOneTimeKeyboard(false);
        
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        
        // Первая строка - ученики и доходы
        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("👥 Мои ученики"));
        row1.add(new KeyboardButton("💰 Доходы"));
        keyboardRows.add(row1);
        
        // Вторая строка - аналитика и реклама
        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton("📈 Аналитика"));
        row2.add(new KeyboardButton("🔗 Реф. ссылка"));
        keyboardRows.add(row2);
        
        // Третья строка - обычные функции
        KeyboardRow row3 = new KeyboardRow();
        row3.add(new KeyboardButton("📸 Анализ еды"));
        row3.add(new KeyboardButton("👤 Профиль"));
        keyboardRows.add(row3);
        
        // Четвертая строка - помощь
        KeyboardRow row4 = new KeyboardRow();
        row4.add(new KeyboardButton("❓ Помощь"));
        keyboardRows.add(row4);
        
        keyboard.setKeyboard(keyboardRows);
        return keyboard;
    }
    
    /**
     * Создает клавиатуру для выбора типа приема пищи
     */
    public ReplyKeyboardMarkup createMealTypeKeyboard() {
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setResizeKeyboard(true);
        keyboard.setOneTimeKeyboard(true);
        
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        
        // Первая строка - основные приемы пищи
        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("🌅 Завтрак"));
        row1.add(new KeyboardButton("🌞 Обед"));
        row1.add(new KeyboardButton("🌙 Ужин"));
        keyboardRows.add(row1);
        
        // Вторая строка - перекусы и тренировки
        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton("🍎 Перекус"));
        row2.add(new KeyboardButton("💪 До тренировки"));
        keyboardRows.add(row2);
        
        // Третья строка
        KeyboardRow row3 = new KeyboardRow();
        row3.add(new KeyboardButton("🏋️ После тренировки"));
        keyboardRows.add(row3);
        
        keyboard.setKeyboard(keyboardRows);
        return keyboard;
    }
    
    /**
     * Создает минимальную клавиатуру только с кнопкой "Назад"
     */
    public ReplyKeyboardMarkup createBackKeyboard() {
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setResizeKeyboard(true);
        keyboard.setOneTimeKeyboard(false);
        
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        
        KeyboardRow row = new KeyboardRow();
        row.add(new KeyboardButton("⬅️ Назад"));
        keyboardRows.add(row);
        
        keyboard.setKeyboard(keyboardRows);
        return keyboard;
    }
    
    /**
     * Удаляет клавиатуру (показывает стандартную клавиатуру телефона)
     */
    public ReplyKeyboardMarkup removeKeyboard() {
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setRemoveKeyboard(true);
        return keyboard;
    }
} 