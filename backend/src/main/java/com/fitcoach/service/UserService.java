package com.fitcoach.service;

import com.fitcoach.model.User;
import com.fitcoach.model.UserProfile;
import com.fitcoach.repository.UserRepository;
import com.fitcoach.repository.UserProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Сервис для управления пользователями
 */
@Service
@Transactional
public class UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserProfileRepository userProfileRepository;
    
    /**
     * Найти или создать пользователя по Telegram данным
     */
    public User findOrCreateUser(String telegramId, String username, String firstName, String lastName) {
        Optional<User> existingUser = userRepository.findByTelegramId(telegramId);
        
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            // Обновляем информацию если изменилась
            boolean updated = false;
            
            if (!user.getUsername().equals(username)) {
                user.setUsername(username);
                updated = true;
            }
            
            if (firstName != null && !firstName.equals(user.getFirstName())) {
                user.setFirstName(firstName);
                updated = true;
            }
            
            if (lastName != null && !lastName.equals(user.getLastName())) {
                user.setLastName(lastName);
                updated = true;
            }
            
            // Обновляем время последней активности
            user.setLastActiveAt(LocalDateTime.now());
            
            if (updated) {
                user = userRepository.save(user);
                logger.info("📝 Обновлен пользователь: {} ({})", username, telegramId);
            }
            
            return user;
        } else {
            // Создаем нового пользователя
            User newUser = new User(telegramId, username);
            newUser.setFirstName(firstName);
            newUser.setLastName(lastName);
            newUser = userRepository.save(newUser);
            
            logger.info("👤 Зарегистрирован новый пользователь: {} ({})", username, telegramId);
            return newUser;
        }
    }
    
    /**
     * Обновить время последней активности
     */
    public void updateLastActiveTime(String telegramId) {
        userRepository.updateLastActiveTime(telegramId, LocalDateTime.now());
    }
    
    /**
     * Найти пользователя по Telegram ID
     */
    public Optional<User> findByTelegramId(String telegramId) {
        return userRepository.findByTelegramId(telegramId);
    }
    
    /**
     * Получить пользователя с профилем
     */
    public Optional<UserWithProfile> getUserWithProfile(String telegramId) {
        Optional<User> userOpt = userRepository.findByTelegramId(telegramId);
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }
        
        User user = userOpt.get();
        Optional<UserProfile> profileOpt = userProfileRepository.findByUser(user);
        
        return Optional.of(new UserWithProfile(user, profileOpt.orElse(null)));
    }
    
    /**
     * Проверить существует ли пользователь
     */
    public boolean userExists(String telegramId) {
        return userRepository.existsByTelegramId(telegramId);
    }
    
    /**
     * Проверить есть ли у пользователя профиль
     */
    public boolean hasProfile(String telegramId) {
        Optional<User> user = userRepository.findByTelegramId(telegramId);
        return user.isPresent() && userProfileRepository.existsByUser(user.get());
    }
    
    /**
     * Получить всех пользователей
     */
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    /**
     * Получить активных пользователей за последние N дней
     */
    @Transactional(readOnly = true)
    public List<User> getActiveUsers(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return userRepository.findActiveUsers(since);
    }
    
    /**
     * Получить новых пользователей за последние N дней
     */
    @Transactional(readOnly = true)
    public List<User> getNewUsers(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return userRepository.findUsersRegisteredSince(since);
    }
    
    /**
     * Получить статистику пользователей
     */
    @Transactional(readOnly = true)
    public UserStatistics getUserStatistics() {
        long totalUsers = userRepository.getTotalUsers();
        
        LocalDateTime last7Days = LocalDateTime.now().minusDays(7);
        LocalDateTime last30Days = LocalDateTime.now().minusDays(30);
        
        long activeUsers7Days = userRepository.getActiveUsersCount(last7Days);
        long activeUsers30Days = userRepository.getActiveUsersCount(last30Days);
        long newUsers7Days = userRepository.getNewUsersCount(last7Days);
        long newUsers30Days = userRepository.getNewUsersCount(last30Days);
        
        return new UserStatistics(totalUsers, activeUsers7Days, activeUsers30Days, 
                                newUsers7Days, newUsers30Days);
    }
    
    /**
     * Удалить пользователя и все связанные данные
     */
    public void deleteUser(String telegramId) {
        Optional<User> userOpt = userRepository.findByTelegramId(telegramId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            // Удаляем профиль если есть
            userProfileRepository.findByUser(user).ifPresent(userProfileRepository::delete);
            
            // Удаляем пользователя
            userRepository.delete(user);
            
            logger.warn("🗑️ Удален пользователь: {} ({})", user.getUsername(), telegramId);
        }
    }
    
    // Вспомогательные классы
    
    /**
     * Класс для передачи пользователя с профилем
     */
    public static class UserWithProfile {
        private final User user;
        private final UserProfile profile;
        
        public UserWithProfile(User user, UserProfile profile) {
            this.user = user;
            this.profile = profile;
        }
        
        public User getUser() {
            return user;
        }
        
        public UserProfile getProfile() {
            return profile;
        }
        
        public boolean hasProfile() {
            return profile != null;
        }
    }
    
    /**
     * Статистика пользователей
     */
    public static class UserStatistics {
        private final long totalUsers;
        private final long activeUsers7Days;
        private final long activeUsers30Days;
        private final long newUsers7Days;
        private final long newUsers30Days;
        
        public UserStatistics(long totalUsers, long activeUsers7Days, long activeUsers30Days,
                             long newUsers7Days, long newUsers30Days) {
            this.totalUsers = totalUsers;
            this.activeUsers7Days = activeUsers7Days;
            this.activeUsers30Days = activeUsers30Days;
            this.newUsers7Days = newUsers7Days;
            this.newUsers30Days = newUsers30Days;
        }
        
        public long getTotalUsers() { return totalUsers; }
        public long getActiveUsers7Days() { return activeUsers7Days; }
        public long getActiveUsers30Days() { return activeUsers30Days; }
        public long getNewUsers7Days() { return newUsers7Days; }
        public long getNewUsers30Days() { return newUsers30Days; }
        
        @Override
        public String toString() {
            return String.format("UserStatistics{total=%d, active7d=%d, active30d=%d, new7d=%d, new30d=%d}",
                               totalUsers, activeUsers7Days, activeUsers30Days, newUsers7Days, newUsers30Days);
        }
    }
} 