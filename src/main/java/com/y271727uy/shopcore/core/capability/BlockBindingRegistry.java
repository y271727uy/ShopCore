package com.y271727uy.shopcore.core.capability;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.event.AttachCapabilitiesEvent;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/** Internal registration only. Publish immutable snapshots; freeze before any world is opened. */
final class BlockBindingRegistry {
    private static volatile Map<Block, EntityBinding<?>> entities = Map.of();
    private static volatile Map<Block, List<CapabilityBinding<?>>> capabilities = Map.of();
    private static boolean frozen;

    private BlockBindingRegistry() { }

    /** Register during common setup (enqueueWork), identically on server and client. */
    static synchronized <T extends BlockEntity> void bindBlockEntity(
            Block block, Supplier<BlockEntityType<T>> type,
            BlockEntityTicker<T> serverTicker, BlockEntityTicker<T> clientTicker) {
        checkMutable(block);
        if (block instanceof EntityBlock) {
            throw new IllegalArgumentException("Cannot replace a native BlockEntity; attach capabilities to it instead");
        }
        if (entities.containsKey(block)) throw new IllegalArgumentException("BlockEntity already bound: " + block);
        var copy = new LinkedHashMap<>(entities);
        copy.put(block, new EntityBinding<>(Objects.requireNonNull(type, "type"), serverTicker, clientTicker));
        entities = Map.copyOf(copy);
    }

    /**
     * Each owner gets a separate lazy value. The factory must use owner.setChanged() on mutations.
     * sides also receives null for unsided queries. This internal entry accepts only ShopCore provider IDs.
     * Capability type registration is still the responsibility of the owning subsystem.
     */
    static synchronized <T> void bindCapability(
            Block block, ResourceLocation id, Capability<T> capability,
            Function<BlockEntity, T> factory, Function<T, CompoundTag> save,
            BiConsumer<T, CompoundTag> load, Predicate<Direction> sides) {
        checkMutable(block);
        Objects.requireNonNull(id, "id");
        if (!id.getNamespace().equals("shopcore")) {
            throw new IllegalArgumentException("Only ShopCore capability bindings are supported: " + id);
        }
        var bindings = new ArrayList<>(capabilities.getOrDefault(block, List.of()));
        if (bindings.stream().anyMatch(binding -> binding.id.equals(id) || binding.capability == capability)) {
            throw new IllegalArgumentException("Duplicate capability binding: " + id);
        }
        bindings.add(new CapabilityBinding<>(id, Objects.requireNonNull(capability, "capability"),
                Objects.requireNonNull(factory, "factory"), Objects.requireNonNull(save, "save"),
                Objects.requireNonNull(load, "load"), Objects.requireNonNull(sides, "sides")));
        var copy = new LinkedHashMap<>(capabilities);
        copy.put(block, List.copyOf(bindings));
        capabilities = Map.copyOf(copy);
    }

    private static void checkMutable(Block block) {
        if (frozen) throw new IllegalStateException("Block bindings are frozen; runtime rebinding is not supported");
        Objects.requireNonNull(block, "block");
        if (block.defaultBlockState().isAir()) throw new IllegalArgumentException("Cannot bind air");
    }

    static synchronized void freeze() {
        for (var binding : entities.values()) {
            if (BlockEntityType.getKey(binding.type()) == null) {
                throw new IllegalStateException("Bound BlockEntityType must be registered");
            }
        }
        frozen = true;
    }

    static boolean hasEntity(BlockState state) {
        Block block = state.getBlock();
        return entities.containsKey(block) || (!(block instanceof EntityBlock) && capabilities.containsKey(block));
    }

    static boolean accepts(BlockState state, BlockEntityType<?> type) {
        var binding = entities.get(state.getBlock());
        return binding != null ? binding.type() == type
                : hasEntity(state) && type == BlockBindingHooks.carrierType();
    }

    static BlockEntity create(BlockPos pos, BlockState state) {
        var binding = entities.get(state.getBlock());
        BlockEntityType<?> type = binding == null ? BlockBindingHooks.carrierType() : binding.type();
        BlockEntity entity = Objects.requireNonNull(type.create(pos, state), "Bound BlockEntity factory returned null");
        if (entity.getType() != type || !entity.getBlockPos().equals(pos) || entity.getBlockState() != state) {
            throw new IllegalStateException("Bound BlockEntity factory returned an incompatible instance");
        }
        return entity;
    }

    @SuppressWarnings("unchecked")
    static <T extends BlockEntity> BlockEntityTicker<T> ticker(BlockState state, Level level, BlockEntityType<T> type) {
        var binding = entities.get(state.getBlock());
        if (binding == null || binding.type() != type) return null;
        // Identity comparison above guarantees the ticker belongs to the requested registered type.
        return (BlockEntityTicker<T>) (level.isClientSide ? binding.clientTicker : binding.serverTicker);
    }

    static void attach(AttachCapabilitiesEvent<BlockEntity> event) {
        BlockEntity owner = event.getObject();
        for (var binding : capabilities.getOrDefault(owner.getBlockState().getBlock(), List.of())) {
            binding.attach(event, owner);
        }
    }

    private record EntityBinding<T extends BlockEntity>(Supplier<BlockEntityType<T>> supplier,
                                                        BlockEntityTicker<T> serverTicker,
                                                        BlockEntityTicker<T> clientTicker) {
        private BlockEntityType<T> type() { return Objects.requireNonNull(supplier.get(), "Unresolved BlockEntityType"); }
    }

    private record CapabilityBinding<T>(ResourceLocation id, Capability<T> capability,
                                         Function<BlockEntity, T> factory, Function<T, CompoundTag> save,
                                         BiConsumer<T, CompoundTag> load, Predicate<Direction> sides) {
        private void attach(AttachCapabilitiesEvent<BlockEntity> event, BlockEntity owner) {
            var provider = new BoundCapabilityProvider<>(capability, () -> factory.apply(owner), save, load, sides);
            event.addCapability(id, provider);
            event.addListener(provider::invalidate);
        }
    }
}
