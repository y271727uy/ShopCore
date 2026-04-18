package com.list.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.list.all.ModRecipes;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class FishPondRecipe implements Recipe<FishPondRecipe.RecipeInput> {

    public final ResourceLocation id;
    public final List<Ingredient> ingredients;
    public final List<ChancedItemStack> results;
    public final int time;
    public final boolean isLava;

    public FishPondRecipe(ResourceLocation id, List<Ingredient> ingredients, List<ChancedItemStack> results, int time, boolean isLava) {
        this.id = id;
        this.ingredients = ingredients;
        this.results = results;
        this.time = time;
        this.isLava = isLava;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public boolean matches(RecipeInput container, Level level) {
        if (container.isLava != this.isLava) return false;
        List<Ingredient> remaining = new ArrayList<>(this.ingredients);
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.isEmpty()) continue;
            boolean matched = false;
            for (int j = 0; j < remaining.size(); j++) {
                if (remaining.get(j).test(stack)) {
                    remaining.remove(j);
                    matched = true;
                    break;
                }
            }
            if (!matched) return false;
        }
        return remaining.isEmpty();
    }

    @Override
    public ItemStack assemble(RecipeInput container, RegistryAccess registryAccess) {
        return results.get(0).itemStack().copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return results.get(0).itemStack();
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.FISH_POND_RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.FISH_POND_RECIPE_TYPE.get();
    }

    public static class RecipeInput implements Container {

        private final NonNullList<ItemStack> input = NonNullList.withSize(9, ItemStack.EMPTY);
        private final boolean isLava;

        public RecipeInput(List<ItemStack> list, boolean isLava) {
            for (int i = 0; i < Math.min(list.size(), 9); i++) {
                input.set(i, list.get(i));
            }
            this.isLava = isLava;
        }

        @Override
        public int getContainerSize() {
            return 9;
        }

        @Override
        public boolean isEmpty() {
            for (ItemStack itemStack : input) {
                if (!itemStack.isEmpty()) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public ItemStack getItem(int slot) {
            return input.get(slot);
        }

        @Override
        public ItemStack removeItem(int slot, int amount) {
            ItemStack itemstack = ContainerHelper.removeItem(this.input, slot, amount);
            if (!itemstack.isEmpty()) {
                this.setChanged();
            }
            return itemstack;
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            ItemStack itemstack = this.input.get(slot);
            if (itemstack.isEmpty()) {
                return ItemStack.EMPTY;
            } else {
                this.input.set(slot, ItemStack.EMPTY);
                return itemstack;
            }
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            this.input.set(slot, stack);
            if (!stack.isEmpty() && stack.getCount() > this.getMaxStackSize()) {
                stack.setCount(this.getMaxStackSize());
            }

            this.setChanged();
        }

        @Override
        public void setChanged() {

        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }

        @Override
        public void clearContent() {
            this.input.clear();
            this.setChanged();
        }
    }

    public static class Serializer implements RecipeSerializer<FishPondRecipe> {

        @Override
        public FishPondRecipe fromJson(ResourceLocation recipeId, JsonObject serializedRecipe) {
            JsonArray ingredientsJson = serializedRecipe.getAsJsonArray("ingredients");
            List<Ingredient> ingredients = new ArrayList<>();
            for (int i = 0; i < ingredientsJson.size(); i++) {
                ingredients.add(Ingredient.fromJson(ingredientsJson.get(i)));
            }

            JsonArray resultsJson = serializedRecipe.getAsJsonArray("results");
            List<ChancedItemStack> results = new ArrayList<>();
            for (int i = 0; i < resultsJson.size(); i++) {
                JsonObject result = resultsJson.get(i).getAsJsonObject();
                ItemStack itemStack = CraftingHelper.getItemStack(result, true);
                JsonElement chanceElement = result.get("chance");
                float chance = chanceElement == null ? 1.0f : chanceElement.getAsFloat();
                results.add(new ChancedItemStack(itemStack, chance));
            }

            int time = serializedRecipe.get("time").getAsInt();
            boolean isLava = serializedRecipe.get("isLava").getAsBoolean();

            return new FishPondRecipe(recipeId, ingredients, results, time, isLava);
        }

        @Override
        public FishPondRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            int ingredientCount = buffer.readInt();
            List<Ingredient> ingredients = new ArrayList<>();
            for (int i = 0; i < ingredientCount; i++) {
                ingredients.add(Ingredient.fromNetwork(buffer));
            }

            int resultCount = buffer.readInt();
            List<ChancedItemStack> results = new ArrayList<>();
            for (int i = 0; i < resultCount; i++) {
                ItemStack itemStack = buffer.readItem();
                float chance = buffer.readFloat();
                results.add(new ChancedItemStack(itemStack, chance));
            }

            int time = buffer.readInt();
            boolean isLava = buffer.readBoolean();
            return new FishPondRecipe(recipeId, ingredients, results, time, isLava);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, FishPondRecipe recipe) {
            buffer.writeInt(recipe.ingredients.size());
            for (Ingredient ingredient : recipe.ingredients) {
                ingredient.toNetwork(buffer);
            }

            buffer.writeInt(recipe.results.size());
            for (ChancedItemStack result : recipe.results) {
                buffer.writeItem(result.itemStack());
                buffer.writeFloat(result.chance());
            }

            buffer.writeInt(recipe.time);
            buffer.writeBoolean(recipe.isLava);
        }
    }

    public static class Builder implements RecipeBuilder {
        private final Advancement.Builder advancement = Advancement.Builder.recipeAdvancement();
        private final List<Ingredient> ingredients = new ArrayList<>();
        private final List<ChancedItemStack> results = new ArrayList<>();
        private int time = 600;
        private boolean isLava;

        @Override
        public RecipeBuilder unlockedBy(String criterionName, CriterionTriggerInstance criterionTrigger) {
            this.advancement.addCriterion(criterionName, criterionTrigger);
            return this;
        }

        @Override
        public RecipeBuilder group(@Nullable String groupName) {
            return this;
        }

        @Override
        public Item getResult() {
            return results.get(0).itemStack().getItem();
        }

        public Builder ingredient(Ingredient ingredient) {
            this.ingredients.add(ingredient);
            return this;
        }


        public Builder result(ItemLike item) {
            return result(item, 1);
        }

        public Builder result(ItemLike item, float chance) {
            return result(item, 1, chance);
        }

        public Builder result(ItemLike item, int count) {
            return result(item, count, 1);
        }

        public Builder result(ItemLike item, int count, float chance) {
            return result(new ItemStack(item, count), chance);
        }

        public Builder result(ItemStack result) {
            this.results.add(new ChancedItemStack(result));
            return this;
        }

        public Builder result(ItemStack result, float chance) {
            this.results.add(new ChancedItemStack(result, chance));
            return this;
        }

        public Builder time(int time) {
            this.time = time;
            return this;
        }

        public Builder isLava(boolean isLava) {
            this.isLava = isLava;
            return this;
        }

        public Builder isLava() {
            this.isLava = true;
            return this;
        }

        @Override
        public void save(Consumer<FinishedRecipe> finishedRecipeConsumer, ResourceLocation recipeId) {
            finishedRecipeConsumer.accept(new Result(recipeId, this.ingredients, this.results, this.time, this.isLava, this.advancement));
        }

        public record Result(
            ResourceLocation id,
            List<Ingredient> ingredients,
            List<ChancedItemStack> results,
            int time,
            boolean isLava,
            Advancement.Builder advancementBuilder
        ) implements FinishedRecipe {

            @Override
            public void serializeRecipeData(JsonObject json) {
                JsonArray ingredientsArray = new JsonArray();
                for (Ingredient ingredient : this.ingredients) {
                    ingredientsArray.add(ingredient.toJson());
                }
                json.add("ingredients", ingredientsArray);

                JsonArray resultsArray = new JsonArray();
                for (ChancedItemStack result : this.results) {
                    JsonObject object = new JsonObject();
                    ItemStack stack = result.itemStack();
                    object.addProperty("item", ForgeRegistries.ITEMS.getKey(stack.getItem()).toString());
                    object.addProperty("count", stack.getCount());
                    if (stack.hasTag()) {
                        object.addProperty("nbt", stack.getTag().toString());
                    }
                    object.addProperty("chance", result.chance());
                    resultsArray.add(object);
                }
                json.add("results", resultsArray);

                json.addProperty("time", this.time);
                json.addProperty("isLava", this.isLava);
            }

            @Override
            public ResourceLocation getId() {
                return this.id;
            }

            @Override
            public RecipeSerializer<?> getType() {
                return ModRecipes.FISH_POND_RECIPE_SERIALIZER.get();
            }

            @Override
            public JsonObject serializeAdvancement() {
                return this.advancementBuilder.serializeToJson();
            }

            @Override
            public ResourceLocation getAdvancementId() {
                return new ResourceLocation(this.id.getNamespace(), "recipes/" + this.id.getPath());
            }
        }
    }
}
