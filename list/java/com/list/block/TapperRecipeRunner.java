package com.list.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Direction;
import com.list.block.TapperBlock;
import com.list.block.TapperRecipe;

/**
 * Centralized hook for executing tapper recipes.
 *
 * The runner is intentionally simple and public: you can replace its logic or call its methods from other code.
 * If you later want to support modpack-level configuration or more complex output handling, replace
 * the implementation here or subclass it and call your subclass instead.
 */
public final class TapperRecipeRunner {
    private TapperRecipeRunner() {}

    public static void runRecipe(Level level, BlockPos pos, BlockState state, TapperRecipe recipe) {
        if (level.isClientSide()) return;

        ItemStack out = TapperRecipe.parseOutput(recipe.output);
        if (!out.isEmpty()) {
            Direction facing = state.getValue(TapperBlock.FACING);
            double dx = pos.getX() + 0.5 + facing.getStepX() * 0.6;
            double dy = pos.getY() + 0.5;
            double dz = pos.getZ() + 0.5 + facing.getStepZ() * 0.6;
            ItemEntity ei = new ItemEntity(level, dx, dy, dz, out.copy());
            level.addFreshEntity(ei);
        }

        var be = level.getBlockEntity(pos);
        if (be instanceof com.list.block.entity.TapperBlockEntity teb) {
            teb.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }
}




