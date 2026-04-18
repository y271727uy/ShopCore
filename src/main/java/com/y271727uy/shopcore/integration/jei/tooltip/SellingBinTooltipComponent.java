package com.y271727uy.shopcore.integration.jei.tooltip;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * TooltipComponent marker for Selling Bin recipes.
 * Client-side rendering is implemented by the matching client tooltip component.
 */
public record SellingBinTooltipComponent(Ingredient input, ItemStack output, String outputPriceText) implements TooltipComponent {
}


