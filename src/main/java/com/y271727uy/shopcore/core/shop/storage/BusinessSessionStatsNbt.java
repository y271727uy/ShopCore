package com.y271727uy.shopcore.core.shop.storage;

import com.y271727uy.shopcore.core.shop.session.BusinessSessionStats;
import net.minecraft.nbt.CompoundTag;

import java.util.Objects;
import java.util.UUID;

public final class BusinessSessionStatsNbt {
    private static final String SESSION_ID = "SessionId";
    private static final String OPENED_AT = "OpenedAt";
    private static final String CLOSED_AT = "ClosedAt";
    private static final String GROSS_REVENUE = "GrossRevenue";
    private static final String NET_REVENUE = "NetRevenue";
    private static final String TAX_PAID = "TaxPaid";
    private static final String ORDERS_CREATED = "OrdersCreated";
    private static final String ORDERS_COMPLETED = "OrdersCompleted";
    private static final String ORDERS_EXPIRED = "OrdersExpired";
    private static final String ORDERS_CANCELLED = "OrdersCancelled";
    private static final String ITEMS_SOLD = "ItemsSold";

    private BusinessSessionStatsNbt() {
    }

    public static CompoundTag save(BusinessSessionStats stats) {
        Objects.requireNonNull(stats, "stats");
        CompoundTag tag = new CompoundTag();
        tag.putUUID(SESSION_ID, stats.sessionId());
        tag.putLong(OPENED_AT, stats.openedAtGameTime());
        tag.putLong(CLOSED_AT, stats.closedAtGameTime());
        tag.putLong(GROSS_REVENUE, stats.grossRevenue());
        tag.putLong(NET_REVENUE, stats.netRevenue());
        tag.putLong(TAX_PAID, stats.taxPaid());
        tag.putInt(ORDERS_CREATED, stats.ordersCreated());
        tag.putInt(ORDERS_COMPLETED, stats.ordersCompleted());
        tag.putInt(ORDERS_EXPIRED, stats.ordersExpired());
        tag.putInt(ORDERS_CANCELLED, stats.ordersCancelled());
        tag.putInt(ITEMS_SOLD, stats.itemsSold());
        return tag;
    }

    public static BusinessSessionStats load(CompoundTag tag) {
        Objects.requireNonNull(tag, "tag");
        UUID sessionId = tag.hasUUID(SESSION_ID) ? tag.getUUID(SESSION_ID) : new UUID(0L, 0L);
        return new BusinessSessionStats(
                sessionId,
                tag.contains(OPENED_AT) ? tag.getLong(OPENED_AT) : -1L,
                tag.contains(CLOSED_AT) ? tag.getLong(CLOSED_AT) : -1L,
                Math.max(0L, tag.getLong(GROSS_REVENUE)),
                Math.max(0L, tag.getLong(NET_REVENUE)),
                Math.max(0L, tag.getLong(TAX_PAID)),
                Math.max(0, tag.getInt(ORDERS_CREATED)),
                Math.max(0, tag.getInt(ORDERS_COMPLETED)),
                Math.max(0, tag.getInt(ORDERS_EXPIRED)),
                Math.max(0, tag.getInt(ORDERS_CANCELLED)),
                Math.max(0, tag.getInt(ITEMS_SOLD))
        );
    }
}
