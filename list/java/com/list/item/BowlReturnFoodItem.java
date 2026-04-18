package com.list.item;

import javax.annotation.Nonnull;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class BowlReturnFoodItem extends Item {
    public BowlReturnFoodItem(Properties properties) {
        super(properties);
    }

    @Override
    public @Nonnull ItemStack finishUsingItem(@Nonnull ItemStack stack, @Nonnull Level level, @Nonnull LivingEntity livingEntity) {
        ItemStack result = super.finishUsingItem(stack, level, livingEntity);
        ItemStack bowl = new ItemStack(Items.BOWL);

        if (livingEntity instanceof Player player && player.getAbilities().instabuild) {
            return result;
        }

        if (result.isEmpty()) {
            return bowl;
        }

        if (livingEntity instanceof Player player) {
            if (!player.getInventory().add(bowl)) {
                player.drop(bowl, false);
            }
        } else {
            livingEntity.spawnAtLocation(bowl);
        }

        return result;
    }
}


