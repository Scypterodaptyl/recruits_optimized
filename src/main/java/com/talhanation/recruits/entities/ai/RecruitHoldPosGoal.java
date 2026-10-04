package com.talhanation.recruits.entities.ai;

import com.talhanation.recruits.entities.AbstractRecruitEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class RecruitHoldPosGoal extends Goal {
    // Recruits walk right up to their spot and are pulled in from 0.7 blocks.
    private static final double SNAP_DISTANCE_SQR = 0.49D;
    private final AbstractRecruitEntity recruit;

    private int timeToRecalcPath;

    public RecruitHoldPosGoal(AbstractRecruitEntity recruit, double within) {
      this.recruit = recruit;
      this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public void start() {
        super.start();
        timeToRecalcPath = 0;
    }

    public boolean canUse() {
        if (this.recruit.getHoldPos() == null) {
            return false;
        }
        else
            return this.recruit.getShouldHoldPos() && !recruit.getFleeing() && !recruit.needsToGetFood() && !recruit.getShouldMount();
    }

    public boolean canContinueToUse() {
        return canUse();
    }

    public void tick() {
        Vec3 pos = this.recruit.getHoldPos();
        if (pos != null) {
            double dx = pos.x() - recruit.getX();
            double dz = pos.z() - recruit.getZ();
            // Same floor: only the horizontal distance counts, so a slot a block higher or lower still pulls in.
            double distance = Math.abs(pos.y() - recruit.getY()) <= 1.5D ? dx * dx + dz * dz : recruit.distanceToSqr(pos);
            if(distance >= SNAP_DISTANCE_SQR) {
                if (--this.timeToRecalcPath <= 0) {
                    this.timeToRecalcPath = this.recruit.getVehicle() != null ? this.adjustedTickDelay(5) : this.adjustedTickDelay(10);
                    this.recruit.getNavigation().moveTo(pos.x(), pos.y(), pos.z(), this.recruit.moveSpeed);
                }

                if (distance < 4.0D && this.recruit.getNavigation().isDone() && this.recruit.hurtTime == 0) {
                    this.recruit.getMoveControl().setWantedPosition(pos.x(), pos.y(), pos.z(), this.recruit.moveSpeed);
                }

                if (recruit.horizontalCollision || recruit.minorHorizontalCollision) {
                    this.recruit.getJumpControl().jump();
                }
            } else if (distance > 1.0E-4 && recruit.hurtTime == 0) {
                recruit.getNavigation().stop();
                recruit.setPos(pos.x(), recruit.getY(), pos.z());
                recruit.setDeltaMovement(0.0D, recruit.getDeltaMovement().y, 0.0D);
            }
        }
    }
}
