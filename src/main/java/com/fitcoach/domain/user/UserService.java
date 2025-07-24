package com.fitcoach.domain.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    private final UserRepository userRepository;
    
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    /**
     * Находит пользователя по Telegram ID
     */
    public Optional<com.fitcoach.domain.user.User> findByTelegramId(String telegramId) {
        return userRepository.findByTelegramId(telegramId);
    }
    
    /**
     * Создает нового пользователя из Telegram данных
     */
    public com.fitcoach.domain.user.User createFromTelegram(User telegramUser) {
        logger.info("Creating new user from Telegram: {} {}", 
                   telegramUser.getFirstName(), telegramUser.getId());
        
        com.fitcoach.domain.user.User user = new com.fitcoach.domain.user.User();
        user.setTelegramId(telegramUser.getId().toString());
        user.setFirstName(telegramUser.getFirstName());
        user.setLastName(telegramUser.getLastName());
        user.setPlatform(Platform.TELEGRAM);
        user.setRole(UserRole.STUDENT);
        user.setStatus(UserStatus.ACTIVE);
        
        return userRepository.save(user);
    }
    
    /**
     * Обновляет профиль пользователя
     */
    public User updateProfile(Long userId, UserProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        
        if (request.getAge() != null) {
            user.setAge(request.getAge());
        }
        
        if (request.getHeightCm() != null) {
            user.setHeightCm(request.getHeightCm());
        }
        
        if (request.getWeightKg() != null) {
            user.setWeightKg(request.getWeightKg());
        }
        
        if (request.getTargetWeightKg() != null) {
            user.setTargetWeightKg(request.getTargetWeightKg());
        }
        
        if (request.getActivityLevel() != null) {
            user.setActivityLevel(request.getActivityLevel());
        }
        
        if (request.getFitnessGoal() != null) {
            user.setFitnessGoal(request.getFitnessGoal());
        }
        
        return userRepository.save(user);
    }
    
    /**
     * Делает пользователя тренером
     */
    public User promoteToCoach(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        
        if (user.isCoach()) {
            throw new RuntimeException("Пользователь уже является тренером");
        }
        
        user.setRole(UserRole.COACH);
        user.setCoachReferralCode(generateReferralCode());
        
        logger.info("User {} promoted to coach with referral code: {}", 
                   userId, user.getCoachReferralCode());
        
        return userRepository.save(user);
    }
    
    /**
     * Присваивает тренера ученику по реферальному коду
     */
    public User assignCoach(Long studentId, String referralCode) {
        User student = userRepository.findById(studentId)
            .orElseThrow(() -> new RuntimeException("Ученик не найден"));
        
        User coach = userRepository.findByCoachReferralCode(referralCode)
            .orElseThrow(() -> new RuntimeException("Тренер с таким кодом не найден"));
        
        if (!coach.isCoach()) {
            throw new RuntimeException("Пользователь не является тренером");
        }
        
        if (student.isCoach()) {
            throw new RuntimeException("Тренер не может быть учеником другого тренера");
        }
        
        student.setCoach(coach);
        student.setCoachReferralCode(referralCode);
        
        logger.info("Student {} assigned to coach {}", studentId, coach.getId());
        
        return userRepository.save(student);
    }
    
    /**
     * Обновляет подписку пользователя
     */
    public User updateSubscription(Long userId, SubscriptionType subscriptionType, int durationDays) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        
        user.setSubscriptionType(subscriptionType);
        
        if (subscriptionType != SubscriptionType.FREE) {
            LocalDateTime endDate = LocalDateTime.now().plusDays(durationDays);
            user.setSubscriptionEndDate(endDate);
        } else {
            user.setSubscriptionEndDate(null);
        }
        
        logger.info("User {} subscription updated to {} until {}", 
                   userId, subscriptionType, user.getSubscriptionEndDate());
        
        return userRepository.save(user);
    }
    
    /**
     * Получает учеников тренера
     */
    @Transactional(readOnly = true)
    public List<User> getCoachStudents(Long coachId) {
        return userRepository.findActiveStudentsByCoach(coachId);
    }
    
    /**
     * Получает активных тренеров
     */
    @Transactional(readOnly = true)
    public List<User> getActiveCoaches() {
        return userRepository.findActiveUsersByRole(UserRole.COACH);
    }
    
    /**
     * Поиск пользователей с фильтрами
     */
    @Transactional(readOnly = true)
    public Page<User> searchUsers(String name, UserRole role, UserStatus status, Pageable pageable) {
        return userRepository.searchUsers(name, role, status, pageable);
    }
    
    /**
     * Находит пользователей с истекающими подписками
     */
    @Transactional(readOnly = true)
    public List<User> findUsersWithExpiringSubscriptions(int daysFromNow) {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(daysFromNow);
        
        return userRepository.findUsersWithExpiringSubscriptions(start, end);
    }
    
    /**
     * Расчет базового метаболизма пользователя
     */
    public int calculateBasalMetabolicRate(User user) {
        if (user.getAge() == null || user.getHeightCm() == null || user.getWeightKg() == null) {
            throw new RuntimeException("Недостаточно данных для расчета BMR");
        }
        
        // Формула Mifflin-St Jeor
        // Мужчины: BMR = 10 * вес(кг) + 6.25 * рост(см) - 5 * возраст(лет) + 5
        // Женщины: BMR = 10 * вес(кг) + 6.25 * рост(см) - 5 * возраст(лет) - 161
        // Пока используем среднее значение, так как у нас нет поля пола
        
        double bmr = 10 * user.getWeightKg() + 6.25 * user.getHeightCm() - 5 * user.getAge() - 78;
        
        return (int) bmr;
    }
    
    /**
     * Расчет дневной потребности в калориях с учетом активности
     */
    public int calculateDailyCalorieNeeds(User user) {
        int bmr = calculateBasalMetabolicRate(user);
        
        if (user.getActivityLevel() == null) {
            return bmr; // Возвращаем базовый метаболизм если уровень активности не указан
        }
        
        double totalCalories = bmr * user.getActivityLevel().getMultiplier();
        
        // Корректируем на цель
        if (user.getFitnessGoal() != null) {
            totalCalories += user.getFitnessGoal().getCalorieAdjustment();
        }
        
        return (int) totalCalories;
    }
    
    /**
     * Получает статистику пользователей по платформам
     */
    @Transactional(readOnly = true)
    public List<Object[]> getUserStatsByPlatform() {
        return userRepository.countUsersByPlatform();
    }
    
    /**
     * Деактивирует неактивных пользователей
     */
    public int deactivateInactiveUsers(int daysSinceLastActivity) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(daysSinceLastActivity);
        List<User> inactiveUsers = userRepository.findInactiveUsers(threshold);
        
        int deactivated = 0;
        for (User user : inactiveUsers) {
            user.setStatus(UserStatus.INACTIVE);
            userRepository.save(user);
            deactivated++;
        }
        
        logger.info("Deactivated {} inactive users", deactivated);
        return deactivated;
    }
    
    // Private helper methods
    
    private String generateReferralCode() {
        String code;
        do {
            code = "FC" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (userRepository.existsByCoachReferralCode(code));
        
        return code;
    }
    
    // Inner classes for requests
    
    public static class UserProfileUpdateRequest {
        private Integer age;
        private Integer heightCm;
        private Integer weightKg;
        private Integer targetWeightKg;
        private ActivityLevel activityLevel;
        private FitnessGoal fitnessGoal;
        
        // Getters and Setters
        public Integer getAge() { return age; }
        public void setAge(Integer age) { this.age = age; }
        
        public Integer getHeightCm() { return heightCm; }
        public void setHeightCm(Integer heightCm) { this.heightCm = heightCm; }
        
        public Integer getWeightKg() { return weightKg; }
        public void setWeightKg(Integer weightKg) { this.weightKg = weightKg; }
        
        public Integer getTargetWeightKg() { return targetWeightKg; }
        public void setTargetWeightKg(Integer targetWeightKg) { this.targetWeightKg = targetWeightKg; }
        
        public ActivityLevel getActivityLevel() { return activityLevel; }
        public void setActivityLevel(ActivityLevel activityLevel) { this.activityLevel = activityLevel; }
        
        public FitnessGoal getFitnessGoal() { return fitnessGoal; }
        public void setFitnessGoal(FitnessGoal fitnessGoal) { this.fitnessGoal = fitnessGoal; }
    }
} 