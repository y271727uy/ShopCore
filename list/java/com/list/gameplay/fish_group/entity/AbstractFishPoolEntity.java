package com.list.gameplay.fish_group.entity;

import com.list.ListMod;
import com.list.gameplay.fish_group.pool.FishKingCooldownSavedData;
import com.list.gameplay.fish_group.pool.FishPoolDefinition;
import com.list.gameplay.fish_group.pool.FishPoolFactory;
import com.list.gameplay.fish_group.pool.FishPoolLootManager;
import lombok.Getter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;

public abstract class AbstractFishPoolEntity extends FishPoolBaseEntity {
    private static final String FISH_POOL_ID_TAG = "FishPoolId";
    private static final String FISH_COUNT_TAG = "FishCount";
    private static final String MAX_FISH_COUNT_TAG = "MaxFishCount";
    private static final String FISH_KING_AWARDED_TAG = "FishKingAwarded";

    @Nullable
    private ResourceLocation fishPoolId;
    private int fishCount;
    private int maxFishCount;
    @Getter
    private boolean fishKingAwarded;

    protected AbstractFishPoolEntity(EntityType<? extends FishPoolBaseEntity> type, Level level) {
        super(type, level);
    }

    protected abstract FishPoolDefinition.Environment getEnvironment();

    protected abstract ResourceLocation getDefaultFishPoolId();

    protected ResourceLocation resolveDefinitionIdFromType(EntityType<?> type) {
        ResourceLocation typeId = ForgeRegistries.ENTITY_TYPES.getKey(type);
        if (typeId != null && FishPoolFactory.get(typeId).isPresent()) {
            return typeId;
        }
        return getDefaultFishPoolId();
    }

    public void setFishPoolDefinition(ResourceLocation fishPoolId) {
        FishPoolDefinition definition = FishPoolFactory.getOrDefault(fishPoolId, getEnvironment());
        boolean shouldRollFishCount = !definition.id().equals(this.fishPoolId) || this.maxFishCount <= 0;
        this.fishPoolId = definition.id();
        this.maxFishCount = shouldRollFishCount
                ? definition.rollFishCount(this.random)
                : Mth.clamp(this.maxFishCount, definition.minFishCount(), definition.maxFishCount());
        this.fishCount = Mth.clamp(this.fishCount, 0, this.maxFishCount);
    }

    public ResourceLocation getFishPoolId() {
        ensureFishPoolDefinition();
        return this.fishPoolId;
    }

    public int getFishCount() {
        return this.fishCount;
    }

    public int getMaxFishCount() {
        ensureFishPoolDefinition();
        return this.maxFishCount;
    }

    public FishPoolDefinition getResolvedFishPoolDefinition() {
        return getFishPoolDefinition();
    }

    public void appendJadeServerData(CompoundTag tag, ServerLevel serverLevel) {
        FishPoolDefinition definition = getFishPoolDefinition();
        tag.putString("fishKingKey", resolveFishKingTranslationKey(definition));
        tag.putString("weatherKey", resolveWeatherTranslationKey(definition));
        tag.putString("timeKey", resolveTimeTranslationKey(definition));
        tag.putString("stateKey", resolveJadeStateTranslationKey(serverLevel, definition));
    }

    @Override
    public AABB getHookInteractionBounds() {
        return this.getBoundingBox().inflate(0.75D, 2.0D, 0.75D);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide || this.isRemoved() || this.isDestroying()) {
            return;
        }

        if (this.level() instanceof ServerLevel serverLevel) {
            FishPoolDefinition definition = getFishPoolDefinition();
            if (!definition.matchesCurrentConditions(serverLevel)) {
                this.removeWithEffects(serverLevel);
            }
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains(FISH_POOL_ID_TAG, Tag.TAG_STRING)) {
            ResourceLocation savedId = ResourceLocation.tryParse(compound.getString(FISH_POOL_ID_TAG));
            this.fishPoolId = savedId == null ? getDefaultFishPoolId() : savedId;
        } else {
            this.fishPoolId = getDefaultFishPoolId();
        }
        this.fishCount = Math.max(0, compound.getInt(FISH_COUNT_TAG));
        this.fishKingAwarded = compound.getBoolean(FISH_KING_AWARDED_TAG);
        if (compound.contains(MAX_FISH_COUNT_TAG, Tag.TAG_INT)) {
            this.maxFishCount = Math.max(1, compound.getInt(MAX_FISH_COUNT_TAG));
        } else {
            this.maxFishCount = getFishPoolDefinition().rollFishCount(this.random);
        }
        this.fishCount = Mth.clamp(this.fishCount, 0, this.maxFishCount);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putString(FISH_POOL_ID_TAG, getFishPoolId().toString());
        compound.putInt(FISH_COUNT_TAG, this.fishCount);
        compound.putInt(MAX_FISH_COUNT_TAG, getMaxFishCount());
        compound.putBoolean(FISH_KING_AWARDED_TAG, this.fishKingAwarded);
    }

    @Override
    public void onFishHookInteract(Player player) {
        if (this.level().isClientSide || !(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (this.isRemoved() || this.isDestroying()) {
            return;
        }
        FishPoolDefinition definition = getFishPoolDefinition();
        if (!definition.matchesFishing(serverLevel)) {
            return;
        }
        if (this.fishCount >= getMaxFishCount()) {
            this.removeWithEffects(serverLevel);
            return;
        }

        ItemStack reward = FishPoolLootManager.INSTANCE.rollReward(getFishPoolId(), serverLevel, serverLevel.random);
        if (reward.isEmpty()) {
            ListMod.LOGGER.warn("Fish pool {} produced no reward", getFishPoolId());
            return;
        }

        giveReward(player, reward);
        tryAwardFishKing(serverLevel, player, definition);
        triggerHurt();
        this.fishCount++;

        if (this.fishCount >= getMaxFishCount()) {
            this.removeWithEffects(serverLevel);
        }
    }

    private void ensureFishPoolDefinition() {
        if (this.fishPoolId == null) {
            setFishPoolDefinition(getDefaultFishPoolId());
        }
    }

    private FishPoolDefinition getFishPoolDefinition() {
        ensureFishPoolDefinition();
        FishPoolDefinition definition = FishPoolFactory.getOrDefault(this.fishPoolId, getEnvironment());
        this.fishPoolId = definition.id();
        return definition;
    }

    private String resolveFishKingTranslationKey(FishPoolDefinition definition) {
        if (definition.fishKing() == null) {
            return "tooltip.list.fish_pool.fish_king.none";
        }
        return this.fishKingAwarded
                ? "tooltip.list.fish_pool.fish_king.awarded"
                : "tooltip.list.fish_pool.fish_king.available";
    }

    private String resolveWeatherTranslationKey(FishPoolDefinition definition) {
        return switch (definition.weatherRequirement()) {
            case CLEAR -> "tooltip.list.fish_pool.weather.clear";
            case RAIN -> "tooltip.list.fish_pool.weather.rain";
            case THUNDER -> "tooltip.list.fish_pool.weather.thunder";
            case ANY -> "tooltip.list.fish_pool.weather.any";
        };
    }

    private String resolveTimeTranslationKey(FishPoolDefinition definition) {
        return switch (definition.timeRequirement()) {
            case DAY -> "tooltip.list.fish_pool.time.day";
            case NIGHT -> "tooltip.list.fish_pool.time.night";
            case ANY -> "tooltip.list.fish_pool.time.any";
        };
    }

    private String resolveJadeStateTranslationKey(ServerLevel serverLevel, FishPoolDefinition definition) {
        if (this.isHookInteracting()) {
            return "tooltip.list.fish_pool.state.fishing";
        }
        return definition.matchesFishing(serverLevel)
                ? "tooltip.list.fish_pool.state.available"
                : "tooltip.list.fish_pool.state.unavailable";
    }

    private void tryAwardFishKing(ServerLevel serverLevel, Player player, FishPoolDefinition definition) {
        if (this.fishKingAwarded || definition.fishKing() == null) {
            return;
        }

        FishKingCooldownSavedData cooldownData = FishKingCooldownSavedData.get(serverLevel);
        if (!cooldownData.canAward(player, serverLevel.getGameTime())) {
            return;
        }
        if (serverLevel.random.nextInt(100) != 0) {
            return;
        }

        Item fishKingItem = ForgeRegistries.ITEMS.getValue(definition.fishKing());
        if (fishKingItem == null || fishKingItem == Items.AIR) {
            ListMod.LOGGER.warn("Fish pool {} is configured with an invalid FishKing item {}", definition.id(), definition.fishKing());
            return;
        }

        giveReward(player, new ItemStack(fishKingItem));
        this.fishKingAwarded = true;
        cooldownData.markAwarded(player, serverLevel.getGameTime());
    }

    private void giveReward(Player player, ItemStack stack) {
        if (!player.addItem(stack)) {
            player.drop(stack, false);
        }
    }
}



