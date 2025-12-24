package com.deliverycore.service;

import com.deliverycore.model.Winner;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing delivery event lifecycle.
 * Handles event start/end, player delivery tracking, and winner calculation.
 */
public interface DeliveryService {
    
    /**
     * Starts a delivery event.
     *
     * @param deliveryName the name of the delivery to start
     * @return the started ActiveEvent, or empty if the delivery doesn't exist or is disabled
     */
    Optional<ActiveEvent> startEvent(String deliveryName);
    
    /**
     * Starts a delivery event with force option.
     *
     * @param deliveryName the name of the delivery to start
     * @param force if true, starts even if disabled
     * @return the started ActiveEvent, or empty if the delivery doesn't exist
     */
    Optional<ActiveEvent> startEvent(String deliveryName, boolean force);
    
    /**
     * Ends a delivery event and calculates winners.
     *
     * @param deliveryName the name of the delivery to end
     * @return the list of winners, empty if event wasn't active
     */
    List<Winner> endEvent(String deliveryName);
    
    /**
     * Records a delivery from a player.
     *
     * @param playerUuid   the player's UUID
     * @param deliveryName the delivery event name
     * @param amount       the number of items delivered
     * @return true if the delivery was recorded, false if event not active
     */
    boolean recordDelivery(UUID playerUuid, String deliveryName, int amount);
    
    /**
     * Gets an active event by name.
     *
     * @param deliveryName the delivery name
     * @return the active event if found
     */
    Optional<ActiveEvent> getActiveEvent(String deliveryName);
    
    /**
     * Gets all currently active events.
     *
     * @return list of active events
     */
    List<ActiveEvent> getAllActiveEvents();
    
    /**
     * Calculates winners for an event based on delivery counts.
     *
     * @param event       the active event
     * @param winnerCount the maximum number of winners
     * @param nameResolver function to resolve player names from UUIDs
     * @return list of winners sorted by delivery count descending
     */
    List<Winner> calculateWinners(ActiveEvent event, int winnerCount, PlayerNameResolver nameResolver);
    
    /**
     * Functional interface for resolving player names from UUIDs.
     */
    @FunctionalInterface
    interface PlayerNameResolver {
        String resolve(UUID uuid);
    }
}
