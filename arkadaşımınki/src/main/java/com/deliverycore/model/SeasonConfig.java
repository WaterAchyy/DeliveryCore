package com.deliverycore.model;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Sezonluk etkinlik konfigürasyonu
 * v1.1 özelliği - Özel tarih aralıkları ve gün seçimi
 */
public record SeasonConfig(
    boolean enabled,
    ZonedDateTime startDate,
    ZonedDateTime endDate,
    List<DayOfWeek> activeDays,
    boolean customDays
) {
    
    /**
     * Varsayılan sezon konfigürasyonu (devre dışı)
     */
    public static SeasonConfig disabled() {
        return new SeasonConfig(
            false,
            null,
            null,
            List.of(),
            false
        );
    }
    
    /**
     * Sezonun geçerli olup olmadığını kontrol eder
     */
    public boolean isValid() {
        if (!enabled) {
            return true; // Devre dışı sezonlar her zaman geçerli
        }
        
        if (startDate == null || endDate == null) {
            return false;
        }
        
        return startDate.isBefore(endDate);
    }
    
    /**
     * Belirli bir günün aktif günler listesinde olup olmadığını kontrol eder
     */
    public boolean isDayActive(DayOfWeek day) {
        if (!customDays || activeDays.isEmpty()) {
            return true; // Özel gün seçimi yoksa her gün aktif
        }
        
        return activeDays.contains(day);
    }
}