package com.y271727uy.shopcore.core.consumer.common.entity;

import com.y271727uy.shopcore.core.consumer.common.ConsumerActor;
import net.minecraft.world.entity.Entity;

public interface EntityBackedConsumerActor extends ConsumerActor {
    Entity entity();
}
