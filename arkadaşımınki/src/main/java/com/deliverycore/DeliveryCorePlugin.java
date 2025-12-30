package com.deliverycore;

import com.deliverycore.command.CommandHandler;
import com.deliverycore.command.CustomItemCommand;
import com.deliverycore.command.DeliverCommand;
import com.deliverycore.config.ConfigManager;
import com.deliverycore.config.ConfigManagerImpl;
import com.deliverycore.gui.DeliveryGUI;
import com.deliverycore.handler.DeliveryHandler;
import com.deliverycore.handler.EventListenerHandler;
import com.deliverycore.handler.WebhookHandler;
import com.deliverycore.placeholder.DeliveryCorePlaceholders;
import com.deliverycore.placeholder.PlaceholderEngine;
import com.deliverycore.placeholder.PlaceholderEngineImpl;
import com.deliverycore.reward.PendingRewardStore;
import com.deliverycore.reward.PendingRewardStoreImpl;
import com.deliverycore.reward.RewardService;
import com.deliverycore.reward.RewardServiceImpl;
import com.deliverycore.service.*;
import com.deliverycore.util.LangManager;
import com.deliverycore.util.LoggingService;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;

public class DeliveryCorePlugin extends JavaPlugin implements TabCompleter {

    private ConfigManager configManager;
    private PlaceholderEngine placeholderEngine;
    private CategoryService categoryService;
    private SchedulerService schedulerService;
    private DeliveryService deliveryService;
    private RewardService rewardService;
    private MessageService messageService;
    private PendingRewardStore pendingRewardStore;
    private DataManager dataManager;
    private ScheduledExecutorService executorService;
    private SeasonService seasonService;
    private CustomItemService customItemService;
    private WebhookHandler webhookHandler;
    private DeliveryHandler deliveryHandler;
    private EventListenerHandler eventListenerHandler;
    private CommandHandler commandHandler;
    private DeliverCommand deliverCommand;
    private DeliveryGUI deliveryGUI;
    private LangManager lang;
    private String currentLanguage = "tr";

    @Override
    public void onEnable() {
        long startTime = System.currentTimeMillis();
        try {
            initializePlugin();
            long loadTime = System.currentTimeMillis() - startTime;
            Bukkit.getConsoleSender().sendMessage("");
            Bukkit.getConsoleSender().sendMessage("§6═══════════════════════════════════════════");
            Bukkit.getConsoleSender().sendMessage("§e       DeliveryCore §7- §fDelivery Event System");
            Bukkit.getConsoleSender().sendMessage("§e       Developer: §fNoramu");
            Bukkit.getConsoleSender().sendMessage("§e       Version: §f1.1.0");
            Bukkit.getConsoleSender().sendMessage("§a       Status: §fEnabled §7(" + loadTime + "ms)");
            Bukkit.getConsoleSender().sendMessage("§6═══════════════════════════════════════════");
            Bukkit.getConsoleSender().sendMessage("");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to enable DeliveryCore", e);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        savePluginData();
        if (executorService != null && !executorService.isShutdown()) executorService.shutdown();
        Bukkit.getConsoleSender().sendMessage("§6[DeliveryCore] §cDisabled!");
    }

    private void initializePlugin() {
        if (!getDataFolder().exists()) getDataFolder().mkdirs();
        saveDefaultConfigs();
        executorService = Executors.newScheduledThreadPool(2);
        dataManager = new DataManager(getDataFolder(), getLogger());
        initializeCoreServices();
        loadConfigurations();
        initializeHandlers();
        registerListeners();
        registerPlaceholderAPI();
        resumeActiveEvents();
        loadSavedEvents();
        deliveryGUI.setLogger(getLogger());
        deliveryGUI.loadItemsConfig(getDataFolder());
        deliveryGUI.loadGUIConfig();
    }

    private void savePluginData() {
        if (deliveryService != null && dataManager != null) {
            var activeEvents = deliveryService.getAllActiveEvents();
            if (!activeEvents.isEmpty()) dataManager.saveAllActiveEvents(activeEvents);
        }
        if (customItemService != null) customItemService.saveCustomItems();
    }

    private void saveDefaultConfigs() {
        saveResourceIfNotExists("config.yml");
        saveResourceIfNotExists("categories.yml");
        saveResourceIfNotExists("deliveries.yml");
        File itemsFile = new File(getDataFolder(), "items.yml");
        if (itemsFile.exists()) itemsFile.delete();
        saveResource("items.yml", false);
        File langFolder = new File(getDataFolder(), "lang");
        if (!langFolder.exists()) langFolder.mkdirs();
        saveResourceIfNotExists("lang/tr.yml");
        saveResourceIfNotExists("lang/en.yml");
    }

    private void saveResourceIfNotExists(String resourcePath) {
        File file = new File(getDataFolder(), resourcePath);
        if (!file.exists()) saveResource(resourcePath, false);
    }

    private void initializeCoreServices() {
        configManager = new ConfigManagerImpl(getDataFolder().getAbsolutePath());
        placeholderEngine = new PlaceholderEngineImpl();
        new LoggingService(getLogger(), placeholderEngine);
        pendingRewardStore = new PendingRewardStoreImpl();
        lang = new LangManager(this);
        SchedulerServiceImpl schedulerImpl = new SchedulerServiceImpl(executorService);
        schedulerImpl.setEventStartCallback(this::handleEventStart);
        schedulerImpl.setEventEndCallback(this::handleEventEnd);
        schedulerService = schedulerImpl;
        rewardService = new RewardServiceImpl(pendingRewardStore, placeholderEngine);
        seasonService = new SeasonServiceImpl(getLogger());
        customItemService = new CustomItemServiceImpl(this);
        customItemService.loadCustomItems();
        webhookHandler = new WebhookHandler(this);
    }

    private void initializeHandlers() {
        deliveryHandler = new DeliveryHandler(deliveryService, deliveryGUI, dataManager, getLogger());
        deliveryHandler.setLangManager(lang);
        eventListenerHandler = new EventListenerHandler(this, deliveryService, deliveryGUI, deliveryHandler, rewardService, this::deliverPendingRewardsToPlayer);
    }

    private void loadConfigurations() {
        configManager.loadAll();
        configManager.validate().forEach(error -> getLogger().warning(String.format("[%s] %s - %s: %s", error.severity(), error.file(), error.field() != null ? error.field() : "genel", error.message())));
        loadLanguageSetting();
        initializeConfigDependentServices();
    }

    private void loadLanguageSetting() {
        try {
            File configFile = new File(getDataFolder(), "config.yml");
            if (configFile.exists()) {
                var config = YamlConfiguration.loadConfiguration(configFile);
                String langCode = config.getString("general.language", "tr");
                currentLanguage = langCode.toLowerCase().trim();
                if (!currentLanguage.equals("tr") && !currentLanguage.equals("en")) currentLanguage = "tr";
                lang.setLanguage(currentLanguage);
            }
        } catch (Exception e) { currentLanguage = "tr"; }
    }

    public String getCurrentLanguage() { return currentLanguage; }

    private void initializeConfigDependentServices() {
        categoryService = new CategoryServiceImpl(configManager.getCategoryConfig());
        deliveryService = new DeliveryServiceImpl(configManager.getDeliveryConfig(), categoryService, schedulerService);
        ((DeliveryServiceImpl) deliveryService).setV11Services(seasonService);
        messageService = new MessageServiceImpl(configManager.getLanguageConfig(), placeholderEngine, this::sendMessageToPlayer, getLogger());
        commandHandler = new CommandHandler(configManager, this::checkPermission, this::sendMessageToPlayer, getLogger());
        commandHandler.setLangManager(lang);
        CustomItemCommand customItemCommand = new CustomItemCommand(customItemService, getLogger());
        customItemCommand.setLangManager(lang);
        commandHandler.setCustomItemCommand(customItemCommand);
        deliveryGUI = new DeliveryGUI(configManager, deliveryService);
        deliveryGUI.setLanguageSupplier(this::getCurrentLanguage);
        deliveryGUI.setDataFolder(getDataFolder());
        loadGUISettings();
        deliverCommand = new DeliverCommand(configManager, this::checkPermission, this::sendMessageToPlayer, getLogger(), deliveryGUI);
        commandHandler.setDeliveryService(deliveryService);
        commandHandler.setReloadCallback(this::reloadAllSettings);
        commandHandler.setManualEndScheduler(this::scheduleManualEnd);
        commandHandler.setWebhookTester(webhookHandler::sendTestWebhook);
        deliverCommand.setDeliveryService(deliveryService);
        deliverCommand.setLangManager(lang);
    }

    private void loadGUISettings() {
        try {
            File configFile = new File(getDataFolder(), "config.yml");
            if (!configFile.exists()) return;
            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            ConfigurationSection catSection = config.getConfigurationSection("category-display-names");
            if (catSection != null) {
                for (String key : catSection.getKeys(false)) {
                    String displayName = catSection.getString(key);
                    if (displayName != null && !displayName.isEmpty()) deliveryGUI.setCategoryDisplayName(key, displayName);
                }
            }
            ConfigurationSection delSection = config.getConfigurationSection("delivery-display-names");
            if (delSection != null) {
                for (String key : delSection.getKeys(false)) {
                    String displayName = delSection.getString(key);
                    if (displayName != null && !displayName.isEmpty()) deliveryGUI.setDeliveryDisplayName(key, displayName);
                }
            }
        } catch (Exception e) { getLogger().warning("GUI ayarlari yuklenemedi: " + e.getMessage()); }
    }

    private void registerListeners() { getServer().getPluginManager().registerEvents(eventListenerHandler, this); }

    private void registerPlaceholderAPI() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new DeliveryCorePlaceholders(deliveryService, deliveryGUI, currentLanguage).register();
            getLogger().info("PlaceholderAPI expansion registered!");
        }
    }

    private void resumeActiveEvents() {
        if (schedulerService != null) schedulerService.resumeActiveEvents();
        if (configManager != null && configManager.getDeliveryConfig() != null) {
            var enabledDeliveries = configManager.getDeliveryConfig().getEnabledDeliveries();
            enabledDeliveries.forEach(delivery -> { try { schedulerService.scheduleEvent(delivery); } catch (Exception ignored) {} });
        }
    }

    private void loadSavedEvents() {
        if (dataManager == null || deliveryService == null) return;
        var savedEvents = dataManager.loadActiveEvents();
        if (savedEvents.isEmpty()) return;
        for (var savedData : savedEvents) {
            try {
                if (savedData.endTime() != null && savedData.endTime().isBefore(java.time.ZonedDateTime.now())) { dataManager.removeActiveEvent(savedData.deliveryName()); continue; }
                var activeEvent = savedData.toActiveEvent();
                ((DeliveryServiceImpl) deliveryService).restoreEvent(activeEvent);
            } catch (Exception ignored) {}
        }
    }

    private void handleEventStart(String deliveryName) {
        if (deliveryService == null) return;
        deliveryService.startEvent(deliveryName).ifPresent(event -> {
            String itemName = deliveryGUI.getItemDisplayName(event.getResolvedItem());
            String catName = deliveryGUI.getCategoryDisplayName(event.getResolvedCategory());
            String delName = deliveryGUI.getDeliveryDisplayName(deliveryName);
            String broadcast = lang.getWithPrefix("event.start.broadcast", "{delivery}", delName, "{item}", itemName);
            Bukkit.broadcastMessage(broadcast);
            String title = lang.get("event.start.title", "{delivery}", delName);
            String subtitle = lang.get("event.start.subtitle", "{item}", itemName);
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendTitle(title, subtitle, 10, 70, 20);
                try { p.playSound(p.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f); } catch (Exception ignored) {}
            }
            webhookHandler.sendStartWebhook(delName, itemName, catName);
            webhookHandler.scheduleWarning(event, deliveryService, deliveryGUI::getItemDisplayName, deliveryGUI::getCategoryDisplayName);
        });
    }

    private void handleEventEnd(String deliveryName) {
        if (deliveryService == null) return;
        var activeEvent = deliveryService.getActiveEvent(deliveryName);
        String item = activeEvent.map(e -> e.getResolvedItem()).orElse("unknown");
        String category = activeEvent.map(e -> e.getResolvedCategory()).orElse("unknown");
        String itemName = deliveryGUI.getItemDisplayName(item);
        String catName = deliveryGUI.getCategoryDisplayName(category);
        String delName = deliveryGUI.getDeliveryDisplayName(deliveryName);
        var winners = deliveryService.endEvent(deliveryName);
        String broadcast = lang.getWithPrefix("event.end.broadcast", "{delivery}", delName);
        Bukkit.broadcastMessage(broadcast);
        if (!winners.isEmpty()) {
            Bukkit.broadcastMessage(lang.getWithPrefix("event.end.winners-header"));
            int rank = 1;
            for (var winner : winners) {
                if (rank > 3) break;
                String color = lang.getRankColor(rank);
                String msg = lang.get("event.end.winner-format", "{color}", color, "{rank}", String.valueOf(rank), "{player}", winner.playerName(), "{count}", String.valueOf(winner.deliveryCount()));
                Bukkit.broadcastMessage(msg);
                rank++;
            }
        }
        String title = lang.get("event.end.title");
        String subtitle = lang.get("event.end.subtitle", "{delivery}", delName);
        for (Player p : Bukkit.getOnlinePlayers()) { p.sendTitle(title, subtitle, 10, 70, 20); }
        webhookHandler.sendEndWebhook(delName, itemName, catName, winners);
    }

    private void scheduleManualEnd(String deliveryName, Long durationMinutes) {
        long ticks = durationMinutes * 60 * 20;
        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (deliveryService.getActiveEvent(deliveryName).isPresent()) { handleEventEnd(deliveryName); }
        }, ticks);
    }

    private void reloadAllSettings() {
        loadLanguageSetting();
        lang.loadLanguages();
        lang.setLanguage(currentLanguage);
        deliveryGUI.loadItemsConfig(getDataFolder());
        deliveryGUI.loadGUIConfig();
        deliveryGUI.reloadLanguage();
        loadGUISettings();
        webhookHandler.loadConfig();
    }

    private void deliverPendingRewardsToPlayer(Player player) {
        if (player == null || !player.isOnline()) return;
        RewardService.PlayerResolver resolver = createPlayerResolver();
        RewardService.CommandExecutor executor = cmd -> { try { Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd); } catch (Exception ignored) {} };
        rewardService.deliverPendingRewards(player.getUniqueId(), resolver, executor, null);
    }

    private RewardService.PlayerResolver createPlayerResolver() {
        return new RewardService.PlayerResolver() {
            @Override public boolean isOnline(UUID uuid) { Player player = Bukkit.getPlayer(uuid); return player != null && player.isOnline(); }
            @Override public String getName(UUID uuid) { Player player = Bukkit.getPlayer(uuid); return player != null ? player.getName() : null; }
            @Override public boolean giveItem(UUID uuid, String item, int amount) {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null || !player.isOnline()) return false;
                try { org.bukkit.Material material = org.bukkit.Material.matchMaterial(item); if (material != null) { player.getInventory().addItem(new org.bukkit.inventory.ItemStack(material, amount)); return true; } } catch (Exception ignored) {}
                return false;
            }
        };
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String senderName = sender instanceof Player ? sender.getName() : "CONSOLE";
        UUID senderUuid = sender instanceof Player p ? p.getUniqueId() : null;
        return switch (command.getName().toLowerCase()) {
            case "deliverycore" -> commandHandler.handleCommand(senderName, args);
            case "teslimat" -> { if (!(sender instanceof Player)) { sender.sendMessage(lang.get("general.player-only")); yield true; } yield deliverCommand.handleTeslimatCommand(senderName, senderUuid, args); }
            case "teslim" -> { if (!(sender instanceof Player)) { sender.sendMessage(lang.get("general.player-only")); yield true; } yield deliverCommand.handleTeslimCommand(senderName, senderUuid, args); }
            default -> false;
        };
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        String senderName = sender instanceof Player ? sender.getName() : "CONSOLE";
        return switch (command.getName().toLowerCase()) {
            case "deliverycore" -> commandHandler.handleTabComplete(senderName, args);
            case "teslimat" -> deliverCommand.handleTeslimatTabComplete(senderName, args);
            case "teslim" -> deliverCommand.handleTeslimTabComplete(senderName, args);
            default -> List.of();
        };
    }

    private boolean checkPermission(String playerName, String permission) {
        if ("CONSOLE".equals(playerName)) return true;
        Player player = Bukkit.getPlayer(playerName);
        return player != null && player.hasPermission(permission);
    }

    private void sendMessageToPlayer(String playerName, String message) {
        if ("CONSOLE".equals(playerName)) { getLogger().info(message); return; }
        Player player = Bukkit.getPlayer(playerName);
        if (player != null && player.isOnline()) player.sendMessage(message);
    }

    public ConfigManager getConfigManager() { return configManager; }
    public DeliveryService getDeliveryService() { return deliveryService; }
    public DeliveryGUI getDeliveryGUI() { return deliveryGUI; }
    public LangManager getLang() { return lang; }
}
