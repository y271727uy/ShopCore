package com.list.block;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Standalone recipe holder and helper utilities for Tapper.
 */
public class TapperRecipe {
    public final String input;
    public final String output;
    public final int time;
    public final String tool;

    public TapperRecipe(String input, String output, int time, String tool) {
        this.input = input;
        this.output = output;
        this.time = time;
        this.tool = tool == null ? "" : tool.trim();
    }

    public static final TapperRecipe[] RECIPES = {
            new TapperRecipe("yeastnfeast:maple_log", "yeastnfeast:maple_syrup", 24000, "minecraft:glass_bottle"),
            new TapperRecipe("forestry:maple_log", "yeastnfeast:maple_syrup", 24000, "minecraft:glass_bottle"),
            new TapperRecipe("minecraft:birch_log", "'cosmopolitan:birch_sap_bottle'", 24000, "minecraft:glass_bottle"),
            new TapperRecipe("foragersinsight:sappy_birch_log", "foragersinsight:birch_syrup_bottle", 30000, "minecraft:glass_bottle"),

            new TapperRecipe("alexscaves:pewen_log","alexscaves:pewen_sap",30000,null),
            new TapperRecipe("minecraft:oak_log", "list:tree_sap", 30000, null),
            new TapperRecipe("minecraft:dark_oak_log", "list:tree_sap", 30000, null),
            new TapperRecipe("#tapper_log", "list:tree_sap", 30000, null),
            //new TapperRecipe("#tapper_pine_log", "list:pine_sap", 30000, "null"), 松脂
            new TapperRecipe("minecraft:jungle_log", "list:urushi_bucket", 30000, "minecraft:bucket"), //乳胶
            new TapperRecipe("forestry:zebrawood_log", "list:raw_lacquer_bucket", 30000, "minecraft:bucket"), //漆树
            //new TapperRecipe("forestry:acacia_desert_log", "", 30000, null), 阿拉伯胶
            //new TapperRecipe("minecraft:acacia_log", "", 30000, null),
            //new TapperRecipe("minecraft:cherry_log", "", 30000, null), 樱树胶
            //new TapperRecipe("vinery:dark_cherry_log", "", 30000, null),
            //new TapperRecipe("forestry:hill_cherry_log", "", 30000, null),
            //new TapperRecipe("minecraft:spruce_log", "", 30000, null),  杉树香脂
            //new TapperRecipe("forestry:sequoia_log", "", 30000, null),
            //new TapperRecipe("forestry:giganteum_log", "", 30000, null),
    };

    public boolean hasToolRequirement() {
        return tool != null && !tool.isBlank();
    }


    public static TapperRecipe findAttachedRecipe(Level level, BlockPos pos, Direction facing) {
        BlockPos attachedPos = switch (facing) {
            case NORTH -> pos.south();
            case SOUTH -> pos.north();
            case EAST -> pos.west();
            case WEST -> pos.east();
            default -> null;
        };

        if (attachedPos == null) return null;
        BlockState attachedState = level.getBlockState(attachedPos);
        Block attached = attachedState.getBlock();
        ResourceLocation key = ForgeRegistries.BLOCKS.getKey(attached);
        String id = key == null ? null : key.toString();

        for (TapperRecipe r : RECIPES) {
            if (r.input == null) continue;
            // tag syntax: start with '#', e.g. "#minecraft:logs"
            if (r.input.startsWith("#")) {
                String tagStr = r.input.substring(1);
                ResourceLocation tagRl = ResourceLocation.tryParse(tagStr);
                if (tagRl == null) continue;
                TagKey<Block> tag = TagKey.create(ForgeRegistries.BLOCKS.getRegistryKey(), tagRl);
                if (attachedState.is(tag)) return r;
            } else {
                if (id != null && id.equals(r.input)) return r;
            }
        }
        return null;
    }

    public static ItemStack parseOutput(String outStr) {
        int count = 1;
        String s = outStr;
        if (s.contains("x ")) {
            String[] parts = s.split("x ", 2);
            try {
                count = Integer.parseInt(parts[0].trim());
                s = parts[1];
            } catch (NumberFormatException ignored) {
            }
        }
        ResourceLocation rl = ResourceLocation.tryParse(s);
        if (rl == null) return ItemStack.EMPTY;
        Item item = ForgeRegistries.ITEMS.getValue(rl);
        if (item == null) return ItemStack.EMPTY;
        return new ItemStack(item, count);
    }
}
