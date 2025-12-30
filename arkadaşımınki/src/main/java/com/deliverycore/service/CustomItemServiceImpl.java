package com.deliverycore.service;

import com.deliverycore.model.CustomItem;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Özel item yönetimi servisi implementasyonu
 * v1.1 özelliği - NBT serileştirme ve dosya yönetimi
 */
public class CustomItemServiceImpl implements CustomItemService {
    
    private final Plugin plugin;
    private final Logger logger;
    private final Map<String, List<CustomItem>> customItems = new ConcurrentHashMap<>();
    private final File customItemsFile;
    
    public CustomItemServiceImpl(Plugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.customItemsFile = new File(plugin.getDataFolder(), "custom-items.yml");
    }
    
    @Override
    public void addCustomItem(String category, String name, ItemStack item, double price) {
        if (category == null || name == null || item == null) {
            throw new IllegalArgumentException("Category, name ve item null olamaz");
        }
        
        try {
            // ItemStack'i serileştir
            String serializedNBT = serializeItemStack(item);
            
            // Display name ve lore al
            String displayName = name;
            List<String> lore = new ArrayList<>();
            
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                if (meta.hasDisplayName()) {
                    displayName = meta.getDisplayName();
                }
                if (meta.hasLore()) {
                    lore = meta.getLore();
                }
            }
            
            // CustomItem oluştur
            CustomItem customItem = new CustomItem(category, name, serializedNBT, price, displayName, lore);
            
            // Kategoriye ekle
            customItems.computeIfAbsent(category, k -> new ArrayList<>()).add(customItem);
            
            logger.info("Özel item eklendi: " + category + ":" + name + " (fiyat: " + price + ")");
            
        } catch (Exception e) {
            logger.severe("Özel item eklenirken hata: " + e.getMessage());
            throw new RuntimeException("Özel item eklenemedi", e);
        }
    }
    
    @Override
    public void removeCustomItem(String category, String name) {
        List<CustomItem> categoryItems = customItems.get(category);
        if (categoryItems != null) {
            categoryItems.removeIf(item -> item.name().equals(name));
            logger.info("Özel item silindi: " + category + ":" + name);
        }
    }
    
    @Override
    public List<CustomItem> getCustomItems(String category) {
        return customItems.getOrDefault(category, new ArrayList<>());
    }
    
    @Override
    public List<CustomItem> getAllCustomItems() {
        List<CustomItem> allItems = new ArrayList<>();
        customItems.values().forEach(allItems::addAll);
        return allItems;
    }
    
    @Override
    public Optional<CustomItem> getCustomItem(String category, String name) {
        return getCustomItems(category).stream()
            .filter(item -> item.name().equals(name))
            .findFirst();
    }
    
    @Override
    public boolean matchesCustomItem(ItemStack item, CustomItem customItem) {
        if (item == null || customItem == null) {
            return false;
        }
        
        try {
            // ItemsAdder item kontrolü
            if (customItem.isItemsAdderItem()) {
                return matchesItemsAdderItem(item, customItem.itemsAdderId());
            }
            
            // Oraxen item kontrolü
            if (customItem.isOraxenItem()) {
                return matchesOraxenItem(item, customItem.oraxenId());
            }
            
            // CustomModelData kontrolü
            if (customItem.hasCustomModelData()) {
                int itemCMD = getCustomModelData(item);
                if (itemCMD != customItem.customModelData()) {
                    return false;
                }
            }
            
            // Vanilla NBT karşılaştırma
            if (customItem.isVanillaItem()) {
                String itemNBT = serializeItemStack(item);
                return itemNBT.equals(customItem.serializedNBT());
            }
            
            return false;
            
        } catch (Exception e) {
            logger.warning("Item eşleştirme hatası: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * ItemsAdder item eşleştirmesi
     */
    private boolean matchesItemsAdderItem(ItemStack item, String itemsAdderId) {
        if (!isItemsAdderEnabled()) {
            return false;
        }
        
        try {
            Class<?> customStackClass = Class.forName("dev.lone.itemsadder.api.CustomStack");
            var byItemStackMethod = customStackClass.getMethod("byItemStack", ItemStack.class);
            var customStack = byItemStackMethod.invoke(null, item);
            
            if (customStack != null) {
                var getNamespacedIDMethod = customStackClass.getMethod("getNamespacedID");
                String id = (String) getNamespacedIDMethod.invoke(customStack);
                return itemsAdderId.equals(id);
            }
        } catch (Exception e) {
            logger.fine("ItemsAdder eşleştirme hatası: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Oraxen item eşleştirmesi
     */
    private boolean matchesOraxenItem(ItemStack item, String oraxenId) {
        if (!isOraxenEnabled()) {
            return false;
        }
        
        try {
            Class<?> oraxenItemsClass = Class.forName("io.th0rgal.oraxen.api.OraxenItems");
            var getIdByItemMethod = oraxenItemsClass.getMethod("getIdByItem", ItemStack.class);
            String id = (String) getIdByItemMethod.invoke(null, item);
            return oraxenId.equals(id);
        } catch (Exception e) {
            logger.fine("Oraxen eşleştirme hatası: " + e.getMessage());
        }
        
        return false;
    }
    
    @Override
    public void saveCustomItems() {
        if (customItems.isEmpty()) return;
        
        try {
            YamlConfiguration config = new YamlConfiguration();
            
            for (Map.Entry<String, List<CustomItem>> entry : customItems.entrySet()) {
                String category = entry.getKey();
                List<CustomItem> items = entry.getValue();
                
                for (int i = 0; i < items.size(); i++) {
                    CustomItem item = items.get(i);
                    String path = category + "." + i;
                    
                    config.set(path + ".name", item.name());
                    config.set(path + ".nbt", item.serializedNBT());
                    config.set(path + ".price", item.price());
                    config.set(path + ".displayName", item.displayName());
                    config.set(path + ".lore", item.lore());
                    
                    // v1.1 alanları
                    if (item.customModelData() != null) {
                        config.set(path + ".customModelData", item.customModelData());
                    }
                    if (item.itemsAdderId() != null && !item.itemsAdderId().isEmpty()) {
                        config.set(path + ".itemsAdderId", item.itemsAdderId());
                    }
                    if (item.oraxenId() != null && !item.oraxenId().isEmpty()) {
                        config.set(path + ".oraxenId", item.oraxenId());
                    }
                }
            }
            
            config.save(customItemsFile);
            logger.info("Özel itemlar kaydedildi: " + customItemsFile.getName());
            
        } catch (Exception e) {
            logger.severe("Özel itemlar kaydedilemedi: " + e.getMessage());
        }
    }
    
    @Override
    public void loadCustomItems() {
        if (!customItemsFile.exists()) {
            return;
        }
        
        try {
            customItems.clear();
            YamlConfiguration config = YamlConfiguration.loadConfiguration(customItemsFile);
            
            for (String category : config.getKeys(false)) {
                if (!config.isConfigurationSection(category)) {
                    continue;
                }
                
                var categorySection = config.getConfigurationSection(category);
                if (categorySection == null) {
                    continue;
                }
                
                for (String itemKey : categorySection.getKeys(false)) {
                    try {
                        String path = category + "." + itemKey;
                        
                        String name = config.getString(path + ".name");
                        String nbt = config.getString(path + ".nbt", "");
                        double price = config.getDouble(path + ".price");
                        String displayName = config.getString(path + ".displayName", name);
                        List<String> lore = config.getStringList(path + ".lore");
                        
                        // v1.1 alanları
                        Integer customModelData = config.contains(path + ".customModelData") 
                            ? config.getInt(path + ".customModelData") : null;
                        String itemsAdderId = config.getString(path + ".itemsAdderId", null);
                        String oraxenId = config.getString(path + ".oraxenId", null);
                        
                        if (name != null) {
                            CustomItem item = new CustomItem(category, name, nbt, price, displayName, lore, 
                                                            customModelData, itemsAdderId, oraxenId);
                            customItems.computeIfAbsent(category, k -> new ArrayList<>()).add(item);
                        }
                        
                    } catch (Exception e) {
                        logger.warning("Özel item yüklenirken hata: " + category + "." + itemKey + " - " + e.getMessage());
                    }
                }
            }
            
            int totalItems = getAllCustomItems().size();
            logger.info("Özel itemlar yüklendi: " + totalItems + " item");
            
        } catch (Exception e) {
            logger.severe("Özel itemlar yüklenemedi: " + e.getMessage());
        }
    }
    
    @Override
    public String serializeItemStack(ItemStack item) {
        if (item == null) {
            return "";
        }
        
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            
            dataOutput.writeObject(item);
            dataOutput.close();
            
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
            
        } catch (Exception e) {
            logger.severe("ItemStack serileştirme hatası: " + e.getMessage());
            return "";
        }
    }
    
    @Override
    public Optional<ItemStack> deserializeItemStack(String nbtString) {
        if (nbtString == null || nbtString.trim().isEmpty()) {
            return Optional.empty();
        }
        
        try {
            byte[] data = Base64.getDecoder().decode(nbtString);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            
            ItemStack item = (ItemStack) dataInput.readObject();
            dataInput.close();
            
            return Optional.of(item);
            
        } catch (Exception e) {
            logger.severe("ItemStack deserileştirme hatası: " + e.getMessage());
            return Optional.empty();
        }
    }
    
    @Override
    public int getCustomItemCount(String category) {
        return getCustomItems(category).size();
    }
    
    @Override
    public boolean customItemExists(String category, String name) {
        return getCustomItem(category, name).isPresent();
    }
    
    @Override
    public void clearAllCustomItems() {
        customItems.clear();
        logger.info("Tüm özel itemlar temizlendi");
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════
    // ItemsAdder Desteği
    // ═══════════════════════════════════════════════════════════════════════════════
    
    @Override
    public void addItemsAdderItem(String category, String name, String itemsAdderId, double price, String displayName) {
        if (category == null || name == null || itemsAdderId == null) {
            throw new IllegalArgumentException("Category, name ve itemsAdderId null olamaz");
        }
        
        CustomItem customItem = CustomItem.itemsAdder(category, name, itemsAdderId, price, displayName);
        customItems.computeIfAbsent(category, k -> new ArrayList<>()).add(customItem);
        
        logger.info("ItemsAdder item eklendi: " + category + ":" + name + " (ID: " + itemsAdderId + ")");
    }
    
    @Override
    public Optional<ItemStack> getItemsAdderItemStack(String itemsAdderId) {
        if (!isItemsAdderEnabled()) {
            logger.warning("ItemsAdder yüklü değil!");
            return Optional.empty();
        }
        
        try {
            // ItemsAdder API kullanarak item al
            Class<?> customStackClass = Class.forName("dev.lone.itemsadder.api.CustomStack");
            var getInstanceMethod = customStackClass.getMethod("getInstance", String.class);
            var customStack = getInstanceMethod.invoke(null, itemsAdderId);
            
            if (customStack != null) {
                var getItemStackMethod = customStackClass.getMethod("getItemStack");
                ItemStack item = (ItemStack) getItemStackMethod.invoke(customStack);
                return Optional.ofNullable(item);
            }
        } catch (ClassNotFoundException e) {
            logger.fine("ItemsAdder API bulunamadı");
        } catch (Exception e) {
            logger.warning("ItemsAdder item alınamadı: " + itemsAdderId + " - " + e.getMessage());
        }
        
        return Optional.empty();
    }
    
    @Override
    public boolean isItemsAdderEnabled() {
        return plugin.getServer().getPluginManager().getPlugin("ItemsAdder") != null;
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════
    // Oraxen Desteği
    // ═══════════════════════════════════════════════════════════════════════════════
    
    @Override
    public void addOraxenItem(String category, String name, String oraxenId, double price, String displayName) {
        if (category == null || name == null || oraxenId == null) {
            throw new IllegalArgumentException("Category, name ve oraxenId null olamaz");
        }
        
        CustomItem customItem = CustomItem.oraxen(category, name, oraxenId, price, displayName);
        customItems.computeIfAbsent(category, k -> new ArrayList<>()).add(customItem);
        
        logger.info("Oraxen item eklendi: " + category + ":" + name + " (ID: " + oraxenId + ")");
    }
    
    @Override
    public Optional<ItemStack> getOraxenItemStack(String oraxenId) {
        if (!isOraxenEnabled()) {
            logger.warning("Oraxen yüklü değil!");
            return Optional.empty();
        }
        
        try {
            // Oraxen API kullanarak item al
            Class<?> oraxenItemsClass = Class.forName("io.th0rgal.oraxen.api.OraxenItems");
            var getItemByIdMethod = oraxenItemsClass.getMethod("getItemById", String.class);
            var itemBuilder = getItemByIdMethod.invoke(null, oraxenId);
            
            if (itemBuilder != null) {
                var buildMethod = itemBuilder.getClass().getMethod("build");
                ItemStack item = (ItemStack) buildMethod.invoke(itemBuilder);
                return Optional.ofNullable(item);
            }
        } catch (ClassNotFoundException e) {
            logger.fine("Oraxen API bulunamadı");
        } catch (Exception e) {
            logger.warning("Oraxen item alınamadı: " + oraxenId + " - " + e.getMessage());
        }
        
        return Optional.empty();
    }
    
    @Override
    public boolean isOraxenEnabled() {
        return plugin.getServer().getPluginManager().getPlugin("Oraxen") != null;
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════
    // CustomModelData Desteği
    // ═══════════════════════════════════════════════════════════════════════════════
    
    @Override
    public void addCustomModelDataItem(String category, String name, String baseItem, int customModelData, double price, String displayName) {
        if (category == null || name == null || baseItem == null) {
            throw new IllegalArgumentException("Category, name ve baseItem null olamaz");
        }
        
        // ItemStack oluştur ve serileştir
        Optional<ItemStack> itemOpt = createCustomModelDataItem(baseItem, customModelData);
        if (itemOpt.isEmpty()) {
            throw new IllegalArgumentException("Geçersiz base item: " + baseItem);
        }
        
        String serializedNBT = serializeItemStack(itemOpt.get());
        CustomItem customItem = CustomItem.withCustomModelData(category, name, serializedNBT, price, displayName, customModelData);
        customItems.computeIfAbsent(category, k -> new ArrayList<>()).add(customItem);
        
        logger.info("CustomModelData item eklendi: " + category + ":" + name + " (CMD: " + customModelData + ")");
    }
    
    @Override
    public Optional<ItemStack> createCustomModelDataItem(String baseItem, int customModelData) {
        try {
            org.bukkit.Material material = org.bukkit.Material.valueOf(baseItem.toUpperCase());
            ItemStack item = new ItemStack(material);
            
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setCustomModelData(customModelData);
                item.setItemMeta(meta);
            }
            
            return Optional.of(item);
        } catch (IllegalArgumentException e) {
            logger.warning("Geçersiz material: " + baseItem);
            return Optional.empty();
        }
    }
    
    @Override
    public int getCustomModelData(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return -1;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.hasCustomModelData()) {
            return meta.getCustomModelData();
        }
        
        return -1;
    }
    
    @Override
    public Optional<ItemStack> createItemStack(CustomItem customItem) {
        if (customItem == null) {
            return Optional.empty();
        }
        
        // ItemsAdder item
        if (customItem.isItemsAdderItem()) {
            return getItemsAdderItemStack(customItem.itemsAdderId());
        }
        
        // Oraxen item
        if (customItem.isOraxenItem()) {
            return getOraxenItemStack(customItem.oraxenId());
        }
        
        // Vanilla NBT item
        if (customItem.isVanillaItem()) {
            return deserializeItemStack(customItem.serializedNBT());
        }
        
        return Optional.empty();
    }
}