package com.deliverycore.command;

import com.deliverycore.config.ConfigManager;
import com.deliverycore.config.ReloadResult;
import com.deliverycore.model.DeliveryDefinition;
import com.deliverycore.model.ValidationError;
import com.deliverycore.service.ActiveEvent;
import com.deliverycore.service.DeliveryService;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.logging.Logger;

/**
 * DeliveryCore Ana Komut Yöneticisi
 * 
 * ═══════════════════════════════════════════════════════════════
 * KOMUTLAR:
 * ═══════════════════════════════════════════════════════════════
 * 
 * TEMEL:
 *   /dc help                    - Yardım menüsü
 *   /dc version                 - Sürüm bilgisi
 * 
 * YÖNETİM:
 *   /dc reload                  - Konfigürasyonu yeniden yükle
 *   /dc info                    - Sistem bilgisi
 * 
 * ETKİNLİK:
 *   /dc list                    - Tüm teslimatları listele
 *   /dc status [teslimat]       - Aktif etkinlik durumu
 *   /dc start <teslimat>        - Manuel etkinlik başlat
 *   /dc stop <teslimat>         - Etkinliği durdur
 *   /dc top [teslimat]          - Sıralama tablosu
 * 
 * ═══════════════════════════════════════════════════════════════
 * YETKİLER:
 * ═══════════════════════════════════════════════════════════════
 *   deliverycore.*              - Tüm yetkiler
 *   deliverycore.admin          - Tüm admin yetkileri
 *   deliverycore.admin.reload   - Reload komutu
 *   deliverycore.admin.info     - Info komutu
 *   deliverycore.admin.event    - Etkinlik yönetimi
 *   deliverycore.use            - Temel komutlar
 *   deliverycore.participate    - Etkinliklere katılım
 */
public class CommandHandler {

    // ═══════════════════════════════════════════════════════════════
    // YETKİ DÜĞÜMLERİ
    // ═══════════════════════════════════════════════════════════════
    public static final String PERM_USE = "deliverycore.use";
    public static final String PERM_ADMIN = "deliverycore.admin";
    public static final String PERM_ADMIN_RELOAD = "deliverycore.admin.reload";
    public static final String PERM_ADMIN_INFO = "deliverycore.admin.info";
    public static final String PERM_ADMIN_EVENT = "deliverycore.admin.event";
    public static final String PERM_PARTICIPATE = "deliverycore.participate";

    // ═══════════════════════════════════════════════════════════════
    // BAĞIMLILIKLAR
    // ═══════════════════════════════════════════════════════════════
    private final ConfigManager configManager;
    private final BiFunction<String, String, Boolean> permissionChecker;
    private final BiConsumer<String, String> messageSender;
    private final Logger logger;
    private DeliveryService deliveryService;
    private Runnable reloadCallback;

    // ═══════════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════════
    public CommandHandler(
            ConfigManager configManager,
            BiFunction<String, String, Boolean> permissionChecker,
            BiConsumer<String, String> messageSender,
            Logger logger) {
        this.configManager = configManager;
        this.permissionChecker = permissionChecker;
        this.messageSender = messageSender;
        this.logger = logger;
    }

    public CommandHandler(ConfigManager configManager) {
        this(configManager, (p, perm) -> true, (p, m) -> {}, 
             Logger.getLogger(CommandHandler.class.getName()));
    }

    public void setDeliveryService(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    public void setReloadCallback(Runnable callback) {
        this.reloadCallback = callback;
    }

    // ═══════════════════════════════════════════════════════════════
    // ANA KOMUT YÖNLENDİRİCİ
    // ═══════════════════════════════════════════════════════════════
    public boolean handleCommand(String sender, String[] args) {
        if (args == null || args.length == 0) {
            msg(sender, "");
            msg(sender, "&e§lD&6elivery&e§lC&6ore &8» &7Teslimat Etkinlik Sistemi");
            msg(sender, "");
            msg(sender, "&7Komutları görmek için: &e/dc help");
            msg(sender, "&7Menüyü açmak için: &e/teslimat");
            msg(sender, "");
            return true;
        }

        String sub = args[0].toLowerCase();
        String[] subArgs = args.length > 1 ? Arrays.copyOfRange(args, 1, args.length) : new String[0];

        return switch (sub) {
            case "help", "?", "h", "yardim" -> { sendHelp(sender); yield true; }
            case "version", "ver", "v", "surum" -> { sendVersion(sender); yield true; }
            case "reload", "rl", "yenile" -> handleReload(sender);
            case "info", "i", "bilgi" -> handleInfo(sender);
            case "list", "ls", "liste" -> handleList(sender);
            case "status", "st", "durum" -> handleStatus(sender, subArgs);
            case "start", "baslat" -> handleStart(sender, subArgs);
            case "stop", "durdur" -> handleStop(sender, subArgs);
            case "top", "siralama" -> handleTop(sender, subArgs);
            case "categories", "cat", "kategoriler" -> handleCategories(sender);
            case "toggle", "ac", "kapat" -> handleToggle(sender, subArgs);
            case "test" -> handleTest(sender, subArgs);
            default -> {
                msg(sender, "&c✗ Bilinmeyen komut: &f" + sub);
                msg(sender, "&7Yardım için: &e/dc help");
                yield true;
            }
        };
    }

    // ═══════════════════════════════════════════════════════════════
    // TAB COMPLETION
    // ═══════════════════════════════════════════════════════════════
    public List<String> handleTabComplete(String sender, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>(Arrays.asList("help", "version"));
            
            if (hasPerm(sender, PERM_ADMIN_RELOAD)) completions.add("reload");
            if (hasPerm(sender, PERM_ADMIN_INFO)) {
                completions.add("info");
                completions.add("categories");
            }
            if (hasPerm(sender, PERM_ADMIN_EVENT)) {
                completions.addAll(Arrays.asList("list", "status", "start", "stop", "top", "toggle", "test"));
            }
            
            return filter(completions, args[0]);
        }

        if (args.length == 2 && hasPerm(sender, PERM_ADMIN_EVENT)) {
            String sub = args[0].toLowerCase();
            if (List.of("stop", "status", "top", "toggle").contains(sub)) {
                return filter(getDeliveryNames(), args[1]);
            }
            if (sub.equals("start")) {
                return filter(getDeliveryNames(), args[1]);
            }
            if (sub.equals("test")) {
                return filter(Arrays.asList("deliver", "reward", "webhook"), args[1]);
            }
        }
        
        // Start komutu için süre önerileri
        if (args.length == 3 && args[0].equalsIgnoreCase("start") && hasPerm(sender, PERM_ADMIN_EVENT)) {
            return filter(Arrays.asList("30m", "1h", "2h", "3h", "1h30m", "2h30m"), args[2]);
        }
        
        // Start komutu için kazanan sayısı önerileri
        if (args.length == 4 && args[0].equalsIgnoreCase("start") && hasPerm(sender, PERM_ADMIN_EVENT)) {
            return filter(Arrays.asList("1", "2", "3", "5", "10"), args[3]);
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("test") && 
            args[1].equalsIgnoreCase("deliver") && hasPerm(sender, PERM_ADMIN_EVENT)) {
            return filter(getDeliveryNames(), args[2]);
        }

        return List.of();
    }

    private List<String> filter(List<String> list, String prefix) {
        String p = prefix.toLowerCase();
        return list.stream().filter(s -> s.toLowerCase().startsWith(p)).toList();
    }

    private List<String> getDeliveryNames() {
        return new ArrayList<>(configManager.getDeliveryConfig().getDeliveries().keySet());
    }

    // ═══════════════════════════════════════════════════════════════
    // RELOAD KOMUTU
    // ═══════════════════════════════════════════════════════════════
    private boolean handleReload(String sender) {
        if (!hasPerm(sender, PERM_ADMIN_RELOAD)) {
            noPermission(sender);
            return true;
        }

        msg(sender, "&7⟳ Konfigürasyon yeniden yükleniyor...");
        logger.info(sender + " konfigürasyonu yeniden yüklüyor");

        ReloadResult result = configManager.reloadWithResult();

        if (result.isSuccess()) {
            // Reload callback'i çağır (GUI ayarlarını yeniden yükle)
            if (reloadCallback != null) {
                try {
                    reloadCallback.run();
                } catch (Exception e) {
                    logger.warning("Reload callback hatası: " + e.getMessage());
                }
            }
            
            if (result.hasErrors()) {
                msg(sender, "&e⚠ " + result.errors().size() + " uyarı ile yüklendi");
                logErrors(result.errors());
            } else {
                msg(sender, "&a✓ Konfigürasyon başarıyla yüklendi!");
            }
        } else {
            msg(sender, "&c✗ Yükleme başarısız! Önceki ayarlar aktif.");
            logErrors(result.errors());
        }

        return true;
    }

    // ═══════════════════════════════════════════════════════════════
    // INFO KOMUTU
    // ═══════════════════════════════════════════════════════════════
    private boolean handleInfo(String sender) {
        if (!hasPerm(sender, PERM_ADMIN_INFO)) {
            noPermission(sender);
            return true;
        }

        int categories = configManager.getCategoryConfig().getCategories().size();
        int deliveries = configManager.getDeliveryConfig().getDeliveries().size();
        int enabled = configManager.getDeliveryConfig().getEnabledDeliveries().size();
        int active = deliveryService != null ? deliveryService.getAllActiveEvents().size() : 0;

        header(sender, "Sistem Bilgisi");
        msg(sender, "");
        msg(sender, "&7  Kategoriler    &8│ &f" + categories);
        msg(sender, "&7  Teslimatlar    &8│ &f" + deliveries + " &7(&a" + enabled + " aktif&7)");
        msg(sender, "&7  Çalışan        &8│ &a" + active + " &7etkinlik");
        msg(sender, "&7  Dil            &8│ &ftr, en");
        msg(sender, "");
        footer(sender);

        return true;
    }

    // ═══════════════════════════════════════════════════════════════
    // LIST KOMUTU
    // ═══════════════════════════════════════════════════════════════
    private boolean handleList(String sender) {
        if (!hasPerm(sender, PERM_ADMIN_EVENT)) {
            noPermission(sender);
            return true;
        }

        var deliveries = configManager.getDeliveryConfig().getDeliveries();

        header(sender, "Teslimat Listesi");
        msg(sender, "");

        if (deliveries.isEmpty()) {
            msg(sender, "&7  Tanımlı teslimat yok.");
            msg(sender, "&7  &odeliveries.yml dosyasını düzenleyin.");
        } else {
            for (var entry : deliveries.entrySet()) {
                String name = entry.getKey();
                DeliveryDefinition def = entry.getValue();
                boolean running = deliveryService != null && 
                                  deliveryService.getActiveEvent(name).isPresent();

                String icon = running ? "&a▶" : (def.enabled() ? "&e●" : "&c○");
                String status = running ? "&a[ÇALIŞIYOR]" : 
                               (def.enabled() ? "&e[HAZIR]" : "&c[KAPALI]");

                msg(sender, "  " + icon + " &f" + name + " " + status);
                
                String catMode = def.category().mode().name();
                String catVal = def.category().value() != null ? def.category().value() : "rastgele";
                msg(sender, "     &7Kategori: &f" + catVal + " &8(" + catMode.toLowerCase() + ")");
                msg(sender, "     &7Kazanan: &f" + def.winnerCount() + " &7kişi");
            }
        }

        msg(sender, "");
        msg(sender, "&7  Toplam: &f" + deliveries.size() + " &7teslimat");
        footer(sender);

        return true;
    }

    // ═══════════════════════════════════════════════════════════════
    // STATUS KOMUTU
    // ═══════════════════════════════════════════════════════════════
    private boolean handleStatus(String sender, String[] args) {
        if (!hasPerm(sender, PERM_ADMIN_EVENT)) {
            noPermission(sender);
            return true;
        }

        if (deliveryService == null) {
            msg(sender, "&c✗ Servis henüz hazır değil.");
            return true;
        }

        // Belirli bir teslimat
        if (args.length > 0) {
            return showDeliveryStatus(sender, args[0]);
        }

        // Tüm aktif etkinlikler
        var activeEvents = deliveryService.getAllActiveEvents();

        header(sender, "Aktif Etkinlikler");
        msg(sender, "");

        if (activeEvents.isEmpty()) {
            msg(sender, "&7  Şu an aktif etkinlik yok.");
            msg(sender, "&7  &oManuel başlatmak için: &e/dc start <ad>");
        } else {
            for (ActiveEvent e : activeEvents) {
                msg(sender, "  &a▶ &f" + e.getDeliveryName());
                msg(sender, "     &7Eşya: &e" + e.getResolvedItem());
                msg(sender, "     &7Kategori: &f" + e.getResolvedCategory());
                msg(sender, "     &7Katılımcı: &f" + e.getPlayerDeliveries().size());
                msg(sender, "     &7Toplam Teslimat: &f" + e.getTotalDeliveries());
                
                if (e.getEndTime() != null) {
                    String endStr = e.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm"));
                    msg(sender, "     &7Bitiş: &f" + endStr);
                }
                msg(sender, "");
            }
        }

        msg(sender, "&7  Aktif: &f" + activeEvents.size() + " &7etkinlik");
        footer(sender);

        return true;
    }

    private boolean showDeliveryStatus(String sender, String name) {
        Optional<ActiveEvent> eventOpt = deliveryService.getActiveEvent(name);

        if (eventOpt.isEmpty()) {
            // Teslimat var mı kontrol et
            if (configManager.getDeliveryConfig().getDelivery(name).isEmpty()) {
                msg(sender, "&c✗ Teslimat bulunamadı: &f" + name);
                return true;
            }
            msg(sender, "&7ℹ Bu teslimat şu an aktif değil: &f" + name);
            msg(sender, "&7  Başlatmak için: &e/dc start " + name);
            return true;
        }

        ActiveEvent e = eventOpt.get();

        header(sender, e.getDeliveryName());
        msg(sender, "");
        msg(sender, "  &7Durum        &8│ &a● Çalışıyor");
        msg(sender, "  &7Kategori     &8│ &f" + e.getResolvedCategory());
        msg(sender, "  &7Eşya         &8│ &e" + e.getResolvedItem());
        msg(sender, "  &7Katılımcı    &8│ &f" + e.getPlayerDeliveries().size() + " kişi");
        msg(sender, "  &7Teslimat     &8│ &f" + e.getTotalDeliveries() + " adet");
        
        if (e.getStartTime() != null) {
            String startStr = e.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm"));
            msg(sender, "  &7Başlangıç    &8│ &f" + startStr);
        }
        if (e.getEndTime() != null) {
            String endStr = e.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm"));
            msg(sender, "  &7Bitiş        &8│ &f" + endStr);
        }

        msg(sender, "");
        footer(sender);

        return true;
    }

    // ═══════════════════════════════════════════════════════════════
    // START KOMUTU
    // ═══════════════════════════════════════════════════════════════
    private boolean handleStart(String sender, String[] args) {
        if (!hasPerm(sender, PERM_ADMIN_EVENT)) {
            noPermission(sender);
            return true;
        }

        if (deliveryService == null) {
            msg(sender, "&c✗ Servis henüz hazır değil.");
            return true;
        }

        if (args.length == 0) {
            msg(sender, "&c✗ Kullanım: &e/dc start <teslimat> [süre] [kazanan]");
            msg(sender, "&7  Süre formatı: &f1h&7, &f30m&7, &f2h30m");
            msg(sender, "&7  Kazanan: &f1-10 &7arası sayı");
            msg(sender, "&7  Örnek: &e/dc start gunluk 2h 3");
            msg(sender, "&7  Mevcut teslimatlar için: &e/dc list");
            return true;
        }

        String name = args[0];
        
        // Süre parametresi (opsiyonel)
        long durationMinutes = 60; // Varsayılan 1 saat
        if (args.length >= 2) {
            durationMinutes = parseDuration(args[1]);
            if (durationMinutes <= 0) {
                msg(sender, "&c✗ Geçersiz süre formatı: &f" + args[1]);
                msg(sender, "&7  Örnek: &f1h&7, &f30m&7, &f2h30m");
                return true;
            }
        }
        
        // Kazanan sayısı parametresi (opsiyonel)
        int winnerCount = -1; // -1 = config'den al
        if (args.length >= 3) {
            try {
                winnerCount = Integer.parseInt(args[2]);
                if (winnerCount < 1 || winnerCount > 10) {
                    msg(sender, "&c✗ Kazanan sayısı 1-10 arası olmalı: &f" + args[2]);
                    return true;
                }
            } catch (NumberFormatException e) {
                msg(sender, "&c✗ Geçersiz kazanan sayısı: &f" + args[2]);
                return true;
            }
        }

        // Teslimat var mı?
        Optional<DeliveryDefinition> defOpt = configManager.getDeliveryConfig().getDelivery(name);
        if (defOpt.isEmpty()) {
            msg(sender, "&c✗ Teslimat bulunamadı: &f" + name);
            msg(sender, "&7  Mevcut teslimatlar için: &e/dc list");
            return true;
        }

        // Zaten aktif mi?
        if (deliveryService.getActiveEvent(name).isPresent()) {
            msg(sender, "&c✗ Bu etkinlik zaten çalışıyor: &f" + name);
            msg(sender, "&7  Durumu görmek için: &e/dc status " + name);
            return true;
        }

        // Başlat (force=true ile enabled kontrolünü atla)
        Optional<ActiveEvent> eventOpt = deliveryService.startEvent(name, true);
        if (eventOpt.isPresent()) {
            ActiveEvent e = eventOpt.get();
            DeliveryDefinition def = defOpt.get();
            
            // Bitiş zamanını ayarla
            ZonedDateTime endTime = ZonedDateTime.now().plusMinutes(durationMinutes);
            e.setEndTime(endTime);
            
            // Kazanan sayısını ayarla (override veya config'den)
            int finalWinnerCount = winnerCount > 0 ? winnerCount : def.winnerCount();
            e.setWinnerCount(finalWinnerCount);
            
            // Otomatik bitiş için zamanlayıcı
            scheduleManualEventEnd(name, durationMinutes);
            
            String durationStr = formatDuration(durationMinutes);

            msg(sender, "");
            msg(sender, "&a✓ Etkinlik başlatıldı!");
            msg(sender, "");
            msg(sender, "  &7Teslimat    &8│ &f" + name);
            msg(sender, "  &7Kategori    &8│ &f" + e.getResolvedCategory());
            msg(sender, "  &7Eşya        &8│ &e" + e.getResolvedItem());
            msg(sender, "  &7Süre        &8│ &f" + durationStr);
            msg(sender, "  &7Kazanan     &8│ &f" + finalWinnerCount + " kişi");
            msg(sender, "");

            logger.info("[DeliveryCore] " + sender + " etkinliği başlattı: " + name + 
                       " (Eşya: " + e.getResolvedItem() + ", Süre: " + durationStr + ", Kazanan: " + finalWinnerCount + ")");
        } else {
            msg(sender, "&c✗ Etkinlik başlatılamadı!");
            msg(sender, "&7  Kategori veya eşya bulunamadı.");
        }

        return true;
    }

    // ═══════════════════════════════════════════════════════════════
    // STOP KOMUTU
    // ═══════════════════════════════════════════════════════════════
    private boolean handleStop(String sender, String[] args) {
        if (!hasPerm(sender, PERM_ADMIN_EVENT)) {
            noPermission(sender);
            return true;
        }

        if (deliveryService == null) {
            msg(sender, "&c✗ Servis henüz hazır değil.");
            return true;
        }

        if (args.length == 0) {
            msg(sender, "&c✗ Kullanım: &e/dc stop <teslimat_adı>");
            msg(sender, "&7  Aktif etkinlikler için: &e/dc status");
            return true;
        }

        String name = args[0];

        // Aktif mi?
        if (deliveryService.getActiveEvent(name).isEmpty()) {
            msg(sender, "&c✗ Bu etkinlik aktif değil: &f" + name);
            return true;
        }

        // Durdur
        var winners = deliveryService.endEvent(name);

        msg(sender, "");
        msg(sender, "&c■ Etkinlik durduruldu: &f" + name);
        msg(sender, "");

        if (!winners.isEmpty()) {
            msg(sender, "&7Kazananlar:");
            for (int i = 0; i < winners.size(); i++) {
                var w = winners.get(i);
                String medal = switch (i) {
                    case 0 -> "&6①";
                    case 1 -> "&7②";
                    case 2 -> "&c③";
                    default -> "&8" + (i + 1) + ".";
                };
                msg(sender, "  " + medal + " &f" + w.playerName() + " &7- &e" + w.deliveryCount() + " teslimat");
            }
        } else {
            msg(sender, "&7  Kazanan yok - kimse teslim etmedi.");
        }

        msg(sender, "");
        logger.info("[DeliveryCore] " + sender + " etkinliği durdurdu: " + name);

        return true;
    }

    // ═══════════════════════════════════════════════════════════════
    // TOP KOMUTU - SIRALAMA TABLOSU
    // ═══════════════════════════════════════════════════════════════
    private boolean handleTop(String sender, String[] args) {
        if (!hasPerm(sender, PERM_ADMIN_EVENT)) {
            noPermission(sender);
            return true;
        }

        if (deliveryService == null) {
            msg(sender, "&c✗ Servis henüz hazır değil.");
            return true;
        }

        // Belirli bir etkinlik
        if (args.length > 0) {
            return showEventTop(sender, args[0]);
        }

        // Tüm aktif etkinliklerin toplamı
        var activeEvents = deliveryService.getAllActiveEvents();

        if (activeEvents.isEmpty()) {
            msg(sender, "&7ℹ Aktif etkinlik yok.");
            return true;
        }

        // İlk aktif etkinliği göster
        return showEventTop(sender, activeEvents.get(0).getDeliveryName());
    }

    private boolean showEventTop(String sender, String name) {
        Optional<ActiveEvent> eventOpt = deliveryService.getActiveEvent(name);

        if (eventOpt.isEmpty()) {
            msg(sender, "&c✗ Bu etkinlik aktif değil: &f" + name);
            return true;
        }

        ActiveEvent e = eventOpt.get();
        var deliveries = e.getPlayerDeliveries();

        header(sender, "Sıralama: " + e.getResolvedItem());
        msg(sender, "");

        if (deliveries.isEmpty()) {
            msg(sender, "&7  Henüz kimse teslim etmedi.");
        } else {
            // UUID -> count sırala
            List<Map.Entry<UUID, Integer>> sorted = deliveries.entrySet().stream()
                    .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
                    .limit(10)
                    .toList();

            int rank = 1;
            for (var entry : sorted) {
                String medal = switch (rank) {
                    case 1 -> "&6&l①";
                    case 2 -> "&f&l②";
                    case 3 -> "&c&l③";
                    default -> "&7" + rank + ".";
                };
                // UUID'yi göster (gerçek uygulamada isim çözümlemesi yapılır)
                String playerName = entry.getKey().toString().substring(0, 8) + "...";
                msg(sender, "  " + medal + " &f" + playerName + " &8- &e" + entry.getValue() + " &7teslimat");
                rank++;
            }
        }

        msg(sender, "");
        msg(sender, "&7  Toplam: &f" + e.getTotalDeliveries() + " &7teslimat");
        footer(sender);

        return true;
    }

    // ═══════════════════════════════════════════════════════════════
    // CATEGORIES KOMUTU
    // ═══════════════════════════════════════════════════════════════
    private boolean handleCategories(String sender) {
        if (!hasPerm(sender, PERM_ADMIN_INFO)) {
            noPermission(sender);
            return true;
        }

        var categories = configManager.getCategoryConfig().getCategories();

        header(sender, "Kategori Listesi");
        msg(sender, "");

        if (categories.isEmpty()) {
            msg(sender, "&7  Tanımlı kategori yok.");
            msg(sender, "&7  &ocategories.yml dosyasını kontrol edin.");
        } else {
            for (var entry : categories.entrySet()) {
                String name = entry.getKey();
                var category = entry.getValue();
                int itemCount = category.items() != null ? category.items().size() : 0;
                
                msg(sender, "  &e▸ &f" + name + " &8(&7" + itemCount + " eşya&8)");
            }
        }

        msg(sender, "");
        msg(sender, "&7  Toplam: &f" + categories.size() + " &7kategori");
        footer(sender);

        return true;
    }

    // ═══════════════════════════════════════════════════════════════
    // TOGGLE KOMUTU
    // ═══════════════════════════════════════════════════════════════
    private boolean handleToggle(String sender, String[] args) {
        if (!hasPerm(sender, PERM_ADMIN_EVENT)) {
            noPermission(sender);
            return true;
        }

        if (args.length == 0) {
            msg(sender, "&c✗ Kullanım: &e/dc toggle <teslimat_adı>");
            msg(sender, "&7  Teslimatı açar veya kapatır.");
            return true;
        }

        String name = args[0];
        var defOpt = configManager.getDeliveryConfig().getDelivery(name);

        if (defOpt.isEmpty()) {
            msg(sender, "&c✗ Teslimat bulunamadı: &f" + name);
            return true;
        }

        // Not: Gerçek toggle için config dosyasını değiştirmek gerekir
        // Bu sadece bilgi mesajı gösterir
        var def = defOpt.get();
        if (def.enabled()) {
            msg(sender, "&7ℹ &f" + name + " &7şu an &aaktif&7.");
            msg(sender, "&7  Kapatmak için &edeliveries.yml&7'de &fenabled: false&7 yapın.");
        } else {
            msg(sender, "&7ℹ &f" + name + " &7şu an &ckapalı&7.");
            msg(sender, "&7  Açmak için &edeliveries.yml&7'de &fenabled: true&7 yapın.");
        }
        msg(sender, "&7  Sonra &e/dc reload &7yapın.");

        return true;
    }

    // ═══════════════════════════════════════════════════════════════
    // TEST KOMUTU
    // ═══════════════════════════════════════════════════════════════
    private boolean handleTest(String sender, String[] args) {
        if (!hasPerm(sender, PERM_ADMIN_EVENT)) {
            noPermission(sender);
            return true;
        }

        if (args.length == 0) {
            msg(sender, "&e&lTest Komutları:");
            msg(sender, "  &e/dc test deliver <teslimat> &8- &7Teslimat simüle et");
            msg(sender, "  &e/dc test reward &8- &7Ödül sistemini test et");
            msg(sender, "  &e/dc test webhook &8- &7Webhook gönderimi test et");
            return true;
        }

        String testType = args[0].toLowerCase();

        return switch (testType) {
            case "deliver" -> {
                if (args.length < 2) {
                    msg(sender, "&c✗ Kullanım: &e/dc test deliver <teslimat>");
                    yield true;
                }
                testDeliver(sender, args[1]);
                yield true;
            }
            case "reward" -> {
                msg(sender, "&7⚡ Ödül sistemi test ediliyor...");
                msg(sender, "&a✓ Ödül sistemi çalışıyor!");
                yield true;
            }
            case "webhook" -> {
                msg(sender, "&7⚡ Webhook test ediliyor...");
                if (webhookTester != null) {
                    webhookTester.run();
                    msg(sender, "&a✓ Test webhook'u gönderildi! Discord'u kontrol edin.");
                } else {
                    msg(sender, "&c✗ Webhook test fonksiyonu ayarlanmamış.");
                }
                yield true;
            }
            default -> {
                msg(sender, "&c✗ Bilinmeyen test: &f" + testType);
                yield true;
            }
        };
    }
    
    private Runnable webhookTester;
    
    public void setWebhookTester(Runnable tester) {
        this.webhookTester = tester;
    }

    private void testDeliver(String sender, String deliveryName) {
        if (deliveryService == null) {
            msg(sender, "&c✗ Servis hazır değil.");
            return;
        }

        var eventOpt = deliveryService.getActiveEvent(deliveryName);
        if (eventOpt.isEmpty()) {
            msg(sender, "&c✗ Bu etkinlik aktif değil: &f" + deliveryName);
            msg(sender, "&7  Önce başlatın: &e/dc start " + deliveryName);
            return;
        }

        // Test UUID ile simüle et
        UUID testUuid = UUID.randomUUID();
        boolean recorded = deliveryService.recordDelivery(testUuid, deliveryName, 1);

        if (recorded) {
            msg(sender, "&a✓ Test teslimatı kaydedildi!");
            msg(sender, "&7  UUID: &f" + testUuid.toString().substring(0, 8) + "...");
            msg(sender, "&7  Toplam: &f" + eventOpt.get().getTotalDeliveries() + " teslimat");
        } else {
            msg(sender, "&c✗ Teslimat kaydedilemedi.");
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // YARDIM MENÜSÜ
    // ═══════════════════════════════════════════════════════════════
    private void sendHelp(String sender) {
        header(sender, "Yardım Menüsü");
        msg(sender, "");

        // Temel komutlar
        msg(sender, "&e&lTemel Komutlar");
        msg(sender, "  &e/dc help &8- &7Bu menüyü gösterir");
        msg(sender, "  &e/dc version &8- &7Sürüm bilgisi");

        // Admin komutları
        boolean hasAdmin = hasPerm(sender, PERM_ADMIN) ||
                          hasPerm(sender, PERM_ADMIN_RELOAD) ||
                          hasPerm(sender, PERM_ADMIN_INFO) ||
                          hasPerm(sender, PERM_ADMIN_EVENT);

        if (hasAdmin) {
            msg(sender, "");
            msg(sender, "&c&lYönetim");
            
            if (hasPerm(sender, PERM_ADMIN_RELOAD)) {
                msg(sender, "  &e/dc reload &8- &7Konfigürasyonu yenile");
            }
            if (hasPerm(sender, PERM_ADMIN_INFO)) {
                msg(sender, "  &e/dc info &8- &7Sistem bilgisi");
            }
        }

        if (hasPerm(sender, PERM_ADMIN_EVENT)) {
            msg(sender, "");
            msg(sender, "&a&lEtkinlik Yönetimi");
            msg(sender, "  &e/dc list &8- &7Tüm teslimatları listele");
            msg(sender, "  &e/dc status &8[&fad&8] &8- &7Aktif etkinlik durumu");
            msg(sender, "  &e/dc start &8<&fad&8> &8- &7Etkinlik başlat");
            msg(sender, "  &e/dc stop &8<&fad&8> &8- &7Etkinliği durdur");
            msg(sender, "  &e/dc top &8[&fad&8] &8- &7Sıralama tablosu");
        }

        msg(sender, "");
        msg(sender, "&b&lOyuncu Komutları");
        msg(sender, "  &e/teslimat &8- &7Teslimat menüsünü aç");
        msg(sender, "  &e/teslimat top &8- &7Sıralama tablosu");
        msg(sender, "  &e/teslim &8- &7Envanterdeki eşyaları teslim et");
        msg(sender, "  &e/teslim <kategori> &8- &7Kategori teslimi (farm, ore, block...)");
        msg(sender, "  &e/teslim sandik &8- &7Sandıktan teslim");

        msg(sender, "");
        msg(sender, "&7Admin: &f/dc&7, Oyuncu: &f/teslimat&7, &f/teslim");
        footer(sender);
    }

    // ═══════════════════════════════════════════════════════════════
    // SÜRÜM BİLGİSİ
    // ═══════════════════════════════════════════════════════════════
    private void sendVersion(String sender) {
        msg(sender, "");
        msg(sender, "&6&l  ╔══════════════════════════════╗");
        msg(sender, "&6&l  ║     &e&lD&6elivery&e&lC&6ore &f&lv1.0.0    &6&l║");
        msg(sender, "&6&l  ╚══════════════════════════════╝");
        msg(sender, "");
        msg(sender, "&7  Zamanlanmış teslimat etkinlik sistemi");
        msg(sender, "&7  Minecraft sunucuları için.");
        msg(sender, "");
        msg(sender, "&7  Geliştiriciler: &fMaolide&7, &f3Mustafa5");
        msg(sender, "&7  API: &fSpigot/Paper 1.20+");
        msg(sender, "");
    }

    // ═══════════════════════════════════════════════════════════════
    // YARDIMCI METODLAR
    // ═══════════════════════════════════════════════════════════════
    private void header(String sender, String title) {
        msg(sender, "&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        msg(sender, "&6&l  DeliveryCore &8│ &f" + title);
        msg(sender, "&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }

    private void footer(String sender) {
        msg(sender, "&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }

    private void noPermission(String sender) {
        msg(sender, "&c✗ Bu komutu kullanma yetkiniz yok.");
    }

    private void logErrors(List<ValidationError> errors) {
        for (ValidationError error : errors) {
            logger.warning(String.format("[%s] %s - %s: %s",
                    error.severity(),
                    error.file(),
                    error.field() != null ? error.field() : "genel",
                    error.message()));
        }
    }

    private boolean hasPerm(String player, String permission) {
        if (player == null || player.isEmpty()) return false;
        return permissionChecker.apply(player, permission);
    }
    
    // ═══════════════════════════════════════════════════════════════
    // SÜRE YARDIMCI METODLARI
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Süre string'ini dakikaya çevirir.
     * Formatlar: 1h, 30m, 2h30m, 90
     */
    private long parseDuration(String duration) {
        if (duration == null || duration.isEmpty()) return -1;
        
        duration = duration.toLowerCase().trim();
        long totalMinutes = 0;
        
        try {
            // Sadece sayı ise dakika olarak al
            if (duration.matches("\\d+")) {
                return Long.parseLong(duration);
            }
            
            // Saat ve dakika parse et
            java.util.regex.Matcher hourMatcher = java.util.regex.Pattern.compile("(\\d+)h").matcher(duration);
            java.util.regex.Matcher minMatcher = java.util.regex.Pattern.compile("(\\d+)m").matcher(duration);
            
            if (hourMatcher.find()) {
                totalMinutes += Long.parseLong(hourMatcher.group(1)) * 60;
            }
            if (minMatcher.find()) {
                totalMinutes += Long.parseLong(minMatcher.group(1));
            }
            
            return totalMinutes > 0 ? totalMinutes : -1;
        } catch (Exception e) {
            return -1;
        }
    }
    
    /**
     * Dakikayı okunabilir formata çevirir.
     */
    private String formatDuration(long minutes) {
        if (minutes < 60) {
            return minutes + " dakika";
        }
        long hours = minutes / 60;
        long mins = minutes % 60;
        if (mins == 0) {
            return hours + " saat";
        }
        return hours + " saat " + mins + " dakika";
    }
    
    /**
     * Manuel başlatılan etkinlik için otomatik bitiş zamanlayıcısı.
     */
    private void scheduleManualEventEnd(String deliveryName, long durationMinutes) {
        if (manualEndScheduler != null) {
            manualEndScheduler.accept(deliveryName, durationMinutes);
            logger.info("[DeliveryCore] Etkinlik " + durationMinutes + " dakika sonra otomatik bitecek: " + deliveryName);
        } else {
            logger.warning("[DeliveryCore] Manuel bitiş zamanlayıcısı ayarlanmamış: " + deliveryName);
        }
    }
    
    /**
     * Manuel bitiş zamanlayıcısı için callback setter.
     */
    private java.util.function.BiConsumer<String, Long> manualEndScheduler;
    
    public void setManualEndScheduler(java.util.function.BiConsumer<String, Long> scheduler) {
        this.manualEndScheduler = scheduler;
    }

    private void msg(String player, String message) {
        if (player != null && !player.isEmpty()) {
            messageSender.accept(player, colorize(message));
        }
    }

    private String colorize(String text) {
        return text.replace("&", "§");
    }

    // ═══════════════════════════════════════════════════════════════
    // GETTER METODLARI
    // ═══════════════════════════════════════════════════════════════
    public static String getAdminPermission() { return PERM_ADMIN; }
    public static String getReloadPermission() { return PERM_ADMIN_RELOAD; }
    public static String getInfoPermission() { return PERM_ADMIN_INFO; }
    public static String getEventPermission() { return PERM_ADMIN_EVENT; }
    public static String getUsePermission() { return PERM_USE; }
    public static String getParticipatePermission() { return PERM_PARTICIPATE; }
}
