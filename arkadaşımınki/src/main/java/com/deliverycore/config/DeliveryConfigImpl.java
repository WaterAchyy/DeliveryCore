package com.deliverycore.config;

import com.deliverycore.model.*;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.time.ZoneId;
import java.util.*;

/**
 * Implementation of DeliveryConfig that parses deliveries.yml.
 * Supports loading from file path or input stream.
 */
public class DeliveryConfigImpl implements DeliveryConfig {
    
    private final Map<String, DeliveryDefinition> deliveries;
    
    /**
     * Creates a DeliveryConfigImpl from a file path.
     *
     * @param filePath the path to deliveries.yml
     * @throws IOException if the file cannot be read
     */
    public DeliveryConfigImpl(String filePath) throws IOException {
        this(new FileInputStream(filePath));
    }
    
    /**
     * Creates a DeliveryConfigImpl from an input stream.
     *
     * @param inputStream the input stream containing YAML data
     */
    public DeliveryConfigImpl(InputStream inputStream) {
        this.deliveries = parseDeliveries(inputStream);
    }
    
    /**
     * Creates a DeliveryConfigImpl from a list of definitions (for testing).
     *
     * @param definitions list of delivery definitions
     */
    public DeliveryConfigImpl(List<DeliveryDefinition> definitions) {
        Map<String, DeliveryDefinition> map = new LinkedHashMap<>();
        for (DeliveryDefinition def : definitions) {
            map.put(def.name(), def);
        }
        this.deliveries = Collections.unmodifiableMap(map);
    }

    @SuppressWarnings("unchecked")
    private Map<String, DeliveryDefinition> parseDeliveries(InputStream inputStream) {
        Yaml yaml = new Yaml();
        Map<String, Object> root = yaml.load(inputStream);
        
        if (root == null) {
            return Collections.emptyMap();
        }
        
        Map<String, DeliveryDefinition> result = new LinkedHashMap<>();
        
        Object deliveriesObj = root.get("deliveries");
        if (deliveriesObj instanceof Map) {
            Map<String, Object> deliveriesData = (Map<String, Object>) deliveriesObj;
            for (Map.Entry<String, Object> entry : deliveriesData.entrySet()) {
                String name = entry.getKey();
                if (entry.getValue() instanceof Map) {
                    Map<String, Object> data = (Map<String, Object>) entry.getValue();
                    DeliveryDefinition def = parseDeliveryDefinition(name, data);
                    result.put(name, def);
                }
            }
        }
        
        return Collections.unmodifiableMap(result);
    }
    
    @SuppressWarnings("unchecked")
    private DeliveryDefinition parseDeliveryDefinition(String name, Map<String, Object> data) {
        boolean enabled = getBoolean(data, "enabled", true);
        boolean visibleBeforeStart = getBoolean(data, "visible-before-start", false);
        SelectionConfig category = parseSelectionConfig(data, "category");
        SelectionConfig item = parseSelectionConfig(data, "item");
        ZoneId timezone = ZoneId.of(getString(data, "timezone", "UTC"));
        ScheduleConfig schedule = parseScheduleConfig((Map<String, Object>) data.get("schedule"));
        int winnerCount = getInt(data, "winners", 1);
        RewardConfig reward = parseRewardConfig((Map<String, Object>) data.get("reward"));
        WebhookConfig webhook = parseWebhookConfig((Map<String, Object>) data.get("webhook"));
        
        return DeliveryDefinition.withoutV11Features(name, enabled, visibleBeforeStart, category, item, timezone, 
                                       schedule, winnerCount, reward, webhook);
    }

    @SuppressWarnings("unchecked")
    private SelectionConfig parseSelectionConfig(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value instanceof Map) {
            Map<String, Object> selectionData = (Map<String, Object>) value;
            String mode = getString(selectionData, "mode", "random");
            String val = getString(selectionData, "value", null);
            return new SelectionConfig(
                "fixed".equalsIgnoreCase(mode) ? SelectionMode.FIXED : SelectionMode.RANDOM,
                val
            );
        } else if (value instanceof String) {
            return SelectionConfig.fixed((String) value);
        }
        return SelectionConfig.random();
    }
    
    private ScheduleConfig parseScheduleConfig(Map<String, Object> data) {
        if (data == null) {
            return new ScheduleConfig("", "");
        }
        return new ScheduleConfig(
            getString(data, "start", ""),
            getString(data, "end", "")
        );
    }
    
    @SuppressWarnings("unchecked")
    private RewardConfig parseRewardConfig(Map<String, Object> data) {
        if (data == null) {
            return RewardConfig.inventory("DIAMOND", 1);
        }
        String typeStr = getString(data, "type", "inventory");
        RewardType type = "command".equalsIgnoreCase(typeStr) ? RewardType.COMMAND : RewardType.INVENTORY;
        String item = getString(data, "item", null);
        int amount = getInt(data, "amount", 1);
        List<String> commands = (List<String>) data.getOrDefault("commands", List.of());
        return new RewardConfig(type, item, amount, commands);
    }

    @SuppressWarnings("unchecked")
    private WebhookConfig parseWebhookConfig(Map<String, Object> data) {
        if (data == null) {
            return WebhookConfig.disabled();
        }
        boolean enabled = getBoolean(data, "enabled", false);
        String url = getString(data, "url", "");
        WebhookEventConfig start = parseWebhookEventConfig((Map<String, Object>) data.get("start"));
        WebhookEventConfig end = parseWebhookEventConfig((Map<String, Object>) data.get("end"));
        return new WebhookConfig(enabled, url, start, end);
    }
    
    @SuppressWarnings("unchecked")
    private WebhookEventConfig parseWebhookEventConfig(Map<String, Object> data) {
        if (data == null) {
            return new WebhookEventConfig(false, new EmbedConfig("", "", "#000000"));
        }
        boolean mentionEveryone = getBoolean(data, "everyone", false);
        EmbedConfig embed = parseEmbedConfig((Map<String, Object>) data.get("embed"));
        return new WebhookEventConfig(mentionEveryone, embed);
    }
    
    private EmbedConfig parseEmbedConfig(Map<String, Object> data) {
        if (data == null) {
            return new EmbedConfig("", "", "#000000");
        }
        return new EmbedConfig(
            getString(data, "title", ""),
            getString(data, "description", ""),
            getString(data, "color", "#000000")
        );
    }
    
    private String getString(Map<String, Object> data, String key, String defaultValue) {
        Object value = data.get(key);
        return value != null ? value.toString() : defaultValue;
    }
    
    private boolean getBoolean(Map<String, Object> data, String key, boolean defaultValue) {
        Object value = data.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return defaultValue;
    }
    
    private int getInt(Map<String, Object> data, String key, int defaultValue) {
        Object value = data.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return defaultValue;
    }

    @Override
    public Map<String, DeliveryDefinition> getDeliveries() {
        return deliveries;
    }
    
    @Override
    public Optional<DeliveryDefinition> getDelivery(String name) {
        return Optional.ofNullable(deliveries.get(name));
    }
    
    @Override
    public List<DeliveryDefinition> getEnabledDeliveries() {
        return deliveries.values().stream()
            .filter(DeliveryDefinition::enabled)
            .toList();
    }
    
    @Override
    public List<DeliveryDefinition> getVisibleDeliveries() {
        return deliveries.values().stream()
            .filter(d -> d.enabled() && d.visibleBeforeStart())
            .toList();
    }
    
    /**
     * Serializes the deliveries to YAML format.
     *
     * @return YAML string representation of deliveries
     */
    public String toYaml() {
        Map<String, Object> root = new LinkedHashMap<>();
        Map<String, Object> deliveriesMap = new LinkedHashMap<>();
        
        for (DeliveryDefinition def : deliveries.values()) {
            deliveriesMap.put(def.name(), serializeDelivery(def));
        }
        
        root.put("deliveries", deliveriesMap);
        
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        Yaml yaml = new Yaml(options);
        return yaml.dump(root);
    }
    
    private Map<String, Object> serializeDelivery(DeliveryDefinition def) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("enabled", def.enabled());
        data.put("visible-before-start", def.visibleBeforeStart());
        data.put("category", serializeSelection(def.category()));
        data.put("item", serializeSelection(def.item()));
        data.put("timezone", def.timezone().getId());
        data.put("schedule", serializeSchedule(def.schedule()));
        data.put("winners", def.winnerCount());
        data.put("reward", serializeReward(def.reward()));
        data.put("webhook", serializeWebhook(def.webhook()));
        return data;
    }

    private Map<String, Object> serializeSelection(SelectionConfig config) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("mode", config.mode().name().toLowerCase());
        if (config.value() != null) {
            data.put("value", config.value());
        }
        return data;
    }
    
    private Map<String, Object> serializeSchedule(ScheduleConfig config) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("start", config.start());
        data.put("end", config.end());
        return data;
    }
    
    private Map<String, Object> serializeReward(RewardConfig config) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("type", config.type().name().toLowerCase());
        if (config.item() != null) {
            data.put("item", config.item());
        }
        data.put("amount", config.itemAmount());
        if (!config.commands().isEmpty()) {
            data.put("commands", new ArrayList<>(config.commands()));
        }
        return data;
    }
    
    private Map<String, Object> serializeWebhook(WebhookConfig config) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("enabled", config.enabled());
        data.put("url", config.url());
        data.put("start", serializeWebhookEvent(config.start()));
        data.put("end", serializeWebhookEvent(config.end()));
        return data;
    }
    
    private Map<String, Object> serializeWebhookEvent(WebhookEventConfig config) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("everyone", config.mentionEveryone());
        data.put("embed", serializeEmbed(config.embed()));
        return data;
    }
    
    private Map<String, Object> serializeEmbed(EmbedConfig config) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("title", config.title());
        data.put("description", config.description());
        data.put("color", config.color());
        return data;
    }
    
    /**
     * Parses YAML string and creates a DeliveryConfigImpl.
     *
     * @param yamlContent the YAML content as string
     * @return a new DeliveryConfigImpl instance
     */
    public static DeliveryConfigImpl fromYaml(String yamlContent) {
        return new DeliveryConfigImpl(new ByteArrayInputStream(yamlContent.getBytes()));
    }
}
