package com.deliverycore.command;

import com.deliverycore.service.CustomItemService;
import com.deliverycore.util.LangManager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.logging.Logger;

public class CustomItemCommand {
    
    private final CustomItemService customItemService;
    private final Logger logger;
    private LangManager lang;
    
    public CustomItemCommand(CustomItemService customItemService, Logger logger) {
        this.customItemService = customItemService;
        this.logger = logger;
    }
    
    public void setLangManager(LangManager lang) { this.lang = lang; }

    public boolean handleAddItem(Player player, String[] args) {
        if (args.length == 0) {
            player.sendMessage(lang.get("command.custom-item.category-prompt"));
            player.sendMessage("");
            var categories = getCategoryNames();
            if (categories.isEmpty()) {
                player.sendMessage(lang.get("command.custom-item.no-categories"));
            } else {
                player.sendMessage(lang.get("command.custom-item.existing-categories"));
                for (String cat : categories) {
                    player.sendMessage("  " + lang.get("command.custom-item.category-entry", "{name}", cat));
                }
            }
            player.sendMessage("");
            player.sendMessage(lang.get("command.custom-item.add-usage"));
            player.sendMessage(lang.get("command.custom-item.add-example"));
            return true;
        }

        if (args.length == 1) {
            String category = args[0];
            player.sendMessage(lang.get("command.custom-item.name-prompt"));
            player.sendMessage("");
            player.sendMessage(lang.get("command.custom-item.category-label", "{category}", category));
            player.sendMessage(lang.get("command.custom-item.add-usage").replace("<kategori>", category).replace("<category>", category));
            var existingItems = getItemNames(category);
            if (!existingItems.isEmpty()) {
                player.sendMessage("");
                player.sendMessage(lang.get("command.custom-item.existing-items"));
                for (String item : existingItems) {
                    player.sendMessage("  " + lang.get("command.custom-item.item-entry", "{name}", item));
                }
            }
            return true;
        }
        
        String category = args[0];
        String name = args[1];
        double price = 0.0;
        
        if (args.length >= 3) {
            try {
                price = Double.parseDouble(args[2]);
                if (price < 0) { player.sendMessage(lang.get("command.custom-item.negative-price", "{value}", args[2])); return true; }
            } catch (NumberFormatException e) { player.sendMessage(lang.get("command.custom-item.invalid-price", "{value}", args[2])); return true; }
        }
        
        ItemStack handItem = player.getInventory().getItemInMainHand();
        if (handItem == null || handItem.getType().isAir()) {
            player.sendMessage(lang.get("command.custom-item.no-item-hand"));
            player.sendMessage(lang.get("command.custom-item.no-item-hint"));
            return true;
        }
        
        if (customItemService.customItemExists(category, name)) {
            player.sendMessage(lang.get("command.custom-item.already-exists", "{id}", category + ":" + name));
            player.sendMessage(lang.get("command.custom-item.already-exists-hint"));
            return true;
        }
        
        try {
            customItemService.addCustomItem(category, name, handItem, price);
            customItemService.saveCustomItems();
            String displayName = handItem.hasItemMeta() && handItem.getItemMeta().hasDisplayName() 
                ? handItem.getItemMeta().getDisplayName() : handItem.getType().name();
            player.sendMessage(lang.get("command.custom-item.add-success"));
            player.sendMessage(lang.get("command.custom-item.add-category", "{category}", category));
            player.sendMessage(lang.get("command.custom-item.add-name", "{name}", name));
            player.sendMessage(lang.get("command.custom-item.add-item", "{display}", displayName));
            String priceStr = price > 0 ? price + " coin" : lang.get("command.custom-item.add-free");
            player.sendMessage(lang.get("command.custom-item.add-price", "{price}", priceStr));
            logger.info(player.getName() + " added custom item: " + category + ":" + name + " (price: " + price + ")");
        } catch (Exception e) {
            player.sendMessage(lang.get("command.custom-item.add-error"));
            player.sendMessage(lang.get("command.custom-item.error-detail", "{error}", e.getMessage()));
            logger.severe("Custom item add error: " + e.getMessage());
        }
        return true;
    }

    public boolean handleRemoveItem(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(lang.get("command.custom-item.remove-usage"));
            player.sendMessage(lang.get("command.custom-item.remove-hint"));
            player.sendMessage(lang.get("command.custom-item.remove-example"));
            return true;
        }
        String category = args[0];
        String name = args[1];
        if (!customItemService.customItemExists(category, name)) {
            player.sendMessage(lang.get("command.custom-item.remove-not-found", "{id}", category + ":" + name));
            player.sendMessage(lang.get("command.custom-item.remove-list-hint", "{category}", category));
            return true;
        }
        try {
            customItemService.removeCustomItem(category, name);
            customItemService.saveCustomItems();
            player.sendMessage(lang.get("command.custom-item.remove-success"));
            player.sendMessage(lang.get("command.custom-item.add-category", "{category}", category));
            player.sendMessage(lang.get("command.custom-item.add-name", "{name}", name));
            logger.info(player.getName() + " removed custom item: " + category + ":" + name);
        } catch (Exception e) {
            player.sendMessage(lang.get("command.custom-item.remove-error"));
            player.sendMessage(lang.get("command.custom-item.error-detail", "{error}", e.getMessage()));
            logger.severe("Custom item remove error: " + e.getMessage());
        }
        return true;
    }

    public boolean handleListCustom(Player player, String[] args) {
        String header = "§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━";
        if (args.length == 0) {
            var allItems = customItemService.getAllCustomItems();
            player.sendMessage(header);
            player.sendMessage(lang.get("command.custom-item.list-title"));
            player.sendMessage(header);
            player.sendMessage("");
            if (allItems.isEmpty()) {
                player.sendMessage(lang.get("command.custom-item.list-empty"));
                player.sendMessage(lang.get("command.custom-item.list-add-hint"));
            } else {
                var groupedItems = allItems.stream().collect(java.util.stream.Collectors.groupingBy(item -> item.category()));
                for (var entry : groupedItems.entrySet()) {
                    String category = entry.getKey();
                    var items = entry.getValue();
                    player.sendMessage(lang.get("command.custom-item.list-category-entry", "{name}", category, "{count}", String.valueOf(items.size())));
                    for (var item : items) {
                        String priceStr = item.hasPrice() ? " " + lang.get("command.custom-item.list-item-price", "{price}", String.valueOf(item.price())) : "";
                        player.sendMessage("    " + lang.get("command.custom-item.list-item-entry", "{name}", item.name()) + priceStr);
                        if (item.hasCustomDisplayName()) player.sendMessage("      " + lang.get("command.custom-item.list-display", "{display}", item.displayName()));
                    }
                    player.sendMessage("");
                }
            }
            player.sendMessage(lang.get("command.custom-item.list-total", "{count}", String.valueOf(allItems.size())));
            player.sendMessage(header);
        } else {
            String category = args[0];
            var items = customItemService.getCustomItems(category);
            player.sendMessage(header);
            player.sendMessage(lang.get("command.custom-item.list-category-title", "{category}", category));
            player.sendMessage(header);
            player.sendMessage("");
            if (items.isEmpty()) {
                player.sendMessage(lang.get("command.custom-item.list-category-empty"));
                player.sendMessage(lang.get("command.custom-item.list-category-add-hint", "{category}", category));
            } else {
                for (var item : items) {
                    String priceStr = item.hasPrice() ? " " + lang.get("command.custom-item.list-item-price", "{price}", String.valueOf(item.price())) : "";
                    player.sendMessage("  " + lang.get("command.custom-item.list-item-entry", "{name}", item.name()) + priceStr);
                    if (item.hasCustomDisplayName()) player.sendMessage("    " + lang.get("command.custom-item.list-display", "{display}", item.displayName()));
                    if (item.hasLore()) player.sendMessage("    " + lang.get("command.custom-item.list-lore", "{count}", String.valueOf(item.lore().size())));
                }
            }
            player.sendMessage("");
            player.sendMessage(lang.get("command.custom-item.list-total", "{count}", String.valueOf(items.size())));
            player.sendMessage(header);
        }
        return true;
    }

    public List<String> getCategoryNames() {
        return customItemService.getAllCustomItems().stream().map(item -> item.category()).distinct().sorted().toList();
    }

    public List<String> getItemNames(String category) {
        return customItemService.getCustomItems(category).stream().map(item -> item.name()).sorted().toList();
    }
}
