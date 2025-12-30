package com.deliverycore.service;

import com.deliverycore.model.SeasonConfig;
import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Sezonluk etkinlik yönetimi servisi
 * v1.1 özelliği - Özel tarih aralıkları ve gün seçimi
 */
public interface SeasonService {
    
    /**
     * Sezonun şu anda aktif olup olmadığını kontrol eder
     * 
     * @param season sezon konfigürasyonu
     * @return sezon aktifse true
     */
    boolean isSeasonActive(SeasonConfig season);
    
    /**
     * Sezonun bitişine kadar kalan gün sayısını hesaplar
     * 
     * @param season sezon konfigürasyonu
     * @return kalan gün sayısı, sezon aktif değilse -1
     */
    long getDaysRemaining(SeasonConfig season);
    
    /**
     * Sezonun bitiş tarihini döndürür
     * 
     * @param season sezon konfigürasyonu
     * @return bitiş tarihi, yoksa null
     */
    ZonedDateTime getSeasonEndDate(SeasonConfig season);
    
    /**
     * Sezonun aktif günlerini döndürür
     * 
     * @param season sezon konfigürasyonu
     * @return aktif günler listesi
     */
    List<DayOfWeek> getActiveDays(SeasonConfig season);
    
    /**
     * Sezon konfigürasyonunun geçerli olup olmadığını kontrol eder
     * 
     * @param season sezon konfigürasyonu
     * @return geçerliyse true
     */
    boolean isValidSeasonConfig(SeasonConfig season);
    
    /**
     * Belirli bir günün sezon için aktif olup olmadığını kontrol eder
     * 
     * @param season sezon konfigürasyonu
     * @param day kontrol edilecek gün
     * @return aktifse true
     */
    boolean isDayActiveInSeason(SeasonConfig season, DayOfWeek day);
    
    /**
     * Mevcut zamanın sezon içinde olup olmadığını kontrol eder
     * 
     * @param season sezon konfigürasyonu
     * @param currentTime kontrol edilecek zaman
     * @return sezon içindeyse true
     */
    boolean isTimeInSeason(SeasonConfig season, ZonedDateTime currentTime);
}