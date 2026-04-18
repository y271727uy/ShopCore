package com.list.item;

import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;

public class EggItem extends Item {

    public static List<EggItem> EGG_ITEMS = new ArrayList<>();

    private final int color;

    public EggItem(Properties properties, int color) {
        super(properties);
        this.color = color;
        EGG_ITEMS.add(this);
    }

    public int getColor(int tintIndex) {
        return this.color;
    }
}
