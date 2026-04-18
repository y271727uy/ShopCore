package com.list.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.list.gameplay.fish_group.pool.FishPoolDefinition;
import com.list.gameplay.fish_group.pool.FishPoolFactory;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.concurrent.CompletableFuture;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class FishPoolLootTableProvider implements DataProvider {
    private final PackOutput.PathProvider pathProvider;

    public FishPoolLootTableProvider(PackOutput output) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "fish_pool_loot_tables/gameplay/fishing_pools");
    }

    @Override
    public @Nonnull CompletableFuture<?> run(@Nonnull CachedOutput output) {
        return CompletableFuture.allOf(
                FishPoolFactory.getAll()
                        .stream()
                        .map(definition -> DataProvider.saveStable(output, serialize(definition), this.pathProvider.json(definition.id())))
                        .toArray(CompletableFuture[]::new)
        );
    }

    private JsonObject serialize(FishPoolDefinition definition) {
        JsonObject root = new JsonObject();
        root.addProperty("MaxFishCount", definition.maxFishCount());
        if (definition.minFishCount() != definition.maxFishCount()) {
            root.addProperty("MinFishCount", definition.minFishCount());
        }
        if (definition.fishKing() != null) {
            root.addProperty("FishKing", definition.fishKing().toString());
        }
        if (!definition.biomes().isEmpty()) {
            if (definition.biomes().size() == 1) {
                root.addProperty("Biome", definition.biomes().get(0).toString());
            } else {
                JsonArray biomeArray = new JsonArray();
                definition.biomes().forEach(biome -> biomeArray.add(biome.toString()));
                root.add("Biome", biomeArray);
            }
        }
        if (definition.weatherRequirement() != FishPoolDefinition.WeatherRequirement.ANY) {
            root.addProperty("WeatherRequirement", definition.weatherRequirement().name());
        }
        if (definition.timeRequirement() != FishPoolDefinition.TimeRequirement.ANY) {
            root.addProperty("TimeRequirement", definition.timeRequirement().serializedName());
        }
        JsonArray entries = new JsonArray();
        definition.outputs().forEach(output -> entries.add(output.toJson()));
        root.add("entries", entries);
        return root;
    }

    @Override
    public @Nonnull String getName() {
        return "Fish Pool Loot Tables";
    }
}


