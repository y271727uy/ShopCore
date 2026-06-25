/**
 * Optional event-driven helpers for high-frequency facts.
 *
 * <p>This package is intentionally not wired into ShopCore globally. Gameplay modules can keep an
 * {@link com.y271727uy.shopcore.core.architecture.eventdriven.EventAccumulator}, record cheap deltas during busy
 * paths, and periodically drain them with
 * {@link com.y271727uy.shopcore.core.architecture.eventdriven.TickFlushScheduler}.</p>
 */
package com.y271727uy.shopcore.core.architecture.eventdriven;
