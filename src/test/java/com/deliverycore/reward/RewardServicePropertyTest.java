package com.deliverycore.reward;

import com.deliverycore.model.PlaceholderContext;
import com.deliverycore.model.RewardConfig;
import com.deliverycore.model.RewardType;
import com.deliverycore.model.Winner;
import com.deliverycore.placeholder.PlaceholderEngineImpl;
import net.jqwik.api.*;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for RewardService and PendingRewardStore.
 */
class RewardServicePropertyTest {
    
    /**
     * Feature: delivery-core, Property 18: Pending Reward Storage Round-Trip
     * For any pending reward stored for an offline player, retrieving pending rewards
     * for that player UUID should include the stored reward with all fields preserved.
     * Validates: Requirement 6.3
     */
    @Property(tries = 100)
    void pendingRewardStorageRoundTrip(
            @ForAll("validPendingReward") PendingReward reward) {
        
        PendingRewardStore store = new PendingRewardStoreImpl();
        
        // Store the reward
        store.store(reward);
        
        // Retrieve rewards for the player
        List<PendingReward> retrieved = store.getRewards(reward.playerUuid());
        
        // Verify the reward is present with all fields preserved
        assertThat(retrieved).isNotEmpty();
        assertThat(retrieved).contains(reward);
        
        // Verify specific field preservation
        PendingReward found = retrieved.stream()
            .filter(r -> r.equals(reward))
            .findFirst()
            .orElseThrow();
        
        assertThat(found.playerUuid()).isEqualTo(reward.playerUuid());
        assertThat(found.deliveryName()).isEqualTo(reward.deliveryName());
        assertThat(found.reward()).isEqualTo(reward.reward());
        assertThat(found.earnedAt()).isEqualTo(reward.earnedAt());
    }
    
    /**
     * Feature: delivery-core, Property 18: Pending Reward Storage Round-Trip (Multiple Rewards)
     * For multiple pending rewards stored for the same player, all rewards should be retrievable.
     * Validates: Requirement 6.3
     */
    @Property(tries = 100)
    void multipleRewardsStorageRoundTrip(
            @ForAll("validRewardConfigs") List<RewardConfig> rewards) {
        
        Assume.that(!rewards.isEmpty());
        UUID playerUuid = UUID.randomUUID();
        
        PendingRewardStore store = new PendingRewardStoreImpl();
        List<PendingReward> storedRewards = new ArrayList<>();
        
        // Store multiple rewards for the same player
        for (int i = 0; i < rewards.size(); i++) {
            PendingReward pending = PendingReward.create(
                playerUuid, 
                "delivery_" + i, 
                rewards.get(i)
            );
            store.store(pending);
            storedRewards.add(pending);
        }
        
        // Retrieve all rewards
        List<PendingReward> retrieved = store.getRewards(playerUuid);
        
        // Verify count matches
        assertThat(retrieved).hasSize(rewards.size());
        
        // Verify all delivery names are present
        Set<String> expectedNames = new HashSet<>();
        for (int i = 0; i < rewards.size(); i++) {
            expectedNames.add("delivery_" + i);
        }
        
        Set<String> actualNames = new HashSet<>();
        for (PendingReward r : retrieved) {
            actualNames.add(r.deliveryName());
        }
        
        assertThat(actualNames).isEqualTo(expectedNames);
    }
    
    /**
     * Feature: delivery-core, Property 19: Multi-Winner Reward Distribution
     * For any list of winners, reward distribution should process all winners
     * and each winner should receive the configured reward.
     * Validates: Requirement 6.5
     */
    @Property(tries = 100)
    void multiWinnerRewardDistribution(
            @ForAll("validWinners") List<Winner> winners,
            @ForAll("validRewardConfig") RewardConfig reward) {
        
        Assume.that(!winners.isEmpty());
        
        PendingRewardStore store = new PendingRewardStoreImpl();
        RewardServiceImpl service = new RewardServiceImpl(
            store, 
            new PlaceholderEngineImpl()
        );
        
        // Track which players received rewards
        Set<UUID> rewardedPlayers = new HashSet<>();
        Map<UUID, Integer> itemsGiven = new HashMap<>();
        
        // Create a resolver that marks all players as online
        RewardService.PlayerResolver resolver = new RewardService.PlayerResolver() {
            @Override
            public boolean isOnline(UUID uuid) {
                return true;
            }
            
            @Override
            public String getName(UUID uuid) {
                return "Player_" + uuid.toString().substring(0, 8);
            }
            
            @Override
            public boolean giveItem(UUID uuid, String item, int amount) {
                rewardedPlayers.add(uuid);
                itemsGiven.merge(uuid, amount, Integer::sum);
                return true;
            }
        };
        
        // Distribute rewards
        service.distributeRewards(
            winners, 
            reward, 
            "test_delivery",
            PlaceholderContext.empty(),
            resolver
        );
        
        // Verify all winners received rewards (for INVENTORY type)
        if (reward.type() == RewardType.INVENTORY && reward.item() != null 
                && !reward.item().isEmpty() && reward.itemAmount() > 0) {
            for (Winner winner : winners) {
                assertThat(rewardedPlayers).contains(winner.playerUuid());
                assertThat(itemsGiven.get(winner.playerUuid())).isEqualTo(reward.itemAmount());
            }
        }
    }
    
    /**
     * Feature: delivery-core, Property 19: Multi-Winner Reward Distribution (Offline Players)
     * For winners who are offline, rewards should be stored as pending.
     * Validates: Requirement 6.5
     */
    @Property(tries = 100)
    void offlineWinnersGetPendingRewards(
            @ForAll("validWinners") List<Winner> winners,
            @ForAll("validRewardConfig") RewardConfig reward) {
        
        Assume.that(!winners.isEmpty());
        
        PendingRewardStore store = new PendingRewardStoreImpl();
        RewardServiceImpl service = new RewardServiceImpl(
            store, 
            new PlaceholderEngineImpl()
        );
        
        // Create a resolver that marks all players as offline
        RewardService.PlayerResolver resolver = new RewardService.PlayerResolver() {
            @Override
            public boolean isOnline(UUID uuid) {
                return false;
            }
            
            @Override
            public String getName(UUID uuid) {
                return "Player_" + uuid.toString().substring(0, 8);
            }
            
            @Override
            public boolean giveItem(UUID uuid, String item, int amount) {
                return false;
            }
        };
        
        // Distribute rewards
        service.distributeRewards(
            winners, 
            reward, 
            "test_delivery",
            PlaceholderContext.empty(),
            resolver
        );
        
        // Verify all winners have pending rewards
        for (Winner winner : winners) {
            assertThat(store.hasPendingRewards(winner.playerUuid())).isTrue();
            
            List<PendingReward> pending = store.getRewards(winner.playerUuid());
            assertThat(pending).isNotEmpty();
            assertThat(pending.get(0).reward()).isEqualTo(reward);
        }
    }
    
    // ==================== Generators ====================
    
    @Provide
    Arbitrary<PendingReward> validPendingReward() {
        return Combinators.combine(
            Arbitraries.create(UUID::randomUUID),
            Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(20),
            validRewardConfig()
        ).as((uuid, deliveryName, reward) -> 
            PendingReward.create(uuid, deliveryName, reward));
    }
    
    @Provide
    Arbitrary<RewardConfig> validRewardConfig() {
        return Arbitraries.oneOf(
            // Inventory reward
            Combinators.combine(
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(30),
                Arbitraries.integers().between(1, 64)
            ).as(RewardConfig::inventory),
            // Command reward
            Arbitraries.strings()
                .alpha()
                .ofMinLength(1)
                .ofMaxLength(50)
                .list()
                .ofMinSize(1)
                .ofMaxSize(5)
                .map(RewardConfig::command)
        );
    }
    
    @Provide
    Arbitrary<List<RewardConfig>> validRewardConfigs() {
        return validRewardConfig().list().ofMinSize(1).ofMaxSize(10);
    }
    
    @Provide
    Arbitrary<List<Winner>> validWinners() {
        return Arbitraries.integers()
            .between(1, 10)
            .flatMap(count -> {
                // Generate a list of delivery counts first
                return Arbitraries.integers()
                    .between(1, 1000)
                    .list()
                    .ofSize(count)
                    .map(deliveryCounts -> {
                        List<Winner> winners = new ArrayList<>();
                        for (int i = 0; i < count; i++) {
                            winners.add(new Winner(
                                UUID.randomUUID(),
                                "Player" + (i + 1),
                                deliveryCounts.get(i),
                                i + 1
                            ));
                        }
                        return winners;
                    });
            });
    }
}
