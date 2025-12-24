package com.deliverycore.config;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.*;

/**
 * Implementation of LanguageConfig that loads language files.
 * Supports Turkish (tr) and English (en) with Turkish as fallback.
 */
public class LanguageConfigImpl implements LanguageConfig {
    
    private final Map<String, Map<String, String>> languages;
    
    /**
     * Creates a LanguageConfigImpl from language directory path.
     *
     * @param langDirectory the directory containing language files
     * @throws IOException if files cannot be read
     */
    public LanguageConfigImpl(String langDirectory) throws IOException {
        this.languages = new LinkedHashMap<>();
        loadLanguageFile(langDirectory + "/tr.yml", TURKISH);
        loadLanguageFile(langDirectory + "/en.yml", ENGLISH);
    }
    
    /**
     * Creates a LanguageConfigImpl from pre-loaded language maps (for testing).
     *
     * @param languages map of locale to message map
     */
    public LanguageConfigImpl(Map<String, Map<String, String>> languages) {
        this.languages = new LinkedHashMap<>();
        for (Map.Entry<String, Map<String, String>> entry : languages.entrySet()) {
            this.languages.put(entry.getKey(), 
                Collections.unmodifiableMap(new LinkedHashMap<>(entry.getValue())));
        }
    }
    
    private void loadLanguageFile(String filePath, String locale) throws IOException {
        File file = new File(filePath);
        if (file.exists()) {
            try (InputStream is = new FileInputStream(file)) {
                loadFromStream(is, locale);
            }
        } else {
            languages.put(locale, Collections.emptyMap());
        }
    }

    /**
     * Loads language data from an input stream.
     *
     * @param inputStream the input stream
     * @param locale      the locale identifier
     */
    public void loadFromStream(InputStream inputStream, String locale) {
        Yaml yaml = new Yaml();
        Map<String, Object> root = yaml.load(inputStream);
        Map<String, String> messages = flattenMessages(root, "");
        languages.put(locale, Collections.unmodifiableMap(messages));
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, String> flattenMessages(Map<String, Object> map, String prefix) {
        Map<String, String> result = new LinkedHashMap<>();
        if (map == null) {
            return result;
        }
        
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            Object value = entry.getValue();
            
            if (value instanceof Map) {
                result.putAll(flattenMessages((Map<String, Object>) value, key));
            } else if (value != null) {
                result.put(key, value.toString());
            }
        }
        return result;
    }
    
    @Override
    public Optional<String> getMessage(String key, String locale) {
        Map<String, String> messages = languages.get(locale);
        if (messages != null) {
            return Optional.ofNullable(messages.get(key));
        }
        return Optional.empty();
    }
    
    @Override
    public String getMessageWithFallback(String key, String locale) {
        // Try requested locale first
        Optional<String> message = getMessage(key, locale);
        if (message.isPresent()) {
            return message.get();
        }
        
        // Fall back to Turkish (default)
        message = getMessage(key, DEFAULT_LOCALE);
        if (message.isPresent()) {
            return message.get();
        }
        
        // Return key if not found
        return key;
    }

    @Override
    public Map<String, String> getMessages(String locale) {
        return languages.getOrDefault(locale, Collections.emptyMap());
    }
    
    @Override
    public Set<String> getMessageKeys() {
        Set<String> keys = new LinkedHashSet<>();
        for (Map<String, String> messages : languages.values()) {
            keys.addAll(messages.keySet());
        }
        return Collections.unmodifiableSet(keys);
    }
    
    @Override
    public Set<String> getSupportedLocales() {
        return Collections.unmodifiableSet(languages.keySet());
    }
    
    /**
     * Serializes the language data to YAML format for a specific locale.
     *
     * @param locale the locale to serialize
     * @return YAML string representation
     */
    public String toYaml(String locale) {
        Map<String, String> messages = languages.get(locale);
        if (messages == null) {
            return "";
        }
        
        // Unflatten the messages back to nested structure
        Map<String, Object> nested = unflattenMessages(messages);
        
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        Yaml yaml = new Yaml(options);
        return yaml.dump(nested);
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> unflattenMessages(Map<String, String> flat) {
        Map<String, Object> result = new LinkedHashMap<>();
        
        for (Map.Entry<String, String> entry : flat.entrySet()) {
            String[] parts = entry.getKey().split("\\.");
            Map<String, Object> current = result;
            
            for (int i = 0; i < parts.length - 1; i++) {
                current = (Map<String, Object>) current.computeIfAbsent(
                    parts[i], k -> new LinkedHashMap<>());
            }
            
            current.put(parts[parts.length - 1], entry.getValue());
        }
        
        return result;
    }
    
    /**
     * Parses YAML string and loads it as a locale.
     *
     * @param yamlContent the YAML content
     * @param locale      the locale identifier
     * @return a new LanguageConfigImpl with the loaded locale
     */
    public static LanguageConfigImpl fromYaml(String yamlContent, String locale) {
        Map<String, Map<String, String>> languages = new LinkedHashMap<>();
        LanguageConfigImpl config = new LanguageConfigImpl(languages);
        config.loadFromStream(new ByteArrayInputStream(yamlContent.getBytes()), locale);
        return config;
    }
    
    /**
     * Creates a LanguageConfigImpl with both Turkish and English from YAML strings.
     *
     * @param turkishYaml Turkish language YAML
     * @param englishYaml English language YAML
     * @return a new LanguageConfigImpl
     */
    public static LanguageConfigImpl fromBothYaml(String turkishYaml, String englishYaml) {
        Map<String, Map<String, String>> languages = new LinkedHashMap<>();
        LanguageConfigImpl config = new LanguageConfigImpl(languages);
        if (turkishYaml != null && !turkishYaml.isEmpty()) {
            config.loadFromStream(new ByteArrayInputStream(turkishYaml.getBytes()), TURKISH);
        }
        if (englishYaml != null && !englishYaml.isEmpty()) {
            config.loadFromStream(new ByteArrayInputStream(englishYaml.getBytes()), ENGLISH);
        }
        return config;
    }
}
