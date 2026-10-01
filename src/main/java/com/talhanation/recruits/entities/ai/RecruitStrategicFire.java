package com.talhanation.recruits.entities.ai;

import com.talhanation.recruits.Main;
import com.talhanation.recruits.config.RecruitsServerConfig;
import com.talhanation.recruits.entities.BowmanEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;

public class RecruitStrategicFire extends Goal {

    private final Boolean consumeArrows;
    private BlockPos pos;
    private BowmanEntity bowman;

    private int attackIntervalMin;
    private int attackTime = -1;
    private final int attackIntervalMax;
    public RecruitStrategicFire(BowmanEntity bowman, int attackIntervalMin, int attackIntervalMax) {
        this.bowman = bowman;
        this.attackIntervalMin = attackIntervalMin;
        this.attackIntervalMax = attackIntervalMax;
        this.consumeArrows = RecruitsServerConfig.RangedRecruitsNeedArrowsToShoot.get();
    }

    @Override
    public boolean canUse() {
        if(bowman.getTarget() == null && this.hasArrows() && bowman.getShouldStrategicFire() && RecruitRangedBowAttackGoal.isHoldingBow(bowman) && bowman.getFollowState() != 5 && !bowman.needsToGetFood() && !bowman.getShouldMount()){
            return true;
        }
        else{
            this.pos = null;
            return false;
        }
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }


    public void stop() {
        super.stop();
        this.attackTime = -1;
        this.bowman.stopUsingItem();
        this.bowman.clearArrowsPos();
    }

    @Override
    public void tick() {
        this.pos = this.bowman.StrategicFirePos();
        if (pos != null) {
            double d0 = this.bowman.distanceToSqr(pos.getX(), bowman.getY(), pos.getZ());

            this.bowman.getLookControl().setLookAt(pos.getX(), pos.getY() + 16, pos.getZ(), 30.0F, 30.0F);
            //Main.LOGGER.debug("d0: " + d0);
            if (this.bowman.isUsingItem()) {

                int i = this.bowman.getTicksUsingItem();
                if (i >= 20) {

                    this.bowman.performRangedAttackXYZ(pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, BowItem.getPowerForTime(i));
                    float f = Mth.sqrt((float) d0) / 44F;
                    this.attackTime = Mth.floor(f * (float) (this.attackIntervalMax - this.attackIntervalMin) + (float) this.attackIntervalMin);
                    this.bowman.stopUsingItem();
                }
            } else if (--this.attackTime <= 0) {
                this.bowman.startUsingItem(ProjectileUtil.getWeaponHoldingHand(this.bowman, predicateBow -> this.isBow(predicateBow.getDefaultInstance())));
            }
        }
    }
    public boolean isBow(ItemStack itemStack){
        String name = this.bowman.getMainHandItem().getDescriptionId();
        return itemStack.is(Items.BOW) || itemStack.getItem() instanceof BowItem || itemStack.getItem() instanceof ProjectileWeaponItem || name.contains("bow");
    }

    private boolean hasArrows(){
        return !consumeArrows || this.bowman.getInventory().hasAnyMatching(item -> item.is(ItemTags.ARROWS));
    }

}
