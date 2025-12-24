package com.deliverycore.config;

import com.deliverycore.model.Category;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.*;

/**
 * Implementation of CategoryConfig that parses categories.yml.
 * Supports loading from file path or input stream.
 */
public class CategoryConfigImpl implements CategoryConfig {
    
    private final Map<String, Category> categories;
    
    /**
     * Creates a CategoryConfigImpl from a file path.
     *
     * @param filePath the path to categories.yml
     * @throws IOException if the file cannot be read
     */
    public CategoryConfigImpl(String filePath) throws IOException {
        this(new FileInputStream(filePath));
    }
    
    /**
     * Creates a CategoryConfigImpl from an input stream.
     *
     * @param inputStream the input stream containing YAML data
     */
    public CategoryConfigImpl(InputStream inputStream) {
        this.categories = parseCategories(inputStream);
    }
    
    /**
     * Creates a CategoryConfigImpl from a pre-parsed map (for testing).
     *
     * @param categoriesMap map of category name to list of items
     */
    public CategoryConfigImpl(Map<String, List<String>> categoriesMap) {
        this.categories = convertToCategories(categoriesMap, Map.of());
    }

    @SuppressWarnings("unchecked")
    private Map<String, Category> parseCategories(InputStream inputStream) {
        Yaml yaml = new Yaml();
        Map<String, Object> root = yaml.load(inputStream);
        
        if (root == null) {
            return Collections.emptyMap();
        }
        
        Map<String, List<String>> categoriesMap = new LinkedHashMap<>();
        Map<String, Map<String, Double>> pricesMap = new LinkedHashMap<>();
        
        Object categoriesObj = root.get("categories");
        if (categoriesObj instanceof Map) {
            Map<String, Object> categoriesData = (Map<String, Object>) categoriesObj;
            for (Map.Entry<String, Object> entry : categoriesData.entrySet()) {
                String categoryName = entry.getKey();
                Object categoryData = entry.getValue();
                
                List<String> items = new ArrayList<>();
                Map<String, Double> prices = new LinkedHashMap<>();
                
                if (categoryData instanceof Map) {
                    Map<String, Object> catMap = (Map<String, Object>) categoryData;
                    Object itemsObj = catMap.get("items");
                    
                    if (itemsObj instanceof Map) {
                        // Yeni format: items: { DIAMOND: 500, EMERALD: 300 }
                        Map<String, Object> itemsMap = (Map<String, Object>) itemsObj;
                        for (Map.Entry<String, Object> itemEntry : itemsMap.entrySet()) {
                            String itemName = itemEntry.getKey();
                            items.add(itemName);
                            
                            // Fiyatı kaydet
                            Object priceObj = itemEntry.getValue();
                            if (priceObj instanceof Number) {
                                prices.put(itemName, ((Number) priceObj).doubleValue());
                            }
                        }
                    } else if (itemsObj instanceof List) {
                        // Eski format: items: [DIAMOND, EMERALD]
                        for (Object item : (List<?>) itemsObj) {
                            if (item != null) {
                                items.add(item.toString());
                            }
                        }
                    }
                } else if (categoryData instanceof List) {
                    // En eski format: category: [item1, item2]
                    for (Object item : (List<?>) categoryData) {
                        if (item != null) {
                            items.add(item.toString());
                        }
                    }
                }
                
                categoriesMap.put(categoryName, items);
                pricesMap.put(categoryName, prices);
            }
        }
        
        return convertToCategories(categoriesMap, pricesMap);
    }
    
    private Map<String, Category> convertToCategories(Map<String, List<String>> categoriesMap, 
                                                       Map<String, Map<String, Double>> pricesMap) {
        Map<String, Category> result = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> entry : categoriesMap.entrySet()) {
            String name = entry.getKey();
            Map<String, Double> prices = pricesMap.getOrDefault(name, Map.of());
            result.put(name, new Category(name, entry.getValue(), prices));
        }
        return Collections.unmodifiableMap(result);
    }

    @Override
    public Map<String, Category> getCategories() {
        return categories;
    }
    
    @Override
    public Optional<Category> getCategory(String name) {
        return Optional.ofNullable(categories.get(name));
    }
    
    @Override
    public List<String> getCategoryNames() {
        return List.copyOf(categories.keySet());
    }
    
    /**
     * Serializes the categories to YAML format.
     *
     * @return YAML string representation of categories
     */
    public String toYaml() {
        Map<String, Object> root = new LinkedHashMap<>();
        Map<String, List<String>> categoriesMap = new LinkedHashMap<>();
        
        for (Map.Entry<String, Category> entry : categories.entrySet()) {
            categoriesMap.put(entry.getKey(), new ArrayList<>(entry.getValue().items()));
        }
        
        root.put("categories", categoriesMap);
        
        Yaml yaml = new Yaml();
        return yaml.dump(root);
    }
    
    /**
     * Parses YAML string and creates a CategoryConfigImpl.
     *
     * @param yamlContent the YAML content as string
     * @return a new CategoryConfigImpl instance
     */
    public static CategoryConfigImpl fromYaml(String yamlContent) {
        return new CategoryConfigImpl(new ByteArrayInputStream(yamlContent.getBytes()));
    }
}
