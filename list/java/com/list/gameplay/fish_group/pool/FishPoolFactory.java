package com.list.gameplay.fish_group.pool;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

public final class FishPoolFactory {
    private static final Map<ResourceLocation, FishPoolDefinition> DEFINITIONS = new LinkedHashMap<>();

    //混合鱼群 海洋
    public static final FishPoolDefinition OCEAN_FISH_POOL = ocean("ocean_fish_pool")
            .minFishCount(1)
            .maxFishCount(8)
            .biomes("minecraft:ocean")
            .output(FishPoolLootEntryDefinition.tag("list:fish_pool/fish_ocean", 100, 1, 5))
            .register();

    //混合鱼群 河流
    public static final FishPoolDefinition RIVER_FISH_POOL = river("river_fish_pool")
            .minFishCount(1)
            .maxFishCount(6)
            .biomes("minecraft:river")
            .output(FishPoolLootEntryDefinition.tag("list:fish_pool/fish_river", 100, 1, 3))
            .register();
    //鲑鱼群
    public static final FishPoolDefinition SALMON_FISH_POOL = river("salmon_fish_pool")
            .minFishCount(1)
            .maxFishCount(6)
            .fishKing((String) null)
            .weather(null)
            .time(null)
            .biomes("minecraft:river")
            .output(FishPoolLootEntryDefinition.item("minecraft:salmon", 100, 1, 1))
            .register();

    //河豚群
    public static final FishPoolDefinition PUFFERFISH_FISH_POOL = ocean("pufferfish_fish_pool")
            .minFishCount(1)
            .maxFishCount(6)
            .fishKing((String) null)
            .weather(null)
            .time(null)
            .biomes("minecraft:ocean")
            .output(FishPoolLootEntryDefinition.item("minecraft:pufferfish", 100, 1, 1))
            .register();

    //鳕鱼群
    public static final FishPoolDefinition COD_FISH_POOL = ocean("cod_fish_pool")
            .minFishCount(1)
            .maxFishCount(6)
            .fishKing((String) null)
            .weather(null)
            .time(null)
            .biomes("minecraft:ocean")
            .output(FishPoolLootEntryDefinition.item("minecraft:cod", 100, 1, 1))
            .register();
    //蓝鳃太阳鱼
    public static final FishPoolDefinition BLUEGILL_FISH_POOL = river("bluegill_fish_pool")
            .minFishCount(1)
            .maxFishCount(6)
            .fishKing((String) null)
            .weather(null)
            .time(null)
            .biomes("minecraft:river")
            .output(FishPoolLootEntryDefinition.item("aquaculture:bluegill", 100, 1, 1))
            .register();

    //虹鳟鱼
    public static final FishPoolDefinition RAINBOW_TROUT_FISH_POOL = river("rainbow_trout_fish_pool")
            .minFishCount(1)
            .maxFishCount(6)
            .fishKing((String) null)
            .weather(null)
            .time(null)
            .biomes("minecraft:river")
            .output(FishPoolLootEntryDefinition.item("aquaculture:rainbow_trout", 50, 1, 1))
            .output(FishPoolLootEntryDefinition.item("aquaculture:brown_trout", 50, 1, 1))
            .register();

    //鲤鱼
    public static final FishPoolDefinition CARP_FISH_POOL = river("carp_fish_pool")
            .minFishCount(1)
            .maxFishCount(6)
            .fishKing((String) null)
            .weather(null)
            .time(null)
            .biomes("minecraft:river")
            .output(FishPoolLootEntryDefinition.item("aquaculture:carp", 50, 1, 1))
            //.output(FishPoolLootEntryDefinition.item("aquaculture:tambaqui", 50, 1, 1))
            .register();

    //龙虾群
    public static final FishPoolDefinition LOBSTER_FISH_POOL = ocean("lobster_fish_pool")
            .minFishCount(1)
            .maxFishCount(5)
            .fishKing((String) null)
            .weather(null)
            .time(null)
            .biomes("minecraft:ocean")
            .output(FishPoolLootEntryDefinition.item("crabbersdelight:clawster", 100, 1, 1))
            .register();

    //鱿鱼群
    public static final FishPoolDefinition SQUID_FISH_POOL = ocean("squid_fish_pool")
            .minFishCount(1)
            .maxFishCount(8)
            .fishKing((String) null)
            .weather(null)
            .time(null)
            .biomes("minecraft:ocean")
            .output(FishPoolLootEntryDefinition.item("culturaldelights:squid", 100, 1, 1))
            .output(FishPoolLootEntryDefinition.item("culturaldelights:glow_squid", 100, 1, 1))
            .register();

    //鲶鱼群
    public static final FishPoolDefinition CATFISH_FISH_POOL = river("catfish_fish_pool")
            .minFishCount(1)
            .maxFishCount(8)
            .fishKing((String) null)
            .weather(null)
            .time(null)
            .biomes("minecraft:river")
            .output(FishPoolLootEntryDefinition.item("aquaculture:catfish", 100, 1, 1))
            .register();

    //条纹鲈鱼群
    public static final FishPoolDefinition STRIPED_BASS_FISH_POOL = river("striped_bass_fish_pool")
            .minFishCount(1)
            .maxFishCount(5)
            .fishKing((String) null)
            .weather(null)
            .time(null)
            .biomes("minecraft:river")
            .output(FishPoolLootEntryDefinition.item("list:striped_bass", 100, 1, 1))
            .output(FishPoolLootEntryDefinition.item("aquaculture:perch", 100, 1, 1))
            .register();

    //红石斑鱼群
    public static final FishPoolDefinition REDSTONE_BASS_FISH_POOL = ocean("redstone_bass_fish_pool")
            .minFishCount(1)
            .maxFishCount(6)
            .fishKing((String) null)
            .weather(null)
            .time(null)
            .biomes("minecraft:ocean")
            .output(FishPoolLootEntryDefinition.item("list:red_grouper", 100, 1, 1))
            .output(FishPoolLootEntryDefinition.item("aquaculture:red_grouper", 100, 1, 1))
            .register();

    //河虾群
    public static final FishPoolDefinition RIVER_SHRIMP_FISH_POOL = river("river_shrimp_fish_pool")
            .minFishCount(1)
            .maxFishCount(4)
            .fishKing((String) null)
            .weather(null)
            .time(null)
            .biomes("minecraft:river")
            .output(FishPoolLootEntryDefinition.item("list:river_shrimp", 100, 1, 1))
            .register();

    //河蟹群
    public static final FishPoolDefinition RIVER_CRAB_FISH_POOL = river("river_crab_fish_pool")
            .minFishCount(1)
            .maxFishCount(4)
            .fishKing((String) null)
            .weather(null)
            .time(null)
            .biomes("minecraft:river")
            .output(FishPoolLootEntryDefinition.item("list:river_crab", 100, 1, 1))
            .register();
            
    //梭子蟹群
    public static final FishPoolDefinition SWIMMING_CRAB_FISH_POOL = ocean("swimming_crab_fish_pool")
            .minFishCount(1)
            .maxFishCount(4)
            .fishKing((String) null)
            .weather(null)
            .time(null)
            .biomes("minecraft:ocean")
            .output(FishPoolLootEntryDefinition.item("list:swimming_crab", 100, 1, 1))
            .register();

    //大黄鱼群
    public static final FishPoolDefinition LARGE_YELLOW_CROAKER_FISH_POOL = ocean("large_yellow_croaker_fish_pool")
            .minFishCount(1)
            .maxFishCount(4)
            .fishKing((String) null)
            .weather(null)
            .time(null)
            .biomes("minecraft:ocean")
            .output(FishPoolLootEntryDefinition.item("list:large_yellow_croaker", 100, 1, 1))
            .register();

    //鹦嘴鱼群
    public static final FishPoolDefinition PARROTFISH_FISH_POOL = ocean("parrotfish_fish_pool")
            .minFishCount(1)
            .maxFishCount(4)
            .fishKing((String) null)
            .weather(null)
            .time(null)
            .biomes("minecraft:ocean")
            .output(FishPoolLootEntryDefinition.item("list:parrotfish", 100, 1, 1))
            .register();

    //银鲳鱼群
    public static final FishPoolDefinition SILVER_POMFRET_FISH_POOL = ocean("silver_pomfret_fish_pool")
            .minFishCount(1)
            .maxFishCount(4)
            .fishKing((String) null)
            .weather(null)
            .time(null)
            .biomes("minecraft:ocean")
            .output(FishPoolLootEntryDefinition.item("list:silver_pomfret", 100, 1, 1))
            .register();

    //苏眉鱼群
    public static final FishPoolDefinition CAMOUFLAGE_GROUPER_FISH_POOL = ocean("camouflage_grouper_fish_pool")
            .minFishCount(1)
            .maxFishCount(6)
            .fishKing((String) null)
            .weather(null)
            .time(null)
            .biomes("minecraft:ocean")
            .output(FishPoolLootEntryDefinition.item("list:camouflage_grouper", 100, 1, 1))
            .register();

    //多宝鱼群
    public static final FishPoolDefinition TURBOT_FISH_POOL = ocean("turbot_fish_pool")
            .minFishCount(1)
            .maxFishCount(4)
            .fishKing((String) null)
            .weather(null)
            .time(null)
            .biomes("minecraft:ocean")
            .output(FishPoolLootEntryDefinition.item("list:turbot", 100, 1, 1))
            .register();

    //比目鱼群
    public static final FishPoolDefinition FLUKE_FISH_POOL = ocean("fluke_fish_pool")
            .minFishCount(1)
            .maxFishCount(4)
            .fishKing((String) null)
            .weather(null)
            .time(null)
            .biomes("minecraft:ocean")
            .output(FishPoolLootEntryDefinition.item("list:fluke", 100, 1, 1))
            .register();

   //鳜鱼群
    public static final FishPoolDefinition SINIPERCA_CHUATSI_FISH_POOL = river("siniperca_chuatsi_fish_pool")
            .minFishCount(1)
            .maxFishCount(4)
            .fishKing((String) null)
            .weather(null)
            .time(null)
            .biomes("minecraft:river")
            .output(FishPoolLootEntryDefinition.item("list:siniperca_chuatsi", 100, 1, 1))
            .register();

    //鲅鱼群
    public static final FishPoolDefinition SPANISH_MACKEREL_FISH_POOL = ocean("spanish_mackerel_fish_pool")
            .minFishCount(1)
            .maxFishCount(4)
            .fishKing((String) null)
            .weather(null)
            .time(null)
            .biomes("minecraft:ocean")
            .output(FishPoolLootEntryDefinition.item("list:spanish_mackerel", 100, 1, 1))
            .register();

    //草鱼群
    public static final FishPoolDefinition GRASS_CARP_FISH_POOL = river("grass_carp_fish_pool")
            .minFishCount(1)
            .maxFishCount(4)
            .fishKing((String) null)
            .weather(null)
            .time(null)
            .biomes("minecraft:river")
            .output(FishPoolLootEntryDefinition.item("list:grass_carp", 100, 1, 1))
            .register();

    //云斑海猪鱼群
    public static final FishPoolDefinition CHECKERBOARD_WRASSE_FISH_POOL = ocean("checkerboard_wrasse_fish_pool")
            .minFishCount(1)
            .maxFishCount(4)
            .fishKing((String) null)
            .weather(null)
            .time(null)
            .biomes("minecraft:ocean")
            .output(FishPoolLootEntryDefinition.item("list:checkerboard_wrasse", 100, 1, 1))
            .register();

    //鸡鱼群
    public static final FishPoolDefinition THREADFIN_BREAM_FISH_POOL = ocean("threadfin_bream_fish_pool")
            .minFishCount(1)
            .maxFishCount(4)
            .fishKing((String) null)
            .weather(null)
            .time(null)
            .biomes("minecraft:ocean")
            .output(FishPoolLootEntryDefinition.item("list:threadfin_bream", 100, 1, 1))
            .register();

/*
        //群
    public static final FishPoolDefinition  = river("")
            .minFishCount(1)
            .maxFishCount(4)
            .fishKing((String) null)
            .weather(null)
            .time(null)
            .biomes("minecraft:river")
            .output(FishPoolLootEntryDefinition.item("", 100, 1, 1))
            .register();

        //群
    public static final FishPoolDefinition  = ocean("")
            .minFishCount(1)
            .maxFishCount(4)
            .fishKing((String) null)
            .weather(null)
            .time(null)
            .biomes("minecraft:ocean")
            .output(FishPoolLootEntryDefinition.item("", 100, 1, 1))
            .register();
*/
    private FishPoolFactory() {
    }

    public static FishPoolBuilder river(String path) {
        return FishPoolBuilder.river(path);
    }

    public static FishPoolBuilder ocean(String path) {
        return FishPoolBuilder.ocean(path);
    }

    public static FishPoolBuilder builder(String path, FishPoolDefinition.Environment environment) {
        return FishPoolBuilder.of(path, environment);
    }

    public static FishPoolDefinition river(
            String path,
            int maxFishCount,
            @Nullable String fishKing,
            @Nullable FishPoolDefinition.WeatherRequirement weatherRequirement,
            FishPoolLootEntryDefinition... outputs
    ) {
        return river(path)
                .maxFishCount(maxFishCount)
                .fishKing(fishKing)
                .weather(weatherRequirement)
                .outputs(outputs)
                .register();
    }

    public static FishPoolDefinition river(
            String path,
            int maxFishCount,
            @Nullable String fishKing,
            @Nullable FishPoolDefinition.WeatherRequirement weatherRequirement,
            @Nullable FishPoolDefinition.TimeRequirement timeRequirement,
            FishPoolLootEntryDefinition... outputs
    ) {
        return river(path)
                .maxFishCount(maxFishCount)
                .fishKing(fishKing)
                .weather(weatherRequirement)
                .time(timeRequirement)
                .outputs(outputs)
                .register();
    }

    public static FishPoolDefinition river(
            String path,
            int maxFishCount,
            @Nullable String fishKing,
            @Nullable String biome,
            @Nullable FishPoolDefinition.WeatherRequirement weatherRequirement,
            FishPoolLootEntryDefinition... outputs
    ) {
        return river(path)
                .maxFishCount(maxFishCount)
                .fishKing(fishKing)
                .biome(biome)
                .weather(weatherRequirement)
                .outputs(outputs)
                .register();
    }

    public static FishPoolDefinition river(
            String path,
            int maxFishCount,
            @Nullable String fishKing,
            @Nullable String biome,
            @Nullable FishPoolDefinition.WeatherRequirement weatherRequirement,
            @Nullable FishPoolDefinition.TimeRequirement timeRequirement,
            FishPoolLootEntryDefinition... outputs
    ) {
        return river(path)
                .maxFishCount(maxFishCount)
                .fishKing(fishKing)
                .biome(biome)
                .weather(weatherRequirement)
                .time(timeRequirement)
                .outputs(outputs)
                .register();
    }

    public static FishPoolDefinition ocean(
            String path,
            int maxFishCount,
            @Nullable String fishKing,
            @Nullable FishPoolDefinition.WeatherRequirement weatherRequirement,
            FishPoolLootEntryDefinition... outputs
    ) {
        return ocean(path)
                .maxFishCount(maxFishCount)
                .fishKing(fishKing)
                .weather(weatherRequirement)
                .outputs(outputs)
                .register();
    }

    public static FishPoolDefinition ocean(
            String path,
            int maxFishCount,
            @Nullable String fishKing,
            @Nullable FishPoolDefinition.WeatherRequirement weatherRequirement,
            @Nullable FishPoolDefinition.TimeRequirement timeRequirement,
            FishPoolLootEntryDefinition... outputs
    ) {
        return ocean(path)
                .maxFishCount(maxFishCount)
                .fishKing(fishKing)
                .weather(weatherRequirement)
                .time(timeRequirement)
                .outputs(outputs)
                .register();
    }

    public static FishPoolDefinition ocean(
            String path,
            int maxFishCount,
            @Nullable String fishKing,
            @Nullable String biome,
            @Nullable FishPoolDefinition.WeatherRequirement weatherRequirement,
            FishPoolLootEntryDefinition... outputs
    ) {
        return ocean(path)
                .maxFishCount(maxFishCount)
                .fishKing(fishKing)
                .biome(biome)
                .weather(weatherRequirement)
                .outputs(outputs)
                .register();
    }

    public static FishPoolDefinition ocean(
            String path,
            int maxFishCount,
            @Nullable String fishKing,
            @Nullable String biome,
            @Nullable FishPoolDefinition.WeatherRequirement weatherRequirement,
            @Nullable FishPoolDefinition.TimeRequirement timeRequirement,
            FishPoolLootEntryDefinition... outputs
    ) {
        return ocean(path)
                .maxFishCount(maxFishCount)
                .fishKing(fishKing)
                .biome(biome)
                .weather(weatherRequirement)
                .time(timeRequirement)
                .outputs(outputs)
                .register();
    }

    public static FishPoolDefinition register(
            String path,
            FishPoolDefinition.Environment environment,
            int maxFishCount,
            @Nullable String fishKing,
            @Nullable FishPoolDefinition.WeatherRequirement weatherRequirement,
            FishPoolLootEntryDefinition... outputs
    ) {
        return builder(path, environment)
                .maxFishCount(maxFishCount)
                .fishKing(fishKing)
                .weather(weatherRequirement)
                .outputs(outputs)
                .register();
    }

    public static FishPoolDefinition register(
            String path,
            FishPoolDefinition.Environment environment,
            int maxFishCount,
            @Nullable String fishKing,
            @Nullable FishPoolDefinition.WeatherRequirement weatherRequirement,
            @Nullable FishPoolDefinition.TimeRequirement timeRequirement,
            FishPoolLootEntryDefinition... outputs
    ) {
        return builder(path, environment)
                .maxFishCount(maxFishCount)
                .fishKing(fishKing)
                .weather(weatherRequirement)
                .time(timeRequirement)
                .outputs(outputs)
                .register();
    }

    public static FishPoolDefinition register(
            String path,
            FishPoolDefinition.Environment environment,
            int maxFishCount,
            @Nullable String fishKing,
            @Nullable String biome,
            @Nullable FishPoolDefinition.WeatherRequirement weatherRequirement,
            FishPoolLootEntryDefinition... outputs
    ) {
        return builder(path, environment)
                .maxFishCount(maxFishCount)
                .fishKing(fishKing)
                .biome(biome)
                .weather(weatherRequirement)
                .outputs(outputs)
                .register();
    }

    public static FishPoolDefinition register(
            String path,
            FishPoolDefinition.Environment environment,
            int maxFishCount,
            @Nullable String fishKing,
            @Nullable String biome,
            @Nullable FishPoolDefinition.WeatherRequirement weatherRequirement,
            @Nullable FishPoolDefinition.TimeRequirement timeRequirement,
            FishPoolLootEntryDefinition... outputs
    ) {
        return builder(path, environment)
                .maxFishCount(maxFishCount)
                .fishKing(fishKing)
                .biome(biome)
                .weather(weatherRequirement)
                .time(timeRequirement)
                .outputs(outputs)
                .register();
    }

    public static FishPoolDefinition register(
            ResourceLocation id,
            FishPoolDefinition.Environment environment,
            int minFishCount,
            int maxFishCount,
            @Nullable ResourceLocation fishKing,
            List<ResourceLocation> biomes,
            @Nullable FishPoolDefinition.WeatherRequirement weatherRequirement,
            @Nullable FishPoolDefinition.TimeRequirement timeRequirement,
            FishPoolLootEntryDefinition... outputs
    ) {
        FishPoolDefinition definition = new FishPoolDefinition(id, environment, minFishCount, maxFishCount, fishKing, biomes, weatherRequirement, timeRequirement, List.of(outputs));
        FishPoolDefinition existing = DEFINITIONS.putIfAbsent(id, definition);
        if (existing != null) {
            throw new IllegalStateException("Duplicate fish pool definition: " + id);
        }
        return definition;
    }

    public static Optional<FishPoolDefinition> get(ResourceLocation id) {
        return Optional.ofNullable(DEFINITIONS.get(id));
    }

    public static List<FishPoolDefinition> getAll() {
        return List.copyOf(DEFINITIONS.values());
    }

    public static FishPoolDefinition getOrDefault(ResourceLocation id, FishPoolDefinition.Environment environment) {
        FishPoolDefinition definition = get(id).orElseGet(() -> getDefault(environment));
        return FishPoolLootManager.INSTANCE.resolveDefinition(definition);
    }

    public static FishPoolDefinition getDefault(FishPoolDefinition.Environment environment) {
        return DEFINITIONS.values()
                .stream()
                .filter(definition -> definition.environment() == environment)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No fish pool definition registered for environment: " + environment));
    }

    public static List<FishPoolDefinition> getAvailable(ServerLevel level, BlockPos pos, FishPoolDefinition.Environment environment) {
        return DEFINITIONS.values()
                .stream()
                .filter(definition -> definition.environment() == environment)
                .map(FishPoolLootManager.INSTANCE::resolveDefinition)
                .filter(definition -> definition.matchesSpawn(level, pos))
                .toList();
    }
}



