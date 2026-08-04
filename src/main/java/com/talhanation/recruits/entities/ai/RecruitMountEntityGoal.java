package com.talhanation.recruits.entities.ai;

import com.talhanation.recruits.config.RecruitsServerConfig;
import com.talhanation.recruits.entities.AbstractRecruitEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

public class RecruitMountEntityGoal extends Goal {

    private final AbstractRecruitEntity recruit;
    private Entity mount;
    private int timeToRecalcPath;

    public RecruitMountEntityGoal(AbstractRecruitEntity recruit) {
        this.recruit = recruit;
    }

    @Override
    public boolean canUse() {
        return recruit.getShouldMount();
    }

    public boolean canContinueToUse() {
        return canUse();
    }

    public void start(){
        this.timeToRecalcPath = 0;
        this.findMount();
        this.recruit.setMountTimer(200);
    }

    public boolean requiresUpdateEveryTick() {
        return true;
    }

    public void tick() {
        if(this.recruit.getVehicle() == null && this.mount != null) {
            if(recruit.getMountTimer() > 0){

                if (--this.timeToRecalcPath <= 0) {
                    this.timeToRecalcPath = this.adjustedTickDelay(10);
                    recruit.getNavigation().moveTo(mount, 1.15F);
                }

                if (recruit.horizontalCollision || recruit.minorHorizontalCollision) {
                    this.recruit.getJumpControl().jump();
                }

                if (recruit.distanceToSqr(mount) < 50D) {
                    recruit.startRiding(mount);
                    if(recruit.isPassenger()) recruit.setShouldMount(false);
                }
            }
            else recruit.setShouldMount(false);
        }
    }

    private void findMount(){
        if (recruit.getMountUUID() == null) return;
        if (!(recruit.getCommandSenderWorld() instanceof ServerLevel serverLevel)) return;

        Entity candidate = serverLevel.getEntity(recruit.getMountUUID());
        if (candidate != null && candidate.distanceToSqr(recruit) <= 32D * 32D && recruit.canMountEntity(candidate)) {
            this.mount = candidate;
        }
    }
}