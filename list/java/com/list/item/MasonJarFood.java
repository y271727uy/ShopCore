package com.list.item;

import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public class MasonJarFood extends Item {

    public MasonJarFood() {
        super(new Item.Properties()
                .food(new FoodProperties.Builder()
                        .nutrition(3)
                        .saturationMod(0.6f)
                        .build())
        );
    }


    public MasonJarFood(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);

        // 动态获取 vintagedelight:mason_jar
        Item jarItem = ForgeRegistries.ITEMS.getValue(
                new ResourceLocation("vintagedelight", "mason_jar")
        );

        if (jarItem != null) {
            ItemStack jar = new ItemStack(jarItem);

            if (entity instanceof Player player) {
                if (!player.getInventory().add(jar)) {
                    player.drop(jar, false);
                }
            }
        }

        return result;
    }
}