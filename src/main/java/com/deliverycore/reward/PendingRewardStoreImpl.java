package com.deliverycore.reward;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of PendingRewardStore.
 * Thread-safe for concurrent access from multiple threads.
 */
public class PendingRewardStoreImpl implements PendingRewardStore {
    
    private final Map<UUID, List<PendingReward>> rewards = new ConcurrentHashMap<>();
    
    @Override
    public void store(PendingReward reward) {
        Objects.requireNonNull(reward, "Reward cannot be null");
        
        rewards.compute(reward.playerUuid(), (uuid, existing) -> {
            List<PendingReward> list = existing != null 
                ? new ArrayList<>(existing) 
                : new ArrayList<>();
            list.add(reward);
            return list;
        });
    }
    
    @Override
    public List<PendingReward> getRewards(UUID playerUuid) {
        Objects.requireNonNull(playerUuid, "Player UUID cannot be null");
        
        List<PendingReward> playerRewards = rewards.get(playerUuid);
        return playerRewards != null 
            ? Collections.unmodifiableList(new ArrayList<>(playerRewards))
            : Collections.emptyList();
    }
    
    @Override
    public List<PendingReward> removeRewards(UUID playerUuid) {
        Objects.requireNonNull(playerUuid, "Player UUID cannot be null");
        
        List<PendingReward> removed = rewards.remove(playerUuid);
        return removed != null 
            ? Collections.unmodifiableList(removed)
            : Collections.emptyList();
    }
    
    @Override
    public boolean hasPendingRewards(UUID playerUuid) {
        Objects.requireNonNull(playerUuid, "Player UUID cannot be null");
        
        List<PendingReward> playerRewards = rewards.get(playerUuid);
        return playerRewards != null && !playerRewards.isEmpty();
    }
    
    @Override
    public int getRewardCount(UUID playerUuid) {
        Objects.requireNonNull(playerUuid, "Player UUID cannot be null");
        
        List<PendingReward> playerRewards = rewards.get(playerUuid);
        return playerRewards != null ? playerRewards.size() : 0;
    }
    
    @Override
    public List<PendingReward> getAllRewards() {
        List<PendingReward> all = new ArrayList<>();
        for (List<PendingReward> playerRewards : rewards.values()) {
            all.addAll(playerRewards);
        }
        return Collections.unmodifiableList(all);
    }
    
    @Override
    public void clear() {
        rewards.clear();
    }
}
