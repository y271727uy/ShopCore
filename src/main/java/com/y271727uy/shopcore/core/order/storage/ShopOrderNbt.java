package com.y271727uy.shopcore.core.order.storage;

import com.y271727uy.shopcore.ShopcoreMod;
import com.y271727uy.shopcore.core.order.OrderLine;
import com.y271727uy.shopcore.core.order.OrderStatus;
import com.y271727uy.shopcore.core.order.ShopOrder;
import com.y271727uy.shopcore.core.shop.instance.ShopId;
import com.y271727uy.shopcore.core.shop.storage.ShopIdNbt;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class ShopOrderNbt {
    private static final String ORDER_ID = "OrderId";
    private static final String SHOP_ID = "ShopId";
    private static final String SHOP_POS = "ShopPos";
    private static final String CUSTOMER_TYPE = "CustomerType";
    private static final String LINES = "Lines";
    private static final String STATUS = "Status";
    private static final String CREATED_GAME_TIME = "CreatedGameTime";
    private static final String EXPIRES_GAME_TIME = "ExpiresGameTime";
    private static final String REQUESTED_ITEM = "RequestedItem";
    private static final String REQUESTED_COUNT = "RequestedCount";
    private static final String DELIVERED_COUNT = "DeliveredCount";
    private static final String UNIT_PRICE = "UnitPrice";

    private ShopOrderNbt() {
    }

    public static CompoundTag save(ShopOrder order) {
        Objects.requireNonNull(order, "order");
        CompoundTag tag = new CompoundTag();
        tag.putUUID(ORDER_ID, order.orderId());
        ShopIdNbt.put(tag, SHOP_ID, order.shopId());
        tag.put(SHOP_POS, NbtUtils.writeBlockPos(order.shopPos()));
        tag.putString(CUSTOMER_TYPE, order.customerType().toString());
        tag.putString(STATUS, order.status().name());
        tag.putLong(CREATED_GAME_TIME, order.createdGameTime());
        tag.putLong(EXPIRES_GAME_TIME, order.expiresGameTime());

        ListTag lines = new ListTag();
        for (OrderLine line : order.lines()) {
            lines.add(saveLine(line));
        }
        tag.put(LINES, lines);
        return tag;
    }

    public static Optional<ShopOrder> load(CompoundTag tag, ShopId fallbackShopId, BlockPos fallbackShopPos) {
        Objects.requireNonNull(tag, "tag");
        Objects.requireNonNull(fallbackShopId, "fallbackShopId");
        Objects.requireNonNull(fallbackShopPos, "fallbackShopPos");
        if (!tag.hasUUID(ORDER_ID)) {
            return Optional.empty();
        }

        ShopId shopId = ShopIdNbt.get(tag, SHOP_ID, fallbackShopId);
        BlockPos shopPos = tag.contains(SHOP_POS) ? NbtUtils.readBlockPos(tag.getCompound(SHOP_POS)) : fallbackShopPos;
        ResourceLocation customerType = ResourceLocation.tryParse(tag.getString(CUSTOMER_TYPE));
        if (customerType == null) {
            customerType = ResourceLocation.fromNamespaceAndPath(ShopcoreMod.MODID, "unknown_customer");
        }

        List<OrderLine> lines = new ArrayList<>();
        ListTag lineTags = tag.getList(LINES, Tag.TAG_COMPOUND);
        for (int i = 0; i < lineTags.size(); i++) {
            loadLine(lineTags.getCompound(i)).ifPresent(lines::add);
        }
        if (lines.isEmpty()) {
            return Optional.empty();
        }

        long created = Math.max(0L, tag.getLong(CREATED_GAME_TIME));
        long expires = Math.max(created, tag.getLong(EXPIRES_GAME_TIME));
        return Optional.of(new ShopOrder(
                tag.getUUID(ORDER_ID),
                shopId,
                shopPos,
                customerType,
                lines,
                enumValue(tag.getString(STATUS), OrderStatus.PENDING),
                created,
                expires
        ));
    }

    private static CompoundTag saveLine(OrderLine line) {
        CompoundTag tag = new CompoundTag();
        tag.put(REQUESTED_ITEM, line.requestedItem().save(new CompoundTag()));
        tag.putInt(REQUESTED_COUNT, line.requestedCount());
        tag.putInt(DELIVERED_COUNT, line.deliveredCount());
        tag.putInt(UNIT_PRICE, line.unitPrice());
        return tag;
    }

    private static Optional<OrderLine> loadLine(CompoundTag tag) {
        ItemStack stack = ItemStack.of(tag.getCompound(REQUESTED_ITEM));
        if (stack.isEmpty()) {
            return Optional.empty();
        }
        int requested = Math.max(1, tag.getInt(REQUESTED_COUNT));
        int delivered = Math.max(0, Math.min(requested, tag.getInt(DELIVERED_COUNT)));
        int unitPrice = Math.max(0, tag.getInt(UNIT_PRICE));
        return Optional.of(new OrderLine(stack, requested, delivered, unitPrice));
    }

    private static <T extends Enum<T>> T enumValue(String value, T fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Enum.valueOf(fallback.getDeclaringClass(), value);
        } catch (IllegalArgumentException ignored) {
            return fallback;
        }
    }
}
