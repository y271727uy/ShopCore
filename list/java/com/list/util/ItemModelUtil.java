package com.list.util;

import com.list.ListMod;
import com.list.item.EggItem;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateItemModelProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ModelFile;

public class ItemModelUtil {
    public static final ResourceLocation TEMPLATE_EGG = ListMod.rl("item/template_egg");

    public static void eggItemModel(DataGenContext<Item, EggItem> ctx, RegistrateItemModelProvider provider) {
        provider.getBuilder(provider.name(ctx::get)).parent(new ModelFile.UncheckedModelFile(TEMPLATE_EGG));
    }
}
