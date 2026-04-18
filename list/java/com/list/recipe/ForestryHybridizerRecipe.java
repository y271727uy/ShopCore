package com.list.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.list.all.ModRecipes;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ForestryHybridizerRecipe implements Recipe<ForestryHybridizerRecipe.RecipeInput> {

    public static final int INPUT_SLOTS = 3;
    public static final int MAX_OUTPUTS = 2;

    public final ResourceLocation id;
    public final List<HybridizerIngredient> inputs; // size=3
    public final List<ChancedItemStack> outputs; // size=2
    /**
     * JEI/display-only override stacks for outputs.
     * Keeps actual machine output logic separate from client recipe rendering,
     * which is important for items with complex NBT/subtype handling.
     */
    public final List<ItemStack> outputDisplayStacks; // size=outputs.size()
    /**
     * If true for a given output index, the serializer/recipe will verify the produced stack NBT
     * exactly equals the prototype stack NBT (same tags). This is mainly to guard against
     * accidental loss/rewriting of NBT in downstream code.
     */
    public final List<Boolean> outputStrictNbt; // size=outputs.size()
    public final int time;

    public ForestryHybridizerRecipe(ResourceLocation id, List<HybridizerIngredient> inputs, List<ChancedItemStack> outputs, List<ItemStack> outputDisplayStacks, List<Boolean> outputStrictNbt, int time) {
        this.id = id;
        this.inputs = inputs;
        this.outputs = outputs;
        this.outputDisplayStacks = outputDisplayStacks;
        this.outputStrictNbt = outputStrictNbt;
        this.time = time;
    }


    @Override
    public boolean matches(RecipeInput container, Level level) {
        if (container.getContainerSize() < INPUT_SLOTS) return false;
        // Only check provided inputs. Missing inputs mean "no requirement" for that slot.
        for (int i = 0; i < inputs.size(); i++) {
            ItemStack stack = container.getItem(i);
            if (!inputs.get(i).test(stack, level)) return false;
        }
        return true;
    }

    @Override
    public ItemStack assemble(RecipeInput container, RegistryAccess registryAccess) {
        ItemStack prototype = outputs.get(0).itemStack();
        ItemStack out = prototype.copy();
        // Optional strict validation that the recipe output keeps exact NBT.
        if (!outputStrictNbt.isEmpty() && outputStrictNbt.get(0)) {
            if (!ItemStack.isSameItemSameTags(out, prototype)) {
                // If this ever triggers, something has modified/stripped NBT unexpectedly.
                return ItemStack.EMPTY;
            }
        }
        return out;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        ItemStack prototype = outputs.get(0).itemStack();
        // Return a copy so callers can't mutate the recipe prototype.
        ItemStack out = prototype.copy();
        if (!outputStrictNbt.isEmpty() && outputStrictNbt.get(0)) {
            if (!ItemStack.isSameItemSameTags(out, prototype)) {
                return ItemStack.EMPTY;
            }
        }
        return out;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.FORESTRY_HYBRIDIZER_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.FORESTRY_HYBRIDIZER_TYPE.get();
    }

    public static class RecipeInput implements Container {
        private final NonNullList<ItemStack> input = NonNullList.withSize(INPUT_SLOTS, ItemStack.EMPTY);

        public RecipeInput(List<ItemStack> stacks) {
            for (int i = 0; i < Math.min(stacks.size(), INPUT_SLOTS); i++) {
                input.set(i, stacks.get(i));
            }
        }

        @Override
        public int getContainerSize() {
            return INPUT_SLOTS;
        }

        @Override
        public boolean isEmpty() {
            for (ItemStack itemStack : input) {
                if (!itemStack.isEmpty()) return false;
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

    public static class Serializer implements RecipeSerializer<ForestryHybridizerRecipe> {
        private static final int DEFAULT_TIME = 200;

        @Override
        public ForestryHybridizerRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            JsonArray inputsJson = GsonHelper.getAsJsonArray(json, "inputs");
            if (inputsJson.isEmpty() || inputsJson.size() > INPUT_SLOTS) {
                throw new JsonParseException("ForestryHybridizer recipe inputs must have 1 to " + INPUT_SLOTS + " entries");
            }
            List<HybridizerIngredient> inputs = new ArrayList<>(inputsJson.size());
            for (int i = 0; i < inputsJson.size(); i++) {
                inputs.add(HybridizerIngredient.fromJson(GsonHelper.convertToJsonObject(inputsJson.get(i), "inputs[" + i + "]")));
            }

            JsonArray outputsJson = GsonHelper.getAsJsonArray(json, "outputs");
            if (outputsJson.isEmpty() || outputsJson.size() > MAX_OUTPUTS) {
                throw new JsonParseException("ForestryHybridizer recipe outputs must have 1 to " + MAX_OUTPUTS + " entries");
            }
            List<ChancedItemStack> outputs = new ArrayList<>(outputsJson.size());
            List<ItemStack> outputDisplayStacks = new ArrayList<>(outputsJson.size());
            List<Boolean> outputStrictNbt = new ArrayList<>(outputsJson.size());
            for (int i = 0; i < outputsJson.size(); i++) {
                JsonObject outObj = GsonHelper.convertToJsonObject(outputsJson.get(i), "outputs[" + i + "]");
                ItemStack stack = CraftingHelper.getItemStack(outObj, true);
                float chance = GsonHelper.getAsFloat(outObj, "chance", 1.0f);
                outputs.add(new ChancedItemStack(stack, chance));
                outputDisplayStacks.add(parseOutputDisplayStack(outObj, stack));
                outputStrictNbt.add(GsonHelper.getAsBoolean(outObj, "strict_nbt", false));
            }

            int time = GsonHelper.getAsInt(json, "time", DEFAULT_TIME);
            if (time <= 0) time = DEFAULT_TIME;

            return new ForestryHybridizerRecipe(recipeId, inputs, outputs, outputDisplayStacks, outputStrictNbt, time);
        }

        @Override
        public @Nullable ForestryHybridizerRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buf) {
            int inCount = buf.readVarInt();
            List<HybridizerIngredient> inputs = new ArrayList<>(inCount);
            for (int i = 0; i < inCount; i++) {
                inputs.add(HybridizerIngredient.fromNetwork(buf));
            }

            int outCount = buf.readVarInt();
            List<ChancedItemStack> outputs = new ArrayList<>(outCount);
            List<ItemStack> outputDisplayStacks = new ArrayList<>(outCount);
            List<Boolean> outputStrictNbt = new ArrayList<>(outCount);
            for (int i = 0; i < outCount; i++) {
                ItemStack stack = buf.readItem();
                float chance = buf.readFloat();
                ItemStack displayStack = buf.readItem();
                boolean strictNbt = buf.readBoolean();
                outputs.add(new ChancedItemStack(stack, chance));
                outputDisplayStacks.add(displayStack);
                outputStrictNbt.add(strictNbt);
            }

            int time = buf.readVarInt();
            return new ForestryHybridizerRecipe(recipeId, inputs, outputs, outputDisplayStacks, outputStrictNbt, time);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, ForestryHybridizerRecipe recipe) {
            buf.writeVarInt(recipe.inputs.size());
            for (HybridizerIngredient in : recipe.inputs) {
                in.toNetwork(buf);
            }

            buf.writeVarInt(recipe.outputs.size());
            for (int i = 0; i < recipe.outputs.size(); i++) {
                ChancedItemStack out = recipe.outputs.get(i);
                buf.writeItem(out.itemStack());
                buf.writeFloat(out.chance());
                ItemStack displayStack = i < recipe.outputDisplayStacks.size() ? recipe.outputDisplayStacks.get(i) : ItemStack.EMPTY;
                buf.writeItem(displayStack);
                boolean strictNbt = i < recipe.outputStrictNbt.size() && recipe.outputStrictNbt.get(i);
                buf.writeBoolean(strictNbt);
            }

            buf.writeVarInt(recipe.time);
        }

        private static ItemStack parseOutputDisplayStack(JsonObject outObj, ItemStack fallback) {
            if (!outObj.has("display_item") && !outObj.has("render_item")) {
                return fallback.copy();
            }

            String fieldName = outObj.has("display_item") ? "display_item" : "render_item";
            JsonObject displayObj = GsonHelper.getAsJsonObject(outObj, fieldName);
            if (!displayObj.has("item")) {
                throw new JsonParseException(fieldName + " must contain 'item'");
            }

            ResourceLocation displayItemId = ResourceLocation.parse(GsonHelper.getAsString(displayObj, "item"));
            Item displayItem = ForgeRegistries.ITEMS.getValue(displayItemId);
            if (displayItem == null) {
                throw new JsonParseException("Unknown " + fieldName + " item: " + displayItemId);
            }

            ItemStack displayStack = new ItemStack(displayItem);
            int displayCount = GsonHelper.getAsInt(displayObj, "count", fallback.getCount());
            displayStack.setCount(Math.max(1, displayCount));

            if (displayObj.has("nbt")) {
                JsonElement nbtEl = displayObj.get("nbt");
                try {
                    CompoundTag displayNbt;
                    if (nbtEl.isJsonObject()) {
                        displayNbt = TagParser.parseTag(nbtEl.toString());
                    } else if (nbtEl.isJsonPrimitive()) {
                        displayNbt = TagParser.parseTag(GsonHelper.convertToString(nbtEl, fieldName + ".nbt"));
                    } else {
                        throw new JsonParseException("Invalid " + fieldName + ".nbt element");
                    }
                    displayStack.setTag(displayNbt);
                } catch (Exception e) {
                    throw new JsonParseException("Failed to parse " + fieldName + ".nbt: " + e.getMessage());
                }
            } else if (fallback.hasTag() && fallback.getTag() != null) {
                displayStack.setTag(fallback.getTag().copy());
            }

            return displayStack;
        }
    }
}


