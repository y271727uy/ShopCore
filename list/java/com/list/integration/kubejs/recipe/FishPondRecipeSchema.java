package com.list.integration.kubejs.recipe;

import dev.latvian.mods.kubejs.item.InputItem;
import dev.latvian.mods.kubejs.item.OutputItem;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.BooleanComponent;
import dev.latvian.mods.kubejs.recipe.component.ItemComponents;
import dev.latvian.mods.kubejs.recipe.component.TimeComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;

public interface FishPondRecipeSchema {

    @SuppressWarnings("unused")
    class FishPondRecipeJS extends RecipeJS {
        public FishPondRecipeJS isLava(boolean isLava) {
            setValue(IS_LAVA, isLava);
            return this;
        }

        public FishPondRecipeJS isLava() {
            setValue(IS_LAVA, true);
            return this;
        }

        public FishPondRecipeJS time(long time) {
            setValue(TIME, time);
            return this;
        }
    }

    RecipeKey<InputItem[]> INPUTS = ItemComponents.INPUT_ARRAY.key("ingredients");
    RecipeKey<OutputItem[]> OUTPUTS = ItemComponents.OUTPUT_ARRAY.key("results");
    RecipeKey<Boolean> IS_LAVA = BooleanComponent.BOOLEAN.key("isLava").optional(false).alwaysWrite();
    RecipeKey<Long> TIME = TimeComponent.TICKS.key("time").optional(600L).alwaysWrite();

    RecipeSchema SCHEMA = new RecipeSchema(FishPondRecipeJS.class, FishPondRecipeJS::new, INPUTS, OUTPUTS, IS_LAVA, TIME)
        .constructor(INPUTS, OUTPUTS);
}
