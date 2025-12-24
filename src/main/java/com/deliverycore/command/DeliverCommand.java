package com.deliverycore.command;

import com.deliverycore.config.ConfigManager;
import com.deliverycore.gui.DeliveryGUI;
import com.deliverycore.service.ActiveEvent;
import com.deliverycore.service.DeliveryService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.logging.Logger;

public class DeliverCommand {

    public static final String PERM_USE = "deliverycore.use";
    public static final String PERM_DELIVER = "deliverycore.deliver";

    private final ConfigManager configManager;
    private final BiFunction<String, String, Boolean> permissionChecker;
    private final BiConsumer<String, String> messageSender;
    private final Logger logger;
    private final DeliveryGUI deliveryGUI;
    private DeliveryService deliveryService;

    private static final String PREFIX = "&e&lD&6elivery&e&lC&6ore &8» &r";

    public DeliverCommand(ConfigManager configManager, BiFunction<String, String, Boolean> permissionChecker,
            BiConsumer<String, String> messageSender, Logger logger, DeliveryGUI deliveryGUI) {
        this.configManager = configManager;
        this.permissionChecker = permissionChecker;
        this.messageSender = messageSender;
        this.logger = logger;
        this.deliveryGUI = deliveryGUI;
    }

    public void setDeliveryService(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    public boolean handleTeslimatCommand(String sender, UUID senderUuid, String[] args) {
        if (!hasPerm(sender, PERM_USE)) { noPermission(sender); return true; }

        Player player = Bukkit.getPlayer(sender);
        if (player == null) { prefix(sender, "&cBu komut sadece oyuncular kullanabilir."); return true; }

        if (args.length == 0) {
            deliveryGUI.openMainMenu(player);
            return true;
        }

        String sub = args[0].toLowerCase();
        return switch (sub) {
            case "help", "yardim", "?" -> { sendPlayerHelp(sender); yield true; }
            case "top", "siralama" -> { showLeaderboard(sender, senderUuid); yield true; }
            case "bilgi", "info" -> { showActiveInfo(sender); yield true; }
            default -> { prefix(sender, "&cBilinmeyen komut: &f" + sub); yield true; }
        };
    }

    // /teslim [teslimat] [sayi] - Envanterdeki eşyaları direkt teslim et
    public boolean handleTeslimCommand(String sender, UUID senderUuid, String[] args) {
        if (!hasPerm(sender, PERM_DELIVER)) { noPermission(sender); return true; }

        Player player = Bukkit.getPlayer(sender);
        if (player == null) { prefix(sender, "&cBu komut sadece oyuncular kullanabilir."); return true; }

        if (deliveryService == null || deliveryService.getAllActiveEvents().isEmpty()) {
            prefix(sender, "&cŞu an aktif teslimat yok.");
            return true;
        }

        ActiveEvent targetEvent;
        int maxAmount = -1; // -1 = hepsini teslim et
        
        if (args.length == 0) {
            // İlk aktif teslimata hepsini teslim et
            targetEvent = deliveryService.getAllActiveEvents().get(0);
        } else {
            // Belirtilen teslimata teslim et
            String deliveryName = args[0].toLowerCase();
            targetEvent = deliveryService.getAllActiveEvents().stream()
                .filter(e -> e.getDeliveryName().equalsIgnoreCase(deliveryName))
                .findFirst()
                .orElse(null);
            
            if (targetEvent == null) {
            prefix(sender, "&cAktif teslimat bulunamadı: &f" + deliveryName);
                showActiveDeliveries(sender);
                return true;
            }
            
            // Sayı belirtilmişse
            if (args.length >= 2) {
                try {
                    maxAmount = Integer.parseInt(args[1]);
                    if (maxAmount <= 0) {
                        prefix(sender, "&cGeçersiz sayı! Pozitif bir sayı girin.");
                        return true;
                    }
                } catch (NumberFormatException e) {
                    prefix(sender, "&cGeçersiz sayı: &f" + args[1]);
                    return true;
                }
            }
        }

        deliverFromInventory(player, senderUuid, targetEvent, maxAmount);
        return true;
    }

    private void deliverFromInventory(Player player, UUID playerUuid, ActiveEvent event, int maxAmount) {
        String requiredItem = event.getResolvedItem();
        Material material;
        
        try {
            material = Material.valueOf(requiredItem.toUpperCase());
        } catch (Exception e) {
            prefix(player.getName(), "&cGeçersiz eşya: &f" + requiredItem);
            return;
        }
        
        // Envanterdeki eşyaları say
        int totalInInventory = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                totalInInventory += item.getAmount();
            }
        }
        
        if (totalInInventory == 0) {
            String itemName = deliveryGUI.getItemDisplayName(requiredItem);
            prefix(player.getName(), "&cEnvanterinde &e" + itemName + " &cyok!");
            return;
        }
        
        // Teslim edilecek miktar
        int toDeliver = (maxAmount <= 0 || maxAmount > totalInInventory) ? totalInInventory : maxAmount;
        
        // Envanterdeki eşyaları kaldır
        int remaining = toDeliver;
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length && remaining > 0; i++) {
            ItemStack item = contents[i];
            if (item != null && item.getType() == material) {
                int itemAmount = item.getAmount();
                if (itemAmount <= remaining) {
                    remaining -= itemAmount;
                    player.getInventory().setItem(i, null);
                } else {
                    item.setAmount(itemAmount - remaining);
                    remaining = 0;
                }
            }
        }
        
        int delivered = toDeliver - remaining;
        deliveryService.recordDelivery(playerUuid, event.getDeliveryName(), delivered);
        
        int total = event.getPlayerDeliveries().getOrDefault(playerUuid, 0);
        int rank = calculateRank(event, playerUuid);
        String itemName = deliveryGUI.getItemDisplayName(requiredItem);
        
        // Title göster
        player.sendTitle("§a§lTESLİM EDİLDİ!", "§e" + delivered + " §7adet §f" + itemName, 10, 50, 10);
        prefix(player.getName(), "&a" + delivered + " &7adet &e" + itemName + " &7teslim edildi!");
        prefix(player.getName(), "&7Toplam: &f" + total + " &8| &7Sıra: &e#" + rank);
        
        try { player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f); } catch (Exception ignored) {}
    }

    private void showActiveDeliveries(String sender) {
        var events = deliveryService.getAllActiveEvents();
        if (events.isEmpty()) return;
        
        StringBuilder sb = new StringBuilder();
        for (var event : events) {
            if (sb.length() > 0) sb.append("&7, ");
            sb.append("&f").append(event.getDeliveryName());
        }
        prefix(sender, "&7Aktif teslimatlar: " + sb);
    }

    public List<String> handleTeslimatTabComplete(String sender, String[] args) {
        if (args.length == 1) return filter(Arrays.asList("help", "top", "bilgi"), args[0]);
        return List.of();
    }
    
    public List<String> handleTeslimTabComplete(String sender, String[] args) {
        if (deliveryService == null) return List.of();
        
        if (args.length == 1) {
            List<String> names = deliveryService.getAllActiveEvents().stream()
                .map(ActiveEvent::getDeliveryName)
                .toList();
            return filter(new ArrayList<>(names), args[0]);
        }
        
        if (args.length == 2) {
            return filter(Arrays.asList("1", "10", "32", "64", "100", "500"), args[1]);
        }
        
        return List.of();
    }

    private void sendPlayerHelp(String sender) {
        msg(sender, "");
        msg(sender, "&e&lD&6elivery&e&lC&6ore &8| &fOyuncu Rehberi");
        msg(sender, "");
        msg(sender, "&e/teslimat &8- &7Teslimat menüsünü aç");
        msg(sender, "&e/teslimat top &8- &7Sıralama tablosu");
        msg(sender, "&e/teslimat bilgi &8- &7Aktif teslimatlar");
        msg(sender, "");
        msg(sender, "&e/teslim &8- &7Tüm eşyaları teslim et");
        msg(sender, "&e/teslim <teslimat> &8- &7Belirli teslimata teslim et");
        msg(sender, "&e/teslim <teslimat> <sayı> &8- &7Belirli miktarda teslim et");
        msg(sender, "");
    }
    
    private void showLeaderboard(String sender, UUID senderUuid) {
        var events = deliveryService != null ? deliveryService.getAllActiveEvents() : List.<ActiveEvent>of();
        if (events.isEmpty()) { prefix(sender, "&7Aktif etkinlik yok."); return; }
        
        ActiveEvent event = events.get(0);
        var deliveries = event.getPlayerDeliveries();
        
        msg(sender, "");
        msg(sender, "&b&lSıralama &8| &f" + deliveryGUI.getDeliveryDisplayName(event.getDeliveryName()));
        msg(sender, "");
        
        if (deliveries.isEmpty()) {
            msg(sender, "&7Henüz kimse teslim etmedi.");
        } else {
            List<Map.Entry<UUID, Integer>> sorted = deliveries.entrySet().stream()
                .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed()).limit(10).toList();
            int rank = 1;
            for (var entry : sorted) {
                Player p = Bukkit.getPlayer(entry.getKey());
                String name = p != null ? p.getName() : entry.getKey().toString().substring(0, 8);
                String hl = entry.getKey().equals(senderUuid) ? " &a(Sen)" : "";
                String color = rank == 1 ? "&6" : rank == 2 ? "&f" : rank == 3 ? "&c" : "&7";
                msg(sender, color + rank + ". &f" + name + hl + " &8- &e" + entry.getValue());
                rank++;
            }
        }
        msg(sender, "");
    }
    
    private void showActiveInfo(String sender) {
        var events = deliveryService != null ? deliveryService.getAllActiveEvents() : List.<ActiveEvent>of();
        if (events.isEmpty()) { prefix(sender, "&7Şu an aktif teslimat yok."); return; }
        
        msg(sender, "");
        msg(sender, "&e&lD&6elivery&e&lC&6ore &8| &fAktif Teslimatlar");
        msg(sender, "");
        for (ActiveEvent event : events) {
            String displayName = deliveryGUI.getDeliveryDisplayName(event.getDeliveryName());
            String itemName = deliveryGUI.getItemDisplayName(event.getResolvedItem());
            msg(sender, "&a" + displayName + " &8- &7Eşya: &e" + itemName);
        }
        msg(sender, "");
    }

    private int calculateRank(ActiveEvent event, UUID playerUuid) {
        var deliveries = event.getPlayerDeliveries();
        int playerCount = deliveries.getOrDefault(playerUuid, 0);
        if (playerCount == 0) return 0;
        int rank = 1;
        for (int count : deliveries.values()) if (count > playerCount) rank++;
        return rank;
    }
    
    private void prefix(String sender, String message) { msg(sender, PREFIX + message); }
    private boolean hasPerm(String sender, String permission) { return permissionChecker.apply(sender, permission); }
    private void noPermission(String sender) { prefix(sender, "&cBu işlem için yetkiniz yok."); }
    private void msg(String sender, String message) { messageSender.accept(sender, message.replace("&", "\u00A7")); }
    private List<String> filter(List<String> list, String prefix) {
        String p = prefix.toLowerCase();
        return list.stream().filter(s -> s.toLowerCase().startsWith(p)).toList();
    }
}
