package com.y271727uy.shopcore.core.order.taking;
import com.y271727uy.shopcore.core.consumer.common.ConsumerActor;
import com.y271727uy.shopcore.core.consumer.common.entity.EntityBackedConsumerActor;
import net.minecraft.nbt.CompoundTag;
import java.util.OptionalLong;
public final class EntityOrderTakingMemory implements OrderTakingMemory {
    // Retain the existing saved entity key.
    public static final String REVEAL_GAME_TIME_TAG = "ShopCoreOverheadOrderRevealGameTime";
    private CompoundTag data(ConsumerActor actor) {
        if (actor instanceof EntityBackedConsumerActor entity) return entity.entity().getPersistentData();
        throw new IllegalArgumentException("actor must be entity-backed");
    }
    public OptionalLong revealTime(ConsumerActor actor) {
        CompoundTag data = data(actor);
        return data.contains(REVEAL_GAME_TIME_TAG) ? OptionalLong.of(data.getLong(REVEAL_GAME_TIME_TAG)) : OptionalLong.empty();
    }
    public void setRevealTime(ConsumerActor actor, long time) { data(actor).putLong(REVEAL_GAME_TIME_TAG, time); }
    public void clear(ConsumerActor actor) { data(actor).remove(REVEAL_GAME_TIME_TAG); }
}
