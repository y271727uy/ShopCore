package com.y271727uy.shopcore.economic.bootstrap;

import com.y271727uy.shopcore.economic.pricesetting.PriceSetting;
import com.y271727uy.shopcore.economic.pricesetting.PriceSettingRegistry;

/**
 * Initializes the economic module and applies the maintained price setting DSL.
 */
public final class ShopcoreEconomicBootstrap {
	private static boolean bootstrapped;

	private ShopcoreEconomicBootstrap() {
	}

	public static void bootstrap() {
		if (bootstrapped) {
			return;
		}
		bootstrapped = true;

		PriceSettingRegistry.clear();
		PriceSetting.registerAll();
	}
}
