package com.y271727uy.shopcore.core.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/** One attachment per BlockEntity. Never invokes user factories from the base BE constructor. */
final class BoundCapabilityProvider<T> implements ICapabilitySerializable<CompoundTag> {
    private final Capability<T> capability;
    private final Supplier<T> factory;
    private final Function<T, CompoundTag> save;
    private final BiConsumer<T, CompoundTag> load;
    private final Predicate<Direction> sides;
    private final LazyOptional<T> optional = LazyOptional.of(this::value);
    private T value;
    private boolean valid = true;

    BoundCapabilityProvider(Capability<T> capability, Supplier<T> factory, Function<T, CompoundTag> save,
                            BiConsumer<T, CompoundTag> load, Predicate<Direction> sides) {
        this.capability = capability;
        this.factory = factory;
        this.save = save;
        this.load = load;
        this.sides = sides;
    }

    private T value() {
        if (value == null) value = Objects.requireNonNull(factory.get(), "Capability factory returned null");
        return value;
    }

    @Override
    public <C> LazyOptional<C> getCapability(Capability<C> requested, @Nullable Direction side) {
        return valid && requested == capability && sides.test(side) ? optional.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        // Saving removed providers is allowed (e.g. Forge snapshots); their public handle stays invalid.
        return Objects.requireNonNull(save.apply(value()), "Capability serializer returned null").copy();
    }

    @Override
    public void deserializeNBT(CompoundTag tag) { load.accept(value(), tag.copy()); }

    void invalidate() {
        valid = false;
        optional.invalidate();
    }
}
