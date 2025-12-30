package com.deliverycore.gui;

import com.deliverycore.config.ConfigManager;
import com.deliverycore.service.ActiveEvent;
import com.deliverycore.service.DeliveryService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class DeliveryGUI {
    private final ConfigManager configManager;
    private final DeliveryService deliveryService;
    private GUIConfig guiConfig;
    
    private final Map<String, String> itemDisplayNames = new ConcurrentHashMap<>();
    private final Map<String, Double> itemPrices = new ConcurrentHashMap<>();
    private final Map<String, String> categoryDisplayNames = new ConcurrentHashMap<>();
    private final Map<String, String> deliveryDisplayNames = new ConcurrentHashMap<>();
    private final Map<String, String> categoryHeads = new ConcurrentHashMap<>();
    private final Map<UUID, String> chestSelectionMode = new ConcurrentHashMap<>();
    private final Map<UUID, Long> chestSelectionTimeout = new ConcurrentHashMap<>();
    private final Map<UUID, String> playerNameCache = new ConcurrentHashMap<>();
    
    private Supplier<String> languageSupplier;
    private File dataFolder;
    private YamlConfiguration langConfig;
    private Logger logger;

    // Head Textures (varsayılan)
    private String HEAD_DELIVERY = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGFhMTg3ZmVkZTg4ZGUwMDJjYmQ5MzA1NzVlYjdiYTQ4ZDNiMWEwNmQ5NjFiZGM1MzU4MDA3NTBhZjc2NDkyNiJ9fX0=";
    private String HEAD_CHEST = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDVjNmRjMmJiZjUxYzM2Y2ZjNzcxNDU4NWE2YTU2ODNlZjJiMTRkNDdkOGZmNzE0NjU0YTg5M2Y1ZGE2MjIifX19";
    private String HEAD_INVENTORY = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWQ5Y2M1OGFkMjVhMWFiMTZkMzZiYjVkNmQ0OTNjOGY1ODkxMjMyNTEyYjVjNDg0YzY2Y2ZkM2I0YjZhYzRiNCJ9fX0=";
    private String HEAD_TOP = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjdkYzNlMjlhMDkyM2U1MmVjZWU2YjRjOWQ1MzNhNzllNzRiYjZiZWQ1NDFiNDk1YTEzYWJkMzU5NjI3NjUzIn19fQ==";
    private String HEAD_HELP = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmFkYzA0OGE3Y2U3OGY3ZGFkNzJhMDdkYTI3ZDg1YzA5MTY4ODFlNTUyMmVlZWQxZTNkYWYyMTdhMzhjMWEifX19";
    private String HEAD_BACK = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQ2OWUwNmU1ZGFkZmQ4NGU1ZjNkMWMyMTA2M2YyNTUzYjJmYTk0NWVlMWQ0ZDcxNTJmZGM1NDI1YmMxMmE5In19fQ==";
    private String HEAD_WAITING = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmNlZjlhYTE0ZTg4NDc3M2VhYzEzNGE0ZWU4OTcyMDYzZjQ2NmRlNjc4MzYzY2Y3YjFhMjFhODViNyJ9fX0=";

    private static final String NORMAL = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String SMALL = "ᴀʙᴄᴅᴇꜰɢʜɪᴊᴋʟᴍɴᴏᴘǫʀsᴛᴜᴠᴡxʏᴢᴀʙᴄᴅᴇꜰɢʜɪᴊᴋʟᴍɴᴏᴘǫʀsᴛᴜᴠᴡxʏᴢ";

    public static final String MAIN_TITLE_TR = "ᴛᴇsʟɪᴍᴀᴛ ᴍᴇɴᴜ̈sᴜ̈";
    public static final String MAIN_TITLE_EN = "ᴅᴇʟɪᴠᴇʀʏ ᴍᴇɴᴜ";
    public static final String DELIVERY_PREFIX_TR = "ᴛᴇsʟɪᴍᴀᴛ: ";
    public static final String DELIVERY_PREFIX_EN = "ᴅᴇʟɪᴠᴇʀʏ: ";
    public static final String LEADERBOARD_TITLE_TR = "sɪʀᴀʟᴀᴍᴀ";
    public static final String LEADERBOARD_TITLE_EN = "ʟᴇᴀᴅᴇʀʙᴏᴀʀᴅ";
    
    public static final String MAIN_TITLE = MAIN_TITLE_TR;
    public static final String DELIVERY_PREFIX = DELIVERY_PREFIX_TR;
    public static final String LEADERBOARD_TITLE = LEADERBOARD_TITLE_TR;

    public DeliveryGUI(ConfigManager cm, DeliveryService ds) {
        this.configManager = cm;
        this.deliveryService = ds;
        initDefaults();
    }
    
    public void setLogger(Logger logger) {
        this.logger = logger;
    }
    
    public void loadGUIConfig() {
        if (dataFolder == null) return;
        guiConfig = new GUIConfig(dataFolder, logger != null ? logger : Logger.getLogger("DeliveryGUI"));
        guiConfig.load();
        
        // Config'den texture'ları yükle
        GUIConfig.ItemConfig cfg;
        if ((cfg = guiConfig.getItemConfig("delivery-head")) != null && cfg.texture != null) HEAD_DELIVERY = cfg.texture;
        if ((cfg = guiConfig.getItemConfig("leaderboard-button")) != null && cfg.texture != null) HEAD_TOP = cfg.texture;
        if ((cfg = guiConfig.getItemConfig("help-button")) != null && cfg.texture != null) HEAD_HELP = cfg.texture;
        if ((cfg = guiConfig.getItemConfig("waiting-head")) != null && cfg.texture != null) HEAD_WAITING = cfg.texture;
        if ((cfg = guiConfig.getItemConfig("inventory-deliver")) != null && cfg.texture != null) HEAD_INVENTORY = cfg.texture;
        if ((cfg = guiConfig.getItemConfig("chest-deliver")) != null && cfg.texture != null) HEAD_CHEST = cfg.texture;
        if ((cfg = guiConfig.getItemConfig("back")) != null && cfg.texture != null) HEAD_BACK = cfg.texture;
    }

    private void initDefaults() {
        categoryDisplayNames.put("farm", "Çiftlik");
        categoryDisplayNames.put("ore", "Maden");
        categoryDisplayNames.put("block", "Blok");
        categoryDisplayNames.put("food", "Yiyecek");
        categoryDisplayNames.put("wood", "Odun");
        categoryDisplayNames.put("rare", "Nadir");
        categoryDisplayNames.put("nether", "Nether");
        categoryDisplayNames.put("mob", "Mob Drop");
        categoryDisplayNames.put("end", "End");
        categoryDisplayNames.put("dye", "Boya");
        categoryDisplayNames.put("tool", "Alet");
        categoryDisplayNames.put("armor", "Zırh");
        categoryDisplayNames.put("combat", "Savaş");
        categoryDisplayNames.put("potion", "İksir");
        categoryDisplayNames.put("decoration", "Dekorasyon");
        categoryDisplayNames.put("redstone", "Kızıltaş");
        categoryDisplayNames.put("misc", "Çeşitli");
    }
    
    /**
     * Dil sağlayıcısını ayarlar
     */
    public void setLanguageSupplier(Supplier<String> supplier) {
        this.languageSupplier = supplier;
    }
    
    /**
     * Data folder'ı ayarlar (dil dosyalarını okumak için)
     */
    public void setDataFolder(File folder) {
        this.dataFolder = folder;
        reloadLanguage();
    }
    
    /**
     * Dil dosyasını yeniden yükler
     */
    public void reloadLanguage() {
        if (dataFolder == null) return;
        String lang = getCurrentLanguage();
        File langFile = new File(dataFolder, "lang/" + lang + ".yml");
        if (langFile.exists()) {
            langConfig = YamlConfiguration.loadConfiguration(langFile);
        }
    }
    
    /**
     * Mevcut dili döndürür
     */
    private String getCurrentLanguage() {
        if (languageSupplier != null) {
            return languageSupplier.get();
        }
        return "tr";
    }
    
    /**
     * Dil dosyasından mesaj alır
     */
    private String getLang(String path, String defaultValue) {
        if (langConfig != null) {
            return langConfig.getString(path, defaultValue);
        }
        return defaultValue;
    }
    
    /**
     * Mevcut dile göre ana menü başlığını döndürür
     */
    public String getMainTitle() {
        return "en".equals(getCurrentLanguage()) ? MAIN_TITLE_EN : MAIN_TITLE_TR;
    }
    
    /**
     * Mevcut dile göre teslimat prefix'ini döndürür
     */
    public String getDeliveryPrefix() {
        return "en".equals(getCurrentLanguage()) ? DELIVERY_PREFIX_EN : DELIVERY_PREFIX_TR;
    }
    
    /**
     * Mevcut dile göre sıralama başlığını döndürür
     */
    public String getLeaderboardTitle() {
        return "en".equals(getCurrentLanguage()) ? LEADERBOARD_TITLE_EN : LEADERBOARD_TITLE_TR;
    }

    public void loadItemsConfig(File dataFolder) {
        File itemsFile = new File(dataFolder, "items.yml");
        if (!itemsFile.exists()) {
            System.out.println("[DeliveryGUI] items.yml bulunamadi: " + itemsFile.getAbsolutePath());
            return;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(itemsFile);
        var itemsSection = config.getConfigurationSection("items");
        if (itemsSection == null) {
            System.out.println("[DeliveryGUI] items.yml'de 'items' section bulunamadi!");
            return;
        }
        int loadedCount = 0;
        for (String key : itemsSection.getKeys(false)) {
            String displayName = itemsSection.getString(key + ".display-name");
            double price = itemsSection.getDouble(key + ".price", 0);
            if (displayName != null) {
                itemDisplayNames.put(key.toUpperCase(), displayName);
                loadedCount++;
            }
            if (price > 0) itemPrices.put(key.toUpperCase(), price);
        }
        System.out.println("[DeliveryGUI] " + loadedCount + " esya yuklendi (items.yml)");
    }

    public static String sc(String text) {
        if (text == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            int idx = NORMAL.indexOf(c);
            sb.append(idx >= 0 ? SMALL.charAt(idx) : c);
        }
        return sb.toString();
    }

    public String getItemDisplayName(String itemName) {
        if (itemName == null) return "Bilinmiyor";
        return itemDisplayNames.getOrDefault(itemName.toUpperCase(), itemName.replace("_", " "));
    }
    public double getItemPrice(String itemName) { return itemPrices.getOrDefault(itemName.toUpperCase(), 0.0); }
    public String getCategoryDisplayName(String catName) { return categoryDisplayNames.getOrDefault(catName.toLowerCase(), catName); }
    public String getDeliveryDisplayName(String deliveryName) { return deliveryDisplayNames.getOrDefault(deliveryName.toLowerCase(), deliveryName); }
    public void setCategoryDisplayName(String cat, String name) { categoryDisplayNames.put(cat.toLowerCase(), name); }
    public void setDeliveryDisplayName(String del, String name) { deliveryDisplayNames.put(del.toLowerCase(), name); }
    public void setCategoryHead(String cat, String texture) { categoryHeads.put(cat.toLowerCase(), texture); }
    public void setItemDisplayName(String item, String name) { itemDisplayNames.put(item.toUpperCase(), name); }
    public void setChestCapacity(int capacity) { }
    public void cachePlayerName(UUID id, String name) { if (id != null && name != null) playerNameCache.put(id, name); }

    public void startChestSelection(Player player, String deliveryName) {
        chestSelectionMode.put(player.getUniqueId(), deliveryName);
        chestSelectionTimeout.put(player.getUniqueId(), System.currentTimeMillis() + 10000);
    }
    public boolean isInChestSelectionMode(UUID id) {
        Long timeout = chestSelectionTimeout.get(id);
        if (timeout == null) return false;
        if (System.currentTimeMillis() > timeout) { cancelChestSelection(id); return false; }
        return chestSelectionMode.containsKey(id);
    }
    public String getChestSelectionDelivery(UUID id) { return chestSelectionMode.get(id); }
    public void cancelChestSelection(UUID id) { chestSelectionMode.remove(id); chestSelectionTimeout.remove(id); }

    public static boolean isDeliveryGUI(String title) { 
        return title.contains(MAIN_TITLE_TR) || title.contains(MAIN_TITLE_EN) || 
               title.contains(DELIVERY_PREFIX_TR) || title.contains(DELIVERY_PREFIX_EN) || 
               title.contains(LEADERBOARD_TITLE_TR) || title.contains(LEADERBOARD_TITLE_EN); 
    }
    public static boolean isMainMenu(String title) { 
        return title.contains(MAIN_TITLE_TR) || title.contains(MAIN_TITLE_EN); 
    }
    public static boolean isDeliveryMenu(String title) { 
        return title.contains(DELIVERY_PREFIX_TR) || title.contains(DELIVERY_PREFIX_EN); 
    }
    public static boolean isLeaderboardMenu(String title) { 
        return title.contains(LEADERBOARD_TITLE_TR) || title.contains(LEADERBOARD_TITLE_EN); 
    }


    // ═══════════════════════════════════════════════════════════════════════════
    // ANA MENÜ
    // ═══════════════════════════════════════════════════════════════════════════

    public void openMainMenu(Player player) {
        boolean isEnglish = "en".equals(getCurrentLanguage());
        Inventory inv = Bukkit.createInventory(null, 54, getMainTitle());
        
        ItemStack bg = createItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 54; i++) inv.setItem(i, bg);

        // Aktif teslimatlar (slot 11-15 üst sıra)
        var events = deliveryService.getAllActiveEvents();
        int[] activeSlots = {11, 12, 13, 14, 15};
        
        for (int i = 0; i < events.size() && i < activeSlots.length; i++) {
            ActiveEvent event = events.get(i);
            inv.setItem(activeSlots[i], createDeliveryItem(player, event));
        }
        
        // Bekleyen teslimatlar (slot 29-33 alt sıra) - aktif olmayan ama enabled olanlar
        var allDeliveries = configManager.getDeliveryConfig().getEnabledDeliveries();
        int[] waitingSlots = {29, 30, 31, 32, 33};
        int waitingIndex = 0;
        
        for (var delivery : allDeliveries) {
            boolean isActive = events.stream().anyMatch(e -> e.getDeliveryName().equals(delivery.name()));
            if (isActive) continue;
            
            if (waitingIndex < waitingSlots.length) {
                inv.setItem(waitingSlots[waitingIndex], createWaitingDeliveryItem(delivery));
                waitingIndex++;
            }
        }

        // Sıralama butonu (slot 47)
        String leaderboardName = isEnglish ? sc("leaderboard") : sc("siralama");
        String leaderboardLore = isEnglish ? sc("see top deliverers") : sc("en cok teslim edenleri gor");
        String clickText = isEnglish ? sc("click") : sc("tikla");
        inv.setItem(47, createHead(HEAD_TOP, "§b" + leaderboardName, Arrays.asList(
            "",
            "§7" + leaderboardLore,
            "",
            "§a" + clickText
        )));

        // Yardım butonu (slot 51)
        String helpName = isEnglish ? sc("help") : sc("yardim");
        String helpLore = isEnglish ? sc("learn about the system") : sc("teslimat sistemini ogren");
        inv.setItem(51, createHead(HEAD_HELP, "§f" + helpName, Arrays.asList(
            "",
            "§7" + helpLore,
            "",
            "§a" + clickText
        )));

        player.openInventory(inv);
    }
    
    private ItemStack createWaitingDeliveryItem(com.deliverycore.model.DeliveryDefinition delivery) {
        boolean isEnglish = "en".equals(getCurrentLanguage());
        String deliveryName = getDeliveryDisplayName(delivery.name());
        String schedule = delivery.schedule().start();
        String nextTime = parseScheduleToReadable(schedule);
        
        String waitingText = isEnglish ? sc("waiting") : sc("bekliyor");
        String startText = isEnglish ? sc("start") : sc("baslangic");
        String notStartedText = isEnglish ? sc("not started yet") : sc("henuz baslamadi");
        
        return createHead(HEAD_WAITING, "§7" + deliveryName, Arrays.asList(
            "",
            "§8" + waitingText,
            "",
            "§7" + startText + " §e" + nextTime,
            "",
            "§8" + notStartedText
        ));
    }
    
    private String parseScheduleToReadable(String schedule) {
        if (schedule == null || schedule.isEmpty()) {
            return "en".equals(getCurrentLanguage()) ? "Unknown" : "Bilinmiyor";
        }
        String lower = schedule.toLowerCase();
        boolean isEnglish = "en".equals(getCurrentLanguage());
        
        Map<String, String> daysTR = Map.of(
            "every day", "Her gün", "every monday", "Her Pazartesi",
            "every tuesday", "Her Salı", "every wednesday", "Her Çarşamba",
            "every thursday", "Her Perşembe", "every friday", "Her Cuma",
            "every saturday", "Her Cumartesi", "every sunday", "Her Pazar"
        );
        Map<String, String> daysEN = Map.of(
            "every day", "Every day", "every monday", "Every Monday",
            "every tuesday", "Every Tuesday", "every wednesday", "Every Wednesday",
            "every thursday", "Every Thursday", "every friday", "Every Friday",
            "every saturday", "Every Saturday", "every sunday", "Every Sunday"
        );
        
        Map<String, String> days = isEnglish ? daysEN : daysTR;
        for (var entry : days.entrySet()) {
            if (lower.contains(entry.getKey())) {
                String time = schedule.replaceAll("(?i)" + entry.getKey().replace(" ", "\\s+") + "\\s*", "").trim();
                return entry.getValue() + " " + time;
            }
        }
        return schedule;
    }

    private ItemStack createDeliveryItem(Player player, ActiveEvent event) {
        boolean isEnglish = "en".equals(getCurrentLanguage());
        String itemName = getItemDisplayName(event.getResolvedItem());
        String deliveryName = getDeliveryDisplayName(event.getDeliveryName());
        int playerCount = event.getPlayerDeliveries().getOrDefault(player.getUniqueId(), 0);
        int totalCount = event.getPlayerDeliveries().values().stream().mapToInt(Integer::intValue).sum();
        int rank = calculateRank(event, player.getUniqueId());

        String itemText = isEnglish ? sc("item") : sc("esya");
        String categoryText = isEnglish ? sc("category") : sc("kategori");
        String totalText = isEnglish ? sc("total") : sc("toplam");
        String yoursText = isEnglish ? sc("yours") : sc("senin");
        String rankText = isEnglish ? sc("rank") : sc("sira");
        String clickDeliverText = isEnglish ? sc("click to deliver") : sc("tikla ve teslim et");

        return createHead(HEAD_DELIVERY, "§a" + deliveryName, Arrays.asList(
            "",
            "§7" + itemText + " §f" + itemName,
            "§7" + categoryText + " §f" + getCategoryDisplayName(event.getResolvedCategory()),
            "",
            "§7" + totalText + " §e" + totalCount,
            "§7" + yoursText + " §a" + playerCount,
            "§7" + rankText + " §e#" + (rank > 0 ? rank : "-"),
            "",
            "§a" + clickDeliverText
        ));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TESLİMAT DETAY MENÜSÜ
    // ═══════════════════════════════════════════════════════════════════════════

    public void openDeliveryMenu(Player player, ActiveEvent event) {
        boolean isEnglish = "en".equals(getCurrentLanguage());
        String deliveryName = getDeliveryDisplayName(event.getDeliveryName());
        Inventory inv = Bukkit.createInventory(null, 45, getDeliveryPrefix() + deliveryName);
        
        ItemStack bg = createItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 45; i++) inv.setItem(i, bg);

        String itemName = getItemDisplayName(event.getResolvedItem());
        int playerCount = event.getPlayerDeliveries().getOrDefault(player.getUniqueId(), 0);
        int totalCount = event.getPlayerDeliveries().values().stream().mapToInt(Integer::intValue).sum();
        int rank = calculateRank(event, player.getUniqueId());

        // Dil string'leri
        String requiredItemText = isEnglish ? sc("required item") : sc("istenen esya");
        String categoryText = isEnglish ? sc("category") : sc("kategori");
        String totalDeliveriesText = isEnglish ? sc("total deliveries") : sc("toplam teslimat");
        String yourDeliveriesText = isEnglish ? sc("your deliveries") : sc("senin teslimat");
        String yourRankText = isEnglish ? sc("your rank") : sc("siran");
        String deliverFromInvText = isEnglish ? sc("deliver from inventory") : sc("envanterden teslim et");
        String allItemsText = isEnglish ? sc("all your") : sc("envanterindeki tum");
        String itemsDeliverText = isEnglish ? sc("items will be delivered") : sc("esyalarini teslim et");
        String deliverFromChestText = isEnglish ? sc("deliver from chest") : sc("sandiktan teslim et");
        String rightClickChestText = isEnglish ? sc("right click a chest") : sc("bir sandiga sag tikla");
        String insideText = isEnglish ? sc("inside") : sc("icindeki");
        String timeLeftText = isEnglish ? sc("10 seconds to select") : sc("10 saniye suren var");
        String clickText = isEnglish ? sc("click") : sc("tikla");
        String backText = isEnglish ? sc("back") : sc("geri");
        String returnMainText = isEnglish ? sc("return to main menu") : sc("ana menuye don");

        // Bilgi (slot 4)
        inv.setItem(4, createHead(HEAD_DELIVERY, "§e" + deliveryName, Arrays.asList(
            "",
            "§7" + requiredItemText,
            "§f  " + itemName,
            "",
            "§7" + categoryText + " §f" + getCategoryDisplayName(event.getResolvedCategory()),
            "§7" + totalDeliveriesText + " §e" + totalCount,
            "",
            "§7" + yourDeliveriesText + " §a" + playerCount,
            "§7" + yourRankText + " §e#" + (rank > 0 ? rank : "-")
        )));

        // Envanterden teslim et (slot 20)
        inv.setItem(20, createHead(HEAD_INVENTORY, "§a" + deliverFromInvText, Arrays.asList(
            "",
            "§7" + allItemsText,
            "§f" + itemName + " §7" + itemsDeliverText,
            "",
            "§a" + clickText
        )));

        // Sandıktan teslim et (slot 24)
        inv.setItem(24, createHead(HEAD_CHEST, "§6" + deliverFromChestText, Arrays.asList(
            "",
            "§7" + rightClickChestText,
            "§7" + insideText + " §f" + itemName,
            "§7" + itemsDeliverText,
            "",
            "§e" + timeLeftText,
            "",
            "§a" + clickText
        )));

        // Geri butonu (slot 40)
        inv.setItem(40, createHead(HEAD_BACK, "§c" + backText, Arrays.asList(
            "",
            "§7" + returnMainText
        )));

        player.openInventory(inv);
    }


    // ═══════════════════════════════════════════════════════════════════════════
    // SIRALAMA MENÜSÜ
    // ═══════════════════════════════════════════════════════════════════════════

    public void openLeaderboard(Player player) {
        boolean isEnglish = "en".equals(getCurrentLanguage());
        Inventory inv = Bukkit.createInventory(null, 54, getLeaderboardTitle());
        
        ItemStack bg = createItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 54; i++) inv.setItem(i, bg);

        var events = deliveryService.getAllActiveEvents();
        if (events.isEmpty()) {
            String noActiveText = isEnglish ? sc("no active delivery") : sc("aktif teslimat yok");
            String waitingText = isEnglish ? sc("waiting for delivery event") : sc("siralama icin teslimat bekleniyor");
            inv.setItem(22, createHead(HEAD_HELP, "§7" + noActiveText, Arrays.asList(
                "",
                "§8" + waitingText
            )));
        } else {
            ActiveEvent event = events.get(0);
            var deliveries = event.getPlayerDeliveries();
            
            List<Map.Entry<UUID, Integer>> sorted = deliveries.entrySet().stream()
                .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
                .limit(10)
                .toList();

            int[] slots = {13, 21, 23, 29, 30, 31, 32, 33, 38, 42};
            String[] colors = {"§6", "§f", "§c", "§7", "§7", "§7", "§7", "§7", "§7", "§7"};
            
            String deliveriesText = isEnglish ? sc("deliveries") : sc("teslimat");
            for (int i = 0; i < sorted.size() && i < slots.length; i++) {
                var entry = sorted.get(i);
                String name = getPlayerName(entry.getKey());
                
                inv.setItem(slots[i], createPlayerHead(entry.getKey(), 
                    colors[i] + "#" + (i + 1) + " " + name, Arrays.asList(
                    "",
                    "§7" + deliveriesText + " §e" + entry.getValue()
                )));
            }
        }

        String backText = isEnglish ? sc("back") : sc("geri");
        String returnMainText = isEnglish ? sc("return to main menu") : sc("ana menuye don");
        inv.setItem(49, createHead(HEAD_BACK, "§c" + backText, Arrays.asList(
            "",
            "§7" + returnMainText
        )));

        player.openInventory(inv);
    }
    
    private String getPlayerName(UUID playerId) {
        Player onlinePlayer = Bukkit.getPlayer(playerId);
        if (onlinePlayer != null) {
            playerNameCache.put(playerId, onlinePlayer.getName());
            return onlinePlayer.getName();
        }
        String cachedName = playerNameCache.get(playerId);
        if (cachedName != null) return cachedName;
        try {
            org.bukkit.OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerId);
            String name = offlinePlayer.getName();
            if (name != null) { playerNameCache.put(playerId, name); return name; }
        } catch (Exception ignored) {}
        return "en".equals(getCurrentLanguage()) ? "Player" : "Oyuncu";
    }

    private int calculateRank(ActiveEvent event, UUID playerUuid) {
        int playerCount = event.getPlayerDeliveries().getOrDefault(playerUuid, 0);
        if (playerCount == 0) return 0;
        int rank = 1;
        for (int count : event.getPlayerDeliveries().values()) {
            if (count > playerCount) rank++;
        }
        return rank;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // ITEM OLUŞTURMA - 1.16.5+ Uyumlu
    // ═══════════════════════════════════════════════════════════════════════════

    private ItemStack createItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) { meta.setDisplayName(name); item.setItemMeta(meta); }
        return item;
    }

    private ItemStack createHead(String texture, String name, List<String> lore) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            // Custom texture uygula
            applyTexture(meta, texture);
            head.setItemMeta(meta);
        }
        return head;
    }

    private ItemStack createPlayerHead(UUID playerId, String name, List<String> lore) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            try {
                org.bukkit.OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerId);
                meta.setOwningPlayer(offlinePlayer);
            } catch (Exception ignored) {}
            head.setItemMeta(meta);
        }
        return head;
    }

    /**
     * Custom texture ile head oluşturma - 1.20+ için Bukkit PlayerProfile API
     */
    private void applyTexture(SkullMeta meta, String texture) {
        try {
            // Base64 decode et ve URL'yi çıkar
            String decoded = new String(Base64.getDecoder().decode(texture));
            int urlStart = decoded.indexOf("http");
            int urlEnd = decoded.indexOf("\"", urlStart);
            
            if (urlStart < 0 || urlEnd <= urlStart) {
                System.out.println("[DeliveryGUI] Texture URL parse edilemedi");
                return;
            }
            
            String skinUrl = decoded.substring(urlStart, urlEnd);
            java.net.URL url = new java.net.URL(skinUrl);
            
            // Bukkit PlayerProfile API kullan (1.18.1+)
            org.bukkit.profile.PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID(), "DeliveryHead");
            org.bukkit.profile.PlayerTextures textures = profile.getTextures();
            textures.setSkin(url);
            profile.setTextures(textures);
            
            meta.setOwnerProfile(profile);
        } catch (Exception e) {
            System.out.println("[DeliveryGUI] Head texture hatasi: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }
}
