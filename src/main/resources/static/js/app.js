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
            this.currentUser = {
                id: user.id,
                firstName: user.first_name,
                lastName: user.last_name,
                username: user.username,
                languageCode: user.language_code
            };
            
            console.log('👤 Пользователь Telegram:', this.currentUser);
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
            // Пока используем тестовые данные из API
            const health = await this.apiCall('/api/test/health');
            console.log('✅ Соединение с API установлено:', health);
        } catch (error) {
            console.error('❌ Ошибка загрузки данных пользователя:', error);
        }
    }

    async loadDashboardData() {
        try {
            // Загружаем статистику дашборда
            const info = await this.apiCall('/api/test/info');
            
            // Обновляем UI с тестовыми данными
            this.updateDashboardUI({
                todayCalories: Math.floor(Math.random() * 1500) + 500,
                currentWeight: '75.2 кг',
                dailyGoal: 2000,
                streakDays: Math.floor(Math.random() * 30) + 1
            });
            
            console.log('📊 Данные дашборда загружены');
        } catch (error) {
            console.error('❌ Ошибка загрузки данных дашборда:', error);
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

    showError(message) {
        // Показываем уведомление об ошибке через Telegram
        if (this.tg.showAlert) {
            this.tg.showAlert(message);
        } else {
            alert(message);
        }
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
}

// Глобальные функции для HTML
window.addFood = function() {
    app.showLoading();
    
    // Имитация добавления еды
    setTimeout(() => {
        app.hideLoading();
        app.showSuccess('Функция добавления еды будет доступна после интеграции с OpenAI API');
        
        // Обновляем калории (демо)
        const currentCalories = parseInt(document.getElementById('todayCalories').textContent) || 0;
        const newCalories = currentCalories + Math.floor(Math.random() * 300) + 100;
        document.getElementById('todayCalories').textContent = newCalories;
        document.getElementById('consumedCalories').textContent = newCalories;
        
        const remaining = Math.max(0, 2000 - newCalories);
        document.getElementById('remainingCalories').textContent = remaining;
    }, 1000);
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

window.aiChat = function() {
    app.showSuccess('AI Помощник будет доступен после интеграции с OpenAI GPT-4');
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

// Инициализация приложения
let app;
document.addEventListener('DOMContentLoaded', () => {
    app = new FitCoachApp();
});

// Обработка ошибок
window.addEventListener('error', (event) => {
    console.error('🚨 Глобальная ошибка:', event.error);
});

// Обработка промисов с ошибками
window.addEventListener('unhandledrejection', (event) => {
    console.error('🚨 Необработанная ошибка промиса:', event.reason);
    event.preventDefault();
}); 