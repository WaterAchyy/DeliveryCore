package com.deliverycore.service;

import com.deliverycore.model.TabDisplayConfig;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Tab list entegrasyonu servisi
 * v1.1 özelliği - Tab list'te etkinlik bilgileri gösterimi
 */
public interface TabListService {
    
    /**
     * Belirli bir oyuncunun tab list'ini günceller
     * 
     * @param player güncellenecek oyuncu
     */
    void updateTabList(Player player);
    
    /**
     * Tüm oyuncuların tab list'ini günceller
     */
    void updateAllTabLists();
    
    /**
     * Belirli bir teslimat için tab görüntülemeyi etkinleştirir
     * 
     * @param deliveryName teslimat adı
     */
    void enableTabDisplay(String deliveryName);
    
    /**
     * Belirli bir teslimat için tab görüntülemeyi devre dışı bırakır
     * 
     * @param deliveryName teslimat adı
     */
    void disableTabDisplay(String deliveryName);
    
    /**
     * Aktif etkinlik için tab içeriğini formatlar
     * 
     * @param event aktif etkinlik
     * @param config tab display konfigürasyonu
     * @return formatlanmış tab içeriği
     */
    String formatTabContent(ActiveEvent event, TabDisplayConfig config);
    
    /**
     * Tab güncellemelerini zamanlar
     * 
     * @param intervalTicks güncelleme aralığı (tick cinsinden)
     */
    void scheduleTabUpdates(long intervalTicks);
    
    /**
     * Tab güncellemelerini durdurur
     */
    void stopTabUpdates();
    
    /**
     * Tab list'te gösterilecek aktif etkinlikleri döndürür
     * 
     * @return aktif etkinlikler listesi
     */
    List<ActiveEvent> getDisplayableEvents();
    
    /**
     * Tab list servisinin aktif olup olmadığını kontrol eder
     * 
     * @return aktifse true
     */
    boolean isEnabled();
    
    /**
     * Tab list servisini etkinleştirir/devre dışı bırakır
     * 
     * @param enabled etkinleştirmek için true
     */
    void setEnabled(boolean enabled);
}