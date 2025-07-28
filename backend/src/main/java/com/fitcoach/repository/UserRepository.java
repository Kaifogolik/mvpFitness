package com.fitcoach.repository;

import com.fitcoach.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository для работы с пользователями
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Найти пользователя по Telegram ID
     */
    Optional<User> findByTelegramId(String telegramId);
    
    /**
     * Найти пользователя по username
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Проверить существует ли пользователь с данным Telegram ID
     */
    boolean existsByTelegramId(String telegramId);
    
    /**
     * Проверить существует ли пользователь с данным username
     */
    boolean existsByUsername(String username);
    
    /**
     * Обновить время последней активности пользователя
     */
    @Modifying
    @Query("UPDATE User u SET u.lastActiveAt = :lastActiveAt WHERE u.telegramId = :telegramId")
    void updateLastActiveTime(@Param("telegramId") String telegramId, 
                             @Param("lastActiveAt") LocalDateTime lastActiveAt);
    
    /**
     * Найти всех пользователей, зарегистрированных после указанной даты
     */
    @Query("SELECT u FROM User u WHERE u.createdAt >= :since ORDER BY u.createdAt DESC")
    java.util.List<User> findUsersRegisteredSince(@Param("since") LocalDateTime since);
    
    /**
     * Найти активных пользователей (активность за последние N дней)
     */
    @Query("SELECT u FROM User u WHERE u.lastActiveAt >= :since ORDER BY u.lastActiveAt DESC")
    java.util.List<User> findActiveUsers(@Param("since") LocalDateTime since);
    
    /**
     * Получить статистику по пользователям
     */
    @Query("SELECT COUNT(u) FROM User u")
    long getTotalUsers();
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.lastActiveAt >= :since")
    long getActiveUsersCount(@Param("since") LocalDateTime since);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :since")
    long getNewUsersCount(@Param("since") LocalDateTime since);
} 