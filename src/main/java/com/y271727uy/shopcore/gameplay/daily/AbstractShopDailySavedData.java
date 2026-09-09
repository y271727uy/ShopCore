package com.y271727uy.shopcore.gameplay.daily;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.saveddata.SavedData;

/** Shared persistence lifecycle for a shop-specific daily edition. */
public abstract class AbstractShopDailySavedData extends SavedData {
    private static final String EDITION_DAY_TAG = "EditionDay";
    private long editionDay = Long.MIN_VALUE;

    protected final void loadEditionDay(CompoundTag tag) {
        if (tag.contains(EDITION_DAY_TAG, Tag.TAG_LONG)) {
            editionDay = tag.getLong(EDITION_DAY_TAG);
        }
    }

    protected final boolean needsNewEdition(long currentDay) {
        return editionDay != currentDay;
    }

    protected final void markEditionCreated(long currentDay) {
        editionDay = currentDay;
        setDirty();
    }

    protected final void saveEditionDay(CompoundTag tag) {
        tag.putLong(EDITION_DAY_TAG, editionDay);
    }
}
