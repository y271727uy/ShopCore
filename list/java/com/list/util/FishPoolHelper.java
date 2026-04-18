package com.list.util;

import com.list.ListMod;
import net.minecraft.resources.ResourceLocation;

public final class FishPoolHelper {
    private FishPoolHelper() {
    }

    public static ResourceLocation of(String path) {
        return ListMod.rl(path);
    }
}

