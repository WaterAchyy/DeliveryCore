package com.deliverycore.model;

import org.bukkit.Location;

/**
 * Configuration for hologram display settings.
 * v1.1 feature for admin-managed holograms showing leaderboards.
 */
public record HologramConfig(
    boolean enabled,
    Location location,
    String format,
    boolean showLeaderboard,
    int maxPlayers,
    int updateIntervalTicks
) {
    
    /**
     * Creates a default hologram configuration.
     */
    public static HologramConfig defaultConfig() {
        return new HologramConfig(
            false,
            null,
            "&6&l⚡ DELIVERY LEADERBOARD ⚡\n&7No active event",
            true,
            10,
            200 // 10 seconds
        );
    }
    
    /**
     * Creates a hologram configuration with specified location.
     */
    public static HologramConfig withLocation(Location location) {
        return new HologramConfig(
            true,
            location,
            "&6&l⚡ DELIVERY LEADERBOARD ⚡\n&7No active event",
            true,
            10,
            200
        );
    }
    
    /**
     * Gets the location as a formatted string.
     */
    public String getLocationString() {
        if (location == null) return "Not set";
        return String.format("%.1f, %.1f, %.1f (%s)", 
            location.getX(), location.getY(), location.getZ(),
            location.getWorld() != null ? location.getWorld().getName() : "unknown");
    }
    
    /**
     * Creates a disabled hologram configuration.
     */
    public static HologramConfig disabled() {
        return new HologramConfig(
            false,
            null,
            "",
            false,
            0,
            0
        );
    }
}