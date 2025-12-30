package com.deliverycore.config;

import com.deliverycore.model.*;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of ConfigManager that loads and validates all configuration files.
 */
public class ConfigManagerImpl implements ConfigManager {
    
    private static final Logger LOGGER = Logger.getLogger(ConfigManagerImpl.class.getName());
    
    private final String dataFolder;
    private CategoryConfig categoryConfig;
    private DeliveryConfig deliveryConfig;
    private LanguageConfig languageConfig;
    private List<ValidationError> validationErrors;
    
    /**
     * Creates a ConfigManagerImpl with the specified data folder.
     *
     * @param dataFolder the plugin data folder path
     */
    public ConfigManagerImpl(String dataFolder) {
        this.dataFolder = dataFolder;
        this.validationErrors = new ArrayList<>();
    }
    
    /**
     * Creates a ConfigManagerImpl with pre-loaded configs (for testing).
     *
     * @param categoryConfig  the category configuration
     * @param deliveryConfig  the delivery configuration
     * @param languageConfig  the language configuration
     */
    public ConfigManagerImpl(CategoryConfig categoryConfig, 
                             DeliveryConfig deliveryConfig,
                             LanguageConfig languageConfig) {
        this.dataFolder = null;
        this.categoryConfig = categoryConfig;
        this.deliveryConfig = deliveryConfig;
        this.languageConfig = languageConfig;
        this.validationErrors = new ArrayList<>();
    }

    @Override
    public void loadAll() {
        validationErrors.clear();
        
        // Load categories
        try {
            String categoriesPath = dataFolder + File.separator + "categories.yml";
            File categoriesFile = new File(categoriesPath);
            if (categoriesFile.exists()) {
                categoryConfig = new CategoryConfigImpl(categoriesPath);
            } else {
                categoryConfig = new CategoryConfigImpl(Collections.emptyMap());
                validationErrors.add(new ValidationError(
                    "categories.yml", null, "File not found", ErrorSeverity.WARNING));
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load categories.yml", e);
            categoryConfig = new CategoryConfigImpl(Collections.emptyMap());
            validationErrors.add(new ValidationError(
                "categories.yml", null, "Failed to load: " + e.getMessage(), ErrorSeverity.ERROR));
        }
        
        // Load deliveries
        try {
            String deliveriesPath = dataFolder + File.separator + "deliveries.yml";
            File deliveriesFile = new File(deliveriesPath);
            if (deliveriesFile.exists()) {
                deliveryConfig = new DeliveryConfigImpl(deliveriesPath);
            } else {
                deliveryConfig = new DeliveryConfigImpl(Collections.emptyList());
                validationErrors.add(new ValidationError(
                    "deliveries.yml", null, "File not found", ErrorSeverity.WARNING));
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load deliveries.yml", e);
            deliveryConfig = new DeliveryConfigImpl(Collections.emptyList());
            validationErrors.add(new ValidationError(
                "deliveries.yml", null, "Failed to load: " + e.getMessage(), ErrorSeverity.ERROR));
        }
        
        // Load languages
        try {
            String langPath = dataFolder + File.separator + "lang";
            File langDir = new File(langPath);
            if (langDir.exists() && langDir.isDirectory()) {
                languageConfig = new LanguageConfigImpl(langPath);
            } else {
                languageConfig = new LanguageConfigImpl(Collections.emptyMap());
                validationErrors.add(new ValidationError(
                    "lang/", null, "Directory not found", ErrorSeverity.WARNING));
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load language files", e);
            languageConfig = new LanguageConfigImpl(Collections.emptyMap());
            validationErrors.add(new ValidationError(
                "lang/", null, "Failed to load: " + e.getMessage(), ErrorSeverity.ERROR));
        }
        
        // Validate loaded configurations
        validationErrors.addAll(validateConfigurations());
    }

    @Override
    public boolean reload() {
        return reloadWithResult().success();
    }
    
    @Override
    public ReloadResult reloadWithResult() {
        // Store current configs in case reload fails
        CategoryConfig prevCategory = categoryConfig;
        DeliveryConfig prevDelivery = deliveryConfig;
        LanguageConfig prevLanguage = languageConfig;
        List<ValidationError> prevErrors = new ArrayList<>(validationErrors);
        
        // Attempt to load new configs
        loadAll();
        
        // Check for critical errors
        boolean hasCriticalErrors = validationErrors.stream()
            .anyMatch(e -> e.severity() == ErrorSeverity.CRITICAL);
        
        if (hasCriticalErrors) {
            // Restore previous configs
            List<ValidationError> newErrors = new ArrayList<>(validationErrors);
            categoryConfig = prevCategory;
            deliveryConfig = prevDelivery;
            languageConfig = prevLanguage;
            validationErrors = prevErrors;
            LOGGER.warning("Configuration reload failed due to critical errors. Previous configuration restored.");
            return ReloadResult.failure(newErrors);
        }
        
        LOGGER.info("Configuration reloaded successfully.");
        
        if (validationErrors.isEmpty()) {
            return ReloadResult.ok();
        } else {
            return ReloadResult.successWithWarnings(new ArrayList<>(validationErrors));
        }
    }
    
    private List<ValidationError> validateConfigurations() {
        List<ValidationError> errors = new ArrayList<>();
        
        // Validate deliveries reference valid categories and v1.1 configurations
        if (deliveryConfig != null && categoryConfig != null) {
            for (DeliveryDefinition delivery : deliveryConfig.getDeliveries().values()) {
                errors.addAll(validateDelivery(delivery));
            }
        }
        
        return errors;
    }
    
    private List<ValidationError> validateDelivery(DeliveryDefinition delivery) {
        List<ValidationError> errors = new ArrayList<>();
        
        // Validate category reference if fixed
        if (delivery.category().mode() == SelectionMode.FIXED) {
            String categoryName = delivery.category().value();
            if (categoryName != null && categoryConfig.getCategory(categoryName).isEmpty()) {
                errors.add(new ValidationError(
                    "deliveries.yml",
                    delivery.name() + ".category.value",
                    "Referenced category '" + categoryName + "' not found",
                    ErrorSeverity.ERROR));
            }
        }
        
        // Validate schedule is not empty
        if (delivery.schedule().start().isEmpty()) {
            errors.add(new ValidationError(
                "deliveries.yml",
                delivery.name() + ".schedule.start",
                "Schedule start is required",
                ErrorSeverity.CRITICAL));
        }
        
        if (delivery.schedule().end().isEmpty()) {
            errors.add(new ValidationError(
                "deliveries.yml",
                delivery.name() + ".schedule.end",
                "Schedule end is required",
                ErrorSeverity.CRITICAL));
        }
        
        // Validate reward configuration
        if (delivery.reward().type() == RewardType.INVENTORY && 
            (delivery.reward().item() == null || delivery.reward().item().isEmpty())) {
            errors.add(new ValidationError(
                "deliveries.yml",
                delivery.name() + ".reward.item",
                "Inventory reward requires item to be specified",
                ErrorSeverity.ERROR));
        }
        
        // Validate v1.1 configurations
        errors.addAll(validateV11Configurations(delivery));
        
        return errors;
    }

    @Override
    public CategoryConfig getCategoryConfig() {
        return categoryConfig;
    }
    
    @Override
    public DeliveryConfig getDeliveryConfig() {
        return deliveryConfig;
    }
    
    @Override
    public LanguageConfig getLanguageConfig() {
        return languageConfig;
    }
    
    @Override
    public List<ValidationError> validate() {
        return Collections.unmodifiableList(validationErrors);
    }
    
    @Override
    public boolean isValid() {
        return validationErrors.stream()
            .noneMatch(e -> e.severity() == ErrorSeverity.CRITICAL || 
                           e.severity() == ErrorSeverity.ERROR);
    }
    
    /**
     * Gets validation errors filtered by severity.
     *
     * @param severity the severity to filter by
     * @return list of errors with the specified severity
     */
    public List<ValidationError> getErrorsBySeverity(ErrorSeverity severity) {
        return validationErrors.stream()
            .filter(e -> e.severity() == severity)
            .toList();
    }
    
    /**
     * Checks if there are any critical errors.
     *
     * @return true if there are critical errors
     */
    public boolean hasCriticalErrors() {
        return validationErrors.stream()
            .anyMatch(e -> e.severity() == ErrorSeverity.CRITICAL);
    }
    
    /**
     * Validates the pre-loaded configurations and returns errors.
     * Use this method when configs are loaded via constructor (for testing).
     *
     * @return list of validation errors
     */
    public List<ValidationError> validateAndReport() {
        validationErrors.clear();
        validationErrors.addAll(validateConfigurations());
        return Collections.unmodifiableList(validationErrors);
    }
    
    /**
     * Validates v1.1 specific configurations.
     */
    private List<ValidationError> validateV11Configurations(DeliveryDefinition delivery) {
        List<ValidationError> errors = new ArrayList<>();
        
        // Validate season configuration
        if (delivery.season().enabled()) {
            if (!delivery.season().isValid()) {
                errors.add(new ValidationError(
                    "deliveries.yml",
                    delivery.name() + ".season",
                    "Invalid season configuration: start date must be before end date",
                    ErrorSeverity.ERROR));
            }
        }
        
        // Validate tab display configuration
        if (delivery.tabDisplay().enabled()) {
            if (!delivery.tabDisplay().isValid()) {
                errors.add(new ValidationError(
                    "deliveries.yml",
                    delivery.name() + ".tabDisplay",
                    "Invalid tab display configuration: format cannot be empty and update interval must be positive",
                    ErrorSeverity.ERROR));
            }
        }
        
        // Validate hologram configuration
        if (delivery.hologram().enabled()) {
            if (delivery.hologram().location() == null) {
                errors.add(new ValidationError(
                    "deliveries.yml",
                    delivery.name() + ".hologram.location",
                    "Hologram location cannot be null when enabled",
                    ErrorSeverity.ERROR));
            }
            
            if (delivery.hologram().updateIntervalTicks() <= 0) {
                errors.add(new ValidationError(
                    "deliveries.yml",
                    delivery.name() + ".hologram.updateIntervalTicks",
                    "Hologram update interval must be positive",
                    ErrorSeverity.ERROR));
            }
        }
        
        return errors;
    }
}
