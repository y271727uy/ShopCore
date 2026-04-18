package com.list.registration;

import com.list.registration.builder.ListBlockBuilder;
import com.list.registration.builder.ListItemBuilder;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class ListRegistrate extends AbstractRegistrate<ListRegistrate> {
    public static ListRegistrate create(String modid) {
        var ret = new ListRegistrate(modid);
        ret.registerEventListeners(ret.getModEventBus());
        return ret;
    }

    protected ListRegistrate(String modid) {
        super(modid);
    }


    public <T extends Item, P> ListItemBuilder<T, P> listItem(P parent, String name, NonNullFunction<Item.Properties, T> factory) {
        return entry(name, callback -> ListItemBuilder.create(this, parent, name, callback, factory));
    }


    public <T extends Item, P> ListItemBuilder<T, P> listItem(P parent, NonNullFunction<Item.Properties, T> factory) {
        return listItem(parent, currentName(), factory);
    }


    public <T extends Item> ListItemBuilder<T, ListRegistrate> listItem(String name, NonNullFunction<Item.Properties, T> factory) {
        return listItem(self(), name, factory);
    }


    public <T extends Item> ListItemBuilder<T, ListRegistrate> listItem(NonNullFunction<Item.Properties, T> factory) {
        return listItem(self(), factory);
    }

    public <T extends Block> ListBlockBuilder<T, ListRegistrate> listBLock(NonNullFunction<BlockBehaviour.Properties, T> factory) {
        return listBLock(self(), factory);
    }

    public <T extends Block> ListBlockBuilder<T, ListRegistrate> listBLock(String name, NonNullFunction<BlockBehaviour.Properties, T> factory) {
        return listBLock(self(), name, factory);
    }

    public <T extends Block, P> ListBlockBuilder<T, P> listBLock(P parent, NonNullFunction<BlockBehaviour.Properties, T> factory) {
        return listBLock(parent, currentName(), factory);
    }

    public <T extends Block, P> ListBlockBuilder<T, P> listBLock(P parent, String name, NonNullFunction<BlockBehaviour.Properties, T> factory) {
        return entry(name, callback -> ListBlockBuilder.create(this, parent, name, callback, factory));
    }
}
