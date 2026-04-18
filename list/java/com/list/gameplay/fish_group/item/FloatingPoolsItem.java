 package com.list.gameplay.fish_group.item;

import com.list.gameplay.fish_group.entity.AbstractFishPoolEntity;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import javax.annotation.Nonnull;
import java.util.List;

@MethodsReturnNonnullByDefault
public class FloatingPoolsItem extends Item {
    public FloatingPoolsItem(Properties properties) {
        super(properties);
    }

    @Override
    public @Nonnull InteractionResultHolder<ItemStack> use(@Nonnull Level level, @Nonnull Player player, @Nonnull InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        HitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY);
        if (hitResult.getType() != HitResult.Type.BLOCK) {
            return InteractionResultHolder.pass(itemStack);
        }

        AABB checkArea = new AABB(
                hitResult.getLocation().x - 4.0D,
                hitResult.getLocation().y - 4.0D,
                hitResult.getLocation().z - 4.0D,
                hitResult.getLocation().x + 4.0D,
                hitResult.getLocation().y + 4.0D,
                hitResult.getLocation().z + 4.0D
        );

        if (!level.getEntitiesOfClass(AbstractFishPoolEntity.class, checkArea).isEmpty()) {
            return InteractionResultHolder.fail(itemStack);
        }

        if (!level.isClientSide) {
            AbstractFishPoolEntity fishPool = null;
            for (FishGroupRegistry.FishPoolRegistration registration : FishGroupRegistry.getFishPoolRegistrations()) {
                if (itemStack.is(registration.item().get())) {
                    fishPool = registration.create(level);
                    break;
                }
            }

            if (fishPool != null) {
                fishPool.setPos(hitResult.getLocation().x, hitResult.getLocation().y - 1.85D, hitResult.getLocation().z);
                fishPool.setYRot(player.getYRot());
                if (level.noCollision(fishPool, fishPool.getBoundingBox())) {
                    level.addFreshEntity(fishPool);
                    level.gameEvent(player, GameEvent.ENTITY_PLACE, hitResult.getLocation());
                    if (!player.getAbilities().instabuild) {
                        itemStack.shrink(1);
                    }
                } else {
                    return InteractionResultHolder.fail(itemStack);
                }
            }
        }

        showParticles(level, hitResult);
        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }

    private void showParticles(Level level, HitResult hitResult) {
        for (int i = 0; i < 20; i++) {
            double xOffset = (level.random.nextDouble() - 0.5D) * 2.0D;
            double yOffset = (level.random.nextDouble() - 0.5D) * 2.0D;
            double zOffset = (level.random.nextDouble() - 0.5D) * 2.0D;
            level.addParticle(ParticleTypes.BUBBLE_POP, hitResult.getLocation().x + xOffset, hitResult.getLocation().y + yOffset, hitResult.getLocation().z + zOffset, 0.0D, 0.0D, 0.0D);
            level.addParticle(ParticleTypes.SPLASH, hitResult.getLocation().x + xOffset, hitResult.getLocation().y + yOffset, hitResult.getLocation().z + zOffset, 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, Level level, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.list.fish_group.not_obtainable")
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xD27D46)).withItalic(true)));
    }
}


