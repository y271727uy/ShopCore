package com.y271727uy.shopcore.core.capability;

import com.y271727uy.shopcore.ShopcoreMod;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Set;

/**
 * Internal bridge for bootstrap and mixins, NOT a supported integration API.
 * Registration and implementation classes deliberately have package visibility.
 */
public final class BlockBindingHooks {
    private static final DeferredRegister<BlockEntityType<?>> TYPES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ShopcoreMod.MODID);
    private static final RegistryObject<BlockEntityType<Carrier>> CARRIER = TYPES.register("capability_carrier",
            () -> new BlockEntityType<>(Carrier::new, Set.of(), null));

    private BlockBindingHooks() { }

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
        modBus.addListener((FMLLoadCompleteEvent event) -> event.enqueueWork(BlockBindingRegistry::freeze));
        MinecraftForge.EVENT_BUS.addGenericListener(BlockEntity.class, BlockBindingRegistry::attach);
    }

    static BlockEntityType<?> carrierType() { return CARRIER.get(); }

    public static boolean hasBinding(BlockState state) { return BlockBindingRegistry.hasEntity(state); }

    public static boolean accepts(BlockState state, BlockEntityType<?> type) {
        return BlockBindingRegistry.accepts(state, type);
    }

    public static BlockEntity create(BlockPos pos, BlockState state) {
        if (!hasBinding(state)) throw new IllegalArgumentException("No BlockEntity binding for " + state);
        return BlockBindingRegistry.create(pos, state);
    }

    public static <T extends BlockEntity> BlockEntityTicker<T> ticker(
            BlockState state, Level level, BlockEntityType<T> type) {
        return BlockBindingRegistry.ticker(state, level, type);
    }

    /** Plain blocks with capability-only bindings receive a minimal, persistent native BE host. */
    private static final class Carrier extends BlockEntity {
        private Carrier(BlockPos pos, BlockState state) { super(CARRIER.get(), pos, state); }
    }
}
