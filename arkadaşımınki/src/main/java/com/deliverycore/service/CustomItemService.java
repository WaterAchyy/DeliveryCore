package com.deliverycore.service;

import com.deliverycore.model.CustomItem;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;

/**
 * Özel item yönetimi servisi
 * v1.1 özelliği - NBT serileştirme ve özel item desteği
 */
public interface CustomItemService {
    
    /**
     * Kategoriye özel item ekler
     * 
     * @param category Kategori adı
     * @param name Item adı
     * @param item ItemStack (NBT verileri ile)
     * @param price Fiyat
     */
    void addCustomItem(String category, String name, ItemStack item, double price);
    
    /**
     * Kategoriden özel item siler
     * 
     * @param category Kategori adı
     * @param name Item adı
     */
    void removeCustomItem(String category, String name);
    
    /**
     * Belirli kategorideki özel itemları döndürür
     * 
     * @param category Kategori adı
     * @return Özel itemlar listesi
     */
    List<CustomItem> getCustomItems(String category);
    
    /**
     * Tüm özel itemları döndürür
     * 
     * @return Tüm özel itemlar listesi
     */
    List<CustomItem> getAllCustomItems();
    
    /**
     * Belirli bir özel item'ı döndürür
     * 
     * @param category Kategori adı
     * @param name Item adı
     * @return Özel item (varsa)
     */
    Optional<CustomItem> getCustomItem(String category, String name);
    
    /**
     * ItemStack'in özel item ile eşleşip eşleşmediğini kontrol eder
     * 
     * @param item Kontrol edilecek ItemStack
     * @param customItem Özel item
     * @return Eşleşiyorsa true
     */
    boolean matchesCustomItem(ItemStack item, CustomItem customItem);
    
    /**
     * Özel itemları dosyaya kaydeder
     */
    void saveCustomItems();
    
    /**
     * Özel itemları dosyadan yükler
     */
    void loadCustomItems();
    
    /**
     * ItemStack'i NBT string'e serileştirir
     * 
     * @param item Serileştirilecek ItemStack
     * @return NBT string
     */
    String serializeItemStack(ItemStack item);
    
    /**
     * NBT string'i ItemStack'e deserileştirir
     * 
     * @param nbtString NBT string
     * @return ItemStack
     */
    Optional<ItemStack> deserializeItemStack(String nbtString);
    
    /**
     * Kategorideki özel item sayısını döndürür
     * 
     * @param category Kategori adı
     * @return Item sayısı
     */
    int getCustomItemCount(String category);
    
    /**
     * Özel item'ın var olup olmadığını kontrol eder
     * 
     * @param category Kategori adı
     * @param name Item adı
     * @return Varsa true
     */
    boolean customItemExists(String category, String name);
    
    /**
     * Tüm özel itemları temizler
     */
    void clearAllCustomItems();
    
    // ═══════════════════════════════════════════════════════════════════════════════
    // ItemsAdder Desteği
    // ═══════════════════════════════════════════════════════════════════════════════
    
    /**
     * ItemsAdder item ekler
     * 
     * @param category Kategori adı
     * @param name Item adı
     * @param itemsAdderId ItemsAdder namespace:id formatında
     * @param price Fiyat
     * @param displayName Görünen isim
     */
    void addItemsAdderItem(String category, String name, String itemsAdderId, double price, String displayName);
    
    /**
     * ItemsAdder item'ı ItemStack'e dönüştürür
     * 
     * @param itemsAdderId ItemsAdder namespace:id
     * @return ItemStack (varsa)
     */
    Optional<ItemStack> getItemsAdderItemStack(String itemsAdderId);
    
    /**
     * ItemsAdder yüklü mü kontrol eder
     * 
     * @return Yüklüyse true
     */
    boolean isItemsAdderEnabled();
    
    // ═══════════════════════════════════════════════════════════════════════════════
    // Oraxen Desteği
    // ═══════════════════════════════════════════════════════════════════════════════
    
    /**
     * Oraxen item ekler
     * 
     * @param category Kategori adı
     * @param name Item adı
     * @param oraxenId Oraxen item ID
     * @param price Fiyat
     * @param displayName Görünen isim
     */
    void addOraxenItem(String category, String name, String oraxenId, double price, String displayName);
    
    /**
     * Oraxen item'ı ItemStack'e dönüştürür
     * 
     * @param oraxenId Oraxen item ID
     * @return ItemStack (varsa)
     */
    Optional<ItemStack> getOraxenItemStack(String oraxenId);
    
    /**
     * Oraxen yüklü mü kontrol eder
     * 
     * @return Yüklüyse true
     */
    boolean isOraxenEnabled();
    
    // ═══════════════════════════════════════════════════════════════════════════════
    // CustomModelData Desteği
    // ═══════════════════════════════════════════════════════════════════════════════
    
    /**
     * CustomModelData ile item ekler
     * 
     * @param category Kategori adı
     * @param name Item adı
     * @param baseItem Temel Minecraft item (örn: DIAMOND_SWORD)
     * @param customModelData CustomModelData değeri
     * @param price Fiyat
     * @param displayName Görünen isim
     */
    void addCustomModelDataItem(String category, String name, String baseItem, int customModelData, double price, String displayName);
    
    /**
     * CustomModelData ile ItemStack oluşturur
     * 
     * @param baseItem Temel Minecraft item
     * @param customModelData CustomModelData değeri
     * @return ItemStack
     */
    Optional<ItemStack> createCustomModelDataItem(String baseItem, int customModelData);
    
    /**
     * ItemStack'in CustomModelData değerini döndürür
     * 
     * @param item ItemStack
     * @return CustomModelData değeri (yoksa -1)
     */
    int getCustomModelData(ItemStack item);
    
    /**
     * CustomItem'dan ItemStack oluşturur (tüm türler için)
     * 
     * @param customItem CustomItem
     * @return ItemStack (varsa)
     */
    Optional<ItemStack> createItemStack(CustomItem customItem);
}