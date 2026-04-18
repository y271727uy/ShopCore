package com.list.item;

import javax.annotation.Nonnull;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.UseAnim;

public class DrinkItem extends Item {
    public DrinkItem(Properties properties) {
        super(properties);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public SoundEvent getDrinkingSound() {
        return SoundEvents.GENERIC_DRINK;
    }

    @Override
    public SoundEvent getEatingSound() {
        return getDrinkingSound();
    }

    @Override
    public @Nonnull ItemStack finishUsingItem(@Nonnull ItemStack stack, @Nonnull Level level, @Nonnull LivingEntity livingEntity) {
        ItemStack result = super.finishUsingItem(stack, level, livingEntity);
        ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);

        if (livingEntity instanceof Player player && player.getAbilities().instabuild) {
            return result;
        }

        if (result.isEmpty()) {
            return bottle;
        }

        if (livingEntity instanceof Player player) {
            if (!player.getInventory().add(bottle)) {
                player.drop(bottle, false);
            }
        } else {
            livingEntity.spawnAtLocation(bottle);
        }

        return result;
    }
}


