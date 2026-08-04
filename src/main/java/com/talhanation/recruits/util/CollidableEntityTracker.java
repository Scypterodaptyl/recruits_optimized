package com.talhanation.recruits.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CollidableEntityTracker {
    private static final Map<Level, Integer> COUNTS = new ConcurrentHashMap<>();

    public static boolean hasCollidableEntities(Level level) {
        return COUNTS.getOrDefault(level, 0) > 0;
    }

    @SubscribeEvent
    public void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (canEverCollide(event.getEntity())) {
            COUNTS.merge(event.getLevel(), 1, Integer::sum);
        }
    }

    @SubscribeEvent
    public void onEntityLeave(EntityLeaveLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (canEverCollide(event.getEntity())) {
            COUNTS.computeIfPresent(event.getLevel(), (level, count) -> count > 1 ? count - 1 : null);
        }
    }

    @SubscribeEvent
    public void onLevelUnload(LevelEvent.Unload event) {
        COUNTS.remove(event.getLevel());
    }

    private static boolean canEverCollide(Entity entity) {
        return entity instanceof Boat || entity instanceof Shulker;
    }
}
