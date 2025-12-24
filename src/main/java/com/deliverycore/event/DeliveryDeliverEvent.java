package com.deliverycore.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

/**
 * Bir oyuncu teslimat yapmaya çalıştığında tetiklenir.
 * Bu event iptal edilirse (cancelled), teslimat gerçekleşmez ve eşyalar oyuncuda kalır.
 */
public class DeliveryDeliverEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final String deliveryName;
    private final String itemType;
    private final int amount;
    private boolean cancelled;
    private String cancelReason;

    public DeliveryDeliverEvent(Player player, String deliveryName, String itemType, int amount) {
        this.player = player;
        this.deliveryName = deliveryName;
        this.itemType = itemType;
        this.amount = amount;
        this.cancelled = false;
    }

    public Player getPlayer() {
        return player;
    }

    public UUID getPlayerUniqueId() {
        return player.getUniqueId();
    }

    public String getDeliveryName() {
        return deliveryName;
    }

    public String getItemType() {
        return itemType;
    }

    public int getAmount() {
        return amount;
    }
    
    public void setCancelReason(String reason) {
        this.cancelReason = reason;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}