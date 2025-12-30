package com.deliverycore.handler;

import com.deliverycore.DeliveryCorePlugin;
import com.deliverycore.gui.DeliveryGUI;
import com.deliverycore.reward.RewardService;
import com.deliverycore.service.ActiveEvent;
import com.deliverycore.service.DeliveryService;
import com.deliverycore.util.LangManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;

import java.util.logging.Logger;

/**
 * Bukkit event'lerini dinleyen sınıf.
 */
public class EventListenerHandler implements Listener {
    
    private final DeliveryCorePlugin plugin;
    private final Logger logger;
    private final DeliveryService deliveryService;
    private final DeliveryGUI deliveryGUI;
    private final DeliveryHandler deliveryHandler;
    private final RewardService rewardService;
    private final RewardDeliveryHelper rewardHelper;
    
    public EventListenerHandler(DeliveryCorePlugin plugin, DeliveryService deliveryService, 
                                DeliveryGUI deliveryGUI, DeliveryHandler deliveryHandler,
                                RewardService rewardService, RewardDeliveryHelper rewardHelper) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.deliveryService = deliveryService;
        this.deliveryGUI = deliveryGUI;
        this.deliveryHandler = deliveryHandler;
        this.rewardService = rewardService;
        this.rewardHelper = rewardHelper;
    }
    
    private LangManager lang() {
        return plugin.getLang();
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        if (rewardService != null && rewardService.hasPendingRewards(player.getUniqueId())) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (rewardHelper != null) {
                    rewardHelper.deliverPendingRewards(player);
                }
            }, 20L);
        }
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        
        String title = event.getView().getTitle();
        if (!DeliveryGUI.isDeliveryGUI(title)) return;
        
        event.setCancelled(true);
        
        if (event.getCurrentItem() == null) return;
        
        int slot = event.getRawSlot();
        
        if (DeliveryGUI.isMainMenu(title)) {
            handleMainMenuClick(player, slot);
        } else if (DeliveryGUI.isDeliveryMenu(title)) {
            handleDeliveryMenuClick(player, slot, title);
        } else if (DeliveryGUI.isLeaderboardMenu(title)) {
            handleLeaderboardClick(player, slot);
        }
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        
        if (!deliveryGUI.isInChestSelectionMode(player.getUniqueId())) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;
        
        Block block = event.getClickedBlock();
        
        // Koruma kontrolü
        if (!canAccessChest(player, block)) {
            player.sendMessage(lang().getWithPrefix("chest.no-access"));
            deliveryGUI.cancelChestSelection(player.getUniqueId());
            return;
        }
        
        // Sandık kontrolü
        Inventory chestInventory = getChestInventory(block);
        
        if (chestInventory == null) {
            player.sendMessage(lang().getWithPrefix("chest.not-chest"));
            return;
        }
        
        event.setCancelled(true);
        
        String deliveryName = deliveryGUI.getChestSelectionDelivery(player.getUniqueId());
        deliveryGUI.cancelChestSelection(player.getUniqueId());
        
        ActiveEvent activeEvent = deliveryService.getActiveEvent(deliveryName).orElse(null);
        if (activeEvent == null) {
            player.sendMessage(lang().getWithPrefix("delivery.no-active"));
            return;
        }
        
        deliveryHandler.deliverFromChest(player, chestInventory, activeEvent);
    }
    
    private Inventory getChestInventory(Block block) {
        BlockState state = block.getState();
        
        if (state instanceof Chest chest) {
            return chest.getInventory();
        }
        
        if (block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST) {
            if (state instanceof Container container) {
                return container.getInventory();
            }
        }
        
        return null;
    }
    
    private boolean canAccessChest(Player player, Block block) {
        if (player.hasPermission("deliverycore.bypass.protection")) {
            return true;
        }
        
        if (hasSignLock(block, player)) {
            return false;
        }
        
        // Koruma plugin kontrolü
        try {
            PlayerInteractEvent testEvent = new PlayerInteractEvent(
                player, Action.RIGHT_CLICK_BLOCK,
                player.getInventory().getItemInMainHand(),
                block, BlockFace.NORTH
            );
            Bukkit.getPluginManager().callEvent(testEvent);
            return !testEvent.isCancelled();
        } catch (Exception e) {
            logger.warning("Koruma kontrolü hatası: " + e.getMessage());
            return true;
        }
    }
    
    private boolean hasSignLock(Block block, Player player) {
        BlockFace[] faces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP};
        
        for (BlockFace face : faces) {
            Block adjacent = block.getRelative(face);
            
            if (adjacent.getState() instanceof Sign sign) {
                // Modern API: Sign.getSide(Side.FRONT).getLines()
                String[] lines;
                try {
                    // 1.20+ API
                    org.bukkit.block.sign.SignSide side = sign.getSide(org.bukkit.block.sign.Side.FRONT);
                    lines = side.getLines();
                } catch (NoSuchMethodError | NoClassDefFoundError e) {
                    // Eski API fallback (1.19 ve öncesi)
                    @SuppressWarnings("deprecation")
                    String[] fallbackLines = sign.getLines();
                    lines = fallbackLines;
                }
                
                if (lines.length > 0) {
                    String firstLine = ChatColor.stripColor(lines[0]).toLowerCase();
                    if (firstLine.contains("private") || firstLine.contains("lock") ||
                        firstLine.contains("kilit") || firstLine.contains("özel")) {
                        
                        if (lines.length > 1) {
                            String ownerLine = ChatColor.stripColor(lines[1]);
                            if (!ownerLine.isEmpty() && !ownerLine.equalsIgnoreCase(player.getName())) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        
        return false;
    }
    
    private void handleMainMenuClick(Player player, int slot) {
        // Sıralama butonu
        if (slot == 47) {
            deliveryGUI.openLeaderboard(player);
            return;
        }
        
        // Yardım butonu
        if (slot == 51) {
            player.closeInventory();
            player.performCommand("teslimat help");
            return;
        }
        
        // Aktif teslimatlar
        if (slot >= 11 && slot <= 15) {
            var events = deliveryService.getAllActiveEvents();
            int index = slot - 11;
            if (index < events.size()) {
                deliveryGUI.openDeliveryMenu(player, events.get(index));
            }
        }
        
        // Bekleyen teslimatlar
        if (slot >= 29 && slot <= 33) {
            player.sendMessage(lang().getWithPrefix("gui.waiting"));
        }
    }
    
    private void handleDeliveryMenuClick(Player player, int slot, String title) {
        String deliveryDisplayName = title.replace(DeliveryGUI.DELIVERY_PREFIX, "");
        
        ActiveEvent activeEvent = deliveryService.getAllActiveEvents().stream()
            .filter(e -> deliveryGUI.getDeliveryDisplayName(e.getDeliveryName()).equals(deliveryDisplayName))
            .findFirst()
            .orElse(null);
        
        if (activeEvent == null) {
            player.sendMessage(lang().getWithPrefix("delivery.no-active"));
            deliveryGUI.openMainMenu(player);
            return;
        }
        
        // Envanterden teslim
        if (slot == 20) {
            player.closeInventory();
            deliveryHandler.deliverFromInventory(player, activeEvent);
            return;
        }
        
        // Sandıktan teslim
        if (slot == 24) {
            player.closeInventory();
            deliveryGUI.startChestSelection(player, activeEvent.getDeliveryName());
            player.sendMessage(lang().getWithPrefix("chest.prompt"));
            return;
        }
        
        // Geri butonu
        if (slot == 40) {
            deliveryGUI.openMainMenu(player);
        }
    }
    
    private void handleLeaderboardClick(Player player, int slot) {
        if (slot == 49) {
            deliveryGUI.openMainMenu(player);
        }
    }
    
    /**
     * Ödül teslimi için yardımcı interface
     */
    public interface RewardDeliveryHelper {
        void deliverPendingRewards(Player player);
    }
}
