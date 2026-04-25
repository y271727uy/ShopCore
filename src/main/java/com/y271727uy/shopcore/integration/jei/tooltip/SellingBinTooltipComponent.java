package com.y271727uy.shopcore.integration.jei.tooltip;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

/**
 * TooltipComponent marker for Selling Bin recipes.
 * Client-side rendering is implemented by the matching client tooltip component.
 */
public record SellingBinTooltipComponent(ItemStack inputPreview, ItemStack output, String outputPriceText) implements TooltipComponent {
}


