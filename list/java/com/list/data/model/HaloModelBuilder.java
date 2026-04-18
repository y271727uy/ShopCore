package com.list.data.model;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.CustomLoaderBuilder;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

public class HaloModelBuilder<T extends ModelBuilder<T>> extends CustomLoaderBuilder<T> {

    private int type = 1;
    private float alpha = 0.6f;


    public HaloModelBuilder(T parent, ExistingFileHelper existingFileHelper) {
        super(new ResourceLocation("avaritia", "halo"), parent, existingFileHelper);
    }

    public HaloModelBuilder<T> type(int type) {
        this.type = type;
        return this;
    }
    public HaloModelBuilder<T> alpha(float alpha) {
        this.alpha = alpha;
        return this;
    }

    @Override
    public JsonObject toJson(JsonObject json) {
        JsonObject jsonObject = super.toJson(json);
        JsonObject haloData = new JsonObject();
        haloData.addProperty("type", this.type);
        haloData.addProperty("alpha", this.alpha);
        jsonObject.add("halo", haloData);
        return jsonObject;
    }
}
