package com.fitcoach.domain.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Basic lookups
    Optional<User> findByTelegramId(String telegramId);
    Optional<User> findByEmail(String email);
    Optional<User> findByCoachReferralCode(String referralCode);
    
    // Coach-related queries
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.status = 'ACTIVE'")
    List<User> findActiveUsersByRole(@Param("role") UserRole role);
    
    @Query("SELECT u FROM User u WHERE u.coach.id = :coachId AND u.status = 'ACTIVE'")
    List<User> findActiveStudentsByCoach(@Param("coachId") Long coachId);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.coach.id = :coachId AND u.hasActiveSubscription() = true")
    Long countActiveSubscriptionsByCoach(@Param("coachId") Long coachId);
    
    // Subscription queries
    @Query("SELECT u FROM User u WHERE u.subscriptionType != 'FREE' AND u.subscriptionEndDate > :now")
    List<User> findUsersWithActiveSubscriptions(@Param("now") LocalDateTime now);
    
    @Query("SELECT u FROM User u WHERE u.subscriptionEndDate BETWEEN :start AND :end")
    List<User> findUsersWithExpiringSubscriptions(@Param("start") LocalDateTime start, 
                                                   @Param("end") LocalDateTime end);
    
    // Platform statistics
    @Query("SELECT u.platform, COUNT(u) FROM User u GROUP BY u.platform")
    List<Object[]> countUsersByPlatform();
    
    // Recent activity
    @Query("SELECT u FROM User u WHERE u.createdAt >= :since ORDER BY u.createdAt DESC")
    List<User> findRecentUsers(@Param("since") LocalDateTime since, Pageable pageable);
    
    // Search functionality
    @Query("SELECT u FROM User u WHERE " +
           "(:name IS NULL OR LOWER(CONCAT(u.firstName, ' ', COALESCE(u.lastName, ''))) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:role IS NULL OR u.role = :role) AND " +
           "(:status IS NULL OR u.status = :status)")
    Page<User> searchUsers(@Param("name") String name,
                          @Param("role") UserRole role,
                          @Param("status") UserStatus status,
                          Pageable pageable);
    
    // Coach earnings calculation helper
    @Query("SELECT u FROM User u WHERE u.coach.id = :coachId AND " +
           "u.subscriptionType != 'FREE' AND u.subscriptionEndDate > CURRENT_TIMESTAMP")
    List<User> findPayingStudentsByCoach(@Param("coachId") Long coachId);
    
    // Fitness goals statistics
    @Query("SELECT u.fitnessGoal, COUNT(u) FROM User u WHERE u.fitnessGoal IS NOT NULL GROUP BY u.fitnessGoal")
    List<Object[]> countUsersByFitnessGoal();
    
    // Onboarding completion
    @Query("SELECT u FROM User u WHERE u.age IS NULL OR u.heightCm IS NULL OR " +
           "u.weightKg IS NULL OR u.fitnessGoal IS NULL")
    List<User> findUsersWithIncompleteProfile();
    
    // Custom query for checking referral code uniqueness
    boolean existsByCoachReferralCode(String referralCode);
    
    // Find users who haven't been active recently (for engagement campaigns)
    @Query("SELECT u FROM User u WHERE u.updatedAt < :threshold AND u.status = 'ACTIVE'")
    List<User> findInactiveUsers(@Param("threshold") LocalDateTime threshold);
} 