package com.deliverycore.service;

import com.deliverycore.model.HologramInfo;
import com.deliverycore.util.Result;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class HologramServiceImpl implements HologramService {
    
    private static final Logger LOGGER = Logger.getLogger(HologramServiceImpl.class.getName());
    private final Map<String, HologramInfo> holograms = new ConcurrentHashMap<>();
    private final Map<String, Object> nativeHolograms = new ConcurrentHashMap<>();
    
    private Plugin plugin;
    private File hologramsFile;
    private boolean decentHologramsAvailable = false;
    
    // Cached reflection objects
    private Class<?> dhapiClass;
    private Method createHologramMethod;
    private Method getHologramMethod;
    private Method setHologramLinesMethod;
    private Method removeHologramMethod;
    
    public HologramServiceImpl() { }
    
    public HologramServiceImpl(Plugin plugin) {
        this.plugin = plugin;
        this.hologramsFile = new File(plugin.getDataFolder(), "holograms.yml");
        checkPlugins();
    }
    
    public void setPlugin(Plugin plugin) {
        this.plugin = plugin;
        this.hologramsFile = new File(plugin.getDataFolder(), "holograms.yml");
    }
    
    private void checkPlugins() {
        try {
            Plugin dhPlugin = Bukkit.getPluginManager().getPlugin("DecentHolograms");
            if (dhPlugin != null && dhPlugin.isEnabled()) {
                // Cache reflection methods
                dhapiClass = Class.forName("eu.decentsoftware.holograms.api.DHAPI");
                Class<?> hologramClass = Class.forName("eu.decentsoftware.holograms.api.holograms.Hologram");
                
                createHologramMethod = dhapiClass.getMethod("createHologram", String.class, Location.class, boolean.class, List.class);
                getHologramMethod = dhapiClass.getMethod("getHologram", String.class);
                setHologramLinesMethod = dhapiClass.getMethod("setHologramLines", hologramClass, List.class);
                removeHologramMethod = dhapiClass.getMethod("removeHologram", String.class);
                
                decentHologramsAvailable = true;
                LOGGER.info("[Hologram] DecentHolograms tespit edildi ve aktif!");
            }
        } catch (Exception e) {
            decentHologramsAvailable = false;
            LOGGER.warning("[Hologram] DecentHolograms API yuklenemedi: " + e.getMessage());
        }
        
        if (!decentHologramsAvailable) {
            LOGGER.info("[Hologram] Hologram eklentisi bulunamadi. Hologram ozelligi devre disi.");
        }
    }
    
    @Override 
    public boolean isEnabled() { 
        return decentHologramsAvailable; 
    }
    
    @Override 
    public String getHologramPluginName() {
        return decentHologramsAvailable ? "DecentHolograms" : "None";
    }

    @Override
    public Result<Void> createHologram(String id, Location location, String deliveryName) {
        List<String> lines = formatLeaderboard(deliveryName, "Bekleniyor...", List.of());
        Result<Void> result = createHologram(id, location, lines);
        if (result.isSuccess()) {
            HologramInfo info = holograms.get(id);
            if (info != null) {
                holograms.put(id, new HologramInfo(info.id(), info.location(), info.lines(), false, deliveryName));
            }
        }
        return result;
    }
    
    @Override
    public Result<Void> createHologram(String id, Location location, List<String> lines) {
        if (!isEnabled()) {
            return Result.failure("Hologram eklentisi yuklu degil! DecentHolograms gerekli.");
        }
        if (id == null || id.trim().isEmpty()) return Result.failure("Hologram ID bos olamaz!");
        if (location == null || location.getWorld() == null) return Result.failure("Gecersiz konum!");
        if (holograms.containsKey(id)) return Result.failure("'" + id + "' ID'li hologram zaten mevcut!");
        
        try {
            Object holo = createDecentHologram(id, location, lines);
            if (holo != null) {
                nativeHolograms.put(id, holo);
                holograms.put(id, HologramInfo.inactive(id, location, new ArrayList<>(lines)));
                saveHolograms();
                LOGGER.info("[Hologram] Olusturuldu: " + id + " @ " + location.getWorld().getName());
                return Result.success(null);
            }
            return Result.failure("Hologram olusturulamadi - API hatasi");
        } catch (Exception e) { 
            LOGGER.warning("[Hologram] Olusturma hatasi: " + e.getMessage());
            e.printStackTrace();
            return Result.failure("Hata: " + e.getMessage()); 
        }
    }
    
    @Override
    public Result<Void> updateHologram(String id, List<String> lines) {
        if (!isEnabled()) return Result.failure("Plugin yuklu degil");
        HologramInfo info = holograms.get(id);
        if (info == null) return Result.failure("Bulunamadi: " + id);
        
        try {
            // Get hologram from DecentHolograms (may have been recreated)
            Object holo = getDecentHologram(id);
            if (holo != null) {
                updateDecentHologram(holo, lines);
                holograms.put(id, new HologramInfo(info.id(), info.location(), new ArrayList<>(lines), info.active(), info.deliveryName()));
                return Result.success(null);
            }
            return Result.failure("Hologram objesi yok");
        } catch (Exception e) { 
            LOGGER.warning("[Hologram] Guncelleme hatasi: " + e.getMessage());
            return Result.failure(e.getMessage()); 
        }
    }
    
    @Override
    public Result<Void> updateHologramLeaderboard(String id, String deliveryName, List<PlayerLeaderboardEntry> data) {
        HologramInfo info = holograms.get(id);
        if (info == null) return Result.failure("Bulunamadi: " + id);
        List<String> lines = formatLeaderboard(deliveryName, null, data);
        Result<Void> result = updateHologram(id, lines);
        if (result.isSuccess()) {
            holograms.put(id, new HologramInfo(info.id(), info.location(), lines, !data.isEmpty(), deliveryName));
        }
        return result;
    }
    
    @Override
    public Result<Void> deleteHologram(String id) {
        if (!isEnabled()) return Result.failure("Plugin yuklu degil");
        if (!holograms.containsKey(id)) return Result.failure("Bulunamadi: " + id);
        
        try {
            deleteDecentHologram(id);
            nativeHolograms.remove(id);
            holograms.remove(id);
            saveHolograms();
            LOGGER.info("[Hologram] Silindi: " + id);
            return Result.success(null);
        } catch (Exception e) { 
            LOGGER.warning("[Hologram] Silme hatasi: " + e.getMessage());
            return Result.failure(e.getMessage()); 
        }
    }
    
    @Override 
    public List<HologramInfo> listHolograms() { 
        return new ArrayList<>(holograms.values()); 
    }
    
    @Override
    public List<HologramInfo> getHologramsForDelivery(String deliveryName) {
        return holograms.values().stream()
            .filter(h -> deliveryName.equals(h.deliveryName()))
            .collect(Collectors.toList());
    }
    
    @Override 
    public boolean hologramExists(String id) { 
        return holograms.containsKey(id); 
    }

    @Override
    public void updateDeliveryHolograms(String deliveryName, List<PlayerLeaderboardEntry> data) {
        if (!isEnabled() || holograms.isEmpty()) return;
        List<String> lines = formatLeaderboard(deliveryName, null, data);
        for (HologramInfo info : getHologramsForDelivery(deliveryName)) {
            updateHologram(info.id(), lines);
        }
    }
    
    @Override
    public void updateAllHolograms(String deliveryName, List<String> data) {
        if (!isEnabled() || holograms.isEmpty()) return;
        for (String id : holograms.keySet()) {
            updateHologram(id, data);
            HologramInfo info = holograms.get(id);
            if (info != null) {
                holograms.put(id, new HologramInfo(info.id(), info.location(), data, deliveryName != null, deliveryName));
            }
        }
    }
    
    @Override
    public List<String> formatLeaderboard(String deliveryName, String itemName, List<PlayerLeaderboardEntry> playerData) {
        List<String> lines = new ArrayList<>();
        
        // Baslik - DecentHolograms renk kodlari
        lines.add("&6&l✦ TESLIMAT SIRALAMASI ✦");
        lines.add("");
        
        if (deliveryName != null && !deliveryName.isEmpty()) {
            lines.add("&e&l" + deliveryName.toUpperCase());
            if (itemName != null && !itemName.isEmpty()) {
                lines.add("&7Istenen: &f" + itemName);
            }
            lines.add("&8═══════════════════");
            lines.add("");
            
            if (playerData == null || playerData.isEmpty()) {
                lines.add("&7Henuz teslimat yok");
                lines.add("&8Ilk sen teslim et!");
            } else {
                int count = Math.min(10, playerData.size());
                for (int i = 0; i < count; i++) {
                    PlayerLeaderboardEntry e = playerData.get(i);
                    String medal = switch (e.rank()) { 
                        case 1 -> "&6&l1."; 
                        case 2 -> "&f&l2."; 
                        case 3 -> "&c&l3."; 
                        default -> "&7" + e.rank() + "."; 
                    };
                    String color = switch (e.rank()) {
                        case 1 -> "&6";
                        case 2 -> "&f";
                        case 3 -> "&c";
                        default -> "&7";
                    };
                    lines.add(medal + " " + color + e.playerName() + " &8- &e" + e.deliveryCount());
                }
                
                int totalDeliveries = playerData.stream().mapToInt(PlayerLeaderboardEntry::deliveryCount).sum();
                lines.add("");
                lines.add("&8═══════════════════");
                lines.add("&7Toplam: &e" + totalDeliveries + " &7teslimat");
            }
        } else {
            lines.add("&7Aktif etkinlik yok");
            lines.add("");
            lines.add("&8═══════════════════");
            lines.add("");
            lines.add("&7Etkinlik basladiginda");
            lines.add("&7siralama burada gorunecek");
        }
        
        return lines;
    }
    
    @Override
    public void saveHolograms() {
        if (hologramsFile == null) return;
        try {
            YamlConfiguration config = new YamlConfiguration();
            for (HologramInfo info : holograms.values()) {
                String path = "holograms." + info.id();
                config.set(path + ".world", info.location().getWorld().getName());
                config.set(path + ".x", info.location().getX());
                config.set(path + ".y", info.location().getY());
                config.set(path + ".z", info.location().getZ());
                config.set(path + ".deliveryName", info.deliveryName());
            }
            config.save(hologramsFile);
        } catch (Exception e) { 
            LOGGER.severe("Hologramlar kaydedilemedi: " + e.getMessage()); 
        }
    }
    
    @Override
    public void loadHolograms() {
        if (!isEnabled()) {
            LOGGER.info("[Hologram] DecentHolograms aktif degil, hologramlar yuklenmiyor.");
            return;
        }
        if (hologramsFile == null || !hologramsFile.exists()) {
            LOGGER.info("[Hologram] holograms.yml bulunamadi.");
            return;
        }
        
        try {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(hologramsFile);
            var section = config.getConfigurationSection("holograms");
            if (section == null) {
                LOGGER.info("[Hologram] Kaydedilmis hologram yok.");
                return;
            }
            
            for (String id : section.getKeys(false)) {
                String path = "holograms." + id;
                String worldName = config.getString(path + ".world");
                var world = Bukkit.getWorld(worldName);
                if (world == null) {
                    LOGGER.warning("[Hologram] Dunya bulunamadi: " + worldName + " (hologram: " + id + ")");
                    continue;
                }
                
                Location loc = new Location(world, 
                    config.getDouble(path + ".x"), 
                    config.getDouble(path + ".y"), 
                    config.getDouble(path + ".z"));
                String deliveryName = config.getString(path + ".deliveryName");
                List<String> lines = formatLeaderboard(deliveryName, null, List.of());
                
                // Eski hologrami sil (varsa)
                try {
                    deleteDecentHologram(id);
                } catch (Exception ignored) {}
                
                // Yeni hologram olustur
                Object holo = createDecentHologram(id, loc, lines);
                if (holo != null) {
                    nativeHolograms.put(id, holo);
                    holograms.put(id, new HologramInfo(id, loc, lines, false, deliveryName));
                    LOGGER.info("[Hologram] Yuklendi: " + id);
                }
            }
            LOGGER.info("[Hologram] Toplam " + holograms.size() + " hologram yuklendi.");
        } catch (Exception e) { 
            LOGGER.severe("[Hologram] Hologramlar yuklenemedi: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== DecentHolograms API Methods ====================
    
    private Object createDecentHologram(String id, Location location, List<String> lines) {
        if (!decentHologramsAvailable || createHologramMethod == null) return null;
        
        try {
            // DecentHolograms & renk kodlarini otomatik cevirir
            return createHologramMethod.invoke(null, id, location, false, lines);
        } catch (Exception e) {
            LOGGER.warning("[Hologram] DecentHolograms create error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    private Object getDecentHologram(String id) {
        if (!decentHologramsAvailable || getHologramMethod == null) return null;
        
        try {
            return getHologramMethod.invoke(null, id);
        } catch (Exception e) {
            return null;
        }
    }
    
    private void updateDecentHologram(Object hologram, List<String> lines) {
        if (!decentHologramsAvailable || setHologramLinesMethod == null || hologram == null) return;
        
        try {
            setHologramLinesMethod.invoke(null, hologram, lines);
        } catch (Exception e) {
            LOGGER.warning("[Hologram] DecentHolograms update error: " + e.getMessage());
        }
    }
    
    private void deleteDecentHologram(String id) {
        if (!decentHologramsAvailable || removeHologramMethod == null) return;
        
        try {
            removeHologramMethod.invoke(null, id);
        } catch (Exception e) {
            LOGGER.warning("[Hologram] DecentHolograms delete error: " + e.getMessage());
        }
    }
}
