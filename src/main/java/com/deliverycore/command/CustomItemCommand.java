package com.deliverycore.command;

import com.deliverycore.service.CustomItemService;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.logging.Logger;

/**
 * Özel item komutları
 * v1.1 özelliği - /dc additem, /dc removeitem, /dc listcustom
 */
public class CustomItemCommand {
    
    private final CustomItemService customItemService;
    private final Logger logger;
    
    public CustomItemCommand(CustomItemService customItemService, Logger logger) {
        this.customItemService = customItemService;
        this.logger = logger;
    }
    
    /**
     * /dc additem [kategori] [isim] [fiyat] komutu
     * Eldeki item'ı kategoriye ekler
     * Kategori belirtilmezse mevcut kategorileri listeler
     */
    public boolean handleAddItem(Player player, String[] args) {
        // Kategori belirtilmemişse, mevcut kategorileri göster
        if (args.length == 0) {
            player.sendMessage("§e§lHangi kategoriye eklemek istiyorsunuz?");
            player.sendMessage("");
            
            // Mevcut kategorileri listele
            var categories = getCategoryNames();
            if (categories.isEmpty()) {
                player.sendMessage("§7  Henüz kategori yok. Yeni kategori oluşturabilirsiniz.");
            } else {
                player.sendMessage("§7Mevcut kategoriler:");
                for (String cat : categories) {
                    player.sendMessage("  §e▸ §f" + cat);
                }
            }
            
            player.sendMessage("");
            player.sendMessage("§7Kullanım: §e/dc additem <kategori> <isim> [fiyat]");
            player.sendMessage("§7Örnek: §e/dc additem rare MySpecialSword 1000");
            return true;
        }
        
        // Sadece kategori belirtilmişse, isim iste
        if (args.length == 1) {
            String category = args[0];
            player.sendMessage("§e§lItem ismi belirtin:");
            player.sendMessage("");
            player.sendMessage("§7Kategori: §f" + category);
            player.sendMessage("§7Kullanım: §e/dc additem " + category + " <isim> [fiyat]");
            player.sendMessage("§7Örnek: §e/dc additem " + category + " MySpecialSword 1000");
            
            // Bu kategorideki mevcut itemları göster
            var existingItems = getItemNames(category);
            if (!existingItems.isEmpty()) {
                player.sendMessage("");
                player.sendMessage("§7Bu kategorideki mevcut itemlar:");
                for (String item : existingItems) {
                    player.sendMessage("  §7• §f" + item);
                }
            }
            return true;
        }
        
        String category = args[0];
        String name = args[1];
        double price = 0.0;
        
        // Fiyat parametresi (opsiyonel)
        if (args.length >= 3) {
            try {
                price = Double.parseDouble(args[2]);
                if (price < 0) {
                    player.sendMessage("§c✗ Fiyat negatif olamaz: §f" + args[2]);
                    return true;
                }
            } catch (NumberFormatException e) {
                player.sendMessage("§c✗ Geçersiz fiyat: §f" + args[2]);
                return true;
            }
        }
        
        // Eldeki item'ı kontrol et
        ItemStack handItem = player.getInventory().getItemInMainHand();
        if (handItem == null || handItem.getType().isAir()) {
            player.sendMessage("§c✗ Elinizde item yok!");
            player.sendMessage("§7  Eklemek istediğiniz item'ı elinize alın.");
            return true;
        }
        
        // Aynı isimde item var mı kontrol et
        if (customItemService.customItemExists(category, name)) {
            player.sendMessage("§c✗ Bu isimde item zaten var: §f" + category + ":" + name);
            player.sendMessage("§7  Farklı bir isim kullanın veya önce silin.");
            return true;
        }
        
        try {
            // Item'ı ekle
            customItemService.addCustomItem(category, name, handItem, price);
            customItemService.saveCustomItems();
            
            String displayName = handItem.hasItemMeta() && handItem.getItemMeta().hasDisplayName() 
                ? handItem.getItemMeta().getDisplayName() 
                : handItem.getType().name();
            
            player.sendMessage("§a✓ Özel item eklendi!");
            player.sendMessage("§7  Kategori: §f" + category);
            player.sendMessage("§7  İsim: §f" + name);
            player.sendMessage("§7  Item: §e" + displayName);
            player.sendMessage("§7  Fiyat: §f" + (price > 0 ? price + " coin" : "Ücretsiz"));
            
            logger.info(player.getName() + " özel item ekledi: " + category + ":" + name + 
                       " (fiyat: " + price + ")");
            
        } catch (Exception e) {
            player.sendMessage("§c✗ Item eklenirken hata oluştu!");
            player.sendMessage("§7  Hata: " + e.getMessage());
            logger.severe("Özel item ekleme hatası: " + e.getMessage());
        }
        
        return true;
    }
    
    /**
     * /dc removeitem <kategori> <isim> komutu
     * Özel item'ı siler
     */
    public boolean handleRemoveItem(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§c✗ Kullanım: §e/dc removeitem <kategori> <isim>");
            player.sendMessage("§7  Özel item'ı siler.");
            player.sendMessage("§7  Örnek: §e/dc removeitem rare MySpecialSword");
            return true;
        }
        
        String category = args[0];
        String name = args[1];
        
        // Item var mı kontrol et
        if (!customItemService.customItemExists(category, name)) {
            player.sendMessage("§c✗ Bu item bulunamadı: §f" + category + ":" + name);
            player.sendMessage("§7  Mevcut itemlar için: §e/dc listcustom " + category);
            return true;
        }
        
        try {
            // Item'ı sil
            customItemService.removeCustomItem(category, name);
            customItemService.saveCustomItems();
            
            player.sendMessage("§a✓ Özel item silindi!");
            player.sendMessage("§7  Kategori: §f" + category);
            player.sendMessage("§7  İsim: §f" + name);
            
            logger.info(player.getName() + " özel item sildi: " + category + ":" + name);
            
        } catch (Exception e) {
            player.sendMessage("§c✗ Item silinirken hata oluştu!");
            player.sendMessage("§7  Hata: " + e.getMessage());
            logger.severe("Özel item silme hatası: " + e.getMessage());
        }
        
        return true;
    }
    
    /**
     * /dc listcustom [kategori] komutu
     * Özel itemları listeler
     */
    public boolean handleListCustom(Player player, String[] args) {
        if (args.length == 0) {
            // Tüm özel itemları listele
            var allItems = customItemService.getAllCustomItems();
            
            player.sendMessage("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            player.sendMessage("§6§l  DeliveryCore §8│ §fÖzel Item Listesi");
            player.sendMessage("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            player.sendMessage("");
            
            if (allItems.isEmpty()) {
                player.sendMessage("§7  Tanımlı özel item yok.");
                player.sendMessage("§7  §oEklemek için: §e/dc additem <kategori> <isim>");
            } else {
                // Kategoriye göre grupla
                var groupedItems = allItems.stream()
                    .collect(java.util.stream.Collectors.groupingBy(item -> item.category()));
                
                for (var entry : groupedItems.entrySet()) {
                    String category = entry.getKey();
                    var items = entry.getValue();
                    
                    player.sendMessage("§e▸ §f" + category + " §8(§7" + items.size() + " item§8)");
                    
                    for (var item : items) {
                        String priceStr = item.hasPrice() ? "§7- §f" + item.price() + " coin" : "";
                        player.sendMessage("    §7• §f" + item.name() + " " + priceStr);
                        if (item.hasCustomDisplayName()) {
                            player.sendMessage("      §8└ §7" + item.displayName());
                        }
                    }
                    player.sendMessage("");
                }
            }
            
            player.sendMessage("§7  Toplam: §f" + allItems.size() + " §7özel item");
            player.sendMessage("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            
        } else {
            // Belirli kategoriyi listele
            String category = args[0];
            var items = customItemService.getCustomItems(category);
            
            player.sendMessage("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            player.sendMessage("§6§l  DeliveryCore §8│ §f" + category + " Kategorisi");
            player.sendMessage("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            player.sendMessage("");
            
            if (items.isEmpty()) {
                player.sendMessage("§7  Bu kategoride özel item yok.");
                player.sendMessage("§7  §oEklemek için: §e/dc additem " + category + " <isim>");
            } else {
                for (var item : items) {
                    String priceStr = item.hasPrice() ? " §8- §f" + item.price() + " coin" : "";
                    player.sendMessage("  §7• §f" + item.name() + priceStr);
                    
                    if (item.hasCustomDisplayName()) {
                        player.sendMessage("    §8└ §7Display: §f" + item.displayName());
                    }
                    
                    if (item.hasLore()) {
                        player.sendMessage("    §8└ §7Lore: §8" + item.lore().size() + " satır");
                    }
                }
            }
            
            player.sendMessage("");
            player.sendMessage("§7  Toplam: §f" + items.size() + " §7item");
            player.sendMessage("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        }
        
        return true;
    }
    
    /**
     * Tab completion için kategori isimlerini döndürür
     */
    public List<String> getCategoryNames() {
        return customItemService.getAllCustomItems().stream()
            .map(item -> item.category())
            .distinct()
            .sorted()
            .toList();
    }
    
    /**
     * Tab completion için belirli kategorideki item isimlerini döndürür
     */
    public List<String> getItemNames(String category) {
        return customItemService.getCustomItems(category).stream()
            .map(item -> item.name())
            .sorted()
            .toList();
    }
}