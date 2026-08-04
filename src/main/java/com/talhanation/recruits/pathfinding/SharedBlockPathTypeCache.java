package com.talhanation.recruits.pathfinding;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.BlockPathTypes;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public final class SharedBlockPathTypeCache {
    private static final long TTL_TICKS = 20L;
    private static final Map<Level, Entry> CACHES = new ConcurrentHashMap<>();

    private static final class Entry {
        final long expiresAt;
        final Map<Long, BlockPathTypes> types = new ConcurrentHashMap<>();

        Entry(long expiresAt) {
            this.expiresAt = expiresAt;
        }
    }

    private SharedBlockPathTypeCache() {
    }

    public static BlockPathTypes computeIfAbsent(Level level, int x, int y, int z, Supplier<BlockPathTypes> compute) {
        Entry entry = currentEntry(level);
        return entry.types.computeIfAbsent(BlockPos.asLong(x, y, z), key -> compute.get());
    }

    private static Entry currentEntry(Level level) {
        long time = level.getGameTime();
        Entry entry = CACHES.get(level);
        if (entry != null && time < entry.expiresAt) {
            return entry;
        }
        return CACHES.compute(level, (lvl, existing) ->
                (existing == null || time >= existing.expiresAt) ? new Entry(time + TTL_TICKS) : existing);
    }
}
