package com.talhanation.recruits.entities.ai.villager;

import com.talhanation.recruits.entities.RecruitEntity;
import com.talhanation.recruits.entities.ai.async.NearbyEntityCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.UUID;

public class FollowCaravanOwner extends Goal {

    public AbstractVillager villager;
    public UUID uuid;
    public RecruitEntity patrolOwner;

    public FollowCaravanOwner(AbstractVillager villager, UUID uuid) {
        this.villager = villager;
        this.uuid = uuid;
    }

    @Override
    public boolean canUse() {
        return true;
    }

    @Override
    public void start() {
        super.start();
        patrolOwner = this.getPatrolOwner();
    }

    @Nullable
    private RecruitEntity getPatrolOwner() {
        if (!(villager.getCommandSenderWorld() instanceof ServerLevel serverLevel)) return null;

        AABB aabb = villager.getBoundingBox().inflate(16D);
        for (LivingEntity entity : NearbyEntityCache.livingEntities(serverLevel)) {
            if (entity instanceof RecruitEntity recruit
                    && aabb.contains(recruit.getX(), recruit.getY(), recruit.getZ())
                    && recruit.getUUID().equals(uuid)) {
                return recruit;
            }
        }
        return null;
    }

    @Override
    public void tick() {
        super.tick();

        if (patrolOwner != null && villager.distanceTo(patrolOwner) > 8F) {
            villager.getNavigation().moveTo(patrolOwner, 1.05D);
        }
    }
}