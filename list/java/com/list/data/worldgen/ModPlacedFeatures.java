package com.list.data.worldgen;

import com.list.ListMod;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.*;
import net.minecraft.world.level.material.Fluids;

import java.util.List;

public class ModPlacedFeatures {
    public static final ResourceKey<PlacedFeature> DISK_BURROWED_SAND = create("disk_burrowed_sand");
    public static final ResourceKey<PlacedFeature> DISK_BURROWED_GRAVEL = create("disk_burrowed_gravel");
    public static final ResourceKey<PlacedFeature> DISK_BURROWED_CLAY = create("disk_burrowed_clay");
    public static final ResourceKey<PlacedFeature> DISK_BURROWED_DIRT = create("disk_burrowed_dirt");
    public static final ResourceKey<PlacedFeature> DISK_BURROWED_MUD = create("disk_burrowed_mud");

    public static void bootstrap(BootstapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);
        register(
                context,
                DISK_BURROWED_SAND,
                configuredFeatures.getOrThrow(ModConfiguredFeatures.DISK_BURROWED_SAND),
                List.of(
                        InSquarePlacement.spread(),
                        PlacementUtils.HEIGHTMAP_OCEAN_FLOOR,
                        BiomeFilter.biome()
                )
        );

        register(
                context,
                DISK_BURROWED_GRAVEL,
                configuredFeatures.getOrThrow(ModConfiguredFeatures.DISK_BURROWED_GRAVEL),
                List.of(
                        RarityFilter.onAverageOnceEvery(3),
                        InSquarePlacement.spread(),
                        PlacementUtils.HEIGHTMAP_OCEAN_FLOOR,
                        BiomeFilter.biome()
                )
        );

        register(
                context,
                DISK_BURROWED_CLAY,
                configuredFeatures.getOrThrow(ModConfiguredFeatures.DISK_BURROWED_CLAY),
                List.of(
                        InSquarePlacement.spread(),
                        PlacementUtils.HEIGHTMAP_OCEAN_FLOOR,
                        BiomeFilter.biome()
                )
        );

        register(
                context,
                DISK_BURROWED_DIRT,
                configuredFeatures.getOrThrow(ModConfiguredFeatures.DISK_BURROWED_DIRT),
                List.of(
                        InSquarePlacement.spread(),
                        PlacementUtils.HEIGHTMAP_OCEAN_FLOOR,
                        BlockPredicateFilter.forPredicate(BlockPredicate.matchesFluids(Direction.UP.getNormal(), Fluids.WATER)),
                        BiomeFilter.biome()
                )
        );

        register(
                context,
                DISK_BURROWED_MUD,
                configuredFeatures.getOrThrow(ModConfiguredFeatures.DISK_BURROWED_MUD),
                List.of(
                        InSquarePlacement.spread(),
                        PlacementUtils.HEIGHTMAP_OCEAN_FLOOR,
                        BlockPredicateFilter.forPredicate(BlockPredicate.alwaysTrue()),
                        BiomeFilter.biome()
                )
        );
    }

    private static ResourceKey<PlacedFeature> create(String key) {
        return ResourceKey.create(Registries.PLACED_FEATURE, ListMod.rl(key));
    }

    private static void register(BootstapContext<PlacedFeature> context, ResourceKey<PlacedFeature> key, Holder<ConfiguredFeature<?, ?>> configuredFeature, List<PlacementModifier> placements) {
        context.register(key, new PlacedFeature(configuredFeature, List.copyOf(placements)));
    }
}
