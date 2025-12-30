package com.deliverycore.model;

import org.bukkit.Location;

import java.util.List;

/**
 * Information about a hologram instance.
 * Used for listing and managing holograms.
 */
public record HologramInfo(
    String id,
    Location location,
    List<String> lines,
    boolean active,
    String deliveryName
) {
    
    /**
     * Gets the location as a formatted string.
     */
    public String getLocationString() {
        if (location == null) return "Unknown";
        return String.format("%.1f, %.1f, %.1f (%s)", 
            location.getX(), location.getY(), location.getZ(),
            location.getWorld() != null ? location.getWorld().getName() : "unknown");
    }
    
    /**
     * Gets the number of lines in the hologram.
     */
    public int getLineCount() {
        return lines != null ? lines.size() : 0;
    }
    
    /**
     * Gets the status as a formatted string.
     */
    public String getStatusString() {
        if (!active) return "&c○ Inactive";
        if (deliveryName != null && !deliveryName.isEmpty()) {
            return "&a● Active (" + deliveryName + ")";
        }
        return "&e● Waiting for event";
    }
    
    /**
     * Creates a hologram info for an inactive hologram.
     */
    public static HologramInfo inactive(String id, Location location, List<String> lines) {
        return new HologramInfo(id, location, lines, false, null);
    }
    
    /**
     * Creates a hologram info for an active hologram.
     */
    public static HologramInfo active(String id, Location location, List<String> lines, String deliveryName) {
        return new HologramInfo(id, location, lines, true, deliveryName);
    }
}