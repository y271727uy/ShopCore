package com.list.gameplay.fish_group.entity;

import com.list.gameplay.fish_group.pool.FishPoolDefinition;
import com.list.gameplay.fish_group.pool.FishPoolFactory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class OceanFishPoolEntity extends AbstractFishPoolEntity {
    public final AnimationState idleAnimationState = new AnimationState();
    private int idleAnimationTimeout = 0;

    public OceanFishPoolEntity(EntityType<? extends OceanFishPoolEntity> type, Level level) {
        super(type, level);
        this.setFishPoolDefinition(resolveDefinitionIdFromType(type));
    }

    @Override
    protected FishPoolDefinition.Environment getEnvironment() {
        return FishPoolDefinition.Environment.OCEAN;
    }

    @Override
    protected ResourceLocation getDefaultFishPoolId() {
        return FishPoolFactory.OCEAN_FISH_POOL.id();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            updateAnimations();
        }
    }

    private void updateAnimations() {
        if (this.idleAnimationTimeout <= 0) {
            this.idleAnimationTimeout = 200;
            this.idleAnimationState.start(this.tickCount);
        } else {
            --this.idleAnimationTimeout;
        }
    }
}

