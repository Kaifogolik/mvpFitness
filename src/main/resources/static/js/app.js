// FitCoach AI - Telegram Mini App JavaScript
class FitCoachApp {
    constructor() {
        this.tg = window.Telegram.WebApp;
        this.apiBaseUrl = window.location.origin;
        this.currentUser = null;
        this.currentTab = 'dashboard';
        
        this.init();
    }

    init() {
        // Инициализация Telegram WebApp
        this.initTelegramWebApp();
        
        // Инициализация UI
        this.initUI();
        
        // Загрузка данных пользователя
        this.loadUserData();
        
        // Загрузка статистики
        this.loadDashboardData();
        
        console.log('🚀 FitCoach AI Mini App инициализирован');
    }

    initTelegramWebApp() {
        // Конфигурация Telegram WebApp
        if (window.Telegram && window.Telegram.WebApp) {
            this.tg.ready();
            this.tg.expand();
            
            // Настройка темы
            this.applyTelegramTheme();
            
            // Настройка главной кнопки
            this.tg.MainButton.setText('Добавить питание');
            this.tg.MainButton.onClick(() => this.addFood());
            
            // Получение данных пользователя Telegram
            const user = this.tg.initDataUnsafe?.user;
            if (user) {
                this.telegramId = user.id.toString();
                this.currentUser = {
                    id: user.id,
                    firstName: user.first_name,
                    lastName: user.last_name,
                    username: user.username,
                    languageCode: user.language_code
                };
                
                console.log('👤 Пользователь Telegram:', this.currentUser);
            } else {
                // Fallback для тестирования в браузере
                this.telegramId = 'test_user';
                this.currentUser = {
                    id: 'test_user',
                    firstName: 'Test',
                    lastName: 'User',
                    username: 'test_user',
                    languageCode: 'ru'
                };
                console.log('🧪 Тестовый режим - используем test_user');
            }
        } else {
            // Полный fallback для браузера без Telegram
            this.telegramId = 'test_user';
            this.currentUser = {
                id: 'test_user',
                firstName: 'Test',
                lastName: 'User',
                username: 'test_user',
                languageCode: 'ru'
            };
            console.log('🌐 Браузерный режим - используем test_user');
        }
    }

    applyTelegramTheme() {
        // Применение цветовой схемы Telegram
        const root = document.documentElement;
        
        if (this.tg.colorScheme === 'dark') {
            root.style.setProperty('--tg-theme-bg-color', this.tg.themeParams.bg_color || '#1a1a1a');
            root.style.setProperty('--tg-theme-text-color', this.tg.themeParams.text_color || '#ffffff');
            root.style.setProperty('--tg-theme-secondary-bg-color', this.tg.themeParams.secondary_bg_color || '#2a2a2a');
            root.style.setProperty('--tg-theme-hint-color', this.tg.themeParams.hint_color || '#8e8e93');
        }
    }

    initUI() {
        // Инициализация навигации
        this.initTabNavigation();
        
        // Инициализация обработчиков событий
        this.initEventHandlers();
        
        // Обновление информации о пользователе в UI
        this.updateUserUI();
    }

    initTabNavigation() {
        const navTabs = document.querySelectorAll('.nav-tab');
        const tabContents = document.querySelectorAll('.tab-content');

        navTabs.forEach(tab => {
            tab.addEventListener('click', () => {
                const targetTab = tab.dataset.tab;
                
                // Удаляем активный класс со всех табов
                navTabs.forEach(t => t.classList.remove('active'));
                tabContents.forEach(content => content.classList.remove('active'));
                
                // Добавляем активный класс к выбранному табу
                tab.classList.add('active');
                document.getElementById(targetTab).classList.add('active');
                
                this.currentTab = targetTab;
                this.onTabChange(targetTab);
            });
        });
    }

    initEventHandlers() {
        // Обработчики для кнопок периода в Progress табе
        const periodBtns = document.querySelectorAll('.period-btn');
        periodBtns.forEach(btn => {
            btn.addEventListener('click', () => {
                periodBtns.forEach(b => b.classList.remove('active'));
                btn.classList.add('active');
                this.loadProgressData(btn.dataset.period);
            });
        });

        // Обработчик для обновления данных при pull-to-refresh
        if (this.tg.isVersionAtLeast('6.2')) {
            this.tg.onEvent('reloadPage', () => {
                this.refreshData();
            });
        }
    }

    updateUserUI() {
        if (this.currentUser) {
            // Обновляем имя пользователя
            const userNameElement = document.getElementById('userName');
            const profileNameElement = document.getElementById('profileName');
            const avatarTextElement = document.getElementById('avatarText');
            
            const fullName = `${this.currentUser.firstName} ${this.currentUser.lastName || ''}`.trim();
            
            if (userNameElement) userNameElement.textContent = fullName;
            if (profileNameElement) profileNameElement.textContent = fullName;
            
            // Обновляем аватар (первые буквы имени)
            if (avatarTextElement && this.currentUser.firstName) {
                const initials = this.currentUser.firstName.charAt(0) + 
                               (this.currentUser.lastName ? this.currentUser.lastName.charAt(0) : '');
                avatarTextElement.textContent = initials.toUpperCase();
            }
        }
    }

    onTabChange(tabName) {
        // Логика при смене таба
        switch(tabName) {
            case 'dashboard':
                this.loadDashboardData();
                this.tg.MainButton.setText('Добавить питание');
                this.tg.MainButton.show();
                break;
            case 'nutrition':
                this.loadNutritionData();
                this.tg.MainButton.setText('Добавить прием пищи');
                this.tg.MainButton.show();
                break;
            case 'progress':
                this.loadProgressData('week');
                this.tg.MainButton.hide();
                break;
            case 'profile':
                this.loadProfileData();
                this.tg.MainButton.hide();
                break;
        }
    }

    // API методы
    async apiCall(endpoint, method = 'GET', data = null) {
        const url = `${this.apiBaseUrl}${endpoint}`;
        const options = {
            method,
            headers: {
                'Content-Type': 'application/json',
            }
        };

        if (data) {
            options.body = JSON.stringify(data);
        }

        try {
            const response = await fetch(url, options);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return await response.json();
        } catch (error) {
            console.error('❌ API Error:', error);
            this.showError('Ошибка соединения с сервером');
            throw error;
        }
    }

    // Загрузка данных
    async loadUserData() {
        try {
            console.log(`👤 Загружаем профиль пользователя: ${this.telegramId}`);
            
            // Загружаем профиль пользователя из Mock API
            const userProfile = await this.apiCall(`/api/users/${this.telegramId}/profile`);
            
            if (userProfile.success) {
                this.currentUserProfile = userProfile.profile;
                this.updateProfileUI(userProfile);
                console.log('✅ Профиль пользователя загружен:', userProfile);
            } else {
                // Показываем форму создания профиля
                this.showProfileCreationForm();
                console.log('📝 Профиль не найден - показываем форму создания');
            }
        } catch (error) {
            console.error('❌ Ошибка загрузки профиля пользователя:', error);
            this.showError('Ошибка загрузки профиля пользователя');
        }
    }

    async loadDashboardData() {
        try {
            console.log(`📊 Загружаем статистику дашборда для: ${this.telegramId}`);
            
            // Загружаем дневную статистику из Mock API
            const dailyStats = await this.apiCall(`/api/nutrition/${this.telegramId}/daily`);
            
            // Загружаем недельную статистику из Mock API  
            const weeklyStats = await this.apiCall(`/api/nutrition/${this.telegramId}/weekly`);
            
            // Загружаем рекомендации из Mock API
            const recommendations = await this.apiCall(`/api/nutrition/${this.telegramId}/recommendations`);
            
            // Обновляем UI с реальными данными
            this.updateDashboardStats(dailyStats, weeklyStats, recommendations);
            
            console.log('✅ Статистика дашборда загружена');
        } catch (error) {
            console.error('❌ Ошибка загрузки данных дашборда:', error);
            this.showError('Ошибка загрузки статистики');
        }
    }

    updateDashboardUI(data) {
        // Обновляем значения в статистических карточках
        document.getElementById('todayCalories').textContent = data.todayCalories;
        document.getElementById('currentWeight').textContent = data.currentWeight;
        document.getElementById('dailyGoal').textContent = data.dailyGoal;
        document.getElementById('streakDays').textContent = data.streakDays;

        // Обновляем прогресс калорий в питании
        document.getElementById('consumedCalories').textContent = data.todayCalories;
        document.getElementById('remainingCalories').textContent = Math.max(0, data.dailyGoal - data.todayCalories);
    }

    updateProfileUI(userProfileData) {
        // Обновляем профиль пользователя в UI
        const profile = userProfileData.profile;
        const user = userProfileData.user;
        
        // Обновляем имя пользователя если есть данные от сервера
        if (user && user.firstName) {
            this.currentUser.firstName = user.firstName;
            this.currentUser.lastName = user.lastName;
            this.updateUserUI();
        }
        
        // Обновляем данные профиля
        if (profile) {
            const goalElement = document.getElementById('currentGoal');
            const weightElement = document.getElementById('currentWeight');
            const heightElement = document.getElementById('currentHeight');
            
            if (goalElement) goalElement.textContent = this.translateGoal(profile.goal);
            if (weightElement) weightElement.textContent = `${profile.currentWeight} кг`;
            if (heightElement) heightElement.textContent = `${profile.height} см`;
        }
        
        console.log('✅ UI профиля обновлен');
    }
    
    updateDashboardStats(dailyStats, weeklyStats, recommendations) {
        // Обновляем статистику дашборда с реальными данными
        if (dailyStats && dailyStats.success) {
            document.getElementById('todayCalories').textContent = Math.round(dailyStats.total_calories);
            document.getElementById('consumedCalories').textContent = Math.round(dailyStats.total_calories);
            document.getElementById('remainingCalories').textContent = Math.round(dailyStats.remaining_calories);
            document.getElementById('dailyGoal').textContent = dailyStats.goal_calories;
            
            // Обновляем БЖУ если есть элементы
            const proteinElement = document.getElementById('todayProtein'); 
            const carbsElement = document.getElementById('todayCarbs');
            const fatElement = document.getElementById('todayFat');
            
            if (proteinElement) proteinElement.textContent = `${Math.round(dailyStats.total_protein)}г`;
            if (carbsElement) carbsElement.textContent = `${Math.round(dailyStats.total_carbs)}г`;
            if (fatElement) fatElement.textContent = `${Math.round(dailyStats.total_fat)}г`;
        }
        
        if (weeklyStats && weeklyStats.success) {
            document.getElementById('weeklyAvgCalories').textContent = Math.round(weeklyStats.average_daily_calories);
            document.getElementById('streakDays').textContent = weeklyStats.days_tracked;
        }
        
        // Показываем рекомендации
        if (recommendations && recommendations.success) {
            this.displayRecommendations(recommendations.recommendations);
        }
        
        console.log('✅ Статистика дашборда обновлена');
    }
    
    showProfileCreationForm() {
        // Показываем форму создания профиля
        console.log('📝 Показываем форму создания профиля');
        
        // Переключаемся на таб Profile
        const profileTab = document.querySelector('[data-tab="profile"]');
        const profileContent = document.getElementById('profile');
        
        if (profileTab && profileContent) {
            // Активируем таб профиля  
            document.querySelectorAll('.nav-tab').forEach(tab => tab.classList.remove('active'));
            document.querySelectorAll('.tab-content').forEach(content => content.classList.remove('active'));
            
            profileTab.classList.add('active');
            profileContent.classList.add('active');
            
            // Показываем сообщение о необходимости заполнить профиль
            const messageDiv = document.createElement('div');
            messageDiv.className = 'profile-setup-message';
            messageDiv.innerHTML = `
                <h3>🎯 Настройка профиля</h3>
                <p>Для персональных рекомендаций заполните свой профиль</p>
            `;
            
            profileContent.insertBefore(messageDiv, profileContent.firstChild);
        }
    }
    
    showError(message) {
        // Показываем ошибку пользователю
        console.error('❌ Ошибка:', message);
        
        // Создаем уведомление об ошибке
        const errorDiv = document.createElement('div');
        errorDiv.className = 'error-notification';
        errorDiv.textContent = message;
        errorDiv.style.cssText = `
            position: fixed;
            top: 20px;
            left: 50%;
            transform: translateX(-50%);
            background: #ff4444;
            color: white;
            padding: 12px 20px;
            border-radius: 8px;
            z-index: 1000;
            font-size: 14px;
        `;
        
        document.body.appendChild(errorDiv);
        
        // Убираем уведомление через 3 секунды
        setTimeout(() => {
            if (errorDiv.parentNode) {
                errorDiv.parentNode.removeChild(errorDiv);
            }
        }, 3000);
    }
    
    displayRecommendations(recommendations) {
        // Отображаем рекомендации в UI
        const recommendationsContainer = document.getElementById('recommendationsList');
        if (recommendationsContainer && recommendations) {
            recommendationsContainer.innerHTML = recommendations
                .map(rec => `<div class="recommendation-item">${rec}</div>`)
                .join('');
        }
    }
    
    translateGoal(goal) {
        // Переводим цель на русский
        const goals = {
            'WEIGHT_LOSS': 'Похудение',
            'WEIGHT_GAIN': 'Набор массы', 
            'MAINTENANCE': 'Поддержание веса',
            'MUSCLE_GAIN': 'Набор мышечной массы'
        };
        return goals[goal] || goal;
    }

    async loadNutritionData() {
        // Загрузка данных о питании
        console.log('🍎 Загрузка данных о питании...');
        // TODO: Реализовать загрузку питания из API
    }

    async loadProgressData(period) {
        // Загрузка данных прогресса
        console.log(`📈 Загрузка прогресса за: ${period}`);
        // TODO: Реализовать загрузку прогресса из API
    }

    async loadProfileData() {
        // Загрузка данных профиля
        console.log('👤 Загрузка данных профиля...');
        
        // Обновляем статистику профиля
        document.getElementById('totalDays').textContent = Math.floor(Math.random() * 100) + 1;
        document.getElementById('totalMeals').textContent = Math.floor(Math.random() * 200);
        document.getElementById('totalWorkouts').textContent = Math.floor(Math.random() * 50);
    }

    // Утилиты
    showLoading() {
        document.getElementById('loadingOverlay').classList.add('show');
    }

    hideLoading() {
        document.getElementById('loadingOverlay').classList.remove('show');
    }

    showSuccess(message) {
        // Показываем уведомление об успехе
        if (this.tg.showAlert) {
            this.tg.showAlert(message);
        } else {
            alert(message);
        }
    }

    async refreshData() {
        this.showLoading();
        try {
            await this.loadUserData();
            await this.loadDashboardData();
            this.showSuccess('Данные обновлены!');
        } catch (error) {
            this.showError('Ошибка обновления данных');
        } finally {
            this.hideLoading();
        }
    }

    // AI функции для Mini App
    async analyzePhotoFromGallery() {
        try {
            // Создаем скрытый input для выбора файла
            const input = document.createElement('input');
            input.type = 'file';
            input.accept = 'image/*';
            input.style.display = 'none';
            
            return new Promise((resolve, reject) => {
                input.onchange = async (event) => {
                    const file = event.target.files[0];
                    if (!file) {
                        reject(new Error('Файл не выбран'));
                        return;
                    }
                    
                    this.showLoading();
                    
                    try {
                        // Конвертируем в base64
                        const base64 = await this.fileToBase64(file);
                        
                        // Отправляем на анализ
                        const analysis = await this.apiCall('/api/ai/analyze-food-base64', 'POST', {
                            imageBase64: base64.split(',')[1], // Убираем data:image/jpeg;base64,
                            mealType: 'OTHER'
                        });
                        
                        if (analysis.success) {
                            this.showFoodAnalysisResult(analysis);
                            resolve(analysis);
                        } else {
                            throw new Error(analysis.message || 'Ошибка анализа');
                        }
                        
                    } catch (error) {
                        console.error('Ошибка анализа фото:', error);
                        this.showError('Не удалось проанализировать фото. Попробуйте через Telegram бота.');
                        reject(error);
                    } finally {
                        this.hideLoading();
                    }
                };
                
                // Запускаем выбор файла
                input.click();
            });
            
        } catch (error) {
            console.error('Ошибка выбора фото:', error);
            this.showError('Ошибка доступа к галерее');
        }
    }

    fileToBase64(file) {
        return new Promise((resolve, reject) => {
            const reader = new FileReader();
            reader.readAsDataURL(file);
            reader.onload = () => resolve(reader.result);
            reader.onerror = error => reject(error);
        });
    }

    showFoodAnalysisResult(analysis) {
        const foods = analysis.detectedFoods || [];
        const totalCalories = analysis.totalCalories || 0;
        
        let message = `🍽️ АНАЛИЗ ПИТАНИЯ\n\n`;
        message += `📊 Общие калории: ${totalCalories} ккал\n`;
        message += `🥄 Общие БЖУ: Б${analysis.totalProteins || 0}г, Ж${analysis.totalFats || 0}г, У${analysis.totalCarbs || 0}г\n\n`;
        
        if (foods.length > 0) {
            message += `🍎 ОБНАРУЖЕННЫЕ ПРОДУКТЫ:\n`;
            foods.forEach((food, index) => {
                message += `${index + 1}. ${food.name}\n`;
                message += `   Калории: ${food.calories} ккал\n`;
                message += `   БЖУ: Б${food.proteins}г, Ж${food.fats}г, У${food.carbs}г\n\n`;
            });
        }
        
        if (analysis.recommendations) {
            message += `💡 РЕКОМЕНДАЦИИ:\n${analysis.recommendations}`;
        }
        
        // Показываем результат
        if (this.tg.showAlert) {
            this.tg.showAlert(message);
        } else {
            alert(message);
        }
        
        // Обновляем калории на дашборде (демо)
        const currentCalories = parseInt(document.getElementById('todayCalories').textContent) || 0;
        const newCalories = currentCalories + totalCalories;
        document.getElementById('todayCalories').textContent = newCalories;
        document.getElementById('consumedCalories').textContent = newCalories;
        
        const dailyGoal = parseInt(document.getElementById('dailyGoal').textContent) || 2000;
        const remaining = Math.max(0, dailyGoal - newCalories);
        document.getElementById('remainingCalories').textContent = remaining;
    }

    async askAIQuestion(question = null) {
        try {
            // Если вопрос не передан, спрашиваем у пользователя
            let userQuestion = question;
            if (!userQuestion) {
                if (this.tg.showPopup) {
                    // Используем Telegram WebApp popup
                    return new Promise((resolve) => {
                        this.tg.showPopup({
                            title: '🤖 AI Помощник',
                            message: 'Задайте вопрос о питании, тренировках или здоровье:',
                            buttons: [
                                {id: 'nutrition', type: 'default', text: '🍎 О питании'},
                                {id: 'workout', type: 'default', text: '💪 О тренировках'},
                                {id: 'custom', type: 'default', text: '✍️ Свой вопрос'},
                                {id: 'cancel', type: 'cancel', text: 'Отмена'}
                            ]
                        }, (buttonId) => {
                            if (buttonId === 'nutrition') {
                                this.askAIQuestion('Дай совет по здоровому питанию для поддержания формы');
                            } else if (buttonId === 'workout') {
                                this.askAIQuestion('Какие упражнения лучше делать для общей физической формы?');
                            } else if (buttonId === 'custom') {
                                const customQuestion = prompt('🤖 Задайте ваш вопрос:');
                                if (customQuestion) {
                                    this.askAIQuestion(customQuestion);
                                }
                            }
                            resolve();
                        });
                    });
                } else {
                    // Fallback для обычных браузеров
                    userQuestion = prompt('🤖 Задайте вопрос AI помощнику:');
                    if (!userQuestion) return;
                }
            }
            
            this.showLoading();
            
            // Отправляем вопрос AI
            const response = await this.apiCall('/api/ai/chat', 'POST', {
                message: userQuestion,
                userId: this.currentUser?.id || 'webapp_user'
            });
            
            this.hideLoading();
            
            if (response.success) {
                // Показываем ответ AI
                const aiMessage = `🤖 AI ПОМОЩНИК\n\n❓ Ваш вопрос:\n${userQuestion}\n\n💡 Ответ:\n${response.response}`;
                
                if (this.tg.showAlert) {
                    this.tg.showAlert(aiMessage);
                } else {
                    alert(aiMessage);
                }
            } else {
                throw new Error(response.message || 'Ошибка AI чата');
            }
            
        } catch (error) {
            this.hideLoading();
            console.error('AI Chat Error:', error);
            this.showError('Не удалось получить ответ от AI. Попробуйте через Telegram бота.');
        }
    }

    // Добавление питания через фото - метод класса
    async addFood() {
        try {
            // Создаем input для файла
            const input = document.createElement('input');
            input.type = 'file';
            input.accept = 'image/*';
            input.capture = 'environment'; // Предпочтение камере
            
            input.onchange = async (event) => {
                const file = event.target.files[0];
                if (!file) return;
                
                this.showLoading('Анализируем изображение...');
                
                try {
                    // Анализируем фото через реальный API
                    const result = await this.analyzeFood(file);
                    
                    this.hideLoading();
                    
                    if (result.success && result.analysis) {
                        console.log('✅ Анализ завершен:', result.analysis);
                        this.showFoodAnalysis(result.analysis);
                        
                        // Обновляем статистику после добавления
                        await this.loadDashboardData();
                    } else {
                        throw new Error(result.message || 'Не удалось проанализировать изображение');
                    }
                } catch (error) {
                    this.hideLoading();
                    console.error('❌ Ошибка анализа:', error);
                    this.showError('Ошибка анализа изображения: ' + error.message);
                }
            };
            
            // Показываем выбор файла
            input.click();
            
        } catch (error) {
            console.error('❌ Ошибка добавления питания:', error);
            this.showError('Не удалось открыть камеру');
        }
    }

    // Анализ фото еды через API
    async analyzeFood(file) {
        const formData = new FormData();
        formData.append('image', file);
        formData.append('telegramId', this.telegramId);

        const response = await fetch(`${this.apiBaseUrl}/api/ai/analyze-food-photo`, {
            method: 'POST',
            body: formData
        });

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }

        return await response.json();
    }

    // Чат с персональным тренером
    async sendChatMessage(message) {
        try {
            console.log('💬 Отправляем сообщение тренеру:', message);
            
            const response = await fetch(`${this.apiBaseUrl}/api/ai/chat`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    message: message,
                    telegramId: this.telegramId
                })
            });

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            const result = await response.json();
            
            if (result.success) {
                console.log('✅ Ответ тренера получен:', result.response);
                return result.response;
            } else {
                throw new Error(result.message || 'Не удалось получить ответ от тренера');
            }
        } catch (error) {
            console.error('❌ Ошибка чата:', error);
            throw error;
        }
    }
}

// Глобальные функции для HTML
window.addFood = async function() {
    // Используем метод класса для анализа фото
    if (window.app) {
        await window.app.addFood();
    } else {
        console.error('❌ App не инициализирован');
        alert('Ошибка: приложение не готово');
    }
};

window.addWeight = function() {
    const weight = prompt('Введите ваш текущий вес (кг):');
    if (weight && !isNaN(weight)) {
        document.getElementById('currentWeight').textContent = `${weight} кг`;
        app.showSuccess(`Вес ${weight} кг записан!`);
    }
};

window.addWorkout = function() {
    app.showSuccess('Функция записи тренировок будет добавлена в следующей версии');
    
    // Обновляем счетчик тренировок (демо)
    const workoutsElement = document.getElementById('totalWorkouts');
    if (workoutsElement) {
        const current = parseInt(workoutsElement.textContent) || 0;
        workoutsElement.textContent = current + 1;
    }
};

window.aiChat = async function() {
    app.showLoading();
    
    try {
        // Проверяем доступность AI чата
        const aiStatus = await app.apiCall('/api/ai/status');
        
        if (!aiStatus.success || !aiStatus.features.ai_chat) {
            app.hideLoading();
            app.showError('AI чат временно недоступен');
            return;
        }
        
        app.hideLoading();
        
        // Предлагаем выбор способа чата
        if (app.tg.showConfirm) {
            app.tg.showConfirm(
                '🤖 Как пообщаться с AI помощником?\n\n💬 Здесь - быстрый вопрос прямо в приложении\n📱 Через бота - полноценный чат в @mvpfitness_bot\n\nВыбрать "Здесь"?',
                async (chatHere) => {
                    if (chatHere) {
                        // Чат прямо в Mini App
                        try {
                            await app.askAIQuestion();
                        } catch (error) {
                            console.error('Mini App chat error:', error);
                        }
                    } else {
                        // Переход к Telegram боту
                        app.tg.showAlert(`
🤖 AI Чат через Telegram бота

1. Откройте @mvpfitness_bot
2. Нажмите "🤖 AI Чат" или просто напишите вопрос
3. Получите экспертный совет по:
   • Питанию и диетам
   • Тренировкам  
   • Здоровому образу жизни
   • Анализу калорий

Полноценный чат с историей сообщений!
                        `);
                        
                        if (app.tg.openTelegramLink) {
                            app.tg.openTelegramLink('https://t.me/mvpfitness_bot?start=chat');
                        }
                    }
                }
            );
        } else {
            // Fallback для браузеров без Telegram WebApp
            const chatHere = confirm('🤖 Задать вопрос здесь? (Нет = перейти к Telegram боту)');
            
            if (chatHere) {
                try {
                    await app.askAIQuestion();
                } catch (error) {
                    console.error('Mini App chat error:', error);
                }
            } else {
                alert('Перейдите к @mvpfitness_bot для полноценного AI чата');
                window.open('https://t.me/mvpfitness_bot', '_blank');
            }
        }
        
    } catch (error) {
        app.hideLoading();
        app.showError('Ошибка подключения к AI чату');
        console.error('AI Chat Error:', error);
    }
};

window.editProfile = function() {
    app.showSuccess('Редактирование профиля будет добавлено в следующей версии');
};

window.becomeCoach = function() {
    if (app.tg.showConfirm) {
        app.tg.showConfirm('Хотите стать тренером? Это даст вам возможность получать доход от учеников.', (confirmed) => {
            if (confirmed) {
                document.getElementById('userStatus').textContent = 'Coach';
                document.getElementById('profileRole').textContent = 'Тренер';
                app.showSuccess('Поздравляем! Теперь вы тренер!');
            }
        });
    } else {
        const confirmed = confirm('Хотите стать тренером?');
        if (confirmed) {
            document.getElementById('userStatus').textContent = 'Coach';
            document.getElementById('profileRole').textContent = 'Тренер';
            app.showSuccess('Поздравляем! Теперь вы тренер!');
        }
    }
};

window.shareApp = function() {
    const shareUrl = 'https://t.me/mvpfitness_bot';
    const shareText = '🤖 Попробуйте FitCoach AI - умного фитнес-помощника с анализом питания!';
    
    if (app.tg.openTelegramLink) {
        app.tg.openTelegramLink(`https://t.me/share/url?url=${encodeURIComponent(shareUrl)}&text=${encodeURIComponent(shareText)}`);
    } else {
        navigator.share({
            title: 'FitCoach AI',
            text: shareText,
            url: shareUrl
        }).catch(() => {
            // Fallback для браузеров без поддержки Web Share API
            const url = `https://t.me/share/url?url=${encodeURIComponent(shareUrl)}&text=${encodeURIComponent(shareText)}`;
            window.open(url, '_blank');
        });
    }
};

window.logout = function() {
    if (app.tg.showConfirm) {
        app.tg.showConfirm('Вы уверены, что хотите выйти?', (confirmed) => {
            if (confirmed) {
                app.tg.close();
            }
        });
    } else {
        const confirmed = confirm('Вы уверены, что хотите выйти?');
        if (confirmed) {
            window.close();
        }
    }
};

// Обработка ошибок
window.addEventListener('error', (event) => {
    console.error('🚨 Глобальная ошибка:', event.error);
});

// Обработка промисов с ошибками
window.addEventListener('unhandledrejection', (event) => {
    console.error('🚨 Необработанная ошибка промиса:', event.reason);
    event.preventDefault();
}); 

// Инициализация приложения
let app;
document.addEventListener('DOMContentLoaded', () => {
    console.log('🚀 Инициализация FitCoach AI Mini App...');
    app = new FitCoachApp();
    window.app = app; // Делаем доступным глобально для HTML функций
});