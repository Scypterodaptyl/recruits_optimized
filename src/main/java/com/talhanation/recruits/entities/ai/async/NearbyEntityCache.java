package com.talhanation.recruits.entities.ai.async;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class NearbyEntityCache {
    private static final Map<ServerLevel, Snapshot> CACHE = new ConcurrentHashMap<>();

    private static final class Snapshot {
        final long gameTime;
        final List<Entity> all;
        List<LivingEntity> living;

        Snapshot(long gameTime, List<Entity> all) {
            this.gameTime = gameTime;
            this.all = all;
        }
    }

    private NearbyEntityCache() {
    }

    public static List<Entity> allEntities(ServerLevel level) {
        return snapshot(level).all;
    }

    public static List<LivingEntity> livingEntities(ServerLevel level) {
        Snapshot snapshot = snapshot(level);
        List<LivingEntity> living = snapshot.living;
        if (living == null) {
            List<LivingEntity> filtered = new ArrayList<>();
            for (Entity entity : snapshot.all) {
                if (entity instanceof LivingEntity livingEntity) {
                    filtered.add(livingEntity);
                }
            }
            living = Collections.unmodifiableList(filtered);
            snapshot.living = living;
        }
        return living;
    }

    private static Snapshot snapshot(ServerLevel level) {
        long time = level.getGameTime();
        Snapshot snapshot = CACHE.get(level);
        if (snapshot == null || snapshot.gameTime != time) {
            List<Entity> all = new ArrayList<>();
            level.getEntities().getAll().forEach(all::add);
            snapshot = new Snapshot(time, Collections.unmodifiableList(all));
            CACHE.put(level, snapshot);
        }
        return snapshot;
    }
}
