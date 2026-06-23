package com.y271727uy.shopcore.economic.algorithm.micromachinelearning.weight;

import java.util.Map;

/**
 * One independent item usage weight table per player.
 *
 * @param <P> player identity type, such as UUID
 * @param <K> item or strategy identity type
 */
public interface PlayerWeightSpace<P, K> {
    void record(P playerKey, K itemKey, double amount, long nowTick);

    WeightSnapshot snapshot(P playerKey, K itemKey, long nowTick);

    Map<K, WeightSnapshot> snapshots(P playerKey, long nowTick);

    void clear(P playerKey);

    void clear(P playerKey, K itemKey);
}
