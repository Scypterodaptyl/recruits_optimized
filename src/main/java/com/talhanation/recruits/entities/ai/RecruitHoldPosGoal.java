package com.talhanation.recruits.entities.ai;

import com.talhanation.recruits.entities.AbstractRecruitEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class RecruitHoldPosGoal extends Goal {
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
            // Horizontal only, so gravity still applies when the ground under the hold pos is removed.
            double dx = recruit.getX() - pos.x();
            double dz = recruit.getZ() - pos.z();
            double distance = dx * dx + dz * dz;
            if(distance >= 0.36) {
                if (--this.timeToRecalcPath <= 0) {
                    this.timeToRecalcPath = this.recruit.getVehicle() != null ? this.adjustedTickDelay(5) : this.adjustedTickDelay(10);
                    this.recruit.getNavigation().moveTo(pos.x(), pos.y(), pos.z(), this.recruit.moveSpeed);
                }

                if (recruit.horizontalCollision || recruit.minorHorizontalCollision) {
                    this.recruit.getJumpControl().jump();
                }
            } else if (distance > 1.0E-4 && recruit.onGround()) {
                recruit.getNavigation().stop();
                recruit.setPos(pos.x(), recruit.getY(), pos.z());
                recruit.setDeltaMovement(0, recruit.getDeltaMovement().y, 0);
            }
        }
    }
}
