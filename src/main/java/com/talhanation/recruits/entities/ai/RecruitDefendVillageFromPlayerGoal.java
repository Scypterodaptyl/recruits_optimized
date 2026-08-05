package com.talhanation.recruits.entities.ai;

import com.talhanation.recruits.entities.AbstractRecruitEntity;
import com.talhanation.recruits.entities.ai.async.NearbyEntityCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class RecruitDefendVillageFromPlayerGoal extends TargetGoal {
    private final AbstractRecruitEntity recruit;
    @Nullable
    private LivingEntity potentialTarget;
    private final TargetingConditions attackTargeting = TargetingConditions.forCombat().range(64.0D).ignoreLineOfSight();
    private int scanDelay;

    public RecruitDefendVillageFromPlayerGoal(AbstractRecruitEntity p_26029_) {
        super(p_26029_, false, true);
        this.recruit = p_26029_;
        this.setFlags(EnumSet.of(Flag.TARGET));
        this.scanDelay = p_26029_.getRandom().nextInt(20);
    }
    public boolean canUse() {
        if (--this.scanDelay > 0) return false;
        this.scanDelay = 20 + this.recruit.getRandom().nextInt(10);

        this.potentialTarget = null;

        if (!(this.recruit.getCommandSenderWorld() instanceof ServerLevel serverLevel)) return false;

        AABB aabb = this.recruit.getBoundingBox().inflate(30.0D, 8.0D, 30.0D);
        List<Villager> list = new ArrayList<>();
        List<Player> list1 = new ArrayList<>();
        for (LivingEntity entity : NearbyEntityCache.livingEntities(serverLevel)) {
            if (!aabb.contains(entity.getX(), entity.getY(), entity.getZ())) continue;
            if (entity instanceof Villager villager) {
                if (!this.attackTargeting.test(this.recruit, villager)) {
                    list.add(villager);
                }
            } else if (entity instanceof Player player) {
                list1.add(player);
            }
        }

        for(Villager villager : list) {
            for(Player player : list1) {
                int i = villager.getPlayerReputation(player);
                if (i <= -100) {
                    this.potentialTarget = player;
                }
            }
        }

        if (this.potentialTarget == null) {
            return false;
        } else {
            return !(this.potentialTarget instanceof Player) || !this.potentialTarget.isSpectator() && !((Player)this.potentialTarget).isCreative();
        }
    }

    public void start() {
        this.recruit.setTarget(this.potentialTarget);
        super.start();
    }
}