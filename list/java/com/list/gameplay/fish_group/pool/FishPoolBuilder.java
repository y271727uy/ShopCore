package com.list.gameplay.fish_group.pool;

import com.list.ListMod;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public final class FishPoolBuilder {
    private final ResourceLocation id;
    private final FishPoolDefinition.Environment environment;
    @Nullable
    private Integer minFishCount;
    private int maxFishCount = 5;
    @Nullable
    private ResourceLocation fishKing;
    private final List<ResourceLocation> biomes = new ArrayList<>();
    @Nullable
    private FishPoolDefinition.WeatherRequirement weatherRequirement;
    @Nullable
    private FishPoolDefinition.TimeRequirement timeRequirement;
    private final List<FishPoolLootEntryDefinition> outputs = new ArrayList<>();

    private FishPoolBuilder(ResourceLocation id, FishPoolDefinition.Environment environment) {
        this.id = id;
        this.environment = environment;
    }

    public static FishPoolBuilder river(String path) {
        return of(ListMod.rl(path), FishPoolDefinition.Environment.RIVER);
    }

    public static FishPoolBuilder ocean(String path) {
        return of(ListMod.rl(path), FishPoolDefinition.Environment.OCEAN);
    }

    public static FishPoolBuilder of(String path, FishPoolDefinition.Environment environment) {
        return of(ListMod.rl(path), environment);
    }

    public static FishPoolBuilder of(ResourceLocation id, FishPoolDefinition.Environment environment) {
        return new FishPoolBuilder(id, environment);
    }

    public FishPoolBuilder minFishCount(int minFishCount) {
        this.minFishCount = minFishCount;
        return this;
    }

    public FishPoolBuilder maxFishCount(int maxFishCount) {
        this.maxFishCount = maxFishCount;
        return this;
    }

    public FishPoolBuilder fishKing(@Nullable String fishKing) {
        this.fishKing = parseNullable(fishKing, "fishKing");
        return this;
    }

    public FishPoolBuilder fishKing(@Nullable ResourceLocation fishKing) {
        this.fishKing = fishKing;
        return this;
    }

    public FishPoolBuilder biome(@Nullable String biome) {
        return this.biomes(biome == null ? new String[0] : new String[]{biome});
    }

    public FishPoolBuilder biome(@Nullable ResourceLocation biome) {
        return this.biomes(biome == null ? new ResourceLocation[0] : new ResourceLocation[]{biome});
    }

    public FishPoolBuilder biomes(String... biomes) {
        this.biomes.clear();
        for (String biome : biomes) {
            ResourceLocation parsed = parseNullable(biome, "biome");
            if (parsed != null) {
                this.biomes.add(parsed);
            }
        }
        return this;
    }

    public FishPoolBuilder biomes(ResourceLocation... biomes) {
        this.biomes.clear();
        for (ResourceLocation biome : biomes) {
            if (biome != null) {
                this.biomes.add(biome);
            }
        }
        return this;
    }

    public FishPoolBuilder weather(@Nullable FishPoolDefinition.WeatherRequirement weatherRequirement) {
        this.weatherRequirement = weatherRequirement;
        return this;
    }

    public FishPoolBuilder time(@Nullable FishPoolDefinition.TimeRequirement timeRequirement) {
        this.timeRequirement = timeRequirement;
        return this;
    }

    public FishPoolBuilder output(FishPoolLootEntryDefinition output) {
        this.outputs.add(output);
        return this;
    }

    public FishPoolBuilder outputs(FishPoolLootEntryDefinition... outputs) {
        java.util.Collections.addAll(this.outputs, outputs);
        return this;
    }

    public FishPoolDefinition register() {
        int resolvedMinFishCount = this.minFishCount == null ? this.maxFishCount : this.minFishCount;
        return FishPoolFactory.register(this.id, this.environment, resolvedMinFishCount, this.maxFishCount, this.fishKing, List.copyOf(this.biomes), this.weatherRequirement, this.timeRequirement, this.outputs.toArray(FishPoolLootEntryDefinition[]::new));
    }

    @Nullable
    private static ResourceLocation parseNullable(@Nullable String rawId, String fieldName) {
        if (rawId == null || rawId.isBlank()) {
            return null;
        }
        ResourceLocation parsed = ResourceLocation.tryParse(rawId);
        if (parsed == null) {
            throw new IllegalArgumentException("Invalid fish pool " + fieldName + " id: " + rawId);
        }
        return parsed;
    }
}

