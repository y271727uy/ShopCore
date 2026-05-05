package com.y271727uy.shopcore.integration.sdm_integration.card;

import com.y271727uy.shopcore.block.SellingBinBlock;
import com.y271727uy.shopcore.block.entity.SellingBinBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@SuppressWarnings("unused")
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BankCardItem extends Item {
    public BankCardItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return useOnSellingBin(context, isTaxExempt());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.shopcore.bank_card.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.shopcore.bank_card.bind_hint").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("tooltip.shopcore.bank_card.unbind_hint").withStyle(ChatFormatting.DARK_GRAY));
        appendTaxTooltip(tooltip);
    }

    protected boolean isTaxExempt() {
        return false;
    }

    protected void appendTaxTooltip(List<Component> tooltip) {
        tooltip.add(Component.translatable("tooltip.shopcore.bank_card.tax_header").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("tooltip.shopcore.bank_card.tax_rule_1").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("tooltip.shopcore.bank_card.tax_rule_2").withStyle(ChatFormatting.RED));
    }

    protected InteractionResult useOnSellingBin(UseOnContext context, boolean taxExempt) {
        Level level = context.getLevel();
        if (!(level.getBlockState(context.getClickedPos()).getBlock() instanceof SellingBinBlock)) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (!(context.getPlayer() instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }

        BlockEntity blockEntity = level.getBlockEntity(context.getClickedPos());
        if (!(blockEntity instanceof SellingBinBlockEntity sellingBin)) {
            return InteractionResult.CONSUME;
        }

        if (!sellingBin.isBound()) {
            if (!sellingBin.bindTo(serverPlayer, taxExempt)) {
                serverPlayer.displayClientMessage(Component.translatable("message.shopcore.selling_bin.bind_fail").withStyle(ChatFormatting.RED), false);
                return InteractionResult.CONSUME;
            }

            serverPlayer.displayClientMessage(Component.translatable("message.shopcore.selling_bin.bind_success").withStyle(ChatFormatting.GREEN), false);
            return InteractionResult.CONSUME;
        }

        if (sellingBin.isBoundTo(serverPlayer)) {
            if (!sellingBin.unbind(serverPlayer)) {
                serverPlayer.displayClientMessage(Component.translatable("message.shopcore.selling_bin.unbind_fail").withStyle(ChatFormatting.RED), false);
                return InteractionResult.CONSUME;
            }

            serverPlayer.displayClientMessage(Component.translatable("message.shopcore.selling_bin.unbind_success").withStyle(ChatFormatting.GREEN), false);
            return InteractionResult.CONSUME;
        }

        if (sellingBin.isBound()) {
            serverPlayer.displayClientMessage(Component.translatable("message.shopcore.selling_bin.bound_other").withStyle(ChatFormatting.RED), false);
            return InteractionResult.CONSUME;
        }

        return InteractionResult.CONSUME;
    }
}
