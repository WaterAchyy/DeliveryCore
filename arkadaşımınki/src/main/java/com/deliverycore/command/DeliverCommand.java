package com.deliverycore.command;

import com.deliverycore.config.ConfigManager;
import com.deliverycore.gui.DeliveryGUI;
import com.deliverycore.service.ActiveEvent;
import com.deliverycore.service.DeliveryService;
import com.deliverycore.util.LangManager;
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
    private LangManager lang;

    public DeliverCommand(ConfigManager configManager, BiFunction<String, String, Boolean> permissionChecker,
            BiConsumer<String, String> messageSender, Logger logger, DeliveryGUI deliveryGUI) {
        this.configManager = configManager;
        this.permissionChecker = permissionChecker;
        this.messageSender = messageSender;
        this.logger = logger;
        this.deliveryGUI = deliveryGUI;
    }

    public void setDeliveryService(DeliveryService deliveryService) { this.deliveryService = deliveryService; }
    public void setLangManager(LangManager lang) { this.lang = lang; }

    public boolean handleTeslimatCommand(String sender, UUID senderUuid, String[] args) {
        if (!hasPerm(sender, PERM_USE)) { noPermission(sender); return true; }
        Player player = Bukkit.getPlayer(sender);
        if (player == null) { prefix(sender, lang.get("general.player-only")); return true; }
        if (args.length == 0) { deliveryGUI.openMainMenu(player); return true; }
        String sub = args[0].toLowerCase();
        return switch (sub) {
            case "help", "yardim", "?" -> { sendPlayerHelp(sender); yield true; }
            case "top", "siralama" -> { showLeaderboard(sender, senderUuid); yield true; }
            case "bilgi", "info" -> { showActiveInfo(sender); yield true; }
            default -> { prefix(sender, lang.get("general.unknown-command")); yield true; }
        };
    }

    public boolean handleTeslimCommand(String sender, UUID senderUuid, String[] args) {
        if (!hasPerm(sender, PERM_DELIVER)) { noPermission(sender); return true; }
        Player player = Bukkit.getPlayer(sender);
        if (player == null) { prefix(sender, lang.get("general.player-only")); return true; }
        if (deliveryService == null || deliveryService.getAllActiveEvents().isEmpty()) {
            prefix(sender, lang.get("delivery.no-active"));
            return true;
        }
        ActiveEvent targetEvent;
        int maxAmount = -1;
        if (args.length == 0) {
            targetEvent = deliveryService.getAllActiveEvents().get(0);
        } else {
            String deliveryName = args[0].toLowerCase();
            targetEvent = deliveryService.getAllActiveEvents().stream()
                .filter(e -> e.getDeliveryName().equalsIgnoreCase(deliveryName))
                .findFirst().orElse(null);
            if (targetEvent == null) {
                prefix(sender, lang.get("delivery.not-found", "{name}", deliveryName));
                showActiveDeliveries(sender);
                return true;
            }
            if (args.length >= 2) {
                try {
                    maxAmount = Integer.parseInt(args[1]);
                    if (maxAmount <= 0) { prefix(sender, lang.get("delivery.invalid-amount")); return true; }
                } catch (NumberFormatException e) { prefix(sender, lang.get("delivery.invalid-amount")); return true; }
            }
        }
        deliverFromInventory(player, senderUuid, targetEvent, maxAmount);
        return true;
    }

    private void deliverFromInventory(Player player, UUID playerUuid, ActiveEvent event, int maxAmount) {
        String requiredItem = event.getResolvedItem();
        Material material;
        try { material = Material.valueOf(requiredItem.toUpperCase()); }
        catch (Exception e) { prefix(player.getName(), lang.get("delivery.invalid-item", "{item}", requiredItem)); return; }
        
        int totalInInventory = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) totalInInventory += item.getAmount();
        }
        if (totalInInventory == 0) {
            String itemName = deliveryGUI.getItemDisplayName(requiredItem);
            prefix(player.getName(), lang.get("delivery.no-item", "{item}", itemName));
            return;
        }
        int toDeliver = (maxAmount <= 0 || maxAmount > totalInInventory) ? totalInInventory : maxAmount;
        int remaining = toDeliver;
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length && remaining > 0; i++) {
            ItemStack item = contents[i];
            if (item != null && item.getType() == material) {
                int itemAmount = item.getAmount();
                if (itemAmount <= remaining) { remaining -= itemAmount; player.getInventory().setItem(i, null); }
                else { item.setAmount(itemAmount - remaining); remaining = 0; }
            }
        }
        int delivered = toDeliver - remaining;
        deliveryService.recordDelivery(playerUuid, event.getDeliveryName(), delivered);
        int total = event.getPlayerDeliveries().getOrDefault(playerUuid, 0);
        int rank = calculateRank(event, playerUuid);
        String itemName = deliveryGUI.getItemDisplayName(requiredItem);
        
        player.sendTitle(lang.get("delivery.success-title"), lang.get("delivery.success-subtitle", "{amount}", String.valueOf(delivered), "{item}", itemName), 10, 50, 10);
        prefix(player.getName(), lang.get("delivery.success", "{amount}", String.valueOf(delivered), "{item}", itemName));
        prefix(player.getName(), lang.get("delivery.stats", "{total}", String.valueOf(total), "{rank}", String.valueOf(rank)));
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
        prefix(sender, lang.get("delivery.active-list", "{list}", sb.toString()));
    }

    public List<String> handleTeslimatTabComplete(String sender, String[] args) {
        if (args.length == 1) return filter(Arrays.asList("help", "top", "bilgi"), args[0]);
        return List.of();
    }

    public List<String> handleTeslimTabComplete(String sender, String[] args) {
        if (deliveryService == null) return List.of();
        if (args.length == 1) {
            List<String> names = deliveryService.getAllActiveEvents().stream().map(ActiveEvent::getDeliveryName).toList();
            return filter(new ArrayList<>(names), args[0]);
        }
        if (args.length == 2) return filter(Arrays.asList("1", "10", "32", "64", "100", "500"), args[1]);
        return List.of();
    }

    private void sendPlayerHelp(String sender) {
        msg(sender, "");
        msg(sender, lang.get("player.help.title"));
        msg(sender, "");
        msg(sender, lang.get("player.help.menu"));
        msg(sender, lang.get("player.help.top"));
        msg(sender, lang.get("player.help.info"));
        msg(sender, "");
        msg(sender, lang.get("player.help.deliver"));
        msg(sender, lang.get("player.help.deliver-name"));
        msg(sender, lang.get("player.help.deliver-amount"));
        msg(sender, "");
    }

    private void showLeaderboard(String sender, UUID senderUuid) {
        var events = deliveryService != null ? deliveryService.getAllActiveEvents() : List.<ActiveEvent>of();
        if (events.isEmpty()) { prefix(sender, lang.get("command.top.no-active")); return; }
        ActiveEvent event = events.get(0);
        var deliveries = event.getPlayerDeliveries();
        msg(sender, "");
        msg(sender, lang.get("player.leaderboard.title", "{delivery}", deliveryGUI.getDeliveryDisplayName(event.getDeliveryName())));
        msg(sender, "");
        if (deliveries.isEmpty()) { msg(sender, lang.get("player.leaderboard.empty")); }
        else {
            List<Map.Entry<UUID, Integer>> sorted = deliveries.entrySet().stream()
                .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed()).limit(10).toList();
            int rank = 1;
            for (var entry : sorted) {
                Player p = Bukkit.getPlayer(entry.getKey());
                String name = p != null ? p.getName() : entry.getKey().toString().substring(0, 8);
                String hl = entry.getKey().equals(senderUuid) ? " " + lang.get("player.leaderboard.you") : "";
                String color = lang.getRankColor(rank);
                msg(sender, color + rank + ". &f" + name + hl + " &8- &e" + entry.getValue());
                rank++;
            }
        }
        msg(sender, "");
    }

    private void showActiveInfo(String sender) {
        var events = deliveryService != null ? deliveryService.getAllActiveEvents() : List.<ActiveEvent>of();
        if (events.isEmpty()) { prefix(sender, lang.get("player.active.none")); return; }
        msg(sender, "");
        msg(sender, lang.get("player.active.title"));
        msg(sender, "");
        for (ActiveEvent event : events) {
            String displayName = deliveryGUI.getDeliveryDisplayName(event.getDeliveryName());
            String itemName = deliveryGUI.getItemDisplayName(event.getResolvedItem());
            msg(sender, lang.get("player.active.entry", "{delivery}", displayName, "{item}", itemName));
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

    private void prefix(String sender, String message) { msg(sender, lang.getPrefix() + " " + message); }
    private boolean hasPerm(String sender, String permission) { return permissionChecker.apply(sender, permission); }
    private void noPermission(String sender) { prefix(sender, lang.get("general.no-permission")); }
    private void msg(String sender, String message) { messageSender.accept(sender, message.replace("&", "\u00A7")); }
    private List<String> filter(List<String> list, String prefix) {
        String p = prefix.toLowerCase();
        return list.stream().filter(s -> s.toLowerCase().startsWith(p)).toList();
    }
}
