-- Создание таблиц для FitCoach AI Platform

-- Таблица пользователей
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    telegram_id VARCHAR(255) UNIQUE NOT NULL,
    username VARCHAR(255) NOT NULL,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_active_at TIMESTAMP
);

-- Таблица профилей пользователей
CREATE TABLE IF NOT EXISTS user_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    height INTEGER,
    weight DECIMAL(5,2),
    age INTEGER,
    gender VARCHAR(10),
    activity_level VARCHAR(20),
    fitness_goal VARCHAR(20),
    daily_calories_goal DECIMAL(7,2),
    daily_proteins_goal DECIMAL(6,2),
    daily_fats_goal DECIMAL(6,2),
    daily_carbs_goal DECIMAL(6,2),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id)
);

-- Таблица записей о питании
CREATE TABLE IF NOT EXISTS nutrition_entries (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    food_name VARCHAR(255) NOT NULL,
    quantity VARCHAR(100),
    calories DECIMAL(8,2) NOT NULL DEFAULT 0,
    proteins DECIMAL(6,2) NOT NULL DEFAULT 0,
    fats DECIMAL(6,2) NOT NULL DEFAULT 0,
    carbs DECIMAL(6,2) NOT NULL DEFAULT 0,
    confidence DECIMAL(4,2),
    notes TEXT,
    image_base64 TEXT,
    date DATE NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Индексы для производительности
CREATE INDEX IF NOT EXISTS idx_users_telegram_id ON users(telegram_id);
CREATE INDEX IF NOT EXISTS idx_user_profiles_user_id ON user_profiles(user_id);
CREATE INDEX IF NOT EXISTS idx_nutrition_entries_user_id ON nutrition_entries(user_id);
CREATE INDEX IF NOT EXISTS idx_nutrition_entries_date ON nutrition_entries(date);
CREATE INDEX IF NOT EXISTS idx_nutrition_entries_user_date ON nutrition_entries(user_id, date);

-- Комментарии к таблицам
COMMENT ON TABLE users IS 'Пользователи системы из Telegram';
COMMENT ON TABLE user_profiles IS 'Профили пользователей с целями и параметрами';
COMMENT ON TABLE nutrition_entries IS 'Записи о питании пользователей';

-- Комментарии к важным полям
COMMENT ON COLUMN users.telegram_id IS 'Уникальный ID пользователя в Telegram';
COMMENT ON COLUMN user_profiles.fitness_goal IS 'Цель фитнеса: LOSE_WEIGHT, BUILD_MUSCLE, MAINTAIN_WEIGHT';
COMMENT ON COLUMN user_profiles.activity_level IS 'Уровень активности: SEDENTARY, LIGHT, MODERATE, ACTIVE, VERY_ACTIVE';
COMMENT ON COLUMN nutrition_entries.confidence IS 'Уверенность ИИ в распознавании еды (0.0-1.0)'; 