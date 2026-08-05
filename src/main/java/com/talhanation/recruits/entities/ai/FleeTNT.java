package com.talhanation.recruits.entities.ai;

import com.talhanation.recruits.entities.AbstractRecruitEntity;
import com.talhanation.recruits.entities.AssassinEntity;
import com.talhanation.recruits.entities.ai.async.NearbyEntityCache;
import com.talhanation.recruits.pathfinding.AsyncPathfinderMob;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class FleeTNT extends Goal {

    private static final double DANGER_RADIUS = 8.0D;
    private static final double SAFE_RADIUS = 14.0D;

    private final AsyncPathfinderMob entity;
    private int cooldown = 0;
    private final List<PrimedTnt> tntEntities = new ArrayList<>();
    private double fleeAngleOffset;

    public FleeTNT(AsyncPathfinderMob creatureEntity) {
        this.entity = creatureEntity;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        refreshNearbyTnt();
        if (tntEntities.isEmpty()) return false;

        double dangerSq = DANGER_RADIUS * DANGER_RADIUS;
        for (PrimedTnt tnt : tntEntities) {
            if (entity.distanceToSqr(tnt) <= dangerSq) return true;
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        refreshNearbyTnt();
        return !tntEntities.isEmpty();
    }

    @Override
    public void start() {
        this.fleeAngleOffset = (entity.getRandom().nextDouble() - 0.5) * 0.5;
    }

    private boolean refreshNearbyTnt() {
        if (--cooldown > 0) {
            return !tntEntities.isEmpty();
        }
        cooldown = 4 + entity.getRandom().nextInt(3);

        tntEntities.clear();
        if (entity.getCommandSenderWorld() instanceof ServerLevel serverLevel) {
            AABB aabb = entity.getBoundingBox().inflate(SAFE_RADIUS);
            for (Entity nearby : NearbyEntityCache.allEntities(serverLevel)) {
                if (nearby instanceof PrimedTnt tnt && aabb.contains(tnt.getX(), tnt.getY(), tnt.getZ())) {
                    tntEntities.add(tnt);
                }
            }
        }
        return !tntEntities.isEmpty();
    }

    @Override
    public void stop() {
        setFleeing(false);
    }

    @Override
    public void tick() {
        if (tntEntities.isEmpty()) {
            setFleeing(false);
            return;
        }

        Vec3 entityPos = new Vec3(entity.getX(), entity.getY(), entity.getZ());
        Vec3 combinedFleeDir = Vec3.ZERO;

        for (PrimedTnt tnt : tntEntities) {
            Vec3 tntPos = new Vec3(tnt.getX(), tnt.getY(), tnt.getZ());
            Vec3 fleeDir = entityPos.subtract(tntPos);

            double distanceSq = entityPos.distanceToSqr(tntPos);
            int fuse = tnt.getFuse();
            double fuseWeight = 1.0 / (fuse + 1.0); // Shorter fuse = higher weight
            double distanceWeight = 1.0 / (distanceSq + 1.0);
            double totalWeight = fuseWeight * distanceWeight;

            combinedFleeDir = combinedFleeDir.add(fleeDir.scale(totalWeight));
        }

        if (combinedFleeDir.lengthSqr() > 0) {
            combinedFleeDir = combinedFleeDir.normalize();
            combinedFleeDir = combinedFleeDir.yRot((float) fleeAngleOffset);

            double fleeDistance = 10.0D;
            Vec3 fleePos = entityPos.add(combinedFleeDir.scale(fleeDistance));

            entity.getNavigation().moveTo(fleePos.x, fleePos.y, fleePos.z, 1.25D);
            setFleeing(true);
        } else {
            setFleeing(false);
        }
    }

    private void setFleeing(boolean fleeing) {
        if (entity instanceof AbstractRecruitEntity recruit) {
            recruit.setFleeing(fleeing);
        }
        if (entity instanceof AssassinEntity assassin) {
            assassin.setFleeing(fleeing);
        }
    }
}
