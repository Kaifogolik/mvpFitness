package com.fitcoach.infrastructure.nutrition.fatsecret;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Optional;

/**
 * Модель ответа поиска продуктов от FatSecret API
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FatSecretSearchResponse {
    
    @JsonProperty("foods")
    private FoodsContainer foods;
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FoodsContainer {
        @JsonProperty("food")
        private List<FatSecretFood> foodList;
        
        @JsonProperty("max_results")
        private String maxResults;
        
        @JsonProperty("page_number")
        private String pageNumber;
        
        @JsonProperty("total_results")
        private String totalResults;
        
        public List<FatSecretFood> getFoodList() { return foodList; }
        public void setFoodList(List<FatSecretFood> foodList) { this.foodList = foodList; }
        
        public String getMaxResults() { return maxResults; }
        public void setMaxResults(String maxResults) { this.maxResults = maxResults; }
        
        public String getPageNumber() { return pageNumber; }
        public void setPageNumber(String pageNumber) { this.pageNumber = pageNumber; }
        
        public String getTotalResults() { return totalResults; }
        public void setTotalResults(String totalResults) { this.totalResults = totalResults; }
    }
    
    public FatSecretSearchResponse() {}
    
    /**
     * Получить первый найденный продукт
     */
    public Optional<FatSecretFood> getFirstFood() {
        if (foods != null && foods.getFoodList() != null && !foods.getFoodList().isEmpty()) {
            return Optional.of(foods.getFoodList().get(0));
        }
        return Optional.empty();
    }
    
    /**
     * Получить все найденные продукты
     */
    public List<FatSecretFood> getAllFoods() {
        if (foods != null && foods.getFoodList() != null) {
            return foods.getFoodList();
        }
        return List.of();
    }
    
    /**
     * Получить количество найденных результатов
     */
    public int getTotalResults() {
        if (foods != null && foods.getTotalResults() != null) {
            try {
                return Integer.parseInt(foods.getTotalResults());
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }
    
    /**
     * Проверить есть ли результаты
     */
    public boolean hasResults() {
        return getTotalResults() > 0;
    }
    
    // Геттеры и сеттеры
    
    public FoodsContainer getFoods() { return foods; }
    public void setFoods(FoodsContainer foods) { this.foods = foods; }
    
    @Override
    public String toString() {
        return String.format("FatSecretSearchResponse{totalResults=%d, foods=%d}",
                getTotalResults(), getAllFoods().size());
    }
} 