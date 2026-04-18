package com.list.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.list.all.ModRecipes;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ForestryGreenhouseRecipe implements Recipe<ForestryGreenhouseRecipe.RecipeInput> {

    public static final int INPUT_SLOTS = 12;
    public static final int MAX_OUTPUTS = 9;

    /** Slots 1-9 in UI => 0-8 in code: basic item-only matching. */
    public static final int BASIC_SLOTS = 9;

    /** Slot 10 in UI => container index 9: optional trench ingredient (positioned). */
    public static final int TRENCH_SLOT_10_INDEX = 9;

    /** Slot 11 in UI => container index 10: water handling slot; NOT part of recipe matching. */
    public static final int WATER_SLOT_11_INDEX = 10;

    /** Slot 12 in UI => container index 11: fuel slot; NOT part of recipe matching. */
    public static final int FUEL_SLOT_12_INDEX = 11;

    public final ResourceLocation id;

    /**
     * Slots 0-8: basic item-only requirements. Missing entries mean "no requirement".
     * <p>
     * Intentionally does NOT support tags/alternatives/nbt to keep behavior simple.
     */
    public final List<BasicIngredient> basicInputs;

    /**
     * Trench ingredient for slot 10 (index 9). Null means no requirement.
     */
    @Nullable
    public final HybridizerIngredient trench10;

    /** If true, trench10 requires EXACT NBT match (no blurry). */
    public final boolean trench10StrictNbt;

    /** Output prototypes with chances; size 1..9. */
    public final List<ChancedItemStack> outputs;
    public final List<Boolean> outputStrictNbt;
    public final int time;
    /** Water cost in mB, consumed when the recipe starts. */
    public final int water;

    /** Whether this recipe requires fuel burning (slot 12). Default false. */
    public final boolean fuel;

    public ForestryGreenhouseRecipe(ResourceLocation id,
                                   List<BasicIngredient> basicInputs,
                                   @Nullable HybridizerIngredient trench10,
                                   boolean trench10StrictNbt,
                                   List<ChancedItemStack> outputs,
                                   List<Boolean> outputStrictNbt,
                                   int time,
                                   int water,
                                   boolean fuel) {
        this.id = id;
        this.basicInputs = basicInputs;
        this.trench10 = trench10;
        this.trench10StrictNbt = trench10StrictNbt;
        this.outputs = outputs;
        this.outputStrictNbt = outputStrictNbt;
        this.time = time;
        this.water = Math.max(0, water);
        this.fuel = fuel;
    }

    @Override
    public boolean matches(RecipeInput container, Level level) {
        if (container.getContainerSize() < INPUT_SLOTS) return false;

        // Slots 0-8: basic, item-id-only matching (SHAPELESS within 1-9 UI slots)
        if (!computeBasicConsumptionPlan(container).isPresent()) return false;

        // Slot 10(index 9): trench requirement if present
        if (trench10 != null) {
            ItemStack stack = container.getItem(TRENCH_SLOT_10_INDEX);
            if (trench10StrictNbt) {
                // Force exact NBT match using the JEI/display override stack when provided.
                // This keeps `render_item` meaningful and allows recipes to keep using `blurry_nbt` elsewhere.
                ItemStack render = trench10.renderStack();
                if (render != null && !render.isEmpty() && render.hasTag()) {
                    if (!ItemStack.isSameItemSameTags(stack, render)) return false;
                } else {
                    // Fallback: if no render stack with NBT exists, we can only fall back to ingredient matching.
                    if (!trench10.test(stack, level)) return false;
                }
            } else {
                if (!trench10.test(stack, level)) return false;
            }
        }

        // Slot 11(index 10): water slot, ignored by recipes

        // Slot 12(index 11): fuel slot, ignored by recipes
        return true;
    }

    /**
     * Compute a deterministic consumption plan for basic inputs (slots 0-8).
     * <p>
     * Returns a map slotIndex->countToExtract when all requirements can be satisfied.
     * Basic rules:
     * - requirements are shapeless within slots 0-8
     * - a requirement may be satisfied by multiple stacks (splitting) if needed
     * - extras in 0-8 are allowed (machine will simply ignore them)
     */
    public java.util.Optional<Map<Integer, Integer>> computeBasicConsumptionPlan(Container container) {
        if (basicInputs.isEmpty()) {
            return java.util.Optional.of(Map.of());
        }

        // Track remaining counts per slot (simulate consumption).
        int[] remaining = new int[BASIC_SLOTS];
        for (int slot = 0; slot < BASIC_SLOTS; slot++) {
            ItemStack stack = container.getItem(slot);
            remaining[slot] = stack.isEmpty() ? 0 : stack.getCount();
        }

        Map<Integer, Integer> plan = new HashMap<>();

        // Greedy deterministic: for each requirement, consume from lowest slot indices first.
        for (BasicIngredient req : basicInputs) {
            int need = req.count();
            for (int slot = 0; slot < BASIC_SLOTS && need > 0; slot++) {
                ItemStack stack = container.getItem(slot);
                if (stack.isEmpty()) continue;
                if (!req.isSameItem(stack)) continue;

                int take = Math.min(need, remaining[slot]);
                if (take <= 0) continue;

                remaining[slot] -= take;
                plan.merge(slot, take, Integer::sum);
                need -= take;
            }
            if (need > 0) {
                return java.util.Optional.empty();
            }
        }

        return java.util.Optional.of(plan);
    }

    @Override
    public ItemStack assemble(RecipeInput container, RegistryAccess registryAccess) {
        return getResultItem(registryAccess);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        if (outputs.isEmpty()) return ItemStack.EMPTY;
        ItemStack prototype = outputs.get(0).itemStack();
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
        return ModRecipes.FORESTRY_GREENHOUSE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.FORESTRY_GREENHOUSE_TYPE.get();
    }

    /**
     * Very small ingredient type for slots 0-8: match by item id only (no tags/nbt/alternatives).
     */
    public record BasicIngredient(ResourceLocation itemId, int count) {
        public BasicIngredient {
            if (itemId == null) throw new IllegalArgumentException("itemId must not be null");
            if (count <= 0) throw new IllegalArgumentException("count must be > 0");
        }

        public boolean test(ItemStack stack) {
            if (stack.isEmpty()) return false;
            if (!stack.is(ForgeRegistries.ITEMS.getValue(itemId))) return false;
            return stack.getCount() >= count;
        }

        public boolean isSameItem(ItemStack stack) {
            if (stack.isEmpty()) return false;
            return stack.is(ForgeRegistries.ITEMS.getValue(itemId));
        }

        public static BasicIngredient fromJson(JsonObject obj) {
            if (obj.has("tag") || obj.has("alternatives") || obj.has("nbt") || obj.has("blurry_nbt")) {
                throw new JsonParseException("Greenhouse basic inputs (slots 1-9) only support 'item' (+ optional 'count'). No tag/nbt/alternatives.");
            }
            if (!obj.has("item")) {
                throw new JsonParseException("Greenhouse basic inputs (slots 1-9) must contain 'item'.");
            }
            ResourceLocation itemId = ResourceLocation.parse(GsonHelper.getAsString(obj, "item"));
            if (!ForgeRegistries.ITEMS.containsKey(itemId)) {
                throw new JsonParseException("Unknown item: " + itemId);
            }
            int count = GsonHelper.getAsInt(obj, "count", 1);
            if (count <= 0) {
                throw new JsonParseException("Greenhouse basic input count must be > 0");
            }
            return new BasicIngredient(itemId, count);
        }

        public void toNetwork(FriendlyByteBuf buf) {
            buf.writeResourceLocation(itemId);
            buf.writeVarInt(count);
        }

        public static BasicIngredient fromNetwork(FriendlyByteBuf buf) {
            ResourceLocation itemId = buf.readResourceLocation();
            int count = buf.readVarInt();
            return new BasicIngredient(itemId, count);
        }
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

    public static class Serializer implements RecipeSerializer<ForestryGreenhouseRecipe> {
        private static final int DEFAULT_TIME = 200;

        @Override
        public ForestryGreenhouseRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            // inputs = basic slots only (1-9 => 0-8). Trench slots are controlled by separate `trench`.
            JsonArray inputsJson = GsonHelper.getAsJsonArray(json, "inputs");
            if (inputsJson.isEmpty() || inputsJson.size() > BASIC_SLOTS) {
                throw new JsonParseException("ForestryGreenhouse recipe 'inputs' must have 1 to " + BASIC_SLOTS + " entries (slots 1-9)");
            }
            List<BasicIngredient> basicInputs = new ArrayList<>(inputsJson.size());
            for (int i = 0; i < inputsJson.size(); i++) {
                basicInputs.add(BasicIngredient.fromJson(GsonHelper.convertToJsonObject(inputsJson.get(i), "inputs[" + i + "]")));
            }

            // trench: {"trench":"10", ...ingredient...}
            // Note: trench value uses the UI slot number (1-12). Container indices are 0-11.
            HybridizerIngredient trench10 = null;
            boolean trench10StrictNbt = false;
            if (json.has("trench")) {
                JsonObject trenchObj = GsonHelper.getAsJsonObject(json, "trench");
                if (!trenchObj.has("trench")) {
                    throw new JsonParseException("ForestryGreenhouse trench object must contain field 'trench' with value '10'");
                }
                String trenchSlot = GsonHelper.getAsString(trenchObj, "trench");

                trench10StrictNbt = GsonHelper.getAsBoolean(trenchObj, "strict_nbt", false);
                // remove selector field before parsing as ingredient
                JsonObject ingredientObj = trenchObj.deepCopy();
                ingredientObj.remove("trench");
                ingredientObj.remove("strict_nbt");
                if (ingredientObj.size() == 0) {
                    throw new JsonParseException("ForestryGreenhouse trench object must also include an ingredient definition (e.g. item/tag/etc)");
                }
                HybridizerIngredient ing = HybridizerIngredient.fromJson(ingredientObj);
                if ("10".equals(trenchSlot)) {
                    trench10 = ing;
                } else {
                    throw new JsonParseException("ForestryGreenhouse trench.trench must be '10' (slot 11 is reserved for water; slot 12 is fuel)");
                }
            }

            JsonArray outputsJson = GsonHelper.getAsJsonArray(json, "outputs");
            if (outputsJson.isEmpty() || outputsJson.size() > MAX_OUTPUTS) {
                throw new JsonParseException("ForestryGreenhouse recipe outputs must have 1 to " + MAX_OUTPUTS + " entries");
            }
            List<ChancedItemStack> outputs = new ArrayList<>(outputsJson.size());
            List<Boolean> outputStrictNbt = new ArrayList<>(outputsJson.size());
            for (int i = 0; i < outputsJson.size(); i++) {
                JsonObject outObj = GsonHelper.convertToJsonObject(outputsJson.get(i), "outputs[" + i + "]");
                ItemStack stack = CraftingHelper.getItemStack(outObj, true);
                float chance = GsonHelper.getAsFloat(outObj, "chance", 1.0f);
                outputs.add(new ChancedItemStack(stack, chance));
                outputStrictNbt.add(GsonHelper.getAsBoolean(outObj, "strict_nbt", false));
            }

            int time = GsonHelper.getAsInt(json, "time", DEFAULT_TIME);
            if (time <= 0) time = DEFAULT_TIME;

            int water = GsonHelper.getAsInt(json, "water", 0);
            if (water < 0) {
                throw new JsonParseException("ForestryGreenhouse recipe 'water' must be >= 0");
            }

            boolean fuel = GsonHelper.getAsBoolean(json, "fuel", false);

            return new ForestryGreenhouseRecipe(recipeId, basicInputs, trench10, trench10StrictNbt, outputs, outputStrictNbt, time, water, fuel);
        }

        @Override
        public @Nullable ForestryGreenhouseRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buf) {
            int inCount = buf.readVarInt();
            List<BasicIngredient> basicInputs = new ArrayList<>(inCount);
            for (int i = 0; i < inCount; i++) {
                basicInputs.add(BasicIngredient.fromNetwork(buf));
            }

            HybridizerIngredient trench10 = null;
            boolean hasTrench10 = buf.readBoolean();
            if (hasTrench10) trench10 = HybridizerIngredient.fromNetwork(buf);

            int outCount = buf.readVarInt();
            List<ChancedItemStack> outputs = new ArrayList<>(outCount);
            List<Boolean> outputStrictNbt = new ArrayList<>(outCount);
            for (int i = 0; i < outCount; i++) {
                ItemStack stack = buf.readItem();
                float chance = buf.readFloat();
                boolean strictNbt = buf.readBoolean();
                outputs.add(new ChancedItemStack(stack, chance));
                outputStrictNbt.add(strictNbt);
            }

            int time = buf.readVarInt();
            int water = buf.readVarInt();
            boolean fuel = buf.readBoolean();
            boolean trench10StrictNbt = buf.readBoolean();
            return new ForestryGreenhouseRecipe(recipeId, basicInputs, trench10, trench10StrictNbt, outputs, outputStrictNbt, time, water, fuel);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, ForestryGreenhouseRecipe recipe) {
            buf.writeVarInt(recipe.basicInputs.size());
            for (BasicIngredient in : recipe.basicInputs) {
                in.toNetwork(buf);
            }

            buf.writeBoolean(recipe.trench10 != null);
            if (recipe.trench10 != null) recipe.trench10.toNetwork(buf);

            buf.writeVarInt(recipe.outputs.size());
            for (int i = 0; i < recipe.outputs.size(); i++) {
                ChancedItemStack out = recipe.outputs.get(i);
                buf.writeItem(out.itemStack());
                buf.writeFloat(out.chance());
                boolean strictNbt = i < recipe.outputStrictNbt.size() && recipe.outputStrictNbt.get(i);
                buf.writeBoolean(strictNbt);
            }

            buf.writeVarInt(recipe.time);
            buf.writeVarInt(Math.max(0, recipe.water));
            buf.writeBoolean(recipe.fuel);
            buf.writeBoolean(recipe.trench10StrictNbt);
        }
    }
}











