package com.y271727uy.shopcore.client;

public final class ShopcoreTooltipEntries {
	private ShopcoreTooltipEntries() {
	}

	public static void registerAll() {
		// 例如，注册一个特殊的标题用于所有属于 "#minecraft:logs" 标签的物品：
		TooltipTitleRegistry.registerTagTitle("#minecraft:logs", "tooltip.shopcore.price.header.logs");

	}
}

