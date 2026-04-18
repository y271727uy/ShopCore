package com.list.gameplay.fish_group.pool;

import com.list.ListMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class FishKingCooldownSavedData extends SavedData {
    public static final long COOLDOWN_TICKS = 72000L;
    private static final String DATA_NAME = ListMod.MODID + "_fish_king_cooldowns";

    private final Map<UUID, Long> cooldownExpiryByPlayer = new HashMap<>();

    public static FishKingCooldownSavedData get(ServerLevel level) {
        return level.getServer()
                .overworld()
                .getDataStorage()
                .computeIfAbsent(FishKingCooldownSavedData::load, FishKingCooldownSavedData::new, DATA_NAME);
    }

    public static FishKingCooldownSavedData load(CompoundTag tag) {
        FishKingCooldownSavedData data = new FishKingCooldownSavedData();
        ListTag cooldowns = tag.getList("Cooldowns", Tag.TAG_COMPOUND);
        for (Tag element : cooldowns) {
            CompoundTag cooldownTag = (CompoundTag) element;
            if (cooldownTag.hasUUID("Player")) {
                data.cooldownExpiryByPlayer.put(cooldownTag.getUUID("Player"), cooldownTag.getLong("Expiry"));
            }
        }
        return data;
    }

    public boolean canAward(Player player, long gameTime) {
        Long expiry = cooldownExpiryByPlayer.get(player.getUUID());
        if (expiry == null) {
            return true;
        }
        if (expiry <= gameTime) {
            cooldownExpiryByPlayer.remove(player.getUUID());
            setDirty();
            return true;
        }
        return false;
    }

    public void markAwarded(Player player, long gameTime) {
        cooldownExpiryByPlayer.put(player.getUUID(), gameTime + COOLDOWN_TICKS);
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag cooldowns = new ListTag();
        cooldownExpiryByPlayer.forEach((playerId, expiry) -> {
            CompoundTag cooldownTag = new CompoundTag();
            cooldownTag.putUUID("Player", playerId);
            cooldownTag.putLong("Expiry", expiry);
            cooldowns.add(cooldownTag);
        });
        tag.put("Cooldowns", cooldowns);
        return tag;
    }
}

