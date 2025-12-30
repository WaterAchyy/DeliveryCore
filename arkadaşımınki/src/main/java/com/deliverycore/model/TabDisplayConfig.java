package com.deliverycore.model;

/**
 * Tab list görüntüleme konfigürasyonu
 * v1.1 özelliği - Tab list entegrasyonu
 */
public record TabDisplayConfig(
    boolean enabled,
    String format,
    boolean showDaysRemaining,
    long updateIntervalTicks
) {
    
    /**
     * Varsayılan tab display konfigürasyonu
     */
    public static TabDisplayConfig defaultConfig() {
        return new TabDisplayConfig(
            false,
            "&6[DeliveryCore] &e{delivery} &7- &a{item} &7({days_remaining} gün kaldı)",
            true,
            100L // 5 saniye (20 tick = 1 saniye)
        );
    }
    
    /**
     * Devre dışı tab display konfigürasyonu
     */
    public static TabDisplayConfig disabled() {
        return new TabDisplayConfig(
            false,
            "",
            false,
            0L
        );
    }
    
    /**
     * Konfigürasyonun geçerli olup olmadığını kontrol eder
     */
    public boolean isValid() {
        if (!enabled) {
            return true; // Devre dışı konfigürasyonlar her zaman geçerli
        }
        
        return format != null && !format.trim().isEmpty() && updateIntervalTicks > 0;
    }
}