package com.y271727uy.shopcore.item;

import com.y271727uy.shopcore.text.TestShopDailySavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class TestShopDailyItem extends Item {
    public TestShopDailyItem(Properties properties) { super(properties); }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        long currentDay = Math.floorDiv(serverPlayer.serverLevel().getDayTime(), 24000L);
        TestShopDailySavedData.DailyEdition edition = TestShopDailySavedData.get(serverPlayer.serverLevel())
                .getOrCreateEdition(currentDay, serverPlayer.getRandom());
        serverPlayer.displayClientMessage(Component.translatable("item.shopcore.test_shop_daily").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
        for (TestShopDailySavedData.NewsEntry entry : edition.entries()) {
            serverPlayer.displayClientMessage(line(entry), false);
        }
        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    private static Component line(TestShopDailySavedData.NewsEntry entry) {
        return switch (entry.type()) {
            case POPULAR -> Component.translatable("message.shopcore.test_shop_daily.popular", itemName(entry.itemId())).withStyle(ChatFormatting.GREEN);
            case OVERSUPPLIED -> Component.translatable("message.shopcore.test_shop_daily.oversupplied", itemName(entry.itemId())).withStyle(ChatFormatting.RED);
            case STABLE -> Component.translatable("message.shopcore.test_shop_daily.stable").withStyle(ChatFormatting.DARK_GRAY);
            case SPOILED_FOOD -> Component.translatable("message.shopcore.test_shop_daily.spoiled_food").withStyle(ChatFormatting.RED);
            case FILLER -> Component.translatable("message.shopcore.test_shop_daily.filler." + entry.count()).withStyle(ChatFormatting.GRAY);
        };
    }

    private static Component itemName(ResourceLocation itemId) {
        Item item = BuiltInRegistries.ITEM.get(itemId);
        return item == net.minecraft.world.item.Items.AIR && !itemId.equals(BuiltInRegistries.ITEM.getKey(item))
                ? Component.literal(itemId.toString()) : item.getDescription();
    }
}
