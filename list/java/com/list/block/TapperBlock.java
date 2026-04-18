package com.list.block;

import com.list.all.ModBlockEntities;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
// TapperRecipeRunner exists as a helper but we inline execution here to avoid signature mismatches
import net.minecraft.world.InteractionHand;
// ...existing code...
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TapperBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public static final BooleanProperty WORKING = BooleanProperty.create("working");
    public static final BooleanProperty MATURE = BooleanProperty.create("mature");

    public TapperBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH)
                .setValue(WORKING, false).setValue(MATURE, false));
    }

    // Boxes derived from Blockbench model (from/to coordinates). We'll build the full shape by OR'ing these boxes.
    // If any box has zero thickness on an axis (from==to), add a tiny epsilon to ensure a valid AABB.
    private static final double EPS = 0.01D;
    private static final double[][] BOXES = new double[][]{
            {4.0, 0.0, 7.0, 12.0, 3.0, 15.0},
            {12.0, 0.0, 6.0, 13.0, 12.0, 16.0},
            {3.0, 0.0, 6.0, 4.0, 12.0, 16.0},
            {4.0, 0.0, 15.0, 12.0, 12.0, 16.0},
            {4.0, 0.0, 6.0, 12.0, 12.0, 7.0},
            {7.0, 13.0, 9.0, 9.0, 14.0, 11.0},
            {7.0, 14.0, 9.0, 9.0, 16.0, 16.0},
            {3.5, 12.0, 11.0, 3.5, 16.0, 13.0},
            {3.5, 16.02, 11.0, 12.5, 16.02, 13.0},
            {12.5, 12.0, 11.0, 12.5, 16.0, 13.0},
            {6.0, 13.0, 15.0, 10.0, 17.0, 16.0},
            {6.5, 16.01, 8.0, 9.5, 16.01, 11.0}
    };

    private static VoxelShape buildShapeForFacing(int k) {
        VoxelShape out = Shapes.empty();
        for (double[] b : BOXES) {
            double x1 = b[0], y1 = b[1], z1 = b[2], x2 = b[3], y2 = b[4], z2 = b[5];
            // ensure non-zero thickness
            if (Math.abs(x2 - x1) < 1e-6) x2 = x1 + EPS;
            if (Math.abs(y2 - y1) < 1e-6) y2 = y1 + EPS;
            if (Math.abs(z2 - z1) < 1e-6) z2 = z1 + EPS;
            double[] r = rotateBox(x1, y1, z1, x2, y2, z2, k);
            out = Shapes.or(out, Block.box(r[0], r[1], r[2], r[3], r[4], r[5]));
        }
        return out;
    }

    private static final VoxelShape SHAPE_NORTH = buildShapeForFacing(0);
    private static final VoxelShape SHAPE_EAST = buildShapeForFacing(1);
    private static final VoxelShape SHAPE_SOUTH = buildShapeForFacing(2);
    private static final VoxelShape SHAPE_WEST = buildShapeForFacing(3);

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new com.list.block.entity.TapperBlockEntity(ModBlockEntities.TAPPER.get(), pos, state);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @SuppressWarnings("deprecation")
    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @SuppressWarnings("deprecation")
    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING, WORKING, MATURE);
    }

    @SuppressWarnings("deprecation")
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        boolean working = state.getValue(WORKING);
        boolean mature = state.getValue(MATURE);
        Direction facing = state.getValue(FACING);
        TapperRecipe recipe = getAttachedRecipe(level, pos, facing);

        if (mature && recipe != null) {
            ItemStack held = player.getItemInHand(hand);
            ItemStack requiredTool = recipe.hasToolRequirement() ? TapperRecipe.parseOutput(recipe.tool) : ItemStack.EMPTY;
            if (recipe.hasToolRequirement() && (requiredTool.isEmpty() || held.isEmpty() || !ItemStack.isSameItemSameTags(held, requiredTool))) {
                return InteractionResult.CONSUME;
            }

            // consume exactly one tool item
            if (recipe.hasToolRequirement()) {
                held.shrink(1);
            }

            // execute recipe: spawn output and reset
            ItemStack out = TapperRecipe.parseOutput(recipe.output);
            if (!out.isEmpty()) {
                double dx = pos.getX() + 0.5 + facing.getStepX() * 0.6;
                double dy = pos.getY() + 0.5;
                double dz = pos.getZ() + 0.5 + facing.getStepZ() * 0.6;
                net.minecraft.world.entity.item.ItemEntity ei = new net.minecraft.world.entity.item.ItemEntity(level, dx, dy, dz, out.copy());
                level.addFreshEntity(ei);
            }

            // play pouring sound when player collects (safe registry lookup)
            // play pouring/collect sound when player collects
                try {
                    // try send sound specifically to the interacting player
                    if (player instanceof net.minecraft.server.level.ServerPlayer sp) {
                        try {
                            Class<?> pktCls = Class.forName("net.minecraft.network.protocol.game.ClientboundSoundPacket");
                            java.lang.reflect.Constructor<?> ctor = null;
                            for (var c : pktCls.getConstructors()) {
                                var params = c.getParameterTypes();
                                if (params.length >= 6 && params[0].getName().equals("net.minecraft.sounds.SoundEvent") && params[1].getName().equals("net.minecraft.sounds.SoundSource")) {
                                    ctor = c;
                                    break;
                                }
                            }
                            if (ctor != null) {
                                Object packet = ctor.newInstance(net.minecraft.sounds.SoundEvents.BUCKET_EMPTY, net.minecraft.sounds.SoundSource.BLOCKS, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1.0f, 1.0f);
                                sp.connection.send((net.minecraft.network.protocol.Packet<?>) packet);
                            } else {
                                level.playSound(sp, pos, net.minecraft.sounds.SoundEvents.BUCKET_EMPTY, net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 1.0f);
                            }
                        } catch (Throwable ex) {
                            level.playSound(sp, pos, net.minecraft.sounds.SoundEvents.BUCKET_EMPTY, net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 1.0f);
                        }
                    } else {
                        level.playSound(player, pos, net.minecraft.sounds.SoundEvents.BUCKET_EMPTY, net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 1.0f);
                    }
                } catch (Throwable t) {
                    level.playSound(player, pos, net.minecraft.sounds.SoundEvents.BUCKET_EMPTY, net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 1.0f);
                }

            level.setBlock(pos, state.setValue(WORKING, false).setValue(MATURE, false).setValue(FACING, facing), 3);
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof com.list.block.entity.TapperBlockEntity teb) {
                teb.startTime = 0L;
                teb.recipeTime = 0;
                teb.initialized = false;
                teb.setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
            }

            return InteractionResult.CONSUME;
        } else if (working && recipe != null) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof com.list.block.entity.TapperBlockEntity teb) {
                if (teb.startTime > 0L) {
                    long remaining = getRemainingTime(level, teb, recipe);
                    if (remaining <= 0L) {
                        level.setBlock(pos, state.setValue(WORKING, true).setValue(MATURE, true).setValue(FACING, facing), 3);
                        player.sendSystemMessage(Component.literal("§6收集已完成，点击即可收获！"));
                        return InteractionResult.CONSUME;
                    }

                    long minutes = (remaining / 20) / 60;
                    long seconds = (remaining / 20) % 60;
                    String timeStr = (minutes > 0 ? minutes + "分" : "") + seconds + "秒";
                    player.sendSystemMessage(Component.literal("§7剩余时间: " + timeStr));
                    ItemStack out = TapperRecipe.parseOutput(recipe.output);
                    if (!out.isEmpty()) {
                        player.sendSystemMessage(Component.literal("§7当前产出: " + out.getHoverName().getString()));
                    }
                    return InteractionResult.CONSUME;
                } else {
                    player.sendSystemMessage(Component.literal("§c收集器计时异常，已自动重启"));
                    resetAndRestartTapper(level, pos, state);
                    return InteractionResult.CONSUME;
                }
            }
        } else {
            player.sendSystemMessage(Component.literal("§c请将收集器附着在合适的树木上"));
            return InteractionResult.CONSUME;
        }

        return InteractionResult.CONSUME;
    }

    // Recipes and parsing are provided by TapperRecipe helper class.
    @Nullable
    public static TapperRecipe getAttachedRecipe(Level level, BlockPos pos, Direction facing) {
        return TapperRecipe.findAttachedRecipe(level, pos, facing);
    }

    private static long getRemainingTime(Level level, com.list.block.entity.TapperBlockEntity teb, TapperRecipe recipe) {
        if (teb.startTime == 0L) return recipe.time;
        long elapsed = level.getGameTime() - teb.startTime;
        return Math.max(0L, (long)recipe.time - elapsed);
    }

    private static void resetAndRestartTapper(Level level, BlockPos pos, BlockState state) {
        level.setBlock(pos, state.setValue(WORKING, false).setValue(MATURE, false).setValue(FACING, state.getValue(FACING)), 3);
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof com.list.block.entity.TapperBlockEntity teb) {
            teb.startTime = 0L;
            teb.recipeTime = 0;
            teb.initialized = false;
            teb.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case NORTH -> SHAPE_NORTH;
            case EAST -> SHAPE_EAST;
            case SOUTH -> SHAPE_SOUTH;
            case WEST -> SHAPE_WEST;
            default -> SHAPE_NORTH;
        };
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case NORTH -> SHAPE_NORTH;
            case EAST -> SHAPE_EAST;
            case SOUTH -> SHAPE_SOUTH;
            case WEST -> SHAPE_WEST;
            default -> SHAPE_NORTH;
        };
    }

    // rotateY removed — we build per-facing shapes directly from BOXES

    // rotate a single AABB by k*90 degrees clockwise around center (8, *, 8)
    private static double[] rotateBox(double x1, double y1, double z1, double x2, double y2, double z2, int k) {
        k = ((k % 4) + 4) % 4;
        double nx1 = x1, nz1 = z1, nx2 = x2, nz2 = z2;
        switch (k) {
            case 0:
                // no change
                break;
            case 1:
                // 90 deg: newX = 16 - z2; newZ = x1
                nx1 = 16.0D - z2;
                nx2 = 16.0D - z1;
                nz1 = x1;
                nz2 = x2;
                break;
            case 2:
                // 180 deg
                nx1 = 16.0D - x2;
                nx2 = 16.0D - x1;
                nz1 = 16.0D - z2;
                nz2 = 16.0D - z1;
                break;
            case 3:
                // 270 deg
                nx1 = z1;
                nx2 = z2;
                nz1 = 16.0D - x2;
                nz2 = 16.0D - x1;
                break;
        }
        return new double[]{nx1, y1, nz1, nx2, y2, nz2};
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide) {
            return null;
        }
        return createTickerHelper(
                blockEntityType,
                ModBlockEntities.TAPPER.get(),
                (l, p, s, be) -> be.tick(l, p, s)
        );
    }

    @SuppressWarnings("deprecation")
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}










