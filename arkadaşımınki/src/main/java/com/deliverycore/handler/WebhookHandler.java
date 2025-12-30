package com.deliverycore.handler;

import com.deliverycore.model.Winner;
import com.deliverycore.service.ActiveEvent;
import com.deliverycore.service.DeliveryService;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class WebhookHandler {
    
    private final JavaPlugin plugin;
    private final Logger logger;
    private final File dataFolder;
    private final HttpClient httpClient;
    
    private boolean enabled;
    private String webhookUrl;
    private boolean mentionEveryone;
    
    private String startTitle, startDesc, startColor, startFooter;
    private String endTitle, endDesc, endColor, endFooter;
    private boolean warningEnabled;
    private int warningMinutes;
    private String warningTitle, warningDesc, warningColor, warningFooter;
    
    private final Map<String, BukkitTask> warningTasks = new ConcurrentHashMap<>();
    private final Set<String> warningSent = ConcurrentHashMap.newKeySet();
    
    public WebhookHandler(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.dataFolder = plugin.getDataFolder();
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build();
        loadConfig();
    }
    
    public void loadConfig() {
        try {
            File configFile = new File(dataFolder, "config.yml");
            if (configFile.exists()) {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
                enabled = config.getBoolean("webhook.enabled", false);
                webhookUrl = config.getString("webhook.url", "");
            }
            
            File discordFile = new File(dataFolder, "webhooks/discord.yml");
            if (!discordFile.exists()) {
                discordFile.getParentFile().mkdirs();
                plugin.saveResource("webhooks/discord.yml", false);
            }
            
            YamlConfiguration discord = YamlConfiguration.loadConfiguration(discordFile);
            
            mentionEveryone = discord.getBoolean("mention-everyone", false);
            
            startTitle = discord.getString("start.title", "📦 {delivery} Başladı!");
            startDesc = discord.getString("start.description", "**Eşya:** {item}\n**Kategori:** {category}");
            startColor = discord.getString("start.color", "#00FF00");
            startFooter = discord.getString("start.footer", "DeliveryCore");
            
            endTitle = discord.getString("end.title", "🏆 {delivery} Sona Erdi!");
            endDesc = discord.getString("end.description", "**Eşya:** {item}\n**Kategori:** {category}");
            endColor = discord.getString("end.color", "#FFD700");
            endFooter = discord.getString("end.footer", "DeliveryCore");
            
            warningEnabled = discord.getBoolean("warning.enabled", true);
            warningMinutes = discord.getInt("warning.minutes-before", 5);
            warningTitle = discord.getString("warning.title", "⏰ {delivery} Bitiyor!");
            warningDesc = discord.getString("warning.description", "Etkinlik **{minutes} dakika** içinde sona erecek!");
            warningColor = discord.getString("warning.color", "#FF6600");
            warningFooter = discord.getString("warning.footer", "Son şansınızı kaçırmayın!");
            
            logger.info("[WEBHOOK] Config yuklendi. Enabled: " + enabled + ", Warning: " + warningMinutes + "dk");
        } catch (Exception e) {
            logger.warning("[WEBHOOK] Config yuklenemedi: " + e.getMessage());
            setDefaults();
        }
    }
    
    private void setDefaults() {
        enabled = false;
        webhookUrl = "";
        mentionEveryone = false;
        startTitle = "📦 {delivery} Başladı!";
        startDesc = "**Eşya:** {item}";
        startColor = "#00FF00";
        startFooter = "DeliveryCore";
        endTitle = "🏆 {delivery} Sona Erdi!";
        endDesc = "**Eşya:** {item}";
        endColor = "#FFD700";
        endFooter = "DeliveryCore";
        warningEnabled = true;
        warningMinutes = 5;
        warningTitle = "⏰ {delivery} Bitiyor!";
        warningDesc = "Etkinlik bitiyor!";
        warningColor = "#FF6600";
        warningFooter = "Son şans!";
    }
    
    public void sendStartWebhook(String delivery, String item, String category) {
        if (!isValid()) return;
        String title = replace(startTitle, delivery, item, category);
        String desc = replace(startDesc, delivery, item, category);
        String footer = replace(startFooter, delivery, item, category);
        sendAsync(buildJson(title, desc, parseColor(startColor), footer, mentionEveryone), "START");
    }
    
    public void sendEndWebhook(String delivery, String item, String category, List<Winner> winners) {
        if (!isValid()) return;
        cancelWarningTask(delivery);
        warningSent.remove(delivery);
        String title = replace(endTitle, delivery, item, category);
        String desc = replace(endDesc, delivery, item, category) + buildWinners(winners);
        String footer = replace(endFooter, delivery, item, category);
        sendAsync(buildJson(title, desc, parseColor(endColor), footer, mentionEveryone), "END");
    }
    
    public void sendWarningWebhook(String delivery, String item, String category, int total, int participants) {
        if (!isValid() || !warningEnabled) return;
        if (warningSent.contains(delivery)) return;
        warningSent.add(delivery);
        
        String title = replace(warningTitle, delivery, item, category)
            .replace("{minutes}", String.valueOf(warningMinutes));
        String desc = replace(warningDesc, delivery, item, category)
            .replace("{minutes}", String.valueOf(warningMinutes))
            .replace("{total}", String.valueOf(total))
            .replace("{participants}", String.valueOf(participants));
        String footer = replace(warningFooter, delivery, item, category);
        sendAsync(buildJson(title, desc, parseColor(warningColor), footer, false), "WARNING");
    }
    
    public void scheduleWarning(ActiveEvent event, DeliveryService deliveryService, 
                                java.util.function.Function<String, String> itemNameResolver,
                                java.util.function.Function<String, String> categoryNameResolver) {
        if (!isValid() || !warningEnabled || event.getEndTime() == null) return;
        
        String deliveryName = event.getDeliveryName();
        cancelWarningTask(deliveryName);
        
        ZonedDateTime warningTime = event.getEndTime().minusMinutes(warningMinutes);
        long delayMs = Duration.between(ZonedDateTime.now(), warningTime).toMillis();
        
        if (delayMs <= 0) return;
        
        long delayTicks = delayMs / 50;
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            var activeEvent = deliveryService.getActiveEvent(deliveryName);
            if (activeEvent.isPresent()) {
                ActiveEvent e = activeEvent.get();
                String item = itemNameResolver.apply(e.getResolvedItem());
                String cat = categoryNameResolver.apply(e.getResolvedCategory());
                int total = e.getTotalDeliveries();
                int participants = e.getPlayerDeliveries().size();
                sendWarningWebhook(deliveryName, item, cat, total, participants);
            }
        }, delayTicks);
        
        warningTasks.put(deliveryName, task);
        logger.info("[WEBHOOK] Warning scheduled for " + deliveryName + " in " + (delayMs/1000/60) + " minutes");
    }
    
    private void cancelWarningTask(String deliveryName) {
        BukkitTask task = warningTasks.remove(deliveryName);
        if (task != null) task.cancel();
    }
    
    public void sendTestWebhook() {
        logger.info("[WEBHOOK TEST] Kontrol ediliyor...");
        if (webhookUrl == null || webhookUrl.isEmpty()) {
            logger.warning("[WEBHOOK TEST] URL bos! config.yml'de webhook.url ayarlayin.");
            return;
        }
        if (!webhookUrl.startsWith("https://discord.com/api/webhooks/")) {
            logger.warning("[WEBHOOK TEST] Gecersiz URL! Discord webhook URL'si olmali.");
            return;
        }
        String json = buildJson("🧪 DeliveryCore Test", "Webhook bağlantısı başarılı!\n\nSunucu: " + Bukkit.getServer().getName(), 65280, "Test", false);
        logger.info("[WEBHOOK TEST] Gonderiliyor...");
        sendAsync(json, "TEST");
    }
    
    private boolean isValid() {
        return enabled && webhookUrl != null && !webhookUrl.isEmpty() && webhookUrl.startsWith("https://discord.com/api/webhooks/");
    }
    
    private String replace(String text, String delivery, String item, String category) {
        if (text == null) return "";
        return text.replace("{delivery}", delivery != null ? delivery : "")
                   .replace("{item}", item != null ? item : "")
                   .replace("{category}", category != null ? category : "");
    }
    
    private String buildWinners(List<Winner> winners) {
        if (winners == null || winners.isEmpty()) return "\n\n*Kimse katılmadı*";
        StringBuilder sb = new StringBuilder("\n\n**🏆 Kazananlar:**");
        int rank = 1;
        for (Winner w : winners) {
            String medal = switch (rank) { case 1 -> "🥇"; case 2 -> "🥈"; case 3 -> "🥉"; default -> rank + "."; };
            sb.append("\n").append(medal).append(" **").append(w.playerName()).append("** - ").append(w.deliveryCount()).append(" teslimat");
            if (++rank > 5) break;
        }
        return sb.toString();
    }
    
    private int parseColor(String hex) {
        if (hex == null || hex.isEmpty()) return 0;
        try { return Integer.parseInt(hex.startsWith("#") ? hex.substring(1) : hex, 16); } 
        catch (Exception e) { return 0; }
    }
    
    private String buildJson(String title, String desc, int color, String footer, boolean mention) {
        StringBuilder json = new StringBuilder("{");
        if (mention) json.append("\"content\":\"@everyone\",");
        json.append("\"embeds\":[{");
        json.append("\"title\":").append(escapeJson(title)).append(",");
        json.append("\"description\":").append(escapeJson(desc)).append(",");
        json.append("\"color\":").append(color);
        if (footer != null && !footer.isEmpty()) json.append(",\"footer\":{\"text\":").append(escapeJson(footer)).append("}");
        json.append("}]}");
        return json.toString();
    }
    
    private String escapeJson(String value) {
        if (value == null) return "\"\"";
        StringBuilder sb = new StringBuilder("\"");
        for (char c : value.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> { if (c < ' ') sb.append(String.format("\\u%04x", (int) c)); else sb.append(c); }
            }
        }
        return sb.append("\"").toString();
    }
    
    private void sendAsync(String json, String tag) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> sendRequest(json, tag));
    }
    
    private void sendRequest(String json, String tag) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(webhookUrl))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/json; charset=UTF-8")
                .header("User-Agent", "DeliveryCore/1.1")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int code = response.statusCode();
            
            if (code >= 200 && code < 300) {
                logger.info("[WEBHOOK] Gonderildi (" + tag + ")");
            } else {
                logger.warning("[WEBHOOK] Hata " + code + ": " + response.body());
            }
        } catch (Exception e) {
            logger.warning("[WEBHOOK] Hata: " + e.getMessage());
        }
    }
    
    public boolean isEnabled() { return enabled; }
    public boolean isWarningEnabled() { return warningEnabled; }
    public int getWarningMinutes() { return warningMinutes; }
}
