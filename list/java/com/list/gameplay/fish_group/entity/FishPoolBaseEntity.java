package com.list.gameplay.fish_group.entity;

import com.list.util.FishPoolHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import java.util.List;

public class FishPoolBaseEntity extends Entity {
    private static final int MAX_INTERACTIONS = 3;
    private static final int HOOK_INTERACTING_DURATION = 10;
    private static final float DESTRUCTION_SPEED = 0.05F;
    private static final EntityDataAccessor<Boolean> IS_DESTROYING = SynchedEntityData.defineId(FishPoolBaseEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> DESTRUCTION_PROGRESS = SynchedEntityData.defineId(FishPoolBaseEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> HURT_TIME = SynchedEntityData.defineId(FishPoolBaseEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> HOOK_INTERACTING_TICKS = SynchedEntityData.defineId(FishPoolBaseEntity.class, EntityDataSerializers.INT);
    private float randomRotation;
    private int interactions = 0;
    private int lifeTicks;
    private int maxLifeTicks;

    public FishPoolBaseEntity(EntityType<? extends FishPoolBaseEntity> type, Level level) {
        super(type, level);
        this.randomRotation = this.random.nextFloat() * 360.0F;
        this.lifeTicks = 0;
        this.maxLifeTicks = 9600;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(IS_DESTROYING, false);
        this.entityData.define(DESTRUCTION_PROGRESS, 0.0F);
        this.entityData.define(HURT_TIME, 0);
        this.entityData.define(HOOK_INTERACTING_TICKS, 0);
    }

    @Override
    public void tick() {
        super.tick();
        this.lifeTicks++;

        if (!this.level().isClientSide) {
            if (this.lifeTicks == 1) {
                spawnPlacementParticles();
            }

            if (this.lifeTicks >= this.maxLifeTicks) {
                this.remove(RemovalReason.DISCARDED);
                return;
            }
        }

        if (this.level().isClientSide && this.tickCount % 120 == 0) {
            spawnPeriodicParticles();
        }

        int currentHurtTime = this.entityData.get(HURT_TIME);
        if (currentHurtTime > 0) {
            this.entityData.set(HURT_TIME, currentHurtTime - 1);
        }

        int currentHookInteractingTicks = this.entityData.get(HOOK_INTERACTING_TICKS);
        if (currentHookInteractingTicks > 0) {
            this.entityData.set(HOOK_INTERACTING_TICKS, currentHookInteractingTicks - 1);
        }

        if (!this.level().isClientSide && !isAboveWater()) {
            this.remove(RemovalReason.DISCARDED);
            return;
        }

        if (this.entityData.get(IS_DESTROYING)) {
            float currentProgress = this.entityData.get(DESTRUCTION_PROGRESS);
            currentProgress += DESTRUCTION_SPEED;
            this.entityData.set(DESTRUCTION_PROGRESS, currentProgress);
            if (currentProgress >= 1.0F) {
                this.remove(RemovalReason.DISCARDED);
            }
        }
    }

    public float getRandomRotation() {
        return randomRotation;
    }

    private boolean isAboveWater() {
        return this.level().getFluidState(this.blockPosition().below()).isSource();
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        interactions = compound.getInt("Interactions");
        boolean destroying = compound.getBoolean("IsDestroying");
        float progress = compound.getFloat("DestructionProgress");
        this.entityData.set(IS_DESTROYING, destroying);
        this.entityData.set(DESTRUCTION_PROGRESS, progress);
        this.entityData.set(HURT_TIME, compound.getInt("HurtTime"));
        this.entityData.set(HOOK_INTERACTING_TICKS, compound.getInt("HookInteractingTicks"));
        lifeTicks = compound.getInt("LifeTicks");
        maxLifeTicks = compound.contains("MaxLifeTicks", Tag.TAG_INT) ? compound.getInt("MaxLifeTicks") : 9600;
        if (compound.contains("RandomRotation", Tag.TAG_FLOAT)) {
            this.randomRotation = compound.getFloat("RandomRotation");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putInt("Interactions", interactions);
        compound.putBoolean("IsDestroying", this.entityData.get(IS_DESTROYING));
        compound.putFloat("DestructionProgress", this.entityData.get(DESTRUCTION_PROGRESS));
        compound.putInt("HurtTime", this.entityData.get(HURT_TIME));
        compound.putInt("HookInteractingTicks", this.entityData.get(HOOK_INTERACTING_TICKS));
        compound.putInt("LifeTicks", lifeTicks);
        compound.putInt("MaxLifeTicks", maxLifeTicks);
        compound.putFloat("RandomRotation", this.randomRotation);
    }

    public void triggerHurt() {
        this.entityData.set(HURT_TIME, 10);
        if (!this.level().isClientSide) {
            maxLifeTicks += 2400;
        }
    }

    public LootTable getLootTable(ServerLevel serverLevel) {
        return serverLevel.getServer().getLootData().getLootTable(FishPoolHelper.of("gameplay/fishing_pools/fish_pool_base"));
    }

    public AABB getHookInteractionBounds() {
        return this.getBoundingBox().inflate(0.5D);
    }

    public void markHookInteracting() {
        this.entityData.set(HOOK_INTERACTING_TICKS, Math.max(this.entityData.get(HOOK_INTERACTING_TICKS), HOOK_INTERACTING_DURATION));
    }

    public boolean isHookInteracting() {
        return this.entityData.get(HOOK_INTERACTING_TICKS) > 0;
    }

    public void onFishHookInteract(Player player) {
        if (!level().isClientSide && level() instanceof ServerLevel serverLevel) {
            markHookInteracting();
            LootTable lootTable = getLootTable(serverLevel);
            LootParams lootParams = new LootParams.Builder(serverLevel)
                    .withParameter(LootContextParams.THIS_ENTITY, this)
                    .withParameter(LootContextParams.ORIGIN, position())
                    .withParameter(LootContextParams.KILLER_ENTITY, player)
                    .withParameter(LootContextParams.DAMAGE_SOURCE, serverLevel.damageSources().generic())
                    .create(LootContextParamSets.ENTITY);
            List<ItemStack> loot = lootTable.getRandomItems(lootParams);
            loot.forEach(stack -> {
                if (!player.addItem(stack)) {
                    player.drop(stack, false);
                }
            });
            triggerHurt();
            interactions++;
            if (interactions >= MAX_INTERACTIONS) {
                removeWithEffects(serverLevel);
            }
        }
    }

    public void removeWithEffects(ServerLevel serverLevel) {
        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GOAT_SCREAMING_HORN_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
        for (int i = 0; i < 35; i++) {
            double yOffset = serverLevel.random.nextDouble() * 15.0;
            double xOffset = 0.25 * (serverLevel.random.nextDouble() - 0.5);
            double zOffset = 0.25 * (serverLevel.random.nextDouble() - 0.5);
            double velocityY = 0.1 + serverLevel.random.nextDouble() * 0.2;
            serverLevel.sendParticles(ParticleTypes.SPLASH, this.getX() + xOffset + 0.5, this.getY() + yOffset, this.getZ() + zOffset + 0.5, 1, 0.0, velocityY, 0.0, 0.0);
        }
        for (int i = 0; i < 18; i++) {
            double xOffset = serverLevel.random.nextGaussian() * 0.2;
            double yOffset = serverLevel.random.nextGaussian() * 0.2;
            double zOffset = serverLevel.random.nextGaussian() * 0.2;
            serverLevel.sendParticles(ParticleTypes.BUBBLE_POP, this.getX(), this.getY(), this.getZ(), 20, xOffset, yOffset, zOffset, 0.1);
            serverLevel.sendParticles(ParticleTypes.POOF, this.getX(), this.getY() + 1, this.getZ(), 10, xOffset, yOffset, zOffset, 0.1);
            serverLevel.sendParticles(ParticleTypes.CRIT, this.getX(), this.getY(), this.getZ(), 5, xOffset, yOffset, zOffset, 0.1);
        }
        this.entityData.set(IS_DESTROYING, true);
    }

    public int getHurtTime() {
        return this.entityData.get(HURT_TIME);
    }

    protected boolean isDestroying() {
        return this.entityData.get(IS_DESTROYING);
    }

    public void triggerInteraction() {
        if (this.isRemoved() || this.isDestroying()) {
            return;
        }
        interactions++;
        triggerHurt();
        if (interactions >= MAX_INTERACTIONS && !level().isClientSide && this.level() instanceof ServerLevel serverLevel) {
            removeWithEffects(serverLevel);
        }
    }

    private void spawnPlacementParticles() {
        if (this.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 10; i++) {
                double xOffset = this.random.nextDouble() - 0.5;
                double zOffset = this.random.nextDouble() - 0.5;
                serverLevel.sendParticles(ParticleTypes.BUBBLE, this.getX() + xOffset, this.getY() + 1.9, this.getZ() + zOffset, 1, 0.0, 0.1, 0.0, 0.0);
            }
        }
    }

    private void spawnPeriodicParticles() {
        double x = this.getX() + (this.random.nextDouble() - 0.5) * 2.0;
        double z = this.getZ() + (this.random.nextDouble() - 0.5) * 2.0;
        this.level().addParticle(ParticleTypes.SPLASH, x, this.getY() + 1.8, z, 0.0, 0.05, 0.0);
    }


    @Override
    public AABB getBoundingBoxForCulling() {
        return new AABB(this.getX() - 1, this.getY() - 1, this.getZ() - 1, this.getX() + 1, this.getY() + 1, this.getZ() + 1);
    }
}


