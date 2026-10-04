package com.talhanation.recruits.entities.ai;

import com.talhanation.recruits.RecruitEvents;
import com.talhanation.recruits.config.RecruitsServerConfig;
import com.talhanation.recruits.entities.AbstractRecruitEntity;
import com.talhanation.recruits.world.RecruitsGroup;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;

import java.util.List;

public class RecruitDrinkSlotPotionGoal extends Goal {
    private final AbstractRecruitEntity recruit;
    private boolean finished;
    private int slot = -1;
    private int cooldown;
    private long lastCheck;

    public RecruitDrinkSlotPotionGoal(AbstractRecruitEntity recruit) {
        this.recruit = recruit;
    }

    @Override
    public boolean canUse() {
        if (recruit.isUsingItem()) return false;

        int forced = recruit.forcedPotionSlot;
        if (forced >= 0) {
            recruit.forcedPotionSlot = -1;
            if (isDrinkable(forced)) {
                slot = forced;
                return true;
            }
            return false;
        }

        long time = recruit.level().getGameTime();
        if (time - lastCheck < 10L) return false;
        lastCheck = time;

        if (cooldown > 0) {
            cooldown -= 10;
            return false;
        }

        RecruitsGroup group = recruit.getGroup() != null ? RecruitEvents.recruitsGroupsManager.getGroup(recruit.getGroup()) : null;
        int combatOnlyValue = group != null ? group.potionCombatOnly : -1;
        boolean combatOnly = combatOnlyValue >= 0 ? combatOnlyValue == 1 : RecruitsServerConfig.PotionOnlyInCombat.get();
        if (combatOnly && recruit.getTarget() == null) return false;

        for (int i = 0; i < 2; i++) {
            if (!isDrinkable(i)) continue;
            int configured = group != null ? group.potionMinHealth[i] : -1;
            int minHealth = configured >= 0 ? configured : (i == 0 ? RecruitsServerConfig.PotionSlot1MinHealth.get() : RecruitsServerConfig.PotionSlot2MinHealth.get());
            if (minHealth > 0 && recruit.getHealth() <= minHealth && !alreadyActive(recruit.potionSlots.getItem(i))) {
                slot = i;
                return true;
            }
        }
        return false;
    }

    private boolean isDrinkable(int i) {
        return recruit.potionSlots.getItem(i).is(Items.POTION) && !PotionUtils.getMobEffects(recruit.potionSlots.getItem(i)).isEmpty();
    }

    private boolean alreadyActive(ItemStack potion) {
        List<MobEffectInstance> effects = PotionUtils.getMobEffects(potion);
        return effects.stream().allMatch(e -> !e.getEffect().isInstantenous() && recruit.hasEffect(e.getEffect()));
    }

    @Override
    public boolean canContinueToUse() {
        return recruit.isUsingItem();
    }

    @Override
    public boolean isInterruptable() {
        return false;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    // The weapon waits in the potion slot while the potion is in the main hand.
    @Override
    public void start() {
        ItemStack potion = recruit.potionSlots.getItem(slot).copy();
        ItemStack weapon = recruit.getMainHandItem().copy();
        recruit.potionSlots.setItem(slot, weapon);
        recruit.isDrinkingPotion = true;
        finished = false;
        recruit.setItemInHand(InteractionHand.MAIN_HAND, potion);
        recruit.startUsingItem(InteractionHand.MAIN_HAND);
    }

    @Override
    public void tick() {
        if (recruit.getUseItemRemainingTicks() <= 2) finished = true;
    }

    @Override
    public void stop() {
        recruit.stopUsingItem();
        ItemStack weapon = recruit.potionSlots.getItem(slot).copy();
        ItemStack potion = recruit.getMainHandItem().copy();
        recruit.setItemInHand(InteractionHand.MAIN_HAND, weapon);
        if (finished) {
            recruit.potionSlots.setItem(slot, ItemStack.EMPTY);
            cooldown = RecruitsServerConfig.PotionCooldownSeconds.get() * 20;
        } else {
            recruit.potionSlots.setItem(slot, potion);
        }
        recruit.isDrinkingPotion = false;
        slot = -1;
    }
}
