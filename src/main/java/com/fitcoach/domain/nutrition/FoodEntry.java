package com.fitcoach.domain.nutrition;

import com.fitcoach.domain.user.User;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "food_entries")
@EntityListeners(AuditingEntityListener.class)
public class FoodEntry {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "photo_url")
    private String photoUrl;
    
    @Column(name = "photo_hash")
    private String photoHash; // для кэширования анализа
    
    // AI Analysis Results
    @Column(name = "total_calories", nullable = false)
    private Integer totalCalories = 0;
    
    @Column(name = "proteins_g")
    private Double proteinsGrams = 0.0;
    
    @Column(name = "carbs_g") 
    private Double carbsGrams = 0.0;
    
    @Column(name = "fats_g")
    private Double fatsGrams = 0.0;
    
    @Column(name = "fiber_g")
    private Double fiberGrams = 0.0;
    
    @Column(name = "sugar_g")
    private Double sugarGrams = 0.0;
    
    // AI Confidence and Details
    @Column(name = "ai_confidence")
    private Double aiConfidence = 0.0; // 0.0 - 1.0
    
    @Column(name = "ai_model_used")
    private String aiModelUsed;
    
    @Column(columnDefinition = "TEXT")
    private String aiAnalysisRaw; // Raw JSON from AI
    
    // Food Items Detected
    @OneToMany(mappedBy = "foodEntry", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DetectedFoodItem> detectedItems = new ArrayList<>();
    
    // User corrections
    @Column(name = "user_corrected")
    private Boolean userCorrected = false;
    
    @Column(name = "user_notes", columnDefinition = "TEXT")
    private String userNotes;
    
    // Meal type
    @Enumerated(EnumType.STRING)
    @Column(name = "meal_type")
    private MealType mealType;
    
    // Timestamps
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "meal_date")
    private LocalDateTime mealDate; // когда была съедена еда
    
    // Constructors
    public FoodEntry() {}
    
    public FoodEntry(User user, String photoUrl, String photoHash) {
        this.user = user;
        this.photoUrl = photoUrl;
        this.photoHash = photoHash;
        this.mealDate = LocalDateTime.now();
    }
    
    // Business methods
    public void updateNutritionFromAI(NutritionAnalysis analysis) {
        this.totalCalories = analysis.getTotalCalories();
        this.proteinsGrams = analysis.getProteins();
        this.carbsGrams = analysis.getCarbs();
        this.fatsGrams = analysis.getFats();
        this.fiberGrams = analysis.getFiber();
        this.sugarGrams = analysis.getSugar();
        this.aiConfidence = analysis.getConfidence();
        this.aiModelUsed = analysis.getModelUsed();
        this.aiAnalysisRaw = analysis.getRawResponse();
    }
    
    public void addDetectedItem(String itemName, Integer calories, Double weight) {
        DetectedFoodItem item = new DetectedFoodItem();
        item.setFoodEntry(this);
        item.setItemName(itemName);
        item.setCalories(calories);
        item.setWeightGrams(weight);
        this.detectedItems.add(item);
    }
    
    public boolean isAccurate() {
        return aiConfidence != null && aiConfidence >= 0.7;
    }
    
    public boolean needsUserReview() {
        return aiConfidence != null && aiConfidence < 0.5;
    }
    
    public int getCaloriesPerMacro(String macroType) {
        return switch (macroType.toLowerCase()) {
            case "protein" -> (int) (proteinsGrams * 4);
            case "carbs" -> (int) (carbsGrams * 4);
            case "fat" -> (int) (fatsGrams * 9);
            default -> 0;
        };
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    
    public String getPhotoHash() { return photoHash; }
    public void setPhotoHash(String photoHash) { this.photoHash = photoHash; }
    
    public Integer getTotalCalories() { return totalCalories; }
    public void setTotalCalories(Integer totalCalories) { this.totalCalories = totalCalories; }
    
    public Double getProteinsGrams() { return proteinsGrams; }
    public void setProteinsGrams(Double proteinsGrams) { this.proteinsGrams = proteinsGrams; }
    
    public Double getCarbsGrams() { return carbsGrams; }
    public void setCarbsGrams(Double carbsGrams) { this.carbsGrams = carbsGrams; }
    
    public Double getFatsGrams() { return fatsGrams; }
    public void setFatsGrams(Double fatsGrams) { this.fatsGrams = fatsGrams; }
    
    public Double getFiberGrams() { return fiberGrams; }
    public void setFiberGrams(Double fiberGrams) { this.fiberGrams = fiberGrams; }
    
    public Double getSugarGrams() { return sugarGrams; }
    public void setSugarGrams(Double sugarGrams) { this.sugarGrams = sugarGrams; }
    
    public Double getAiConfidence() { return aiConfidence; }
    public void setAiConfidence(Double aiConfidence) { this.aiConfidence = aiConfidence; }
    
    public String getAiModelUsed() { return aiModelUsed; }
    public void setAiModelUsed(String aiModelUsed) { this.aiModelUsed = aiModelUsed; }
    
    public String getAiAnalysisRaw() { return aiAnalysisRaw; }
    public void setAiAnalysisRaw(String aiAnalysisRaw) { this.aiAnalysisRaw = aiAnalysisRaw; }
    
    public List<DetectedFoodItem> getDetectedItems() { return detectedItems; }
    public void setDetectedItems(List<DetectedFoodItem> detectedItems) { this.detectedItems = detectedItems; }
    
    public Boolean getUserCorrected() { return userCorrected; }
    public void setUserCorrected(Boolean userCorrected) { this.userCorrected = userCorrected; }
    
    public String getUserNotes() { return userNotes; }
    public void setUserNotes(String userNotes) { this.userNotes = userNotes; }
    
    public MealType getMealType() { return mealType; }
    public void setMealType(MealType mealType) { this.mealType = mealType; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getMealDate() { return mealDate; }
    public void setMealDate(LocalDateTime mealDate) { this.mealDate = mealDate; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FoodEntry foodEntry = (FoodEntry) o;
        return Objects.equals(id, foodEntry.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "FoodEntry{" +
                "id=" + id +
                ", totalCalories=" + totalCalories +
                ", aiConfidence=" + aiConfidence +
                ", mealType=" + mealType +
                '}';
    }
} 