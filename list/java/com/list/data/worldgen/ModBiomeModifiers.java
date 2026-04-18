package com.list.data.worldgen;

import com.list.ListMod;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ForgeBiomeModifiers;
import net.minecraftforge.registries.ForgeRegistries;

public class ModBiomeModifiers {
    public static final ResourceKey<BiomeModifier> DISK_BURROWED_SAND = create("disk_burrowed_sand");
    public static final ResourceKey<BiomeModifier> DISK_BURROWED_GRAVEL = create("disk_burrowed_gravel");
    public static final ResourceKey<BiomeModifier> DISK_BURROWED_CLAY = create("disk_burrowed_clay");
    public static final ResourceKey<BiomeModifier> DISK_BURROWED_DIRT = create("disk_burrowed_dirt");
    public static final ResourceKey<BiomeModifier> DISK_BURROWED_MUD = create("disk_burrowed_mud");

    public static void bootstrap(BootstapContext<BiomeModifier> context) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        HolderGetter<PlacedFeature> features = context.lookup(Registries.PLACED_FEATURE);

        context.register(
                DISK_BURROWED_SAND,
                new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                        HolderSet.direct(
                                biomes.getOrThrow(Biomes.BEACH),
                                biomes.getOrThrow(Biomes.OCEAN),
                                biomes.getOrThrow(Biomes.DEEP_OCEAN),
                                biomes.getOrThrow(Biomes.COLD_OCEAN),
                                biomes.getOrThrow(Biomes.DEEP_COLD_OCEAN),
                                biomes.getOrThrow(Biomes.LUKEWARM_OCEAN),
                                biomes.getOrThrow(Biomes.DEEP_LUKEWARM_OCEAN),
                                biomes.getOrThrow(Biomes.WARM_OCEAN),
                                biomes.getOrThrow(Biomes.FROZEN_OCEAN),
                                biomes.getOrThrow(Biomes.DEEP_FROZEN_OCEAN)
                        ),
                        HolderSet.direct(features.getOrThrow(ModPlacedFeatures.DISK_BURROWED_SAND)),
                        GenerationStep.Decoration.UNDERGROUND_ORES
                )
        );

        context.register(
                DISK_BURROWED_GRAVEL,
                new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                        HolderSet.direct(
                                biomes.getOrThrow(Biomes.OCEAN),
                                biomes.getOrThrow(Biomes.DEEP_OCEAN),
                                biomes.getOrThrow(Biomes.COLD_OCEAN),
                                biomes.getOrThrow(Biomes.DEEP_COLD_OCEAN),
                                biomes.getOrThrow(Biomes.LUKEWARM_OCEAN),
                                biomes.getOrThrow(Biomes.DEEP_LUKEWARM_OCEAN),
                                biomes.getOrThrow(Biomes.WARM_OCEAN),
                                biomes.getOrThrow(Biomes.FROZEN_OCEAN),
                                biomes.getOrThrow(Biomes.DEEP_FROZEN_OCEAN)
                        ),
                        HolderSet.direct(features.getOrThrow(ModPlacedFeatures.DISK_BURROWED_GRAVEL)),
                        GenerationStep.Decoration.UNDERGROUND_ORES
                )
        );

        context.register(
                DISK_BURROWED_CLAY,
                new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                        HolderSet.direct(
                                biomes.getOrThrow(Biomes.RIVER),
                                biomes.getOrThrow(Biomes.SWAMP)
                        ),
                        HolderSet.direct(features.getOrThrow(ModPlacedFeatures.DISK_BURROWED_CLAY)),
                        GenerationStep.Decoration.UNDERGROUND_ORES
                )
        );

        context.register(
                DISK_BURROWED_DIRT,
                new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                        HolderSet.direct(
                                biomes.getOrThrow(Biomes.RIVER),
                                biomes.getOrThrow(Biomes.OCEAN),
                                biomes.getOrThrow(Biomes.SWAMP),
                                biomes.getOrThrow(Biomes.MANGROVE_SWAMP)
                        ),
                        HolderSet.direct(features.getOrThrow(ModPlacedFeatures.DISK_BURROWED_DIRT)),
                        GenerationStep.Decoration.UNDERGROUND_ORES
                )
        );

        context.register(
                DISK_BURROWED_MUD,
                new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                        HolderSet.direct(
                                biomes.getOrThrow(Biomes.MANGROVE_SWAMP)
                        ),
                        HolderSet.direct(features.getOrThrow(ModPlacedFeatures.DISK_BURROWED_MUD)),
                        GenerationStep.Decoration.UNDERGROUND_ORES
                )
        );
    }

    private static ResourceKey<BiomeModifier> create(String key) {
        return ResourceKey.create(ForgeRegistries.Keys.BIOME_MODIFIERS, ListMod.rl(key));
    }
}
