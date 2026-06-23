package com.y271727uy.shopcore.core.consumer.common.entity;

import com.y271727uy.shopcore.core.consumer.common.ConsumerActor;
import com.y271727uy.shopcore.core.consumer.common.ConsumerMemory;
import com.y271727uy.shopcore.core.consumer.common.wait.QueueConsumerPhase;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.Entity;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class EntityPersistentConsumerMemory implements ConsumerMemory {
    private static final String ROOT_TAG = "ShopCoreConsumer";
    private static final String PHASE_TAG = "QueuePhase";
    private static final String JOINED_GAME_TIME_TAG = "JoinedGameTime";
    private static final String ORDER_ID_TAG = "OrderId";
    private static final String SHOP_POS_TAG = "ShopPos";

    @Override
    public QueueConsumerPhase phase(ConsumerActor actor) {
        CompoundTag tag = readTag(actor);
        if (!tag.contains(PHASE_TAG)) {
            return null;
        }
        try {
            return QueueConsumerPhase.valueOf(tag.getString(PHASE_TAG));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    @Override
    public void setPhase(ConsumerActor actor, QueueConsumerPhase phase) {
        Objects.requireNonNull(phase, "phase");
        writeTag(actor).putString(PHASE_TAG, phase.name());
    }

    @Override
    public long joinedGameTime(ConsumerActor actor) {
        return readTag(actor).getLong(JOINED_GAME_TIME_TAG);
    }

    @Override
    public void setJoinedGameTime(ConsumerActor actor, long gameTime) {
        if (gameTime < 0L) {
            throw new IllegalArgumentException("gameTime cannot be negative");
        }
        writeTag(actor).putLong(JOINED_GAME_TIME_TAG, gameTime);
    }

    @Override
    public Optional<UUID> orderId(ConsumerActor actor) {
        CompoundTag tag = readTag(actor);
        if (!tag.hasUUID(ORDER_ID_TAG)) {
            return Optional.empty();
        }
        return Optional.of(tag.getUUID(ORDER_ID_TAG));
    }

    @Override
    public void setOrderId(ConsumerActor actor, UUID orderId) {
        Objects.requireNonNull(orderId, "orderId");
        writeTag(actor).putUUID(ORDER_ID_TAG, orderId);
    }

    @Override
    public void clearOrderId(ConsumerActor actor) {
        writeTag(actor).remove(ORDER_ID_TAG);
    }

    public Optional<BlockPos> shopPos(ConsumerActor actor) {
        CompoundTag tag = readTag(actor);
        if (!tag.contains(SHOP_POS_TAG)) {
            return Optional.empty();
        }
        return Optional.of(NbtUtils.readBlockPos(tag.getCompound(SHOP_POS_TAG)));
    }

    public void setShopPos(ConsumerActor actor, BlockPos shopPos) {
        Objects.requireNonNull(shopPos, "shopPos");
        writeTag(actor).put(SHOP_POS_TAG, NbtUtils.writeBlockPos(shopPos));
    }

    public void clearShopPos(ConsumerActor actor) {
        writeTag(actor).remove(SHOP_POS_TAG);
    }

    protected CompoundTag readTag(ConsumerActor actor) {
        return requireEntity(actor).getPersistentData().getCompound(ROOT_TAG);
    }

    protected CompoundTag writeTag(ConsumerActor actor) {
        Entity entity = requireEntity(actor);
        CompoundTag persistentData = entity.getPersistentData();
        CompoundTag tag = persistentData.getCompound(ROOT_TAG);
        persistentData.put(ROOT_TAG, tag);
        return tag;
    }

    protected Entity requireEntity(ConsumerActor actor) {
        Objects.requireNonNull(actor, "actor");
        if (actor instanceof EntityBackedConsumerActor entityActor) {
            return entityActor.entity();
        }
        throw new IllegalArgumentException("actor must be an EntityBackedConsumerActor");
    }
}
