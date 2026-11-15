package com.y271727uy.shopcore.core.consumer.common.movement;

import com.y271727uy.shopcore.core.consumer.common.ConsumerActor;
import com.y271727uy.shopcore.core.consumer.common.ConsumerMemory;
import com.y271727uy.shopcore.core.consumer.common.ConsumerNavigator;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** A shop's replaceable movement script; the navigator remains the movement backend. */
public interface ConsumerMovement {
    Vec3 spawnPosition();

    boolean entranceClear(List<ConsumerActor> actors);

    void tick(List<ConsumerActor> actors, ConsumerNavigator navigator, ConsumerMemory memory,
              boolean shopOpen, long gameTime);
}
