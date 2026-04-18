package com.list.all;

import com.list.ListMod;
import com.list.recipe.ForestryGreenhouseRecipe;
import com.list.recipe.ForestryHybridizerRecipe;
import com.list.recipe.FishPondRecipe;
import com.list.recipe.SellingBinRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRecipes {
    private static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, ListMod.MODID);
    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, ListMod.MODID);

    public static final RegistryObject<RecipeType<FishPondRecipe>> FISH_POND_RECIPE_TYPE = RECIPE_TYPES.register(
        "fish_pond",
        () -> RecipeType.simple(ListMod.rl("fish_pond"))
    );

    public static final RegistryObject<RecipeSerializer<FishPondRecipe>> FISH_POND_RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register(
        "fish_pond",
        FishPondRecipe.Serializer::new
    );

    public static final RegistryObject<RecipeType<ForestryHybridizerRecipe>> FORESTRY_HYBRIDIZER_TYPE = RECIPE_TYPES.register(
        "forestry_hybridizer",
        () -> RecipeType.simple(ListMod.rl("forestry_hybridizer"))
    );

    public static final RegistryObject<RecipeSerializer<ForestryHybridizerRecipe>> FORESTRY_HYBRIDIZER_SERIALIZER = RECIPE_SERIALIZERS.register(
        "forestry_hybridizer",
        ForestryHybridizerRecipe.Serializer::new
    );

    public static final RegistryObject<RecipeType<ForestryGreenhouseRecipe>> FORESTRY_GREENHOUSE_TYPE = RECIPE_TYPES.register(
        "forestry_greenhouse",
        () -> RecipeType.simple(ListMod.rl("forestry_greenhouse"))
    );

    public static final RegistryObject<RecipeSerializer<ForestryGreenhouseRecipe>> FORESTRY_GREENHOUSE_SERIALIZER = RECIPE_SERIALIZERS.register(
        "forestry_greenhouse",
        ForestryGreenhouseRecipe.Serializer::new
    );

    public static final RegistryObject<RecipeType<SellingBinRecipe>> SELLING_BIN_RECIPE_TYPE = RECIPE_TYPES.register(
        "selling_bin",
        () -> RecipeType.simple(ListMod.rl("selling_bin"))
    );

    public static final RegistryObject<RecipeSerializer<SellingBinRecipe>> SELLING_BIN_RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register(
        "selling_bin",
        SellingBinRecipe.Serializer::new
    );

    public static void register(IEventBus bus) {
        RECIPE_TYPES.register(bus);
        RECIPE_SERIALIZERS.register(bus);
    }
}
