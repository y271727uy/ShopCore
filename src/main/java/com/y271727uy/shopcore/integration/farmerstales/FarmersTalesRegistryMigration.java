package com.y271727uy.shopcore.integration.farmerstales;

import com.y271727uy.shopcore.ShopcoreMod;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.MissingMappingsEvent;

import java.util.Set;

/** Preserves old ShopCore tree blocks when a world is opened with Farmer's Tales installed. */
@Mod.EventBusSubscriber(modid = ShopcoreMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class FarmersTalesRegistryMigration {
    private static final String TARGET_MOD_ID = "farmerstales";
    private static final Set<String> MIGRATED_PATHS = Set.of("tree_compost", "tree_stump");

    private FarmersTalesRegistryMigration() {
    }

    @SubscribeEvent
    public static void onMissingMappings(MissingMappingsEvent event) {
        if (!ModList.get().isLoaded(TARGET_MOD_ID)) {
            return;
        }

        remap(event, ForgeRegistries.Keys.BLOCKS, ForgeRegistries.BLOCKS);
        remap(event, ForgeRegistries.Keys.ITEMS, ForgeRegistries.ITEMS);
        remap(event, ForgeRegistries.Keys.BLOCK_ENTITY_TYPES, ForgeRegistries.BLOCK_ENTITY_TYPES);
    }

    private static <T> void remap(
            MissingMappingsEvent event,
            ResourceKey<? extends Registry<T>> registryKey,
            IForgeRegistry<T> registry
    ) {
        for (MissingMappingsEvent.Mapping<T> mapping : event.getMappings(registryKey, ShopcoreMod.MODID)) {
            String path = mapping.getKey().getPath();
            if (!MIGRATED_PATHS.contains(path)) {
                continue;
            }

            T target = registry.getValue(ResourceLocation.fromNamespaceAndPath(TARGET_MOD_ID, path));
            if (target != null) {
                mapping.remap(target);
            }
        }
    }
}
