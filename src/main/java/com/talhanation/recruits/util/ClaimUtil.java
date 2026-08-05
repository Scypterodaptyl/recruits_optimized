package com.talhanation.recruits.util;

import com.talhanation.recruits.world.RecruitsClaim;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.ChunkPos;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class ClaimUtil {
    public static List<LivingEntity> getLivingEntitiesInClaim(Level level, RecruitsClaim claim, Predicate<LivingEntity> filter) {
        List<ChunkPos> claimedChunks = claim.getClaimedChunks();
        if (claimedChunks.isEmpty()) return new ArrayList<>();

        int minChunkX = Integer.MAX_VALUE, minChunkZ = Integer.MAX_VALUE;
        int maxChunkX = Integer.MIN_VALUE, maxChunkZ = Integer.MIN_VALUE;
        for (ChunkPos pos : claimedChunks) {
            minChunkX = Math.min(minChunkX, pos.x);
            minChunkZ = Math.min(minChunkZ, pos.z);
            maxChunkX = Math.max(maxChunkX, pos.x);
            maxChunkZ = Math.max(maxChunkZ, pos.z);
        }

        Set<ChunkPos> claimedSet = new HashSet<>(claimedChunks);
        int minY = -64;
        int maxY = level.getMaxBuildHeight();
        AABB box = new AABB(
                new ChunkPos(minChunkX, minChunkZ).getMinBlockX(), minY, new ChunkPos(minChunkX, minChunkZ).getMinBlockZ(),
                new ChunkPos(maxChunkX, maxChunkZ).getMaxBlockX() + 1, maxY, new ChunkPos(maxChunkX, maxChunkZ).getMaxBlockZ() + 1
        );

        return level.getEntitiesOfClass(LivingEntity.class, box,
                entity -> claimedSet.contains(entity.chunkPosition()) && filter.test(entity));
    }

    public static List<LivingEntity> getLivingEntitiesInChunk(Level level, ChunkPos chunkPos, Predicate<LivingEntity> filter) {
        int minX = chunkPos.getMinBlockX();
        int minZ = chunkPos.getMinBlockZ();
        int maxX = chunkPos.getMaxBlockX();
        int maxZ = chunkPos.getMaxBlockZ();
        int minY = -64;
        int maxY = level.getMaxBuildHeight();
        AABB box = new AABB(minX, minY, minZ, maxX + 1, maxY, maxZ + 1);
        return level.getEntitiesOfClass(LivingEntity.class, box, filter);
    }
    public static List<LivingEntity> getLivingEntitiesInArea(Level level, AABB area, Predicate<LivingEntity> filter) {
        return level.getEntitiesOfClass(LivingEntity.class, area, filter);
    }
}

