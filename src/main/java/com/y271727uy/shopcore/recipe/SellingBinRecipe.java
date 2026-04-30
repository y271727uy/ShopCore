package com.y271727uy.shopcore.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.y271727uy.shopcore.all.ModRecipes;
import com.y271727uy.shopcore.client.sellingbin.SellingBinClientPriceCache;
import com.y271727uy.shopcore.gameplay.sellingbin.SellingBinGroupManager;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.CraftingHelper;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SellingBinRecipe implements Recipe<SellingBinRecipe.RecipeInput> {

    public final ResourceLocation id;
    public final Ingredient input;
    private final ItemStack[] inputChoices;
    private final int inputCount;
    /**
     * Prototype output stack (includes NBT if provided in JSON).
     * The runtime output count is computed from base/max (if present) and then applied to a copy.
     */
    public final ItemStack output;

    /**
     * Optional: if both are present, output count is random in [base, max].
     */
    @Nullable public final Integer base;
    @Nullable public final Integer max;

    /**
     * Optional recipe grouping key. Not used yet, but reserved for future use.
     */
    public final String group;

    public SellingBinRecipe(ResourceLocation id, Ingredient input, int inputCount, ItemStack output, @Nullable Integer base, @Nullable Integer max, String group) {
        this.id = id;
        this.input = input;
        this.inputChoices = input.getItems();
        this.inputCount = Math.max(1, inputCount);
        this.output = output;
        this.base = base;
        this.max = max;
        this.group = group;
    }

    @Override
    public String getGroup() {
        return group;
    }

    public boolean isMultiChoiceInput() {
        return inputChoices.length > 1;
    }

    public ResourceLocation getPriceKey(ItemStack stack) {
        if (!isMultiChoiceInput() || stack.isEmpty() || !input.test(stack)) {
            return id;
        }

        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return ResourceLocation.fromNamespaceAndPath(
                id.getNamespace(),
                "selling_bin/" + id.getPath() + "/" + itemId.getNamespace() + "/" + itemId.getPath()
        );
    }

    public ItemStack[] getInputChoices() {
        return inputChoices;
    }

    public int getInputCount() {
        return inputCount;
    }

    public ItemStack getPrimaryInputPreview() {
        if (inputChoices.length == 0) {
            return ItemStack.EMPTY;
        }
        ItemStack preview = inputChoices[0].copy();
        preview.setCount(inputCount);
        return preview;
    }

    public ItemStack pickRandomInputChoice(RandomSource random) {
        if (inputChoices.length == 0) {
            return ItemStack.EMPTY;
        }
        ItemStack preview;
        if (inputChoices.length == 1) {
            preview = inputChoices[0].copy();
        } else {
            preview = inputChoices[random.nextInt(inputChoices.length)].copy();
        }
        preview.setCount(inputCount);
        return preview;
    }

    @Override
    public boolean matches(RecipeInput container, Level level) {
        ItemStack stack = container.getItem(0);
        if (stack.isEmpty()) return false;
        return input.test(stack);
    }

    @Override
    public ItemStack assemble(RecipeInput container, RegistryAccess registryAccess) {
        // Vanilla assemble must be deterministic; return prototype copy.
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return output;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.SELLING_BIN_RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.SELLING_BIN_RECIPE_TYPE.get();
    }

    public int rollOutputCount(Level level) {
        return rollOutputCount(level, getPrimaryInputPreview());
    }

    public int rollOutputCount(Level level, ItemStack inputStack) {
        int priceBonus = getPriceBonus(level, inputStack);
        int b = getMinOutputCount(priceBonus);
        int m = getMaxOutputCount(priceBonus);
        // inclusive range
        return b + level.random.nextInt(m - b + 1);
    }

    public long getRawMinOutputCount(int priceBonus) {
        if (base == null || max == null) {
            return (long) output.getCount() + priceBonus;
        }
        return (long) base + priceBonus;
    }

    public int getMinOutputCount(int priceBonus) {
        return clampToPositiveInt(getRawMinOutputCount(priceBonus));
    }

    public int getMaxOutputCount(int priceBonus) {
        if (base == null || max == null) {
            return clampToPositiveInt((long) output.getCount() + priceBonus);
        }

        int min = getMinOutputCount(priceBonus);
        return Math.max(min, clampToPositiveInt((long) max + priceBonus));
    }

    public ItemStack getDisplayOutput(int priceBonus) {
        ItemStack displayOutput = output.copy();
        displayOutput.setCount(Math.max(1, getMaxOutputCount(priceBonus)));
        return displayOutput;
    }

    private int getPriceBonus(Level level) {
        return getPriceBonus(level, ItemStack.EMPTY);
    }

    private int getPriceBonus(Level level, ItemStack inputStack) {
        if (level instanceof ServerLevel serverLevel) {
            return SellingBinGroupManager.getPriceBonus(serverLevel, getPriceKey(inputStack));
        }
        return SellingBinClientPriceCache.getPriceBonus(getPriceKey(inputStack));
    }

    private static int clampToPositiveInt(long value) {
        if (value <= 0L) {
            return 1;
        }
        return value >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) value;
    }

    public static class RecipeInput implements Container {
        private final List<ItemStack> stacks;

        public RecipeInput(List<ItemStack> stacks) {
            this.stacks = stacks;
        }

        @Override
        public int getContainerSize() {
            return stacks.size();
        }

        @Override
        public boolean isEmpty() {
            for (ItemStack s : stacks) {
                if (!s.isEmpty()) return false;
            }
            return true;
        }

        @Override
        public ItemStack getItem(int slot) {
            return stacks.get(slot);
        }

        @Override
        public ItemStack removeItem(int slot, int amount) {
            ItemStack removed = ContainerHelper.removeItem(stacks, slot, amount);
            setChanged();
            return removed;
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            ItemStack itemstack = stacks.get(slot);
            if (itemstack.isEmpty()) {
                return ItemStack.EMPTY;
            }
            stacks.set(slot, ItemStack.EMPTY);
            return itemstack;
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            stacks.set(slot, stack);
            setChanged();
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
            stacks.clear();
            setChanged();
        }
    }

    public static class Serializer implements RecipeSerializer<SellingBinRecipe> {

        @Override
        public SellingBinRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            if (!json.has("input")) {
                throw new JsonParseException("SellingBin recipe missing 'input'");
            }
            JsonElement inputElement = json.get("input");
            Ingredient input = Ingredient.fromJson(inputElement);
            int inputCount = 1;
            if (inputElement != null && inputElement.isJsonObject()) {
                inputCount = Math.max(1, GsonHelper.getAsInt(inputElement.getAsJsonObject(), "count", 1));
            }

            if (!json.has("output")) {
                throw new JsonParseException("SellingBin recipe missing 'output'");
            }
            // Supports strict NBT definition via Forge CraftingHelper: {"item":"...","count":1,"nbt":"{...}"}
            ItemStack output = CraftingHelper.getItemStack(GsonHelper.getAsJsonObject(json, "output"), true);

            Integer base = json.has("base") ? GsonHelper.getAsInt(json, "base") : null;
            Integer max = json.has("max") ? GsonHelper.getAsInt(json, "max") : null;

            String group = GsonHelper.getAsString(json, "group", "");
            // Backward compat: older JSON used required key "list". If present and group not set, keep its string form.
            if (group.isEmpty() && json.has("list")) {
                JsonElement listEl = json.get("list");
                group = listEl == null ? "" : listEl.toString();
            }

            // If only one of base/max is provided, ignore both (as requested: optional and should not crash)
            if ((base == null) != (max == null)) {
                base = null;
                max = null;
            }

            return new SellingBinRecipe(recipeId, input, inputCount, output, base, max, group);
        }

        @Override
        public @Nullable SellingBinRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buf) {
            Ingredient input = Ingredient.fromNetwork(buf);
            int inputCount = buf.readVarInt();
            ItemStack output = buf.readItem();

            boolean hasRange = buf.readBoolean();
            Integer base = null;
            Integer max = null;
            if (hasRange) {
                base = buf.readVarInt();
                max = buf.readVarInt();
            }

            String group = buf.readUtf();
            return new SellingBinRecipe(recipeId, input, inputCount, output, base, max, group);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, SellingBinRecipe recipe) {
            recipe.input.toNetwork(buf);
            buf.writeVarInt(recipe.inputCount);
            buf.writeItem(recipe.output);

            boolean hasRange = recipe.base != null && recipe.max != null;
            buf.writeBoolean(hasRange);
            if (hasRange) {
                buf.writeVarInt(recipe.base);
                buf.writeVarInt(recipe.max);
            }

            buf.writeUtf(recipe.group);
        }
    }
}

