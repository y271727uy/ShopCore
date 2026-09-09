package com.y271727uy.shopcore.integration.jei;

import com.y271727uy.shopcore.ShopcoreMod;
import com.y271727uy.shopcore.integration.jei.sdmshop.SdmShopDataBridge;
import com.y271727uy.shopcore.integration.jei.sdmshop.SdmShopJeiEntry;
import com.y271727uy.shopcore.integration.jei.sdmshop.SdmShopRuntimeBridge;
import com.y271727uy.shopcore.integration.jei.sdmshop.category.SdmShopCategory;
import com.y271727uy.shopcore.integration.jei.sdmshop.event.ShopDataLoadedEvent;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Registers ShopCore's selling-bin recipes and optional SDM Shop listings with JEI. */
@JeiPlugin
public final class ShopcoreJeiPlugin implements IModPlugin {
    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(ShopcoreMod.MODID, "jei");
    private final Set<SdmShopJeiEntry> registeredShopEntries = new LinkedHashSet<>();
    private volatile mezz.jei.api.recipe.IRecipeManager recipeManager;

    public ShopcoreJeiPlugin() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public ResourceLocation getPluginUid() { return UID; }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new SdmShopCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<SdmShopJeiEntry> entries = SdmShopDataBridge.collectEntries();
        synchronized (registeredShopEntries) { registeredShopEntries.addAll(entries); }
        if (!entries.isEmpty()) registration.addRecipes(SdmShopCategory.RECIPE_TYPE, entries);
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        recipeManager = runtime.getRecipeManager();
        SdmShopRuntimeBridge.setRuntime(runtime);
        addNewShopEntries(SdmShopDataBridge.refreshEntries());
    }

    @Override
    public void onRuntimeUnavailable() {
        recipeManager = null;
        SdmShopRuntimeBridge.setRuntime(null);
    }

    @SubscribeEvent
    public void onShopDataLoaded(ShopDataLoadedEvent event) {
        SdmShopJeiEntry entry = SdmShopJeiEntry.from(event.getShopTab(), event.getEntry());
        if (!entry.itemStack().isEmpty()) {
            SdmShopDataBridge.recordEntry(entry, event.getShopTab());
            addNewShopEntries(List.of(entry));
        }
    }

    private void addNewShopEntries(List<SdmShopJeiEntry> entries) {
        mezz.jei.api.recipe.IRecipeManager manager = recipeManager;
        if (manager == null || entries.isEmpty()) return;
        List<SdmShopJeiEntry> newEntries;
        synchronized (registeredShopEntries) { newEntries = entries.stream().filter(registeredShopEntries::add).toList(); }
        if (!newEntries.isEmpty()) manager.addRecipes(SdmShopCategory.RECIPE_TYPE, newEntries);
    }
}


