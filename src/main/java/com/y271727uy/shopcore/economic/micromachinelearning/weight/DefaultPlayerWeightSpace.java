package com.y271727uy.shopcore.economic.micromachinelearning.weight;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * In-memory player-scoped item weight store with lazy linear decay.
 */
public final class DefaultPlayerWeightSpace<P, K> implements PlayerWeightSpace<P, K> {
    private final WeightConfig config;
    private final Map<P, PlayerTable<K>> playerTables = new HashMap<>();
    private final LinkedHashMap<P, Boolean> playerAccessOrder = new LinkedHashMap<>();

    public DefaultPlayerWeightSpace() {
        this(WeightConfig.DEFAULT);
    }

    public DefaultPlayerWeightSpace(WeightConfig config) {
        this.config = Objects.requireNonNull(config, "config");
    }

    @Override
    public void record(P playerKey, K itemKey, double amount, long nowTick) {
        Objects.requireNonNull(playerKey, "playerKey");
        Objects.requireNonNull(itemKey, "itemKey");
        requireFiniteNonNegative(amount, "amount");
        if (amount == 0.0D) {
            return;
        }

        PlayerTable<K> table = playerTables.get(playerKey);
        if (table == null) {
            table = new PlayerTable<>();
            playerTables.put(playerKey, table);
        }
        touchPlayer(playerKey);
        Entry entry = table.entries.computeIfAbsent(itemKey, ignored -> new Entry(0.0D, nowTick));
        entry.weight = clamp(decay(entry, nowTick) + amount, 0.0D, config.maxItemWeight());
        entry.lastTick = nowTick;
        table.touch(itemKey);
        evictOverflow(table);
        evictPlayerOverflow();
    }

    @Override
    public WeightSnapshot snapshot(P playerKey, K itemKey, long nowTick) {
        Objects.requireNonNull(playerKey, "playerKey");
        Objects.requireNonNull(itemKey, "itemKey");

        PlayerTable<K> table = playerTables.get(playerKey);
        if (table == null) {
            return WeightSnapshot.ZERO;
        }
        touchPlayer(playerKey);

        Entry entry = table.entries.get(itemKey);
        if (entry == null) {
            return WeightSnapshot.ZERO;
        }

        double weight = decay(entry, nowTick);
        if (weight <= 0.0D) {
            table.entries.remove(itemKey);
            table.accessOrder.remove(itemKey);
            removePlayerIfEmpty(playerKey, table);
            return WeightSnapshot.ZERO;
        }

        entry.weight = weight;
        entry.lastTick = nowTick;
        table.touch(itemKey);
        return toSnapshot(weight);
    }

    @Override
    public Map<K, WeightSnapshot> snapshots(P playerKey, long nowTick) {
        Objects.requireNonNull(playerKey, "playerKey");

        PlayerTable<K> table = playerTables.get(playerKey);
        if (table == null) {
            return Map.of();
        }
        touchPlayer(playerKey);

        Map<K, WeightSnapshot> result = new LinkedHashMap<>();
        Iterator<Map.Entry<K, Entry>> iterator = table.entries.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<K, Entry> item = iterator.next();
            double weight = decay(item.getValue(), nowTick);
            if (weight <= 0.0D) {
                iterator.remove();
                table.accessOrder.remove(item.getKey());
                continue;
            }

            item.getValue().weight = weight;
            item.getValue().lastTick = nowTick;
            result.put(item.getKey(), toSnapshot(weight));
        }

        removePlayerIfEmpty(playerKey, table);
        return Map.copyOf(result);
    }

    @Override
    public void clear(P playerKey) {
        Objects.requireNonNull(playerKey, "playerKey");
        playerTables.remove(playerKey);
        playerAccessOrder.remove(playerKey);
    }

    @Override
    public void clear(P playerKey, K itemKey) {
        Objects.requireNonNull(playerKey, "playerKey");
        Objects.requireNonNull(itemKey, "itemKey");

        PlayerTable<K> table = playerTables.get(playerKey);
        if (table == null) {
            return;
        }

        table.entries.remove(itemKey);
        table.accessOrder.remove(itemKey);
        removePlayerIfEmpty(playerKey, table);
    }

    private WeightSnapshot toSnapshot(double weight) {
        double pressure = weight / (weight + config.pressureResistance());
        double penaltyMultiplier = 1.0D - pressure * config.maxPenalty();
        return new WeightSnapshot(weight, pressure, penaltyMultiplier);
    }

    private double decay(Entry entry, long nowTick) {
        long elapsed = Math.max(0L, nowTick - entry.lastTick);
        if (elapsed == 0L || config.decayPerTick() == 0.0D) {
            return entry.weight;
        }
        return Math.max(0.0D, entry.weight - elapsed * config.decayPerTick());
    }

    private void evictOverflow(PlayerTable<K> table) {
        while (table.entries.size() > config.maxItemsPerPlayer()) {
            Iterator<K> iterator = table.accessOrder.keySet().iterator();
            if (!iterator.hasNext()) {
                return;
            }
            K eldest = iterator.next();
            iterator.remove();
            table.entries.remove(eldest);
        }
    }

    private void evictPlayerOverflow() {
        while (playerTables.size() > config.maxPlayers()) {
            Iterator<P> iterator = playerAccessOrder.keySet().iterator();
            if (!iterator.hasNext()) {
                return;
            }
            P eldest = iterator.next();
            iterator.remove();
            playerTables.remove(eldest);
        }
    }

    private void removePlayerIfEmpty(P playerKey, PlayerTable<K> table) {
        if (table.entries.isEmpty()) {
            playerTables.remove(playerKey);
            playerAccessOrder.remove(playerKey);
        }
    }

    private void touchPlayer(P playerKey) {
        playerAccessOrder.remove(playerKey);
        playerAccessOrder.put(playerKey, Boolean.TRUE);
    }

    private static void requireFiniteNonNegative(double value, String name) {
        if (!Double.isFinite(value) || value < 0.0D) {
            throw new IllegalArgumentException(name + " must be a finite non-negative value");
        }
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static final class PlayerTable<K> {
        private final Map<K, Entry> entries = new HashMap<>();
        private final LinkedHashMap<K, Boolean> accessOrder = new LinkedHashMap<>();

        private void touch(K itemKey) {
            accessOrder.remove(itemKey);
            accessOrder.put(itemKey, Boolean.TRUE);
        }
    }

    private static final class Entry {
        private double weight;
        private long lastTick;

        private Entry(double weight, long lastTick) {
            this.weight = weight;
            this.lastTick = lastTick;
        }
    }
}
