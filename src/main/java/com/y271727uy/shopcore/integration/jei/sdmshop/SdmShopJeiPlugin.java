package com.y271727uy.shopcore.integration.jei.sdmshop;

import com.y271727uy.shopcore.ShopcoreMod;
import com.y271727uy.shopcore.integration.jei.sdmshop.category.SdmShopCategory;
import com.y271727uy.shopcore.integration.jei.sdmshop.event.ShopDataLoadedEvent;
import com.mojang.logging.LogUtils;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;

import org.slf4j.Logger;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@JeiPlugin
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class SdmShopJeiPlugin implements IModPlugin {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final RecipeType<SdmShopJeiEntry> SDM_SHOP = RecipeType.create(ShopcoreMod.MODID, "sdm_shop", SdmShopJeiEntry.class);

    private final Set<SdmShopJeiEntry> registeredEntries = ConcurrentHashMap.newKeySet();
    private final Set<SdmShopJeiEntry> pendingRuntimeEntries = ConcurrentHashMap.newKeySet();

    private volatile boolean recipeRegistrationComplete;

    public SdmShopJeiPlugin() {
        LOGGER.info("Initializing SDM shop JEI plugin");
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(ShopcoreMod.MODID, "sdm_shop_jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IJeiHelpers jeiHelpers = registration.getJeiHelpers();
        registration.addRecipeCategories(new SdmShopCategory(jeiHelpers, SDM_SHOP));
        LOGGER.info("Registered SDM shop JEI category");
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<SdmShopJeiEntry> entries = SdmShopDataBridge.refreshEntries();
        LOGGER.info("Discovered {} SDM shop entries for JEI", entries.size());
        if (entries.isEmpty()) {
            LOGGER.warn("No SDM shop entries were discovered; JEI category will be empty until the SDM shop data source is exposed at runtime.");
        }
        registration.addRecipes(SDM_SHOP, entries);
        registeredEntries.addAll(entries);
        pendingRuntimeEntries.removeAll(entries);
        recipeRegistrationComplete = true;
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        ItemStack copperCoin = SdmShopCurrencyItems.copperCoin();
        if (!copperCoin.isEmpty()) {
            registration.addRecipeCatalyst(copperCoin, SDM_SHOP);
        }
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        LOGGER.info("SDM shop JEI runtime available");
        SdmShopRuntimeBridge.setRuntime(jeiRuntime);
        flushPendingEntries(jeiRuntime);
    }

    @SubscribeEvent
    public void onShopDataLoaded(ShopDataLoadedEvent event) {
        SdmShopJeiEntry entry = SdmShopJeiEntry.from(event.getShopTab(), event.getEntry());
        if (!registeredEntries.add(entry)) {
            return;
        }

        SdmShopDataBridge.recordEntry(entry);
        LOGGER.info("Received SDM shop entry '{}' for JEI", entry.shopName());
        if (!recipeRegistrationComplete) {
            return;
        }

        IJeiRuntime runtime = SdmShopRuntimeBridge.getRuntime();
        if (runtime != null) {
            runtime.getRecipeManager().addRecipes(SDM_SHOP, List.of(entry));
        } else {
            pendingRuntimeEntries.add(entry);
        }
    }

    private void flushPendingEntries(IJeiRuntime jeiRuntime) {
        if (pendingRuntimeEntries.isEmpty()) {
            return;
        }

        jeiRuntime.getRecipeManager().addRecipes(SDM_SHOP, List.copyOf(pendingRuntimeEntries));
        pendingRuntimeEntries.clear();
    }
}






