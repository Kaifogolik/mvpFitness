package com.fitcoach.infrastructure.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * База данных популярных продуктов для российской кухни
 * Используется для комбинирования с ИИ анализом и fallback
 */
@Service
public class ProductsDatabase {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductsDatabase.class);
    
    private final Map<String, FoodProduct> productsDB;
    private final Map<String, List<String>> categoryKeywords;
    
    public ProductsDatabase() {
        this.productsDB = initializeProductsDatabase();
        this.categoryKeywords = initializeCategoryKeywords();
        logger.info("🗄️ Инициализирована база продуктов: {} продуктов", productsDB.size());
    }
    
    /**
     * Ищет продукт по названию с поддержкой нечеткого поиска
     */
    public Optional<FoodProduct> findProduct(String foodName) {
        if (foodName == null || foodName.trim().isEmpty()) {
            return Optional.empty();
        }
        
        String normalizedName = normalizeProductName(foodName);
        
        // Точное совпадение
        FoodProduct exactMatch = productsDB.get(normalizedName);
        if (exactMatch != null) {
            logger.debug("Найдено точное совпадение: {}", foodName);
            return Optional.of(exactMatch);
        }
        
        // Нечеткий поиск
        Optional<FoodProduct> fuzzyMatch = findFuzzyMatch(normalizedName);
        if (fuzzyMatch.isPresent()) {
            logger.debug("Найдено нечеткое совпадение: {} -> {}", foodName, fuzzyMatch.get().getName());
            return fuzzyMatch;
        }
        
        return Optional.empty();
    }
    
    /**
     * Получает продукты по категории
     */
    public List<FoodProduct> getProductsByCategory(String category) {
        List<String> keywords = categoryKeywords.get(category.toLowerCase());
        if (keywords == null || keywords.isEmpty()) {
            return Collections.emptyList();
        }
        
        return productsDB.values().stream()
            .filter(product -> keywords.stream()
                .anyMatch(keyword -> product.getName().toLowerCase().contains(keyword)))
            .collect(Collectors.toList());
    }
    
    /**
     * Создает анализ питания для найденного продукта
     */
    public NutritionAnalysis createAnalysisFromProduct(FoodProduct product, String quantity, double weightGrams) {
        NutritionAnalysis analysis = new NutritionAnalysis();
        
        // Вычисляем пищевую ценность для конкретного веса
        double multiplier = weightGrams / 100.0; // БЖУ указаны на 100г
        
        double calories = product.getCalories() * multiplier;
        double proteins = product.getProteins() * multiplier;
        double fats = product.getFats() * multiplier;
        double carbs = product.getCarbs() * multiplier;
        
        analysis.setTotalCalories(calories);
        analysis.setTotalProteins(proteins);
        analysis.setTotalFats(fats);
        analysis.setTotalCarbs(carbs);
        analysis.setConfidenceLevel(0.9); // Высокая уверенность для базы данных
        
        // Создаем список обнаруженных продуктов
        NutritionAnalysis.DetectedFood detectedFood = new NutritionAnalysis.DetectedFood(
            product.getName(), quantity, calories, proteins, fats, carbs, 0.9
        );
        analysis.setDetectedFoods(Collections.singletonList(detectedFood));
        
        analysis.setAnalysisNotes(String.format(
            "Данные из базы продуктов для %s (%s)", product.getName(), quantity));
        
        analysis.setHealthRecommendations(generateHealthRecommendations(product));
        
        return analysis;
    }
    
    /**
     * Инициализирует базу продуктов
     */
    private Map<String, FoodProduct> initializeProductsDatabase() {
        Map<String, FoodProduct> db = new HashMap<>();
        
        // Основные российские блюда
        addProduct(db, "борщ", 45, 2.8, 1.8, 6.5, "суп");
        addProduct(db, "щи", 38, 2.1, 1.4, 5.8, "суп");
        addProduct(db, "солянка", 65, 4.2, 3.8, 5.2, "суп");
        addProduct(db, "окрошка", 52, 2.8, 1.9, 7.3, "суп");
        
        // Каши
        addProduct(db, "гречневая каша", 132, 4.5, 2.3, 25.0, "каша");
        addProduct(db, "рисовая каша", 116, 2.2, 0.5, 25.8, "каша");
        addProduct(db, "овсяная каша", 88, 3.0, 1.7, 15.0, "каша");
        addProduct(db, "манная каша", 98, 3.0, 3.2, 15.3, "каша");
        
        // Мясные блюда
        addProduct(db, "котлеты", 250, 18.2, 18.4, 5.9, "мясо");
        addProduct(db, "шницель", 234, 19.8, 15.6, 4.2, "мясо");
        addProduct(db, "бефстроганов", 193, 16.7, 11.9, 5.9, "мясо");
        addProduct(db, "жареная курица", 204, 20.8, 8.8, 6.2, "птица");
        addProduct(db, "плов", 196, 6.0, 6.7, 30.4, "гарнир");
        
        // Рыбные блюда
        addProduct(db, "жареная рыба", 145, 19.2, 6.8, 1.8, "рыба");
        addProduct(db, "рыбные котлеты", 168, 16.4, 8.9, 6.1, "рыба");
        addProduct(db, "селедка под шубой", 208, 8.2, 17.9, 4.1, "салат");
        
        // Овощные блюда
        addProduct(db, "винегрет", 76, 1.6, 4.6, 8.2, "салат");
        addProduct(db, "оливье", 198, 5.5, 16.5, 7.8, "салат");
        addProduct(db, "греческий салат", 188, 4.2, 17.4, 4.2, "салат");
        addProduct(db, "цезарь", 301, 14.8, 16.2, 25.9, "салат");
        
        // Выпечка и хлеб
        addProduct(db, "черный хлеб", 214, 6.6, 1.2, 40.9, "хлеб");
        addProduct(db, "белый хлеб", 242, 8.1, 1.0, 48.8, "хлеб");
        addProduct(db, "блины", 233, 6.1, 12.3, 26.0, "выпечка");
        addProduct(db, "оладьи", 194, 6.2, 3.9, 35.1, "выпечка");
        addProduct(db, "сырники", 220, 18.6, 7.0, 18.4, "выпечка");
        
        // Молочные продукты
        addProduct(db, "творог", 103, 16.7, 0.6, 1.8, "молочное");
        addProduct(db, "кефир", 40, 2.8, 0.1, 3.8, "молочное");
        addProduct(db, "йогурт", 66, 5.0, 3.2, 3.5, "молочное");
        addProduct(db, "сметана", 206, 2.8, 20.0, 3.2, "молочное");
        
        // Популярные продукты
        addProduct(db, "макароны", 112, 3.4, 0.4, 23.2, "гарнир");
        addProduct(db, "картофель отварной", 82, 2.0, 0.4, 16.1, "гарнир");
        addProduct(db, "картофель жареный", 192, 2.8, 9.5, 23.4, "гарнир");
        addProduct(db, "пюре", 106, 2.3, 4.2, 14.3, "гарнир");
        
        // Фастфуд
        addProduct(db, "бургер", 540, 25.0, 31.0, 40.0, "фастфуд");
        addProduct(db, "пицца", 266, 11.0, 10.4, 33.5, "фастфуд");
        addProduct(db, "шаурма", 309, 16.0, 15.0, 30.0, "фастфуд");
        
        return db;
    }
    
    /**
     * Инициализирует ключевые слова по категориям
     */
    private Map<String, List<String>> initializeCategoryKeywords() {
        Map<String, List<String>> keywords = new HashMap<>();
        
        keywords.put("суп", Arrays.asList("борщ", "щи", "солянка", "окрошка", "харчо", "суп"));
        keywords.put("каша", Arrays.asList("каша", "гречневая", "рисовая", "овсяная", "манная"));
        keywords.put("мясо", Arrays.asList("котлеты", "шницель", "мясо", "говядина", "свинина", "баранина"));
        keywords.put("птица", Arrays.asList("курица", "птица", "индейка", "утка"));
        keywords.put("рыба", Arrays.asList("рыба", "селедка", "семга", "треска", "судак"));
        keywords.put("салат", Arrays.asList("салат", "винегрет", "оливье", "цезарь", "греческий"));
        keywords.put("молочное", Arrays.asList("творог", "кефир", "молоко", "йогурт", "сметана"));
        keywords.put("выпечка", Arrays.asList("блины", "оладьи", "сырники", "пирог", "булка"));
        
        return keywords;
    }
    
    /**
     * Добавляет продукт в базу
     */
    private void addProduct(Map<String, FoodProduct> db, String name, double calories, 
                          double proteins, double fats, double carbs, String category) {
        FoodProduct product = new FoodProduct(name, calories, proteins, fats, carbs, category);
        db.put(normalizeProductName(name), product);
        
        // Добавляем альтернативные названия
        for (String alt : generateAlternativeNames(name)) {
            db.put(normalizeProductName(alt), product);
        }
    }
    
    /**
     * Генерирует альтернативные названия продукта
     */
    private List<String> generateAlternativeNames(String name) {
        List<String> alternatives = new ArrayList<>();
        
        // Добавляем варианты без прилагательных
        if (name.contains(" ")) {
            String[] words = name.split(" ");
            if (words.length == 2) {
                alternatives.add(words[1]); // "гречневая каша" -> "каша"
                alternatives.add(words[0]); // "гречневая каша" -> "гречневая"
            }
        }
        
        return alternatives;
    }
    
    /**
     * Нормализует название продукта для поиска
     */
    private String normalizeProductName(String name) {
        return name.toLowerCase()
            .replaceAll("[^а-яё\\s]", "")
            .replaceAll("\\s+", " ")
            .trim();
    }
    
    /**
     * Выполняет нечеткий поиск продукта
     */
    private Optional<FoodProduct> findFuzzyMatch(String searchName) {
        return productsDB.entrySet().stream()
            .filter(entry -> {
                String productName = entry.getKey();
                return calculateSimilarity(searchName, productName) > 0.7;
            })
            .map(Map.Entry::getValue)
            .findFirst();
    }
    
    /**
     * Вычисляет схожесть строк (простой алгоритм)
     */
    private double calculateSimilarity(String s1, String s2) {
        String longer = s1.length() > s2.length() ? s1 : s2;
        String shorter = s1.length() > s2.length() ? s2 : s1;
        
        if (longer.length() == 0) {
            return 1.0;
        }
        
        if (longer.contains(shorter) || shorter.contains(longer)) {
            return 0.8;
        }
        
        return (longer.length() - editDistance(longer, shorter)) / (double) longer.length();
    }
    
    /**
     * Вычисляет расстояние редактирования (Левенштейна)
     */
    private int editDistance(String s1, String s2) {
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();
        
        int[] costs = new int[s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            int lastValue = i;
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    costs[j] = j;
                } else {
                    if (j > 0) {
                        int newValue = costs[j - 1];
                        if (s1.charAt(i - 1) != s2.charAt(j - 1)) {
                            newValue = Math.min(Math.min(newValue, lastValue), costs[j]) + 1;
                        }
                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
            }
            if (i > 0) {
                costs[s2.length()] = lastValue;
            }
        }
        return costs[s2.length()];
    }
    
    /**
     * Генерирует рекомендации по здоровью для продукта
     */
    private List<String> generateHealthRecommendations(FoodProduct product) {
        List<String> recommendations = new ArrayList<>();
        
        // Рекомендации на основе категории
        switch (product.getCategory()) {
            case "мясо":
                recommendations.add("🥗 Добавьте овощной гарнир для лучшего усвоения");
                recommendations.add("💧 Увеличьте потребление воды при употреблении мяса");
                break;
            case "каша":
                recommendations.add("🍓 Добавьте ягоды или фрукты для витаминов");
                recommendations.add("🥜 Орехи или семена увеличат пищевую ценность");
                break;
            case "салат":
                recommendations.add("🫒 Используйте оливковое масло для заправки");
                recommendations.add("🌿 Добавьте зелень для дополнительных витаминов");
                break;
            default:
                recommendations.add("🥛 Не забывайте про водный баланс");
                recommendations.add("🚶 Умеренная физическая активность поможет усвоению");
        }
        
        return recommendations;
    }
    
    /**
     * Класс для представления продукта
     */
    public static class FoodProduct {
        private final String name;
        private final double calories;
        private final double proteins;
        private final double fats;
        private final double carbs;
        private final String category;
        
        public FoodProduct(String name, double calories, double proteins, double fats, 
                          double carbs, String category) {
            this.name = name;
            this.calories = calories;
            this.proteins = proteins;
            this.fats = fats;
            this.carbs = carbs;
            this.category = category;
        }
        
        // Getters
        public String getName() { return name; }
        public double getCalories() { return calories; }
        public double getProteins() { return proteins; }
        public double getFats() { return fats; }
        public double getCarbs() { return carbs; }
        public String getCategory() { return category; }
        
        @Override
        public String toString() {
            return String.format("%s: %.0f ккал (Б:%.1f Ж:%.1f У:%.1f)", 
                name, calories, proteins, fats, carbs);
        }
    }
} 