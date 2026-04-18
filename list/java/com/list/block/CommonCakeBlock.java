package com.list.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.function.Supplier;

/**
 * 通用蛋糕方块，基于原版 {@link CakeBlock} 行为，只替换食用时的营养来源。
 */
public class CommonCakeBlock extends CakeBlock {

    private final Supplier<? extends Item> sliceProvider;

    public CommonCakeBlock(Supplier<? extends Item> sliceProvider, BlockBehaviour.Properties properties) {
        super(properties);
        this.sliceProvider = sliceProvider;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        FoodProperties food = sliceProvider.get().getFoodProperties();
        if (food != null && player.canEat(food.canAlwaysEat())) {
            player.getFoodData().eat(food.getNutrition(), food.getSaturationModifier());
            int bites = state.getValue(BITES);
            if (bites < 6) {
                level.setBlock(pos, state.setValue(BITES, bites + 1), 3);
            } else {
                level.removeBlock(pos, false);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}




