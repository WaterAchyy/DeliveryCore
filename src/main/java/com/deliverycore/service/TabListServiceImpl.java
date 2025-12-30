package com.deliverycore.service;

import com.deliverycore.model.PlaceholderContext;
import com.deliverycore.model.TabDisplayConfig;
import com.deliverycore.placeholder.PlaceholderEngine;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * Tab list entegrasyonu servisi implementasyonu
 * TAB plugin ile entegre çalışır veya vanilla header/footer kullanır
 */
public class TabListServiceImpl implements TabListService {
    
    private Plugin plugin;
    private Logger logger;
    private DeliveryService deliveryService;
    private SeasonService seasonService;
    private PlaceholderEngine placeholderEngine;
    
    private final Set<String> enabledDeliveries = ConcurrentHashMap.newKeySet();
    private BukkitTask updateTask;
    private boolean enabled = false;
    private boolean tabPluginAvailable = false;
    
    public TabListServiceImpl(
        Plugin plugin,
        Logger logger,
        DeliveryService deliveryService,
        SeasonService seasonService,
        PlaceholderEngine placeholderEngine
    ) {
        this.plugin = plugin;
        this.logger = logger;
        this.deliveryService = deliveryService;
        this.seasonService = seasonService;
        this.placeholderEngine = placeholderEngine;
        checkTabPlugin();
    }
    
    public TabListServiceImpl() {
        this.plugin = null;
        this.logger = Logger.getLogger(TabListServiceImpl.class.getName());
        this.deliveryService = null;
        this.seasonService = null;
        this.placeholderEngine = null;
    }
    
    private void checkTabPlugin() {
        Plugin tabPlugin = Bukkit.getPluginManager().getPlugin("TAB");
        if (tabPlugin != null && tabPlugin.isEnabled()) {
            tabPluginAvailable = true;
            logger.info("[TAB] TAB plugin tespit edildi - entegrasyon aktif");
        } else {
            tabPluginAvailable = false;
            logger.info("[TAB] TAB plugin bulunamadi - vanilla header/footer kullanilacak");
        }
    }
    
    public void setDeliveryService(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }
    
    public void setPlugin(Plugin plugin) {
        this.plugin = plugin;
        checkTabPlugin();
    }
    
    @Override
    public void updateTabList(Player player) {
        if (!enabled || player == null || !player.isOnline()) return;
        
        try {
            List<ActiveEvent> events = getDisplayableEvents();
            if (events.isEmpty()) {
                clearTabList(player);
                return;
            }
            
            StringBuilder header = new StringBuilder();
            StringBuilder footer = new StringBuilder();
            
            header.append("§6§l⚡ DeliveryCore ⚡\n");
            
            for (ActiveEvent event : events) {
                String line = formatEventLine(event);
                footer.append(line).append("\n");
            }
            
            if (tabPluginAvailable) {
                // TAB plugin API kullan
                setTabPluginContent(player, header.toString(), footer.toString());
            } else {
                // Vanilla API kullan
                player.setPlayerListHeaderFooter(header.toString(), footer.toString());
            }
            
        } catch (Exception e) {
            logger.warning("[TAB] Guncelleme hatasi: " + e.getMessage());
        }
    }
    
    private String formatEventLine(ActiveEvent event) {
        StringBuilder sb = new StringBuilder();
        sb.append("§e").append(event.getDeliveryName());
        sb.append(" §8| §f").append(event.getResolvedItem());
        sb.append(" §8| §a").append(event.getTotalDeliveries()).append(" teslimat");
        
        // Kalan süre
        if (event.getEndTime() != null) {
            long remaining = Duration.between(ZonedDateTime.now(), event.getEndTime()).toMinutes();
            if (remaining > 0) {
                sb.append(" §8| §c").append(remaining).append("dk");
            }
        }
        
        return sb.toString();
    }
    
    private void setTabPluginContent(Player player, String header, String footer) {
        try {
            // TAB plugin reflection API
            Class<?> tabApi = Class.forName("me.neznamy.tab.api.TabAPI");
            var getInstance = tabApi.getMethod("getInstance");
            var api = getInstance.invoke(null);
            
            var getPlayer = api.getClass().getMethod("getPlayer", java.util.UUID.class);
            var tabPlayer = getPlayer.invoke(api, player.getUniqueId());
            
            if (tabPlayer != null) {
                // Header/Footer ayarla
                var headerFooterManager = api.getClass().getMethod("getHeaderFooterManager");
                var hfManager = headerFooterManager.invoke(api);
                
                if (hfManager != null) {
                    var setHeader = hfManager.getClass().getMethod("setHeader", Object.class, String.class);
                    var setFooter = hfManager.getClass().getMethod("setFooter", Object.class, String.class);
                    setHeader.invoke(hfManager, tabPlayer, header);
                    setFooter.invoke(hfManager, tabPlayer, footer);
                }
            }
        } catch (Exception e) {
            // TAB API başarısız, vanilla'ya düş
            player.setPlayerListHeaderFooter(header, footer);
        }
    }
    
    @Override
    public void updateAllTabLists() {
        if (!enabled) return;
        Bukkit.getOnlinePlayers().forEach(this::updateTabList);
    }
    
    @Override
    public void enableTabDisplay(String deliveryName) {
        enabledDeliveries.add(deliveryName);
        logger.info("[TAB] Etkinlestirildi: " + deliveryName);
        updateAllTabLists();
    }
    
    @Override
    public void disableTabDisplay(String deliveryName) {
        enabledDeliveries.remove(deliveryName);
        logger.info("[TAB] Devre disi: " + deliveryName);
        updateAllTabLists();
    }
    
    @Override
    public String formatTabContent(ActiveEvent event, TabDisplayConfig config) {
        return formatEventLine(event);
    }
    
    @Override
    public void scheduleTabUpdates(long intervalTicks) {
        stopTabUpdates();
        if (intervalTicks <= 0 || plugin == null) return;
        
        updateTask = Bukkit.getScheduler().runTaskTimer(plugin, this::updateAllTabLists, 0L, intervalTicks);
        logger.info("[TAB] Guncellemeler zamanlandı: " + intervalTicks + " tick");
    }
    
    @Override
    public void stopTabUpdates() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
    }
    
    @Override
    public List<ActiveEvent> getDisplayableEvents() {
        if (deliveryService == null) return new ArrayList<>();
        return deliveryService.getAllActiveEvents().stream()
            .filter(event -> enabledDeliveries.contains(event.getDeliveryName()))
            .collect(Collectors.toList());
    }
    
    @Override
    public boolean isEnabled() { return enabled; }
    
    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled) {
            logger.info("[TAB] Servis aktif");
            updateAllTabLists();
        } else {
            logger.info("[TAB] Servis devre disi");
            stopTabUpdates();
            Bukkit.getOnlinePlayers().forEach(this::clearTabList);
        }
    }
    
    private void clearTabList(Player player) {
        try {
            player.setPlayerListHeaderFooter("", "");
        } catch (Exception ignored) {}
    }
    
    public void sendActionBar(Player player, String message) {
        try {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
        } catch (Exception ignored) {}
    }
    
    public boolean isTabPluginAvailable() { return tabPluginAvailable; }
}