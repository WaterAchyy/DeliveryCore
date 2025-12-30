package com.deliverycore.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;

public class GUIConfig {
    
    private final File dataFolder;
    private final Logger logger;
    private YamlConfiguration config;
    
    private String mainTitle = "ᴛᴇsʟɪᴍᴀᴛ ᴍᴇɴᴜ̈sᴜ̈";
    private int mainSize = 54;
    private Material bgMaterial = Material.BLACK_STAINED_GLASS_PANE;
    
    private String deliveryTitle = "ᴛᴇsʟɪᴍᴀᴛ: {delivery}";
    private int deliverySize = 45;
    
    private String leaderboardTitle = "sɪʀᴀʟᴀᴍᴀ";
    private int leaderboardSize = 54;
    
    private int[] activeSlots = {11, 12, 13, 14, 15};
    private int[] waitingSlots = {29, 30, 31, 32, 33};
    private int[] rankSlots = {13, 21, 23, 29, 30, 31, 32, 33, 38, 42};
    private String[] rankColors = {"§6", "§f", "§c", "§7", "§7", "§7", "§7", "§7", "§7", "§7"};
    
    private final Map<String, ItemConfig> itemConfigs = new HashMap<>();
    private String customItemProvider = "vanilla";
    
    public GUIConfig(File dataFolder, Logger logger) {
        this.dataFolder = dataFolder;
        this.logger = logger;
    }
    
    public void load() {
        File guiFile = new File(dataFolder, "gui.yml");
        if (!guiFile.exists()) return;
        
        try {
            config = YamlConfiguration.loadConfiguration(guiFile);
            
            // Main menu
            mainTitle = config.getString("main-menu.title", mainTitle);
            mainSize = config.getInt("main-menu.size", mainSize);
            String bgMat = config.getString("main-menu.background.material", "BLACK_STAINED_GLASS_PANE");
            try { bgMaterial = Material.valueOf(bgMat); } catch (Exception ignored) {}
            
            // Slots
            activeSlots = toIntArray(config.getIntegerList("main-menu.active-slots"), activeSlots);
            waitingSlots = toIntArray(config.getIntegerList("main-menu.waiting-slots"), waitingSlots);
            
            // Delivery menu
            deliveryTitle = config.getString("delivery-menu.title", deliveryTitle);
            deliverySize = config.getInt("delivery-menu.size", deliverySize);
            
            // Leaderboard menu
            leaderboardTitle = config.getString("leaderboard-menu.title", leaderboardTitle);
            leaderboardSize = config.getInt("leaderboard-menu.size", leaderboardSize);
            rankSlots = toIntArray(config.getIntegerList("leaderboard-menu.rank-slots"), rankSlots);
            List<String> colors = config.getStringList("leaderboard-menu.rank-colors");
            if (!colors.isEmpty()) rankColors = colors.toArray(new String[0]);
            
            // Custom items provider
            customItemProvider = config.getString("custom-items.provider", "vanilla");
            
            // Load item configs
            loadItemConfigs("main-menu.items");
            loadItemConfigs("delivery-menu.items");
            loadItemConfigs("leaderboard-menu.items");
            
            logger.info("[GUI] Config yuklendi");
        } catch (Exception e) {
            logger.warning("[GUI] Config yuklenemedi: " + e.getMessage());
        }
    }
    
    private void loadItemConfigs(String path) {
        if (config == null) return;
        ConfigurationSection section = config.getConfigurationSection(path);
        if (section == null) return;
        
        for (String key : section.getKeys(false)) {
            String itemPath = path + "." + key;
            ItemConfig itemConfig = new ItemConfig();
            itemConfig.slot = config.getInt(itemPath + ".slot", -1);
            itemConfig.material = config.getString(itemPath + ".material", "PLAYER_HEAD");
            itemConfig.texture = config.getString(itemPath + ".texture", null);
            itemConfig.name = config.getString(itemPath + ".name", null);
            itemConfig.lore = config.getStringList(itemPath + ".lore");
            itemConfigs.put(key, itemConfig);
        }
    }
    
    private int[] toIntArray(List<Integer> list, int[] defaultVal) {
        if (list == null || list.isEmpty()) return defaultVal;
        return list.stream().mapToInt(Integer::intValue).toArray();
    }
    
    public ItemStack createItem(String configKey, Map<String, String> placeholders) {
        ItemConfig cfg = itemConfigs.get(configKey);
        if (cfg == null) return createDefaultItem(configKey);
        
        ItemStack item = parseItem(cfg.material, cfg.texture);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (cfg.name != null) {
                String name = replacePlaceholders(cfg.name, placeholders);
                meta.setDisplayName(name);
            }
            if (cfg.lore != null && !cfg.lore.isEmpty()) {
                List<String> lore = new ArrayList<>();
                for (String line : cfg.lore) {
                    lore.add(replacePlaceholders(line, placeholders));
                }
                meta.setLore(lore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }
    
    public ItemStack parseItem(String material, String texture) {
        if (material == null) material = "STONE";
        
        // ItemsAdder
        if (material.startsWith("itemsadder:")) {
            ItemStack item = getItemsAdderItem(material.substring(11));
            if (item != null) return item;
        }
        
        // Oraxen
        if (material.startsWith("oraxen:")) {
            ItemStack item = getOraxenItem(material.substring(7));
            if (item != null) return item;
        }
        
        // Nexo
        if (material.startsWith("nexo:")) {
            ItemStack item = getNexoItem(material.substring(5));
            if (item != null) return item;
        }
        
        // Player Head with texture
        if (material.equals("PLAYER_HEAD") && texture != null) {
            return createTexturedHead(texture);
        }
        
        // Vanilla material
        try {
            Material mat = Material.valueOf(material.toUpperCase());
            return new ItemStack(mat);
        } catch (Exception e) {
            return new ItemStack(Material.STONE);
        }
    }
    
    public ItemStack createTexturedHead(String texture) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            applyTexture(meta, texture);
            head.setItemMeta(meta);
        }
        return head;
    }
    
    private void applyTexture(SkullMeta meta, String texture) {
        try {
            String decoded = new String(Base64.getDecoder().decode(texture));
            int urlStart = decoded.indexOf("http");
            int urlEnd = decoded.indexOf("\"", urlStart);
            if (urlStart < 0 || urlEnd <= urlStart) return;
            
            String skinUrl = decoded.substring(urlStart, urlEnd);
            URL url = new URL(skinUrl);
            
            PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID(), "GUI");
            PlayerTextures textures = profile.getTextures();
            textures.setSkin(url);
            profile.setTextures(textures);
            meta.setOwnerProfile(profile);
        } catch (Exception e) {
            logger.warning("[GUI] Texture hatasi: " + e.getMessage());
        }
    }
    
    private ItemStack getItemsAdderItem(String id) {
        try {
            Class<?> cls = Class.forName("dev.lone.itemsadder.api.CustomStack");
            Object stack = cls.getMethod("getInstance", String.class).invoke(null, id);
            if (stack != null) {
                return (ItemStack) cls.getMethod("getItemStack").invoke(stack);
            }
        } catch (Exception ignored) {}
        return null;
    }
    
    private ItemStack getOraxenItem(String id) {
        try {
            Class<?> cls = Class.forName("io.th0rgal.oraxen.api.OraxenItems");
            Object builder = cls.getMethod("getItemById", String.class).invoke(null, id);
            if (builder != null) {
                return (ItemStack) builder.getClass().getMethod("build").invoke(builder);
            }
        } catch (Exception ignored) {}
        return null;
    }
    
    private ItemStack getNexoItem(String id) {
        try {
            Class<?> cls = Class.forName("com.nexomc.nexo.api.NexoItems");
            Object builder = cls.getMethod("itemFromId", String.class).invoke(null, id);
            if (builder != null) {
                return (ItemStack) builder.getClass().getMethod("build").invoke(builder);
            }
        } catch (Exception ignored) {}
        return null;
    }
    
    private ItemStack createDefaultItem(String key) {
        return new ItemStack(Material.STONE);
    }
    
    private String replacePlaceholders(String text, Map<String, String> placeholders) {
        if (text == null || placeholders == null) return text;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            text = text.replace(entry.getKey(), entry.getValue());
        }
        return text;
    }
    
    // Getters
    public String getMainTitle() { return mainTitle; }
    public int getMainSize() { return mainSize; }
    public Material getBgMaterial() { return bgMaterial; }
    public String getDeliveryTitle() { return deliveryTitle; }
    public int getDeliverySize() { return deliverySize; }
    public String getLeaderboardTitle() { return leaderboardTitle; }
    public int getLeaderboardSize() { return leaderboardSize; }
    public int[] getActiveSlots() { return activeSlots; }
    public int[] getWaitingSlots() { return waitingSlots; }
    public int[] getRankSlots() { return rankSlots; }
    public String[] getRankColors() { return rankColors; }
    public ItemConfig getItemConfig(String key) { return itemConfigs.get(key); }
    public String getCustomItemProvider() { return customItemProvider; }
    
    public static class ItemConfig {
        public int slot = -1;
        public String material;
        public String texture;
        public String name;
        public List<String> lore;
    }
}
