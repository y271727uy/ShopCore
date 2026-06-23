package com.y271727uy.shopcore.core.menu.storage;

import com.y271727uy.shopcore.ShopcoreMod;
import com.y271727uy.shopcore.core.menu.DemandPoolMenuEntry;
import com.y271727uy.shopcore.core.menu.ListingMenuEntry;
import com.y271727uy.shopcore.core.menu.ShopMenuEntry;
import com.y271727uy.shopcore.core.menu.ShopMenuEntryKind;
import com.y271727uy.shopcore.core.menu.ShopMenuSnapshot;
import com.y271727uy.shopcore.core.menu.pool.DemandPoolKey;
import com.y271727uy.shopcore.core.order.storage.ShopListingNbt;
import com.y271727uy.shopcore.core.shop.instance.ShopId;
import com.y271727uy.shopcore.core.shop.storage.ShopIdNbt;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ShopMenuSnapshotNbt {
    public static final String MENU = "Menu";

    private static final String SHOP_ID = "ShopId";
    private static final String VERSION = "Version";
    private static final String ENTRIES = "Entries";
    private static final String KIND = "Kind";
    private static final String SLOT_INDEX = "SlotIndex";
    private static final String MENU_ID = "MenuId";
    private static final String ENABLED = "Enabled";
    private static final String LISTING = "Listing";
    private static final String POOL_KEY = "PoolKey";
    private static final String MAX_SELECTIONS = "MaxSelections";

    private ShopMenuSnapshotNbt() {
    }

    public static CompoundTag save(ShopMenuSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot");
        CompoundTag tag = new CompoundTag();
        ShopIdNbt.put(tag, SHOP_ID, snapshot.shopId());
        tag.putLong(VERSION, snapshot.version());

        ListTag entries = new ListTag();
        for (ShopMenuEntry entry : snapshot.entries()) {
            entries.add(saveEntry(entry));
        }
        tag.put(ENTRIES, entries);
        return tag;
    }

    public static ShopMenuSnapshot load(CompoundTag tag, ShopId fallbackShopId) {
        Objects.requireNonNull(tag, "tag");
        Objects.requireNonNull(fallbackShopId, "fallbackShopId");
        ShopId shopId = ShopIdNbt.get(tag, SHOP_ID, fallbackShopId);
        List<ShopMenuEntry> entries = new ArrayList<>();
        ListTag list = tag.getList(ENTRIES, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            loadEntry(list.getCompound(i)).ifPresent(entries::add);
        }
        return new ShopMenuSnapshot(shopId, entries, Math.max(0L, tag.getLong(VERSION)));
    }

    private static CompoundTag saveEntry(ShopMenuEntry entry) {
        CompoundTag tag = new CompoundTag();
        tag.putString(KIND, entry.kind().name());
        tag.putInt(SLOT_INDEX, entry.slotIndex());
        tag.putString(MENU_ID, entry.menuId());
        tag.putBoolean(ENABLED, entry.enabled());
        if (entry instanceof ListingMenuEntry listingEntry) {
            tag.put(LISTING, ShopListingNbt.save(listingEntry.listing()));
        } else if (entry instanceof DemandPoolMenuEntry demandPoolEntry) {
            tag.putString(POOL_KEY, demandPoolEntry.poolKey().id().toString());
            tag.putInt(MAX_SELECTIONS, demandPoolEntry.maxSelections());
        }
        return tag;
    }

    private static java.util.Optional<ShopMenuEntry> loadEntry(CompoundTag tag) {
        ShopMenuEntryKind kind = entryKind(tag.getString(KIND));
        if (kind == ShopMenuEntryKind.LISTING && tag.contains(LISTING)) {
            return java.util.Optional.of(ListingMenuEntry.of(ShopListingNbt.load(tag.getCompound(LISTING))));
        }
        if (kind == ShopMenuEntryKind.DEMAND_POOL) {
            ResourceLocation poolId = ResourceLocation.tryParse(tag.getString(POOL_KEY));
            if (poolId == null) {
                poolId = ResourceLocation.fromNamespaceAndPath(ShopcoreMod.MODID, "unknown");
            }
            return java.util.Optional.of(new DemandPoolMenuEntry(
                    Math.max(0, tag.getInt(SLOT_INDEX)),
                    tag.getString(MENU_ID),
                    !tag.contains(ENABLED) || tag.getBoolean(ENABLED),
                    DemandPoolKey.of(poolId),
                    Math.max(1, tag.getInt(MAX_SELECTIONS))
            ));
        }
        return java.util.Optional.empty();
    }

    private static ShopMenuEntryKind entryKind(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return ShopMenuEntryKind.valueOf(value);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
