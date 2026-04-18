package com.list.gameplay.fish_group.pool;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

import javax.annotation.Nullable;
import java.util.List;

public record FishPoolDefinition(
        ResourceLocation id,
        Environment environment,
        int minFishCount,
        int maxFishCount,
        @Nullable ResourceLocation fishKing,
        List<ResourceLocation> biomes,
        WeatherRequirement weatherRequirement,
        TimeRequirement timeRequirement,
        List<FishPoolLootEntryDefinition> outputs
) {
    public FishPoolDefinition {
        minFishCount = Math.max(1, minFishCount);
        maxFishCount = Math.max(minFishCount, maxFishCount);
        biomes = List.copyOf(biomes);
        weatherRequirement = weatherRequirement == null ? WeatherRequirement.ANY : weatherRequirement;
        timeRequirement = timeRequirement == null ? TimeRequirement.ANY : timeRequirement;
        outputs = List.copyOf(outputs);
        if (outputs.isEmpty()) {
            throw new IllegalArgumentException("Fish pool definition must contain at least one output: " + id);
        }
    }

    public boolean matchesCurrentConditions(ServerLevel level) {
        return this.weatherRequirement.matches(level) && this.timeRequirement.matches(level);
    }

    public int rollFishCount(RandomSource random) {
        return this.minFishCount == this.maxFishCount
                ? this.maxFishCount
                : Mth.nextInt(random, this.minFishCount, this.maxFishCount);
    }

    public boolean matchesSpawn(ServerLevel level, BlockPos pos) {
        return this.matchesCurrentConditions(level) && this.matchesBiome(level, pos);
    }

    public boolean matchesFishing(ServerLevel level) {
        return this.matchesCurrentConditions(level);
    }

    private boolean matchesBiome(ServerLevel level, BlockPos pos) {
        if (this.biomes.isEmpty()) {
            return true;
        }

        return level.getBiome(pos)
                .unwrapKey()
                .map(resourceKey -> this.biomes.contains(resourceKey.location()))
                .orElse(false);
    }

    public enum Environment {
        OCEAN,
        RIVER
    }

    public enum WeatherRequirement {
        ANY {
            @Override
            public boolean matches(ServerLevel level) {
                return true;
            }
        },
        CLEAR {
            @Override
            public boolean matches(ServerLevel level) {
                return !level.isRaining() && !level.isThundering();
            }
        },
        RAIN {
            @Override
            public boolean matches(ServerLevel level) {
                return level.isRaining() && !level.isThundering();
            }
        },
        THUNDER {
            @Override
            public boolean matches(ServerLevel level) {
                return level.isThundering();
            }
        };

        public abstract boolean matches(ServerLevel level);
    }

    public enum TimeRequirement {
        ANY("null") {
            @Override
            public boolean matches(ServerLevel level) {
                return true;
            }
        },
        DAY("day") {
            @Override
            public boolean matches(ServerLevel level) {
                return level.isDay();
            }
        },
        NIGHT("night") {
            @Override
            public boolean matches(ServerLevel level) {
                return level.isNight();
            }
        };

        private final String serializedName;

        TimeRequirement(String serializedName) {
            this.serializedName = serializedName;
        }

        public String serializedName() {
            return this.serializedName;
        }

        public abstract boolean matches(ServerLevel level);
    }
}


