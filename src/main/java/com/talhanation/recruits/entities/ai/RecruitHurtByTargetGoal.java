package com.talhanation.recruits.entities.ai;

import com.talhanation.recruits.entities.AbstractRecruitEntity;
import com.talhanation.recruits.entities.ai.async.NearbyEntityCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

public class RecruitHurtByTargetGoal extends HurtByTargetGoal {
    private static final TargetingConditions HURT_BY_TARGETING = TargetingConditions.forCombat().ignoreLineOfSight().ignoreInvisibilityTesting();

    private boolean alertSameType;
    private int timestamp;
    private Class<?>[] toIgnoreAlert;
    private final AbstractRecruitEntity recruit;

    public RecruitHurtByTargetGoal(AbstractRecruitEntity recruit) {
        super(recruit);
        this.recruit = recruit;
    }

    public boolean canUse() {
        int i = this.recruit.getLastHurtByMobTimestamp();
        LivingEntity livingentity = this.recruit.getLastHurtByMob();

        if (i != this.timestamp && livingentity != null) {
            return this.canAttack(livingentity, HURT_BY_TARGETING) && (recruit.getState() != 3) && !(this.recruit.getTeam() != null && this.recruit.getTeam().isAlliedTo(livingentity.getTeam()) && !this.recruit.getTeam().isAllowFriendlyFire());
        }
        return false;
    }

    public @NotNull HurtByTargetGoal setAlertOthers(Class<?> @NotNull ... p_220794_1_) {
        this.alertSameType = true;
        this.toIgnoreAlert = p_220794_1_;
        return this;
    }

    public void start() {
        LivingEntity hurtingMob = this.recruit.getLastHurtByMob();
        this.recruit.setTarget(hurtingMob);
        this.targetMob = this.recruit.getTarget();
        this.timestamp = this.recruit.getLastHurtByMobTimestamp();
        this.unseenMemoryTicks = 300;
        if (this.alertSameType) {
            this.alertOthers();
        }

        super.start();
    }

    protected void alertOthers() {
        if (!(this.recruit.getCommandSenderWorld() instanceof ServerLevel serverLevel)) return;

        double d0 = this.getFollowDistance();
        AABB axisalignedbb = AABB.unitCubeFromLowerCorner(this.recruit.position())
                .inflate(d0, 16.0D, d0);

        for (LivingEntity entity : NearbyEntityCache.livingEntities(serverLevel)) {
            if (!(entity instanceof AbstractRecruitEntity recruitToAlert)) continue;
            if (recruitToAlert.getClass() != this.recruit.getClass()) continue;
            if (!axisalignedbb.contains(recruitToAlert.position())) continue;

            if (this.recruit == recruitToAlert ||
                    recruitToAlert.getTarget() != null ||
                    this.recruit.getLastHurtByMob() == null ||
                    recruitToAlert.getOwnerUUID() == null ||
                    !recruitToAlert.getOwnerUUID().equals(this.recruit.getOwnerUUID()) ||
                    recruitToAlert.isAlliedTo(this.recruit.getLastHurtByMob())) continue;

            boolean shouldIgnore = false;
            if (this.toIgnoreAlert != null) {
                for (Class<?> oclass : this.toIgnoreAlert) {
                    if (recruitToAlert.getClass() == oclass) {
                        shouldIgnore = true;
                        break;
                    }
                }
            }
            if (shouldIgnore) continue;

            this.alertOther(recruitToAlert, this.recruit.getLastHurtByMob());
        }
    }
}
