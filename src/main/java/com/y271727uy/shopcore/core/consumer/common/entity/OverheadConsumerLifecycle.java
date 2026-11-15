package com.y271727uy.shopcore.core.consumer.common.entity;
import com.y271727uy.shopcore.core.consumer.common.runtime.OrderTakingLifecycle;
import com.y271727uy.shopcore.core.order.book.ShopOrderBook;
import com.y271727uy.shopcore.core.order.taking.*;
import com.y271727uy.shopcore.core.order.prompt.overhead.OverheadOrderTakingPresentation;
import com.y271727uy.shopcore.core.order.prompt.screen.PlayerScreenOrderPromptService;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import java.util.function.Supplier;
/** Compatibility preset: delayed automatic order taking plus overhead rendering. */
public class OverheadConsumerLifecycle<A extends EntityBackedConsumerActor> extends OrderTakingLifecycle<A> {
    public OverheadConsumerLifecycle(long waitTicks, Component waitingText,
            Supplier<ShopOrderBook> orders, Runnable clearShopPrompts) {
        super(new OrderTakingController<>(new AutomaticOrderTaking(waitTicks, new EntityOrderTakingMemory()),
                new OverheadOrderTakingPresentation<A>(waitingText)), orders, clearShopPrompts);
    }

    public static void clearNearbyPlayerHud(ServerLevel level, BlockPos shopPos) {
        Vec3 center = Vec3.atCenterOf(shopPos);
        for (ServerPlayer player : level.players()) {
            if (player.position().distanceToSqr(center) <= 32.0D * 32.0D) {
                PlayerScreenOrderPromptService.clear(player);
            }
        }
    }
}
