package com.deliverycore.config;

import com.deliverycore.model.DeliveryDefinition;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Interface for accessing delivery configuration data from deliveries.yml.
 * Provides methods to retrieve delivery definitions and filter by enabled status.
 */
public interface DeliveryConfig {
    
    /**
     * Gets all deliveries as a map.
     *
     * @return an unmodifiable map of delivery names to DeliveryDefinition objects
     */
    Map<String, DeliveryDefinition> getDeliveries();
    
    /**
     * Gets a specific delivery by name.
     *
     * @param name the delivery name
     * @return an Optional containing the delivery if found, empty otherwise
     */
    Optional<DeliveryDefinition> getDelivery(String name);
    
    /**
     * Gets all enabled deliveries.
     *
     * @return an unmodifiable list of enabled DeliveryDefinition objects
     */
    List<DeliveryDefinition> getEnabledDeliveries();
    
    /**
     * Gets all deliveries that should be visible before start (for GUI).
     *
     * @return an unmodifiable list of visible DeliveryDefinition objects
     */
    List<DeliveryDefinition> getVisibleDeliveries();
}
