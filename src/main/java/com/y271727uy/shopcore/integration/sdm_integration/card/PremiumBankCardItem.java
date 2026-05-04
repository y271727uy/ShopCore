package com.y271727uy.shopcore.integration.sdm_integration.card;

import com.y271727uy.shopcore.block.SellingBinBlock;
import com.y271727uy.shopcore.block.entity.SellingBinBlockEntity;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.item.Item;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class PremiumBankCardItem extends BankCardItem {
    public PremiumBankCardItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        BlockState blockState = level.getBlockState(clickedPos);
        if (!(blockState.getBlock() instanceof SellingBinBlock)) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (!(context.getPlayer() instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }

        BlockEntity blockEntity = level.getBlockEntity(clickedPos);
        if (!(blockEntity instanceof SellingBinBlockEntity sellingBin)) {
            return InteractionResult.CONSUME;
        }

        if (!sellingBin.isBound()) {
            if (!sellingBin.bindTo(serverPlayer)) {
                serverPlayer.displayClientMessage(Component.literal("账户绑定失败!").withStyle(ChatFormatting.RED), false);
                return InteractionResult.CONSUME;
            }

            serverPlayer.displayClientMessage(Component.literal("账户绑定成功!").withStyle(ChatFormatting.GREEN), false);
            return InteractionResult.CONSUME;
        }

        if (sellingBin.isBoundTo(serverPlayer)) {
            if (!sellingBin.unbind(serverPlayer)) {
                serverPlayer.displayClientMessage(Component.literal("解绑账户失败!").withStyle(ChatFormatting.RED), false);
                return InteractionResult.CONSUME;
            }

            serverPlayer.displayClientMessage(Component.literal("解绑账户成功!").withStyle(ChatFormatting.GREEN), false);
            return InteractionResult.CONSUME;
        }

        if (sellingBin.isBound()) {
            serverPlayer.displayClientMessage(Component.literal("账户已绑定其他玩家!").withStyle(ChatFormatting.RED), false);
            return InteractionResult.CONSUME;
        }

        return InteractionResult.CONSUME;
    }
}




