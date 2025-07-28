package com.fitcoach.repository;

import com.fitcoach.model.User;
import com.fitcoach.model.UserProfile;
import com.fitcoach.model.UserProfile.FitnessGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository для работы с профилями пользователей
 */
@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    
    /**
     * Найти профиль по пользователю
     */
    Optional<UserProfile> findByUser(User user);
    
    /**
     * Найти профиль по Telegram ID пользователя
     */
    @Query("SELECT p FROM UserProfile p WHERE p.user.telegramId = :telegramId")
    Optional<UserProfile> findByUserTelegramId(@Param("telegramId") String telegramId);
    
    /**
     * Проверить существует ли профиль для пользователя
     */
    boolean existsByUser(User user);
    
    /**
     * Найти профили по цели фитнеса
     */
    List<UserProfile> findByFitnessGoal(FitnessGoal fitnessGoal);
    
    /**
     * Найти пользователей с похожими целями (для рекомендаций)
     */
    @Query("SELECT p FROM UserProfile p WHERE p.fitnessGoal = :goal AND p.user.id != :excludeUserId")
    List<UserProfile> findSimilarGoalProfiles(@Param("goal") FitnessGoal goal, 
                                             @Param("excludeUserId") Long excludeUserId);
    
    /**
     * Найти профили пользователей в определенном диапазоне возраста
     */
    @Query("SELECT p FROM UserProfile p WHERE p.age BETWEEN :minAge AND :maxAge")
    List<UserProfile> findByAgeRange(@Param("minAge") Integer minAge, 
                                    @Param("maxAge") Integer maxAge);
    
    /**
     * Найти профили с похожими физическими параметрами
     */
    @Query("SELECT p FROM UserProfile p WHERE " +
           "p.gender = :gender AND " +
           "p.age BETWEEN :minAge AND :maxAge AND " +
           "p.weight BETWEEN :minWeight AND :maxWeight AND " +
           "p.user.id != :excludeUserId")
    List<UserProfile> findSimilarPhysicalProfiles(@Param("gender") UserProfile.Gender gender,
                                                  @Param("minAge") Integer minAge,
                                                  @Param("maxAge") Integer maxAge,
                                                  @Param("minWeight") Double minWeight,
                                                  @Param("maxWeight") Double maxWeight,
                                                  @Param("excludeUserId") Long excludeUserId);
    
    /**
     * Получить статистику по профилям
     */
    @Query("SELECT COUNT(p) FROM UserProfile p")
    long getTotalProfiles();
    
    @Query("SELECT COUNT(p) FROM UserProfile p WHERE p.fitnessGoal = :goal")
    long getProfilesByGoalCount(@Param("goal") FitnessGoal goal);
    
    @Query("SELECT p.fitnessGoal, COUNT(p) FROM UserProfile p GROUP BY p.fitnessGoal")
    List<Object[]> getGoalsStatistics();
    
    /**
     * Найти неполные профили (без всех необходимых данных)
     */
    @Query("SELECT p FROM UserProfile p WHERE " +
           "p.age IS NULL OR p.weight IS NULL OR p.height IS NULL OR " +
           "p.gender IS NULL OR p.activityLevel IS NULL")
    List<UserProfile> findIncompleteProfiles();
    
    /**
     * Найти профили с просроченными данными (не обновлялись давно)
     */
    @Query("SELECT p FROM UserProfile p WHERE " +
           "p.updatedAt IS NULL OR p.updatedAt < :threshold")
    List<UserProfile> findOutdatedProfiles(@Param("threshold") java.time.LocalDateTime threshold);
} 