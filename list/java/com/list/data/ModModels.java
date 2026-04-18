package com.list.data;

import com.tterrag.registrate.providers.RegistrateItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;

public class ModModels {
    public static void initItem(RegistrateItemModelProvider provider) {
        provider.getBuilder("template_egg")
            .parent(new ModelFile.UncheckedModelFile("item/generated"))
            .texture("layer0", "item/template_egg");
    }
}
