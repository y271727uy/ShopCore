package com.y271727uy.shopcore.economic.shopmenu;

import java.util.Objects;

/**
 * Lightweight binding object for future block entities or other systems.
 */
public record MenuBinding(String menuId) {
	public MenuBinding {
		Objects.requireNonNull(menuId, "menuId");
	}
}

