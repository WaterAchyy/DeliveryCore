package com.deliverycore.handler;

import com.deliverycore.gui.DeliveryGUI;
import com.deliverycore.service.ActiveEvent;
import com.deliverycore.service.DataManager;
import com.deliverycore.service.DeliveryService;
import com.deliverycore.util.LangManager;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;
import java.util.logging.Logger;

public class DeliveryHandler {
    
    private final DeliveryService deliveryService;
    private final DeliveryGUI deliveryGUI;
    private final DataManager dataManager;
    private final Logger logger;
    private LangManager lang;
    
    public DeliveryHandler(DeliveryService deliveryService, DeliveryGUI deliveryGUI, 
                          DataManager dataManager, Logger logger) {
        this.deliveryService = deliveryService;
        this.deliveryGUI = deliveryGUI;
        this.dataManager = dataManager;
        this.logger = logger;
    }
    
    public void setLangManager(LangManager lang) {
        this.lang = lang;
    }
    
    public void deliverFromInventory(Player player, ActiveEvent activeEvent) {
        String requiredItem = activeEvent.getResolvedItem();
        Material material = parseMaterial(requiredItem);
        
        if (material == null) return;
        
        int count = countItems(player.getInventory(), material);
        
        if (count == 0) {
            String itemName = deliveryGUI.getItemDisplayName(requiredItem);
            player.sendMessage(lang.getWithPrefix("delivery.no-item", "{item}", itemName));
            return;
        }
        
        int delivered = removeItems(player.getInventory(), material, count);
        recordDelivery(player, activeEvent, delivered);
        sendFeedback(player, activeEvent, delivered, requiredItem, false);
    }
    
    public void deliverFromChest(Player player, Inventory chestInventory, ActiveEvent activeEvent) {
        String requiredItem = activeEvent.getResolvedItem();
        Material material = parseMaterial(requiredItem);
        
        if (material == null) return;
        
        int count = countItems(chestInventory, material);
        
        if (count == 0) {
            String itemName = deliveryGUI.getItemDisplayName(requiredItem);
            player.sendMessage(lang.getWithPrefix("chest.empty", "{item}", itemName));
            return;
        }
        
        int delivered = removeItems(chestInventory, material, count);
        recordDelivery(player, activeEvent, delivered);
        sendFeedback(player, activeEvent, delivered, requiredItem, true);
    }
    
    private Material parseMaterial(String itemName) {
        try {
            return Material.valueOf(itemName.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }
    
    private int countItems(Inventory inventory, Material material) {
        int count = 0;
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() == material) {
                count += item.getAmount();
            }
        }
        return count;
    }
    
    private int removeItems(Inventory inventory, Material material, int maxAmount) {
        int remaining = maxAmount;
        ItemStack[] contents = inventory.getContents();
        
        for (int i = 0; i < contents.length && remaining > 0; i++) {
            ItemStack item = contents[i];
            if (item != null && item.getType() == material) {
                int itemAmount = item.getAmount();
                if (itemAmount <= remaining) {
                    remaining -= itemAmount;
                    inventory.setItem(i, null);
                } else {
                    item.setAmount(itemAmount - remaining);
                    remaining = 0;
                }
            }
        }
        return maxAmount - remaining;
    }
    
    private void recordDelivery(Player player, ActiveEvent activeEvent, int amount) {
        deliveryService.recordDelivery(player.getUniqueId(), activeEvent.getDeliveryName(), amount);
        
        if (dataManager != null) {
            dataManager.saveActiveEvent(activeEvent);
            dataManager.updatePlayerStats(player.getUniqueId(), player.getName(), amount);
        }
        
        deliveryGUI.cachePlayerName(player.getUniqueId(), player.getName());
    }
    
    private void sendFeedback(Player player, ActiveEvent activeEvent, int delivered, String requiredItem, boolean fromChest) {
        int total = activeEvent.getPlayerDeliveries().getOrDefault(player.getUniqueId(), 0);
        int rank = calculateRank(activeEvent, player.getUniqueId());
        String itemName = deliveryGUI.getItemDisplayName(requiredItem);
        
        // Title
        String title = lang.get("delivery.success", "{amount}", String.valueOf(delivered), "{item}", itemName);
        player.sendTitle("§a§l✓", title, 10, 50, 10);
        
        // Message
        String msg = fromChest 
            ? lang.get("chest.success", "{amount}", String.valueOf(delivered), "{item}", itemName)
            : lang.get("delivery.success", "{amount}", String.valueOf(delivered), "{item}", itemName);
        player.sendMessage(lang.getPrefix() + " " + msg);
        
        // Stats
        player.sendMessage(lang.getPrefix() + " " + lang.get("delivery.stats", 
            "{total}", String.valueOf(total), "{rank}", String.valueOf(rank)));
        
        try {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        } catch (Exception ignored) {}
    }
    
    public int calculateRank(ActiveEvent event, UUID playerUuid) {
        int playerCount = event.getPlayerDeliveries().getOrDefault(playerUuid, 0);
        if (playerCount == 0) return 0;
        
        int rank = 1;
        for (int count : event.getPlayerDeliveries().values()) {
            if (count > playerCount) rank++;
        }
        return rank;
    }
}
