package com.deliverycore.command;

import com.deliverycore.config.ConfigManager;
import com.deliverycore.config.ReloadResult;
import com.deliverycore.model.DeliveryDefinition;
import com.deliverycore.model.ValidationError;
import com.deliverycore.service.ActiveEvent;
import com.deliverycore.service.DeliveryService;
import com.deliverycore.util.LangManager;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.logging.Logger;

public class CommandHandler {

    public static final String PERM_USE = "deliverycore.use";
    public static final String PERM_ADMIN = "deliverycore.admin";
    public static final String PERM_ADMIN_RELOAD = "deliverycore.admin.reload";
    public static final String PERM_ADMIN_INFO = "deliverycore.admin.info";
    public static final String PERM_ADMIN_EVENT = "deliverycore.admin.event";
    public static final String PERM_PARTICIPATE = "deliverycore.participate";

    private final ConfigManager configManager;
    private final BiFunction<String, String, Boolean> permissionChecker;
    private final BiConsumer<String, String> messageSender;
    private final Logger logger;
    private DeliveryService deliveryService;
    private CustomItemCommand customItemCommand;
    private Runnable reloadCallback;
    private Runnable webhookTester;
    private java.util.function.BiConsumer<String, Long> manualEndScheduler;
    private LangManager lang;

    public CommandHandler(ConfigManager configManager, BiFunction<String, String, Boolean> permissionChecker,
            BiConsumer<String, String> messageSender, Logger logger) {
        this.configManager = configManager;
        this.permissionChecker = permissionChecker;
        this.messageSender = messageSender;
        this.logger = logger;
    }

    public void setLangManager(LangManager lang) { this.lang = lang; }
    public void setDeliveryService(DeliveryService ds) { this.deliveryService = ds; }
    public void setCustomItemCommand(CustomItemCommand cmd) { this.customItemCommand = cmd; }
    public void setReloadCallback(Runnable cb) { this.reloadCallback = cb; }
    public void setWebhookTester(Runnable tester) { this.webhookTester = tester; }
    public void setManualEndScheduler(java.util.function.BiConsumer<String, Long> scheduler) { this.manualEndScheduler = scheduler; }

    public boolean handleCommand(String sender, String[] args) {
        if (args == null || args.length == 0) {
            msg(sender, ""); msg(sender, lang.get("command.main.title")); msg(sender, "");
            msg(sender, lang.get("command.main.help-hint")); msg(sender, lang.get("command.main.menu-hint")); msg(sender, "");
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
            case "additem", "itemeekle" -> handleAddItem(sender, subArgs);
            case "removeitem", "itemsil" -> handleRemoveItem(sender, subArgs);
            case "listcustom", "customliste" -> handleListCustom(sender);
            default -> { msg(sender, lang.get("command.main.unknown", "{cmd}", sub)); msg(sender, lang.get("command.main.unknown-hint")); yield true; }
        };
    }

    public List<String> handleTabComplete(String sender, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>(Arrays.asList("help", "version"));
            if (hasPerm(sender, PERM_ADMIN_RELOAD)) completions.add("reload");
            if (hasPerm(sender, PERM_ADMIN_INFO)) { completions.add("info"); completions.add("categories"); }
            if (hasPerm(sender, PERM_ADMIN_EVENT)) completions.addAll(Arrays.asList("list", "status", "start", "stop", "top", "toggle", "test"));
            return filter(completions, args[0]);
        }
        if (args.length == 2 && hasPerm(sender, PERM_ADMIN_EVENT)) {
            String sub = args[0].toLowerCase();
            if (List.of("stop", "status", "top", "toggle", "start").contains(sub)) return filter(getDeliveryNames(), args[1]);
            if (sub.equals("test")) return filter(Arrays.asList("deliver", "reward", "webhook"), args[1]);
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("start") && hasPerm(sender, PERM_ADMIN_EVENT)) return filter(Arrays.asList("30m", "1h", "2h", "3h"), args[2]);
        if (args.length == 4 && args[0].equalsIgnoreCase("start") && hasPerm(sender, PERM_ADMIN_EVENT)) return filter(Arrays.asList("1", "2", "3", "5", "10"), args[3]);
        return List.of();
    }

    private List<String> filter(List<String> list, String prefix) { return list.stream().filter(s -> s.toLowerCase().startsWith(prefix.toLowerCase())).toList(); }
    private List<String> getDeliveryNames() { return new ArrayList<>(configManager.getDeliveryConfig().getDeliveries().keySet()); }

    private boolean handleReload(String sender) {
        if (!hasPerm(sender, PERM_ADMIN_RELOAD)) { noPermission(sender); return true; }
        msg(sender, lang.get("command.reload.loading"));
        ReloadResult result = configManager.reloadWithResult();
        if (result.isSuccess()) {
            if (reloadCallback != null) try { reloadCallback.run(); } catch (Exception e) { logger.warning("Reload error: " + e.getMessage()); }
            if (lang != null) lang.loadLanguages();
            msg(sender, result.hasErrors() ? lang.get("command.reload.warning", "{count}", String.valueOf(result.errors().size())) : lang.get("command.reload.success"));
        } else { msg(sender, lang.get("command.reload.failed")); }
        return true;
    }

    private boolean handleInfo(String sender) {
        if (!hasPerm(sender, PERM_ADMIN_INFO)) { noPermission(sender); return true; }
        int categories = configManager.getCategoryConfig().getCategories().size();
        int deliveries = configManager.getDeliveryConfig().getDeliveries().size();
        int enabled = configManager.getDeliveryConfig().getEnabledDeliveries().size();
        int active = deliveryService != null ? deliveryService.getAllActiveEvents().size() : 0;
        header(sender, lang.getRaw("command.info.title"));
        msg(sender, ""); msg(sender, lang.get("command.info.categories", "{count}", String.valueOf(categories)));
        msg(sender, lang.get("command.info.deliveries", "{total}", String.valueOf(deliveries), "{enabled}", String.valueOf(enabled)));
        msg(sender, lang.get("command.info.running", "{count}", String.valueOf(active)));
        msg(sender, lang.get("command.info.language")); msg(sender, ""); footer(sender);
        return true;
    }

    private boolean handleList(String sender) {
        if (!hasPerm(sender, PERM_ADMIN_EVENT)) { noPermission(sender); return true; }
        var deliveries = configManager.getDeliveryConfig().getDeliveries();
        header(sender, lang.getRaw("command.list.title")); msg(sender, "");
        if (deliveries.isEmpty()) { msg(sender, lang.get("command.list.empty")); msg(sender, lang.get("command.list.empty-hint")); }
        else {
            for (var entry : deliveries.entrySet()) {
                String name = entry.getKey(); DeliveryDefinition def = entry.getValue();
                boolean running = deliveryService != null && deliveryService.getActiveEvent(name).isPresent();
                msg(sender, "  " + (running ? lang.get("command.list.running", "{name}", name) : (def.enabled() ? lang.get("command.list.ready", "{name}", name) : lang.get("command.list.disabled", "{name}", name))));
                msg(sender, "     " + lang.get("command.list.category", "{value}", def.category().value() != null ? def.category().value() : "random", "{mode}", def.category().mode().name().toLowerCase()));
                msg(sender, "     " + lang.get("command.list.winners", "{count}", String.valueOf(def.winnerCount())));
            }
        }
        msg(sender, ""); msg(sender, lang.get("command.list.total", "{count}", String.valueOf(deliveries.size()))); footer(sender);
        return true;
    }

    private boolean handleStatus(String sender, String[] args) {
        if (!hasPerm(sender, PERM_ADMIN_EVENT)) { noPermission(sender); return true; }
        if (deliveryService == null) { msg(sender, lang.get("general.service-not-ready")); return true; }
        if (args.length > 0) return showDeliveryStatus(sender, args[0]);
        var activeEvents = deliveryService.getAllActiveEvents();
        header(sender, "Active Events"); msg(sender, "");
        if (activeEvents.isEmpty()) { msg(sender, lang.get("command.status.no-active")); msg(sender, lang.get("command.status.no-active-hint")); }
        else {
            for (ActiveEvent e : activeEvents) {
                msg(sender, "  &a▶ &f" + e.getDeliveryName());
                msg(sender, "     " + lang.get("command.status.item", "{item}", e.getResolvedItem()));
                msg(sender, "     " + lang.get("command.status.category", "{category}", e.getResolvedCategory()));
                msg(sender, "     " + lang.get("command.status.participants", "{count}", String.valueOf(e.getPlayerDeliveries().size())));
                if (e.getEndTime() != null) msg(sender, "     " + lang.get("command.status.end-time", "{time}", e.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm"))));
                msg(sender, "");
            }
        }
        footer(sender); return true;
    }

    private boolean showDeliveryStatus(String sender, String name) {
        Optional<ActiveEvent> eventOpt = deliveryService.getActiveEvent(name);
        if (eventOpt.isEmpty()) {
            if (configManager.getDeliveryConfig().getDelivery(name).isEmpty()) { msg(sender, lang.get("command.status.not-found", "{name}", name)); return true; }
            msg(sender, lang.get("command.status.not-active", "{name}", name)); msg(sender, lang.get("command.status.start-hint", "{name}", name)); return true;
        }
        ActiveEvent e = eventOpt.get();
        header(sender, e.getDeliveryName()); msg(sender, "");
        msg(sender, "  " + lang.get("command.status.running"));
        msg(sender, "  " + lang.get("command.status.category", "{category}", e.getResolvedCategory()));
        msg(sender, "  " + lang.get("command.status.item", "{item}", e.getResolvedItem()));
        msg(sender, "  " + lang.get("command.status.participants", "{count}", String.valueOf(e.getPlayerDeliveries().size())));
        if (e.getEndTime() != null) msg(sender, "  " + lang.get("command.status.end-time", "{time}", e.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm"))));
        msg(sender, ""); footer(sender); return true;
    }

    private boolean handleStart(String sender, String[] args) {
        if (!hasPerm(sender, PERM_ADMIN_EVENT)) { noPermission(sender); return true; }
        if (deliveryService == null) { msg(sender, lang.get("general.service-not-ready")); return true; }
        if (args.length == 0) { msg(sender, lang.get("command.start.usage")); msg(sender, lang.get("command.start.duration-format")); msg(sender, lang.get("command.start.example")); return true; }
        String name = args[0];
        long durationMinutes = 60;
        if (args.length >= 2) { durationMinutes = parseDuration(args[1]); if (durationMinutes <= 0) { msg(sender, lang.get("command.start.invalid-duration", "{value}", args[1])); return true; } }
        int winnerCount = -1;
        if (args.length >= 3) { try { winnerCount = Integer.parseInt(args[2]); if (winnerCount < 1 || winnerCount > 10) { msg(sender, lang.get("command.start.invalid-winner", "{value}", args[2])); return true; } } catch (NumberFormatException e) { msg(sender, lang.get("command.start.invalid-winner", "{value}", args[2])); return true; } }
        Optional<DeliveryDefinition> defOpt = configManager.getDeliveryConfig().getDelivery(name);
        if (defOpt.isEmpty()) { msg(sender, lang.get("command.start.not-found", "{name}", name)); return true; }
        if (deliveryService.getActiveEvent(name).isPresent()) { msg(sender, lang.get("command.start.already-running", "{name}", name)); return true; }
        Optional<ActiveEvent> eventOpt = deliveryService.startEvent(name, true);
        if (eventOpt.isPresent()) {
            ActiveEvent e = eventOpt.get();
            e.setEndTime(ZonedDateTime.now().plusMinutes(durationMinutes));
            int finalWinnerCount = winnerCount > 0 ? winnerCount : defOpt.get().winnerCount();
            e.setWinnerCount(finalWinnerCount);
            if (manualEndScheduler != null) manualEndScheduler.accept(name, durationMinutes);
            msg(sender, ""); msg(sender, lang.get("command.start.success")); msg(sender, "");
            msg(sender, "  " + lang.get("command.start.info-delivery", "{name}", name));
            msg(sender, "  " + lang.get("command.start.info-category", "{category}", e.getResolvedCategory()));
            msg(sender, "  " + lang.get("command.start.info-item", "{item}", e.getResolvedItem()));
            msg(sender, "  " + lang.get("command.start.info-duration", "{duration}", formatDuration(durationMinutes)));
            msg(sender, "  " + lang.get("command.start.info-winners", "{count}", String.valueOf(finalWinnerCount)));
            msg(sender, "");
        } else { msg(sender, lang.get("command.start.failed")); }
        return true;
    }

    private boolean handleStop(String sender, String[] args) {
        if (!hasPerm(sender, PERM_ADMIN_EVENT)) { noPermission(sender); return true; }
        if (deliveryService == null) { msg(sender, lang.get("general.service-not-ready")); return true; }
        if (args.length == 0) { msg(sender, lang.get("command.stop.usage")); return true; }
        String name = args[0];
        if (deliveryService.getActiveEvent(name).isEmpty()) { msg(sender, lang.get("command.stop.not-active", "{name}", name)); return true; }
        var winners = deliveryService.endEvent(name);
        msg(sender, ""); msg(sender, lang.get("command.stop.success", "{name}", name)); msg(sender, "");
        if (!winners.isEmpty()) {
            msg(sender, lang.get("command.stop.winners-header"));
            for (int i = 0; i < winners.size(); i++) {
                var w = winners.get(i);
                msg(sender, lang.get("command.stop.winner-format", "{medal}", lang.getMedal(i + 1), "{player}", w.playerName(), "{count}", String.valueOf(w.deliveryCount())));
            }
        } else { msg(sender, lang.get("command.stop.no-winners")); }
        msg(sender, ""); return true;
    }

    private boolean handleTop(String sender, String[] args) {
        if (!hasPerm(sender, PERM_ADMIN_EVENT)) { noPermission(sender); return true; }
        if (deliveryService == null) { msg(sender, lang.get("general.service-not-ready")); return true; }
        var activeEvents = deliveryService.getAllActiveEvents();
        if (activeEvents.isEmpty()) { msg(sender, lang.get("command.top.no-active")); return true; }
        ActiveEvent e = args.length > 0 ? deliveryService.getActiveEvent(args[0]).orElse(activeEvents.get(0)) : activeEvents.get(0);
        var deliveries = e.getPlayerDeliveries();
        header(sender, lang.getRaw("command.top.title").replace("{item}", e.getResolvedItem())); msg(sender, "");
        if (deliveries.isEmpty()) { msg(sender, lang.get("command.top.empty")); }
        else {
            List<Map.Entry<UUID, Integer>> sorted = deliveries.entrySet().stream().sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed()).limit(10).toList();
            int rank = 1;
            for (var entry : sorted) {
                String playerName = resolvePlayerName(entry.getKey());
                msg(sender, lang.get("command.top.entry", "{medal}", lang.getMedal(rank), "{player}", playerName, "{count}", String.valueOf(entry.getValue())));
                rank++;
            }
        }
        msg(sender, ""); msg(sender, lang.get("command.top.total", "{count}", String.valueOf(e.getTotalDeliveries()))); footer(sender);
        return true;
    }

    private String resolvePlayerName(UUID uuid) {
        org.bukkit.entity.Player player = org.bukkit.Bukkit.getPlayer(uuid);
        if (player != null) return player.getName();
        org.bukkit.OfflinePlayer offline = org.bukkit.Bukkit.getOfflinePlayer(uuid);
        String name = offline.getName();
        return name != null ? name : uuid.toString().substring(0, 8);
    }

    private boolean handleCategories(String sender) {
        if (!hasPerm(sender, PERM_ADMIN_INFO)) { noPermission(sender); return true; }
        var categories = configManager.getCategoryConfig().getCategories();
        header(sender, lang.getRaw("command.categories.title")); msg(sender, "");
        if (categories.isEmpty()) { msg(sender, lang.get("command.categories.empty")); }
        else { for (var entry : categories.entrySet()) { msg(sender, "  " + lang.get("command.categories.entry", "{name}", entry.getKey(), "{count}", String.valueOf(entry.getValue().items() != null ? entry.getValue().items().size() : 0))); } }
        msg(sender, ""); msg(sender, lang.get("command.categories.total", "{count}", String.valueOf(categories.size()))); footer(sender);
        return true;
    }

    private boolean handleToggle(String sender, String[] args) {
        if (!hasPerm(sender, PERM_ADMIN_EVENT)) { noPermission(sender); return true; }
        if (args.length == 0) { msg(sender, lang.get("command.toggle.usage")); return true; }
        var defOpt = configManager.getDeliveryConfig().getDelivery(args[0]);
        if (defOpt.isEmpty()) { msg(sender, lang.get("command.toggle.not-found", "{name}", args[0])); return true; }
        msg(sender, defOpt.get().enabled() ? lang.get("command.toggle.enabled", "{name}", args[0]) : lang.get("command.toggle.disabled", "{name}", args[0]));
        msg(sender, lang.get("command.toggle.reload-hint"));
        return true;
    }

    private boolean handleTest(String sender, String[] args) {
        if (!hasPerm(sender, PERM_ADMIN_EVENT)) { noPermission(sender); return true; }
        if (args.length == 0) { msg(sender, lang.get("command.test.title")); msg(sender, "  " + lang.get("command.test.deliver")); msg(sender, "  " + lang.get("command.test.reward")); msg(sender, "  " + lang.get("command.test.webhook")); return true; }
        return switch (args[0].toLowerCase()) {
            case "deliver" -> { if (args.length < 2) { msg(sender, lang.get("command.test.deliver-usage")); yield true; } testDeliver(sender, args[1]); yield true; }
            case "reward" -> { msg(sender, lang.get("command.test.reward-testing")); msg(sender, lang.get("command.test.reward-success")); yield true; }
            case "webhook" -> { msg(sender, lang.get("command.test.webhook-testing")); if (webhookTester != null) { webhookTester.run(); msg(sender, lang.get("command.test.webhook-success")); } else { msg(sender, lang.get("command.test.webhook-not-set")); } yield true; }
            default -> { msg(sender, lang.get("command.test.unknown", "{type}", args[0])); yield true; }
        };
    }

    private void testDeliver(String sender, String deliveryName) {
        if (deliveryService == null) { msg(sender, lang.get("general.service-not-ready")); return; }
        var eventOpt = deliveryService.getActiveEvent(deliveryName);
        if (eventOpt.isEmpty()) { msg(sender, lang.get("command.test.deliver-not-active", "{name}", deliveryName)); return; }
        UUID testUuid = UUID.randomUUID();
        if (deliveryService.recordDelivery(testUuid, deliveryName, 1)) {
            msg(sender, lang.get("command.test.deliver-success"));
            msg(sender, lang.get("command.test.deliver-total", "{count}", String.valueOf(eventOpt.get().getTotalDeliveries())));
        } else { msg(sender, lang.get("command.test.deliver-failed")); }
    }

    private boolean handleAddItem(String sender, String[] args) {
        if (!hasPerm(sender, PERM_ADMIN_EVENT)) { noPermission(sender); return true; }
        if (customItemCommand == null) { msg(sender, lang.get("general.service-not-ready")); return true; }
        org.bukkit.entity.Player player = org.bukkit.Bukkit.getPlayer(sender);
        if (player == null) { msg(sender, lang.get("general.player-only")); return true; }
        return customItemCommand.handleAddItem(player, args);
    }

    private boolean handleRemoveItem(String sender, String[] args) {
        if (!hasPerm(sender, PERM_ADMIN_EVENT)) { noPermission(sender); return true; }
        if (customItemCommand == null) { msg(sender, lang.get("general.service-not-ready")); return true; }
        org.bukkit.entity.Player player = org.bukkit.Bukkit.getPlayer(sender);
        if (player == null) { msg(sender, lang.get("general.player-only")); return true; }
        return customItemCommand.handleRemoveItem(player, args);
    }

    private boolean handleListCustom(String sender) {
        if (!hasPerm(sender, PERM_ADMIN_INFO)) { noPermission(sender); return true; }
        if (customItemCommand == null) { msg(sender, lang.get("general.service-not-ready")); return true; }
        org.bukkit.entity.Player player = org.bukkit.Bukkit.getPlayer(sender);
        if (player == null) { msg(sender, lang.get("general.player-only")); return true; }
        return customItemCommand.handleListCustom(player, new String[0]);
    }

    private void sendHelp(String sender) {
        msg(sender, lang.get("command.help.header")); msg(sender, lang.get("command.help.title")); msg(sender, lang.get("command.help.header")); msg(sender, "");
        msg(sender, lang.get("command.help.basic-title")); msg(sender, "  " + lang.get("command.help.basic-help")); msg(sender, "  " + lang.get("command.help.basic-version"));
        if (hasPerm(sender, PERM_ADMIN_RELOAD) || hasPerm(sender, PERM_ADMIN_INFO)) {
            msg(sender, ""); msg(sender, lang.get("command.help.admin-title"));
            if (hasPerm(sender, PERM_ADMIN_RELOAD)) msg(sender, "  " + lang.get("command.help.admin-reload"));
            if (hasPerm(sender, PERM_ADMIN_INFO)) msg(sender, "  " + lang.get("command.help.admin-info"));
        }
        if (hasPerm(sender, PERM_ADMIN_EVENT)) {
            msg(sender, ""); msg(sender, lang.get("command.help.event-title"));
            msg(sender, "  " + lang.get("command.help.event-list")); msg(sender, "  " + lang.get("command.help.event-status"));
            msg(sender, "  " + lang.get("command.help.event-start")); msg(sender, "  " + lang.get("command.help.event-stop"));
            msg(sender, "  " + lang.get("command.help.event-top"));
        }
        msg(sender, ""); msg(sender, lang.get("command.help.player-title"));
        msg(sender, "  " + lang.get("command.help.player-menu")); msg(sender, "  " + lang.get("command.help.player-top"));
        msg(sender, "  " + lang.get("command.help.player-deliver"));
        msg(sender, ""); msg(sender, lang.get("command.help.footer")); msg(sender, lang.get("command.help.header"));
    }

    private void sendVersion(String sender) {
        msg(sender, ""); msg(sender, lang.get("command.version.line1")); msg(sender, lang.get("command.version.line2")); msg(sender, lang.get("command.version.line3"));
        msg(sender, ""); msg(sender, lang.get("command.version.desc1")); msg(sender, lang.get("command.version.desc2"));
        msg(sender, ""); msg(sender, lang.get("command.version.devs")); msg(sender, lang.get("command.version.api")); msg(sender, "");
    }

    private void header(String sender, String title) { msg(sender, "&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"); msg(sender, "&6&l  DeliveryCore &8│ &f" + title); msg(sender, "&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"); }
    private void footer(String sender) { msg(sender, "&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"); }
    private void noPermission(String sender) { msg(sender, lang.get("general.no-permission")); }
    private boolean hasPerm(String player, String permission) { return player != null && !player.isEmpty() && permissionChecker.apply(player, permission); }
    private void msg(String player, String message) { if (player != null && !player.isEmpty()) messageSender.accept(player, message.replace("&", "§")); }

    private long parseDuration(String duration) {
        if (duration == null || duration.isEmpty()) return -1;
        duration = duration.toLowerCase().trim();
        try {
            if (duration.matches("\\d+")) return Long.parseLong(duration);
            long total = 0;
            var hourMatcher = java.util.regex.Pattern.compile("(\\d+)h").matcher(duration);
            var minMatcher = java.util.regex.Pattern.compile("(\\d+)m").matcher(duration);
            if (hourMatcher.find()) total += Long.parseLong(hourMatcher.group(1)) * 60;
            if (minMatcher.find()) total += Long.parseLong(minMatcher.group(1));
            return total > 0 ? total : -1;
        } catch (Exception e) { return -1; }
    }

    private String formatDuration(long minutes) {
        if (minutes < 60) return minutes + (lang.getCurrentLanguage().equals("en") ? " min" : " dk");
        long hours = minutes / 60; long mins = minutes % 60;
        String h = lang.getCurrentLanguage().equals("en") ? "h" : "s";
        String m = lang.getCurrentLanguage().equals("en") ? "m" : "dk";
        return mins == 0 ? hours + h : hours + h + " " + mins + m;
    }

    public static String getAdminPermission() { return PERM_ADMIN; }
    public static String getUsePermission() { return PERM_USE; }
}
