package com.list.util;

import com.list.gameplay.fish_group.pool.FishPoolDefinition;
import com.list.gameplay.fish_group.pool.FishPoolFactory;
import com.list.gameplay.fish_group.entity.AbstractFishPoolEntity;
import com.list.gameplay.fish_group.item.FishGroupRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3d;

import java.util.HashMap;
import java.util.Map;

public class FloatingPoolsSpawnerHelper {
    private static final int MAX_COUNT = 6;
    private static final int SPAWN_INTERVAL = 3000;
    private static final Map<ResourceKey<Level>, SpawnState> SPAWN_STATE_BY_DIMENSION = new HashMap<>();

    private static void attemptSpawn(ServerLevel level) {
        if (level.players().isEmpty()) return;
        RandomSource random = level.random;
        var player = level.players().get(random.nextInt(level.players().size()));
        ResourceKey<Level> dimension = level.dimension();
        SpawnState spawnState = SPAWN_STATE_BY_DIMENSION.get(dimension);

        if (spawnState == null || spawnState.lastValidSpawn() == null || (level.getGameTime() - spawnState.lastSpawnTime()) > 12000) {
            BlockPos lastValidSpawn = findValidSpawnPos(level, player.blockPosition());
            SPAWN_STATE_BY_DIMENSION.put(dimension, new SpawnState(lastValidSpawn, level.getGameTime()));
            spawnState = SPAWN_STATE_BY_DIMENSION.get(dimension);
        }

        BlockPos lastValidSpawn = spawnState.lastValidSpawn();
        if (lastValidSpawn == null) return;

        int spawnX = lastValidSpawn.getX();
        int spawnY = lastValidSpawn.getY();
        int spawnZ = lastValidSpawn.getZ();

        AABB area = new AABB(spawnX - 128, spawnY - 128, spawnZ - 128, spawnX + 128, spawnY + 128, spawnZ + 128);
        int totalCount = level.getEntitiesOfClass(AbstractFishPoolEntity.class, area).size();
        if (totalCount >= MAX_COUNT) return;

        var biome = level.getBiome(lastValidSpawn);
        java.util.List<FishPoolDefinition> availablePools = biome.is(BiomeTags.IS_OCEAN)
                ? FishPoolFactory.getAvailable(level, lastValidSpawn, FishPoolDefinition.Environment.OCEAN)
                : biome.is(BiomeTags.IS_RIVER)
                ? FishPoolFactory.getAvailable(level, lastValidSpawn, FishPoolDefinition.Environment.RIVER)
                : java.util.List.of();

        if (availablePools.isEmpty()) return;

        spawnFishPool(level, random, availablePools, spawnX, spawnY, spawnZ);
    }

    private static BlockPos findValidSpawnPos(ServerLevel level, BlockPos center) {
        RandomSource random = level.random;
        for (int i = 0; i < 10; i++) {
            int offsetX = random.nextInt(100 * 2) - 100;
            int offsetZ = random.nextInt(100 * 2) - 100;
            BlockPos newPos = center.offset(offsetX, 0, offsetZ);
            var biome = level.getBiome(newPos);
            if (biome.is(BiomeTags.IS_RIVER) || biome.is(BiomeTags.IS_OCEAN)) {
                int spawnY = level.getHeight(Heightmap.Types.WORLD_SURFACE, newPos.getX(), newPos.getZ()) - 2;
                return new BlockPos(newPos.getX(), spawnY, newPos.getZ());
            }
        }
        return null;
    }

    private static void spawnFishPool(ServerLevel level, RandomSource random, java.util.List<FishPoolDefinition> availablePools, int spawnX, int spawnY, int spawnZ) {
        FishPoolDefinition definition = availablePools.get(random.nextInt(availablePools.size()));
        AbstractFishPoolEntity fishPool = FishGroupRegistry.getFishPoolRegistration(definition.id()).create(level);
        if (fishPool == null) {
            return;
        }
        fishPool.setFishPoolDefinition(definition.id());
        Vector3d spawnPos = new Vector3d(spawnX + 0.5, spawnY, spawnZ + 0.5);
        fishPool.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
        level.addFreshEntity(fishPool);
    }

    public static void tick(ServerLevel level) {
        if (level.getGameTime() % SPAWN_INTERVAL == 0) attemptSpawn(level);
    }

    private record SpawnState(BlockPos lastValidSpawn, long lastSpawnTime) {
    }
}

