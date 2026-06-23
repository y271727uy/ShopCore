package com.y271727uy.shopcore.core.shop.shopmenu;

import java.util.Objects;

/**
 * Lightweight binding object for future block entities or other systems.
 */
public record MenuBinding(String menuId) {
	public MenuBinding {
		Objects.requireNonNull(menuId, "menuId");
	}
}

