package com.list.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * A single-slot ingredient for ForestryHybridizer.
 *
 * Supports:
 * - item
 * - tag
 * - alternatives: [ {item|tag, nbt?}, ... ]
 *
 * NBT matching:
 * - "nbt"        : EXACT (same tags)
 * - "blurry_nbt" : FUZZY (pattern must be contained in the stack tag; stack may have extra keys)
 */
public sealed interface HybridizerIngredient permits HybridizerIngredient.Simple, HybridizerIngredient.Alternatives {

    boolean test(ItemStack stack, @Nullable Level level);

    int count();

    /**
     * Primary item(s) preview for UI; may be empty when tag cannot be resolved.
     */
    List<ItemStack> previewStacks();

    void toNetwork(FriendlyByteBuf buf);

    static HybridizerIngredient fromNetwork(FriendlyByteBuf buf) {
        int kind = buf.readVarInt();
        return switch (kind) {
            case 0 -> Simple.fromNetwork(buf);
            case 1 -> Alternatives.fromNetwork(buf);
            default -> throw new IllegalArgumentException("Unknown HybridizerIngredient kind: " + kind);
        };
    }

    static HybridizerIngredient fromJson(JsonObject obj) {
        if (obj.has("alternatives")) {
            JsonArray arr = GsonHelper.getAsJsonArray(obj, "alternatives");
            if (arr.isEmpty()) {
                throw new JsonParseException("Hybridizer ingredient alternatives cannot be empty");
            }
            int count = GsonHelper.getAsInt(obj, "count", 1);
            if (count <= 0) {
                throw new JsonParseException("Hybridizer ingredient count must be > 0");
            }
            List<Simple> alts = new ArrayList<>();
            for (JsonElement el : arr) {
                if (!el.isJsonObject()) {
                    throw new JsonParseException("Hybridizer ingredient alternatives entries must be objects");
                }
                alts.add(Simple.fromJson(el.getAsJsonObject(), count));
            }
            return new Alternatives(count, alts);
        }

        return Simple.fromJson(obj, GsonHelper.getAsInt(obj, "count", 1));
    }

    enum NbtMode {
        NONE,
        EXACT,
        BLURRY
    }

    /**
     * JEI/display-only override stack.
     * <p>
     * This is intentionally separated from matching NBT logic (nbt/blurry_nbt)
     * to avoid JEI rendering issues when ingredient stacks carry complex NBT.
     */
    @Nullable
    default ItemStack renderStack() {
        return null;
    }

    record Simple(int count,
                  @Nullable ResourceLocation itemId,
                  @Nullable ResourceLocation tagId,
                  @Nullable CompoundTag nbt,
                  NbtMode nbtMode,
                  @Nullable ItemStack renderItem) implements HybridizerIngredient {
        public Simple {
            if (count <= 0) {
                throw new IllegalArgumentException("count must be > 0");
            }
            if ((itemId == null) == (tagId == null)) {
                throw new IllegalArgumentException("HybridizerIngredient.Simple must have exactly one of itemId or tagId");
            }
            if (nbtMode == null) {
                throw new IllegalArgumentException("nbtMode must not be null");
            }
        }

        @Override
        public @Nullable ItemStack renderStack() {
            return renderItem;
        }

        static Simple fromJson(JsonObject obj, int defaultCount) {
            ResourceLocation itemId = null;
            ResourceLocation tagId = null;

            int count = GsonHelper.getAsInt(obj, "count", defaultCount);
            if (count <= 0) {
                throw new JsonParseException("Hybridizer ingredient count must be > 0");
            }

            if (obj.has("item")) {
                itemId = ResourceLocation.parse(GsonHelper.getAsString(obj, "item"));
                if (!ForgeRegistries.ITEMS.containsKey(itemId)) {
                    throw new JsonParseException("Unknown item: " + itemId);
                }
            }
            if (obj.has("tag")) {
                tagId = ResourceLocation.parse(GsonHelper.getAsString(obj, "tag"));
            }

            if ((itemId == null) == (tagId == null)) {
                throw new JsonParseException("Hybridizer ingredient must have exactly one of 'item' or 'tag'");
            }

            ItemStack renderItem = null;
            if (obj.has("render_item")) {
                JsonObject renderObj = GsonHelper.getAsJsonObject(obj, "render_item");
                if (!renderObj.has("item")) {
                    throw new JsonParseException("render_item must contain 'item'");
                }
                ResourceLocation renderItemId = ResourceLocation.parse(GsonHelper.getAsString(renderObj, "item"));
                Item renderMcItem = ForgeRegistries.ITEMS.getValue(renderItemId);
                if (renderMcItem == null) {
                    throw new JsonParseException("Unknown render_item item: " + renderItemId);
                }
                renderItem = new ItemStack(renderMcItem);
                int renderCount = GsonHelper.getAsInt(renderObj, "count", 1);
                if (renderCount > 0) {
                    renderItem.setCount(renderCount);
                }
                if (renderObj.has("nbt")) {
                    JsonElement renderNbtEl = renderObj.get("nbt");
                    try {
                        CompoundTag renderNbt;
                        if (renderNbtEl.isJsonObject()) {
                            renderNbt = TagParser.parseTag(renderNbtEl.toString());
                        } else if (renderNbtEl.isJsonPrimitive()) {
                            renderNbt = TagParser.parseTag(GsonHelper.convertToString(renderNbtEl, "render_item.nbt"));
                        } else {
                            throw new JsonParseException("Invalid render_item.nbt element");
                        }
                        renderItem.setTag(renderNbt);
                    } catch (Exception e) {
                        throw new JsonParseException("Failed to parse render_item.nbt: " + e.getMessage());
                    }
                }
            }

            CompoundTag nbt = null;
            if (obj.has("nbt")) {
                if (obj.has("blurry_nbt")) {
                    throw new JsonParseException("Hybridizer ingredient cannot have both 'nbt' and 'blurry_nbt'");
                }
                JsonElement nbtEl = obj.get("nbt");
                try {
                    if (nbtEl.isJsonObject()) {
                        nbt = TagParser.parseTag(nbtEl.toString());
                    } else if (nbtEl.isJsonPrimitive()) {
                        nbt = TagParser.parseTag(GsonHelper.convertToString(nbtEl, "nbt"));
                    } else {
                        throw new JsonParseException("Invalid nbt element");
                    }
                } catch (Exception e) {
                    throw new JsonParseException("Failed to parse nbt: " + e.getMessage());
                }

                return new Simple(count, itemId, tagId, nbt, NbtMode.EXACT, renderItem);
            }

            if (obj.has("blurry_nbt")) {
                JsonElement nbtEl = obj.get("blurry_nbt");
                try {
                    if (nbtEl.isJsonObject()) {
                        nbt = TagParser.parseTag(nbtEl.toString());
                    } else if (nbtEl.isJsonPrimitive()) {
                        nbt = TagParser.parseTag(GsonHelper.convertToString(nbtEl, "blurry_nbt"));
                    } else {
                        throw new JsonParseException("Invalid blurry_nbt element");
                    }
                } catch (Exception e) {
                    throw new JsonParseException("Failed to parse blurry_nbt: " + e.getMessage());
                }

                return new Simple(count, itemId, tagId, nbt, NbtMode.BLURRY, renderItem);
            }

            return new Simple(count, itemId, tagId, null, NbtMode.NONE, renderItem);
        }

        static Simple fromNetwork(FriendlyByteBuf buf) {
            boolean isItem = buf.readBoolean();
            ResourceLocation id = buf.readResourceLocation();
            int count = buf.readVarInt();
            CompoundTag nbt = buf.readNullable(FriendlyByteBuf::readNbt);

            // Back-compat: older payload had no mode; it implied EXACT if nbt != null.
            NbtMode mode = nbt != null ? NbtMode.EXACT : NbtMode.NONE;
            if (buf.readableBytes() > 0) {
                int ordinal = buf.readVarInt();
                if (ordinal < 0 || ordinal >= NbtMode.values().length) {
                    throw new IllegalArgumentException("Invalid NbtMode ordinal: " + ordinal);
                }
                mode = NbtMode.values()[ordinal];
            }

            ItemStack renderItem = null;
            if (buf.readableBytes() > 0) {
                renderItem = buf.readNullable(FriendlyByteBuf::readItem);
            }

            return isItem ? new Simple(count, id, null, nbt, mode, renderItem) : new Simple(count, null, id, nbt, mode, renderItem);
        }

        @Override
        public boolean test(ItemStack stack, @Nullable Level level) {
            if (stack.isEmpty()) return false;
            if (stack.getCount() < count) return false;

            if (itemId != null) {
                Item item = ForgeRegistries.ITEMS.getValue(itemId);
                if (item == null || stack.getItem() != item) return false;
            } else {
                // tagId
                // TagKey lookup: namespace+path is enough; actual contents are resolved by ItemStack#is
                ResourceLocation nonNullTagId = tagId;
                if (nonNullTagId == null) return false;
                TagKey<Item> key = TagKey.create(ForgeRegistries.ITEMS.getRegistryKey(), nonNullTagId);
                if (!stack.is(key)) return false;
            }

            if (nbtMode == NbtMode.NONE || nbt == null) {
                return true;
            }

            if (!stack.hasTag()) return false;

            if (nbtMode == NbtMode.EXACT) {
                // Exact match: stack must have tag and equals()
                return nbt.equals(stack.getTag());
            }

            if (nbtMode == NbtMode.BLURRY) {
                // Fuzzy match: recipe NBT must be a subset of the stack tag.
                // 'true' means compare list contents.
                return net.minecraft.nbt.NbtUtils.compareNbt(nbt, stack.getTag(), true);
            }

            return true;
        }

        @Override
        public List<ItemStack> previewStacks() {
            List<ItemStack> out = new ArrayList<>();
            if (itemId != null) {
                Item item = ForgeRegistries.ITEMS.getValue(itemId);
                if (item != null) {
                    ItemStack stack = new ItemStack(item);
                    if (nbt != null && nbtMode != NbtMode.NONE) stack.setTag(nbt.copy());
                    out.add(stack);
                }
            }
            return out;
        }

        @SuppressWarnings("removal")
        @Override
        public void toNetwork(FriendlyByteBuf buf) {
            buf.writeVarInt(0);
            buf.writeBoolean(itemId != null);
            ResourceLocation id = itemId != null ? itemId : tagId;
            if (id == null) {
                // should never happen due to constructor validation
                id = ResourceLocation.fromNamespaceAndPath("minecraft", "air");
            }
            buf.writeResourceLocation(id);
            buf.writeVarInt(count);
            buf.writeNullable(nbt, FriendlyByteBuf::writeNbt);
            buf.writeVarInt(nbtMode.ordinal());
            buf.writeNullable(renderItem, FriendlyByteBuf::writeItem);
        }
    }

    record Alternatives(int count, List<Simple> alternatives) implements HybridizerIngredient {
        public Alternatives {
            if (count <= 0) {
                throw new IllegalArgumentException("count must be > 0");
            }
        }

        static Alternatives fromNetwork(FriendlyByteBuf buf) {
            int n = buf.readVarInt();
            int count = buf.readVarInt();
            List<Simple> alts = new ArrayList<>(n);
            for (int i = 0; i < n; i++) {
                // nested Simple payload without kind prefix
                alts.add(Simple.fromNetwork(buf));
            }
            return new Alternatives(count, alts);
        }

        @Override
        public boolean test(ItemStack stack, @Nullable Level level) {
            if (stack.isEmpty()) return false;
            if (stack.getCount() < count) return false;
            for (Simple s : alternatives) {
                if (s.test(stack, level)) return true;
            }
            return false;
        }

        @Override
        public int count() {
            return count;
        }

        @Override
        public List<ItemStack> previewStacks() {
            List<ItemStack> out = new ArrayList<>();
            for (Simple s : alternatives) {
                out.addAll(s.previewStacks());
            }
            return out;
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf) {
            buf.writeVarInt(1);
            buf.writeVarInt(alternatives.size());
            buf.writeVarInt(count);
            for (Simple s : alternatives) {
                // write Simple payload without kind prefix
                buf.writeBoolean(s.itemId != null);
                ResourceLocation id = s.itemId != null ? s.itemId : s.tagId;
                if (id == null) {
                    id = ResourceLocation.fromNamespaceAndPath("minecraft", "air");
                }
                buf.writeResourceLocation(id);
                buf.writeVarInt(s.count);
                buf.writeNullable(s.nbt, FriendlyByteBuf::writeNbt);
                buf.writeVarInt(s.nbtMode.ordinal());
                buf.writeNullable(s.renderItem, FriendlyByteBuf::writeItem);
            }
        }
    }
}


