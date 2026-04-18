package com.list.event;

import com.list.all.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "list")
public class FishyLemonDropEventHandler {
    private static final String FISHY_LEMON_FLAG = "list_fishy_lemon_exploded";

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        Level level = event.getLevel();
        Entity entity = event.getEntity();
        if (!(entity instanceof ItemEntity itemEntity)) {
            return;
        }

        if (!itemEntity.getItem().is(ModItems.FISHY_LEMON.get())) {
            return;
        }

        if (level.isClientSide || itemEntity.getPersistentData().getBoolean(FISHY_LEMON_FLAG)) {
            return;
        }

        itemEntity.getPersistentData().putBoolean(FISHY_LEMON_FLAG, true);

        double x = itemEntity.getX();
        double y = itemEntity.getY();
        double z = itemEntity.getZ();

        itemEntity.discard();

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.explode(
                    null,
                    x,
                    y,
                    z,
                    4.0F,
                    Level.ExplosionInteraction.NONE
            );
        }
    }
}






