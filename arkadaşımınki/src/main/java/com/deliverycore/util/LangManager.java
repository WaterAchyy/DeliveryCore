package com.deliverycore.util;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class LangManager {
    
    private final JavaPlugin plugin;
    private final Map<String, YamlConfiguration> languages = new HashMap<>();
    private String currentLang = "tr";
    private String prefix = "";
    
    public LangManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadLanguages();
    }
    
    public void loadLanguages() {
        languages.clear();
        
        File langFolder = new File(plugin.getDataFolder(), "lang");
        if (!langFolder.exists()) langFolder.mkdirs();
        
        File trFile = new File(langFolder, "tr.yml");
        if (trFile.exists()) {
            languages.put("tr", YamlConfiguration.loadConfiguration(trFile));
        }
        
        File enFile = new File(langFolder, "en.yml");
        if (enFile.exists()) {
            languages.put("en", YamlConfiguration.loadConfiguration(enFile));
        }
        
        YamlConfiguration lang = languages.get(currentLang);
        if (lang != null) {
            prefix = color(lang.getString("prefix", "&e&lD&6elivery&e&lC&6ore &8»"));
        }
    }
    
    public void setLanguage(String lang) {
        if (languages.containsKey(lang)) {
            currentLang = lang;
            YamlConfiguration config = languages.get(lang);
            prefix = color(config.getString("prefix", "&e&lD&6elivery&e&lC&6ore &8»"));
        }
    }
    
    public String getCurrentLanguage() {
        return currentLang;
    }
    
    public String get(String key) {
        return get(key, new String[0]);
    }
    
    public String get(String key, String... replacements) {
        YamlConfiguration lang = languages.get(currentLang);
        if (lang == null) lang = languages.get("tr");
        if (lang == null) return key;
        
        String msg = lang.getString(key, key);
        
        for (int i = 0; i < replacements.length - 1; i += 2) {
            msg = msg.replace(replacements[i], replacements[i + 1]);
        }
        
        return color(msg);
    }
    
    public String getRaw(String key) {
        YamlConfiguration lang = languages.get(currentLang);
        if (lang == null) lang = languages.get("tr");
        if (lang == null) return key;
        return lang.getString(key, key);
    }
    
    public String getWithPrefix(String key, String... replacements) {
        return prefix + " " + get(key, replacements);
    }
    
    public String getPrefix() {
        return prefix;
    }
    
    public String getMedal(int rank) {
        return switch (rank) {
            case 1 -> get("medals.first");
            case 2 -> get("medals.second");
            case 3 -> get("medals.third");
            default -> get("medals.other", "{rank}", String.valueOf(rank));
        };
    }
    
    public String getRankColor(int rank) {
        return switch (rank) {
            case 1 -> get("colors.first");
            case 2 -> get("colors.second");
            case 3 -> get("colors.third");
            default -> get("colors.other");
        };
    }
    
    public String color(String text) {
        if (text == null) return "";
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
