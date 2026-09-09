package com.y271727uy.shopcore.item;

import com.y271727uy.shopcore.gameplay.sellingbin.SellingBinGroupManager;
import com.y271727uy.shopcore.gameplay.sellingbin.AgricultureDailySavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class AgricultureDailyItem extends Item {
    public AgricultureDailyItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, net.minecraft.world.entity.player.Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }

        ServerLevel marketLevel = serverPlayer.getServer().overworld();
        sendDaily(serverPlayer, SellingBinGroupManager.getAgricultureDailyEdition(marketLevel));
        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    private static void sendDaily(ServerPlayer player, AgricultureDailySavedData.DailyEdition edition) {
        player.displayClientMessage(Component.translatable("item.shopcore.agriculture_daily").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
        player.displayClientMessage(Component.translatable("message.shopcore.agriculture_daily.down_header").withStyle(ChatFormatting.RED), false);
        sendEntries(player, edition.decreases(), true);
        player.displayClientMessage(Component.translatable("message.shopcore.agriculture_daily.up_header").withStyle(ChatFormatting.GREEN), false);
        sendEntries(player, edition.increases(), false);
        player.displayClientMessage(Component.translatable("message.shopcore.agriculture_daily.filler", Component.translatable("message.shopcore.agriculture_daily.filler." + edition.fillerNewsIndex())).withStyle(ChatFormatting.GRAY), false);
    }

    private static void sendEntries(ServerPlayer player, List<AgricultureDailySavedData.MarketEntry> entries, boolean decreases) {
        if (entries.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.shopcore.agriculture_daily.none").withStyle(ChatFormatting.DARK_GRAY), false);
            return;
        }
        for (AgricultureDailySavedData.MarketEntry entry : entries) {
            Component adjustment = Component.literal((entry.bonus() > 0 ? "+" : "") + entry.bonus())
                    .withStyle(decreases ? ChatFormatting.RED : ChatFormatting.GREEN);
            player.displayClientMessage(Component.translatable("message.shopcore.agriculture_daily.entry", itemName(entry.itemId()), adjustment), false);
        }
    }

    private static Component itemName(ResourceLocation itemId) {
        Item item = BuiltInRegistries.ITEM.get(itemId);
        return item == net.minecraft.world.item.Items.AIR && !itemId.equals(BuiltInRegistries.ITEM.getKey(item))
                ? Component.literal(itemId.toString())
                : item.getDescription();
    }
}
