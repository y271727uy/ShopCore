package com.list.data.worldgen;

import com.list.ListMod;
import com.list.all.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.DiskConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.RuleBasedBlockStateProvider;

import java.util.List;

public class ModConfiguredFeatures {
    public static final ResourceKey<ConfiguredFeature<?, ?>> DISK_BURROWED_SAND = create("disk_burrowed_sand");
    public static final ResourceKey<ConfiguredFeature<?, ?>> DISK_BURROWED_GRAVEL = create("disk_burrowed_gravel");
    public static final ResourceKey<ConfiguredFeature<?, ?>> DISK_BURROWED_CLAY = create("disk_burrowed_clay");
    public static final ResourceKey<ConfiguredFeature<?, ?>> DISK_BURROWED_DIRT = create("disk_burrowed_dirt");
    public static final ResourceKey<ConfiguredFeature<?, ?>> DISK_BURROWED_MUD = create("disk_burrowed_mud");

    public static void bootstrap(BootstapContext<ConfiguredFeature<?, ?>> context) {
        register(context, DISK_BURROWED_SAND, Feature.DISK, new DiskConfiguration(
                RuleBasedBlockStateProvider.simple(ModBlocks.BURROWED_SAND.get()),
                BlockPredicate.matchesBlocks(List.of(Blocks.SAND)),
                UniformInt.of(0, 1),
                1
        ));

        register(context, DISK_BURROWED_GRAVEL, Feature.DISK, new DiskConfiguration(
                RuleBasedBlockStateProvider.simple(ModBlocks.BURROWED_GRAVEL.get()),
                BlockPredicate.matchesBlocks(List.of(Blocks.GRAVEL)),
                UniformInt.of(1, 3),
                1
        ));

        register(context, DISK_BURROWED_CLAY, Feature.DISK, new DiskConfiguration(
                RuleBasedBlockStateProvider.simple(ModBlocks.BURROWED_CLAY.get()),
                BlockPredicate.matchesBlocks(List.of(Blocks.CLAY)),
                UniformInt.of(0, 1),
                1
        ));

        register(context, DISK_BURROWED_DIRT, Feature.DISK, new DiskConfiguration(
                RuleBasedBlockStateProvider.simple(ModBlocks.BURROWED_DIRT.get()),
                BlockPredicate.matchesBlocks(List.of(Blocks.DIRT)),
                UniformInt.of(0, 1),
                1
        ));

        register(context, DISK_BURROWED_MUD, Feature.DISK, new DiskConfiguration(
                RuleBasedBlockStateProvider.simple(ModBlocks.BURROWED_MUD.get()),
                BlockPredicate.matchesBlocks(List.of(Blocks.MUD)),
                UniformInt.of(0, 1),
                1
        ));
    }

    private static ResourceKey<ConfiguredFeature<?, ?>> create(String key) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, ListMod.rl(key));
    }

    private static <FC extends FeatureConfiguration, F extends Feature<FC>> void register(BootstapContext<ConfiguredFeature<?, ?>> context, ResourceKey<ConfiguredFeature<?, ?>> key, F feature, FC config) {
        context.register(key, new ConfiguredFeature<>(feature, config));
    }
}
