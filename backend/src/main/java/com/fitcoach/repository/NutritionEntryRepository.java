package com.fitcoach.repository;

import com.fitcoach.model.NutritionEntry;
import com.fitcoach.model.NutritionEntry.MealType;
import com.fitcoach.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository для работы с записями о питании
 */
@Repository
public interface NutritionEntryRepository extends JpaRepository<NutritionEntry, Long> {
    
    /**
     * Найти все записи пользователя
     */
    List<NutritionEntry> findByUserOrderByTimestampDesc(User user);
    
    /**
     * Найти записи пользователя по Telegram ID
     */
    @Query("SELECT n FROM NutritionEntry n WHERE n.user.telegramId = :telegramId ORDER BY n.timestamp DESC")
    List<NutritionEntry> findByUserTelegramIdOrderByTimestampDesc(@Param("telegramId") String telegramId);
    
    /**
     * Найти записи пользователя за конкретную дату
     */
    List<NutritionEntry> findByUserAndDateOrderByTimestamp(User user, LocalDate date);
    
    /**
     * Найти записи пользователя за период
     */
    @Query("SELECT n FROM NutritionEntry n WHERE n.user = :user AND n.date BETWEEN :startDate AND :endDate ORDER BY n.timestamp DESC")
    List<NutritionEntry> findByUserAndDateRange(@Param("user") User user, 
                                               @Param("startDate") LocalDate startDate, 
                                               @Param("endDate") LocalDate endDate);
    
    /**
     * Найти записи по типу приема пищи
     */
    List<NutritionEntry> findByUserAndMealTypeOrderByTimestampDesc(User user, MealType mealType);
    
    /**
     * Получить суммарные калории пользователя за день
     */
    @Query("SELECT COALESCE(SUM(n.calories), 0) FROM NutritionEntry n WHERE n.user = :user AND n.date = :date")
    Double getTotalCaloriesForDate(@Param("user") User user, @Param("date") LocalDate date);
    
    /**
     * Получить суммарные БЖУ пользователя за день
     */
    @Query("SELECT COALESCE(SUM(n.proteins), 0), COALESCE(SUM(n.fats), 0), COALESCE(SUM(n.carbs), 0) " +
           "FROM NutritionEntry n WHERE n.user = :user AND n.date = :date")
    List<Object[]> getTotalNutrientsForDate(@Param("user") User user, @Param("date") LocalDate date);
    
    /**
     * Получить дневную статистику за период
     */
    @Query("SELECT n.date, " +
           "COALESCE(SUM(n.calories), 0), " +
           "COALESCE(SUM(n.proteins), 0), " +
           "COALESCE(SUM(n.fats), 0), " +
           "COALESCE(SUM(n.carbs), 0), " +
           "COUNT(n) " +
           "FROM NutritionEntry n " +
           "WHERE n.user = :user AND n.date BETWEEN :startDate AND :endDate " +
           "GROUP BY n.date ORDER BY n.date DESC")
    List<Object[]> getDailyStatistics(@Param("user") User user, 
                                     @Param("startDate") LocalDate startDate, 
                                     @Param("endDate") LocalDate endDate);
    
    /**
     * Получить статистику по типам приемов пищи за период
     */
    @Query("SELECT n.mealType, " +
           "COALESCE(SUM(n.calories), 0), " +
           "COUNT(n) " +
           "FROM NutritionEntry n " +
           "WHERE n.user = :user AND n.date BETWEEN :startDate AND :endDate " +
           "GROUP BY n.mealType")
    List<Object[]> getMealTypeStatistics(@Param("user") User user, 
                                        @Param("startDate") LocalDate startDate, 
                                        @Param("endDate") LocalDate endDate);
    
    /**
     * Найти самые популярные продукты пользователя
     */
    @Query("SELECT n.foodName, COUNT(n), AVG(n.calories) " +
           "FROM NutritionEntry n " +
           "WHERE n.user = :user " +
           "GROUP BY n.foodName " +
           "ORDER BY COUNT(n) DESC")
    List<Object[]> getFavoriteProducts(@Param("user") User user);
    
    /**
     * Получить средние калории по дням недели - ВРЕМЕННО ОТКЛЮЧЕНО из-за несовместимости H2/PostgreSQL
     */
//    @Query(value = "SELECT EXTRACT(dow FROM daily_stats.date) as day_of_week, " +
//                   "       AVG(daily_stats.daily_calories) as avg_calories " +
//                   "FROM (SELECT date, SUM(calories) as daily_calories " +
//                   "      FROM nutrition_entry " +
//                   "      WHERE user_id = :#{#user.id} AND date >= :since " +
//                   "      GROUP BY date) as daily_stats " +
//                   "GROUP BY EXTRACT(dow FROM daily_stats.date) " +
//                   "ORDER BY EXTRACT(dow FROM daily_stats.date)", 
//           nativeQuery = true)
//    List<Object[]> getAverageCaloriesByDayOfWeek(@Param("user") User user, 
//                                                @Param("since") LocalDate since);
    
    /**
     * Найти записи с низкой уверенностью ИИ (для проверки)
     */
    @Query("SELECT n FROM NutritionEntry n WHERE n.user = :user AND n.confidence < :threshold ORDER BY n.timestamp DESC")
    List<NutritionEntry> findLowConfidenceEntries(@Param("user") User user, 
                                                 @Param("threshold") Double threshold);
    
    /**
     * Получить последние записи пользователя
     */
    @Query("SELECT n FROM NutritionEntry n WHERE n.user = :user ORDER BY n.timestamp DESC")
    List<NutritionEntry> findRecentEntries(@Param("user") User user, 
                                          org.springframework.data.domain.Pageable pageable);
    
    /**
     * Проверить есть ли записи у пользователя за сегодня
     */
    @Query("SELECT COUNT(n) > 0 FROM NutritionEntry n WHERE n.user = :user AND n.date = :date")
    boolean hasEntriesForDate(@Param("user") User user, @Param("date") LocalDate date);
    
    /**
     * Удалить старые записи (для очистки данных)
     */
    @Query("DELETE FROM NutritionEntry n WHERE n.timestamp < :threshold")
    void deleteEntriesOlderThan(@Param("threshold") LocalDateTime threshold);
    
    /**
     * Получить общую статистику системы
     */
    @Query("SELECT COUNT(n) FROM NutritionEntry n")
    long getTotalEntries();
    
    @Query("SELECT COUNT(DISTINCT n.user) FROM NutritionEntry n")
    long getTotalActiveUsers();
    
    @Query("SELECT COUNT(n) FROM NutritionEntry n WHERE n.timestamp >= :since")
    long getEntriesCountSince(@Param("since") LocalDateTime since);
} 