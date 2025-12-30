package com.deliverycore.config;

import com.deliverycore.model.ValidationError;

import java.util.List;

/**
 * Central configuration manager that loads and validates all configuration files.
 * Provides access to category, delivery, and language configurations.
 */
public interface ConfigManager {
    
    /**
     * Loads all configuration files.
     * Should be called during plugin initialization.
     */
    void loadAll();
    
    /**
     * Reloads all configuration files.
     * Validates new configuration before applying changes.
     *
     * @return true if reload was successful, false if validation failed
     */
    boolean reload();
    
    /**
     * Reloads all configuration files and returns detailed result.
     * Validates new configuration before applying changes.
     * If validation fails, the previous configuration is preserved.
     *
     * @return a ReloadResult containing success status and any errors
     */
    ReloadResult reloadWithResult();
    
    /**
     * Gets the category configuration.
     *
     * @return the CategoryConfig instance
     */
    CategoryConfig getCategoryConfig();
    
    /**
     * Gets the delivery configuration.
     *
     * @return the DeliveryConfig instance
     */
    DeliveryConfig getDeliveryConfig();
    
    /**
     * Gets the language configuration.
     *
     * @return the LanguageConfig instance
     */
    LanguageConfig getLanguageConfig();
    
    /**
     * Validates all configurations and returns any errors found.
     *
     * @return a list of validation errors, empty if all valid
     */
    List<ValidationError> validate();
    
    /**
     * Checks if the configuration is valid.
     *
     * @return true if all configurations are valid
     */
    boolean isValid();
}
