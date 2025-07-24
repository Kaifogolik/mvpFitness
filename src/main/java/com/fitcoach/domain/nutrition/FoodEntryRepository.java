package com.fitcoach.domain.nutrition;

import com.fitcoach.domain.user.User;
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
public interface FoodEntryRepository extends JpaRepository<FoodEntry, Long> {
    
    // Basic user queries
    List<FoodEntry> findByUserOrderByMealDateDesc(User user);
    
    Page<FoodEntry> findByUserOrderByMealDateDesc(User user, Pageable pageable);
    
    // Date range queries
    @Query("SELECT f FROM FoodEntry f WHERE f.user = :user AND f.mealDate BETWEEN :start AND :end ORDER BY f.mealDate DESC")
    List<FoodEntry> findByUserAndDateRange(@Param("user") User user, 
                                          @Param("start") LocalDateTime start, 
                                          @Param("end") LocalDateTime end);
    
    // Today's entries
    @Query("SELECT f FROM FoodEntry f WHERE f.user = :user AND DATE(f.mealDate) = CURRENT_DATE ORDER BY f.mealDate ASC")
    List<FoodEntry> findTodaysEntries(@Param("user") User user);
    
    // Meal type queries
    List<FoodEntry> findByUserAndMealTypeOrderByMealDateDesc(User user, MealType mealType);
    
    // Calorie calculations
    @Query("SELECT SUM(f.totalCalories) FROM FoodEntry f WHERE f.user = :user AND DATE(f.mealDate) = CURRENT_DATE")
    Optional<Integer> getTodaysTotalCalories(@Param("user") User user);
    
    @Query("SELECT SUM(f.totalCalories) FROM FoodEntry f WHERE f.user = :user AND f.mealDate BETWEEN :start AND :end")
    Optional<Integer> getTotalCaloriesInRange(@Param("user") User user,
                                            @Param("start") LocalDateTime start,
                                            @Param("end") LocalDateTime end);
    
    // Macro calculations for today
    @Query("SELECT SUM(f.proteinsGrams), SUM(f.carbsGrams), SUM(f.fatsGrams) FROM FoodEntry f " +
           "WHERE f.user = :user AND DATE(f.mealDate) = CURRENT_DATE")
    Object[] getTodaysMacros(@Param("user") User user);
    
    // Weekly statistics
    @Query("SELECT DATE(f.mealDate), SUM(f.totalCalories) FROM FoodEntry f " +
           "WHERE f.user = :user AND f.mealDate >= :weekStart " +
           "GROUP BY DATE(f.mealDate) ORDER BY DATE(f.mealDate)")
    List<Object[]> getWeeklyCalorieStats(@Param("user") User user, @Param("weekStart") LocalDateTime weekStart);
    
    // Photo hash lookup (for caching)
    Optional<FoodEntry> findByPhotoHash(String photoHash);
    
    // AI confidence queries
    @Query("SELECT f FROM FoodEntry f WHERE f.user = :user AND f.aiConfidence < :threshold ORDER BY f.createdAt DESC")
    List<FoodEntry> findLowConfidenceEntries(@Param("user") User user, @Param("threshold") Double threshold);
    
    // User correction tracking
    @Query("SELECT COUNT(f) FROM FoodEntry f WHERE f.user = :user AND f.userCorrected = true")
    Long countCorrectedEntries(@Param("user") User user);
    
    // Most recent entry
    Optional<FoodEntry> findFirstByUserOrderByMealDateDesc(User user);
    
    // Entries needing review
    @Query("SELECT f FROM FoodEntry f WHERE f.aiConfidence < 0.5 AND f.userCorrected = false ORDER BY f.createdAt DESC")
    List<FoodEntry> findEntriesNeedingReview(Pageable pageable);
    
    // Popular food items analysis
    @Query("SELECT di.itemName, COUNT(di) as frequency FROM DetectedFoodItem di " +
           "JOIN di.foodEntry f WHERE f.user = :user " +
           "GROUP BY di.itemName ORDER BY frequency DESC")
    List<Object[]> findMostFrequentFoods(@Param("user") User user, Pageable pageable);
    
    // Average daily calories for period
    @Query("SELECT AVG(daily.totalCals) FROM (" +
           "SELECT DATE(f.mealDate) as day, SUM(f.totalCalories) as totalCals " +
           "FROM FoodEntry f WHERE f.user = :user AND f.mealDate BETWEEN :start AND :end " +
           "GROUP BY DATE(f.mealDate)) daily")
    Optional<Double> getAverageDailyCalories(@Param("user") User user,
                                           @Param("start") LocalDateTime start,
                                           @Param("end") LocalDateTime end);
    
    // Streak calculation helper
    @Query("SELECT DATE(f.mealDate) FROM FoodEntry f " +
           "WHERE f.user = :user AND f.mealDate >= :since " +
           "GROUP BY DATE(f.mealDate) ORDER BY DATE(f.mealDate) DESC")
    List<Object[]> findLoggedDays(@Param("user") User user, @Param("since") LocalDateTime since);
    
    // Coach analytics - student progress
    @Query("SELECT f FROM FoodEntry f WHERE f.user.coach = :coach AND f.mealDate >= :since ORDER BY f.mealDate DESC")
    List<FoodEntry> findRecentEntriesByCoachStudents(@Param("coach") User coach, @Param("since") LocalDateTime since);
} 