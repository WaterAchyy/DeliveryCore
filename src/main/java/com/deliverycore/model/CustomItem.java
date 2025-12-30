package com.deliverycore.model;

import java.util.List;
import java.util.Objects;

/**
 * Özel item modeli - NBT verileri ile birlikte saklanır
 * v1.1 özelliği - Modded itemlar, özel isimler, enchantlar desteklenir
 * ItemsAdder, Oraxen ve CustomModelData desteği
 */
public record CustomItem(
    String category,
    String name,
    String serializedNBT,
    double price,
    String displayName,
    List<String> lore,
    Integer customModelData,
    String itemsAdderId,
    String oraxenId
) {
    
    /**
     * CustomItem oluşturur ve doğrular
     */
    public CustomItem {
        Objects.requireNonNull(category, "Category cannot be null");
        Objects.requireNonNull(name, "Name cannot be null");
        Objects.requireNonNull(displayName, "Display name cannot be null");
        Objects.requireNonNull(lore, "Lore cannot be null");
        
        if (category.trim().isEmpty()) {
            throw new IllegalArgumentException("Category cannot be empty");
        }
        
        if (name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        
        // serializedNBT boş olabilir (ItemsAdder/Oraxen itemları için)
        if (serializedNBT == null) {
            serializedNBT = "";
        }
        
        if (price < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
    }
    
    /**
     * Backward compatibility constructor (eski 6 parametreli)
     */
    public CustomItem(String category, String name, String serializedNBT, 
                     double price, String displayName, List<String> lore) {
        this(category, name, serializedNBT, price, displayName, lore, null, null, null);
    }
    
    /**
     * Basit custom item oluşturur (lore olmadan)
     */
    public static CustomItem simple(String category, String name, String serializedNBT, 
                                  double price, String displayName) {
        return new CustomItem(category, name, serializedNBT, price, displayName, List.of(), null, null, null);
    }
    
    /**
     * ItemsAdder item oluşturur
     */
    public static CustomItem itemsAdder(String category, String name, String itemsAdderId, 
                                        double price, String displayName) {
        return new CustomItem(category, name, "", price, displayName, List.of(), null, itemsAdderId, null);
    }
    
    /**
     * Oraxen item oluşturur
     */
    public static CustomItem oraxen(String category, String name, String oraxenId, 
                                   double price, String displayName) {
        return new CustomItem(category, name, "", price, displayName, List.of(), null, null, oraxenId);
    }
    
    /**
     * CustomModelData ile item oluşturur
     */
    public static CustomItem withCustomModelData(String category, String name, String serializedNBT,
                                                 double price, String displayName, int customModelData) {
        return new CustomItem(category, name, serializedNBT, price, displayName, List.of(), customModelData, null, null);
    }
    
    /**
     * Custom item'ın benzersiz ID'sini döndürür
     */
    public String getUniqueId() {
        return category + ":" + name;
    }
    
    /**
     * Custom item'ın geçerli olup olmadığını kontrol eder
     */
    public boolean isValid() {
        try {
            // Temel validasyonlar
            if (category == null || category.trim().isEmpty()) return false;
            if (name == null || name.trim().isEmpty()) return false;
            if (displayName == null) return false;
            if (lore == null) return false;
            if (price < 0) return false;
            
            // En az bir kaynak olmalı: NBT, ItemsAdder, Oraxen veya CustomModelData
            boolean hasSource = (serializedNBT != null && !serializedNBT.trim().isEmpty()) ||
                               (itemsAdderId != null && !itemsAdderId.trim().isEmpty()) ||
                               (oraxenId != null && !oraxenId.trim().isEmpty()) ||
                               (customModelData != null);
            
            return hasSource;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Custom item'ın fiyatının olup olmadığını kontrol eder
     */
    public boolean hasPrice() {
        return price > 0;
    }
    
    /**
     * Custom item'ın lore'unun olup olmadığını kontrol eder
     */
    public boolean hasLore() {
        return !lore.isEmpty();
    }
    
    /**
     * Custom item'ın display name'inin özel olup olmadığını kontrol eder
     */
    public boolean hasCustomDisplayName() {
        return !displayName.trim().isEmpty() && !displayName.equals(name);
    }
    
    /**
     * ItemsAdder item olup olmadığını kontrol eder
     */
    public boolean isItemsAdderItem() {
        return itemsAdderId != null && !itemsAdderId.trim().isEmpty();
    }
    
    /**
     * Oraxen item olup olmadığını kontrol eder
     */
    public boolean isOraxenItem() {
        return oraxenId != null && !oraxenId.trim().isEmpty();
    }
    
    /**
     * CustomModelData olup olmadığını kontrol eder
     */
    public boolean hasCustomModelData() {
        return customModelData != null && customModelData > 0;
    }
    
    /**
     * Vanilla NBT item olup olmadığını kontrol eder
     */
    public boolean isVanillaItem() {
        return !isItemsAdderItem() && !isOraxenItem() && 
               serializedNBT != null && !serializedNBT.trim().isEmpty();
    }
}