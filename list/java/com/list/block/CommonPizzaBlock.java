package com.list.block;

import com.list.block.pizza.PizzaBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Supplier;

/**
 * 通用披萨方块包装类，复用现有 servings-based {@link PizzaBlock} 行为。
 * 这个文件之前混入了未完成的带 BlockEntity 模板代码，导致类名、构造器和依赖全部失配。
 */
public class CommonPizzaBlock extends PizzaBlock {

    private static final VoxelShape FULL_SHAPE = box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);
    private static final VoxelShape TWO_SLICE_SHAPE = Shapes.or(
            box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 8.0D),
            box(0.0D, 0.0D, 8.0D, 8.0D, 2.0D, 16.0D)
    );
    private static final VoxelShape ONE_SLICE_SHAPE = Shapes.or(
            box(0.0D, 0.0D, 0.0D, 8.0D, 2.0D, 16.0D),
            box(8.0D, 0.0D, 0.0D, 16.0D, 2.0D, 8.0D)
    );
    private static final VoxelShape LAST_SLICE_SHAPE = box(0.0D, 0.0D, 0.0D, 8.0D, 2.0D, 8.0D);

    public CommonPizzaBlock(Supplier<? extends Item> sliceProvider, BlockBehaviour.Properties properties) {
        super(sliceProvider, properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getPizzaShape(state);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getPizzaShape(state);
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return getPizzaShape(state);
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    private static VoxelShape getPizzaShape(BlockState state) {
        return switch (state.getValue(SERVINGS)) {
            case 3 -> FULL_SHAPE;
            case 2 -> TWO_SLICE_SHAPE;
            case 1 -> ONE_SLICE_SHAPE;
            case 0 -> LAST_SLICE_SHAPE;
            default -> FULL_SHAPE;
        };
    }
}