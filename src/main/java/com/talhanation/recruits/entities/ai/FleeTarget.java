package com.talhanation.recruits.entities.ai;

import com.talhanation.recruits.entities.AssassinEntity;
import com.talhanation.recruits.pathfinding.AsyncPathfinderMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class FleeTarget extends Goal {

    AsyncPathfinderMob entity;

    public FleeTarget(AsyncPathfinderMob creatureEntity) {
    this.entity = creatureEntity;
    this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        float currentHealth = entity.getHealth();
        float maxHealth = entity.getMaxHealth();


        return (currentHealth <  maxHealth - maxHealth / 2.25);
    }

    @Override
    public void tick() {
        super.tick();

        LivingEntity target = entity.getTarget();
        boolean inRange = target != null && target.distanceToSqr(entity.getX(), entity.getY(), entity.getZ()) <= 32D * 32D;

        if (inRange) {
            double fleeDistance = 64.0D;
            Vec3 vecTarget = new Vec3(target.getX(), target.getY(), target.getZ());
            Vec3 vecRec = new Vec3(entity.getX(), entity.getY(), entity.getZ());
            Vec3 fleeDir = vecRec.subtract(vecTarget);
            fleeDir = fleeDir.normalize();
            Vec3 fleePos = new Vec3(vecRec.x + fleeDir.x * fleeDistance, vecRec.y + fleeDir.y * fleeDistance, vecRec.z + fleeDir.z * fleeDistance);
            entity.getNavigation().moveTo(fleePos.x, fleePos.y, fleePos.z, 1.25D);
            if (entity instanceof AssassinEntity recruit) {
                recruit.setFleeing(true);
            }
        } else if (entity instanceof AssassinEntity recruit) {
            recruit.setFleeing(false);
        }
    }
}
