package com.talhanation.recruits.client.render;

import com.talhanation.recruits.compat.musketmod.IWeapon;
import com.talhanation.recruits.entities.AbstractRecruitEntity;
import com.talhanation.recruits.entities.CrossBowmanEntity;
import com.talhanation.recruits.entities.ICompanion;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;

public final class RecruitArmPoses {

    private RecruitArmPoses() {
    }

    public static void extract(AbstractRecruitEntity recruit, RecruitRenderState state) {
        state.variant = recruit.getVariant();
        state.color = recruit.getColor();
        state.biome = recruit.getBiome();
        state.hasTeam = recruit.getTeam() != null;
        state.isCompanion = recruit instanceof ICompanion;
    }

    public static HumanoidModel.ArmPose getArmPose(AbstractRecruitEntity recruit, HumanoidArm arm, boolean useForgeExtensions) {
        boolean isMainArm = recruit.getMainArm() == arm;
        HumanoidModel.ArmPose mainPose = getArmPose(recruit, InteractionHand.MAIN_HAND, useForgeExtensions);
        if (isMainArm) {
            return mainPose;
        }
        if (mainPose.isTwoHanded()) {
            return recruit.getOffhandItem().isEmpty() ? HumanoidModel.ArmPose.EMPTY : HumanoidModel.ArmPose.ITEM;
        }
        return getArmPose(recruit, InteractionHand.OFF_HAND, useForgeExtensions);
    }

    private static HumanoidModel.ArmPose getArmPose(AbstractRecruitEntity recruit, InteractionHand hand, boolean useForgeExtensions) {
        ItemStack itemstack = recruit.getItemInHand(hand);
        boolean isMusket = IWeapon.isMusketModWeapon(itemstack) && (recruit instanceof CrossBowmanEntity crossBowman) && crossBowman.isAggressive();
        if (itemstack.isEmpty()) {
            return HumanoidModel.ArmPose.EMPTY;
        } else {
            if (recruit.getUsedItemHand() == hand && recruit.getUseItemRemainingTicks() > 0) {
                ItemUseAnimation useanim = itemstack.getUseAnimation();
                if (useanim == ItemUseAnimation.BLOCK) {
                    return HumanoidModel.ArmPose.BLOCK;
                }

                if (useanim == ItemUseAnimation.BOW) {
                    return HumanoidModel.ArmPose.BOW_AND_ARROW;
                }

                if (useanim == ItemUseAnimation.TRIDENT) {
                    return HumanoidModel.ArmPose.THROW_TRIDENT;
                }

                if (useanim == ItemUseAnimation.CROSSBOW && hand == recruit.getUsedItemHand() || isMusket) {
                    return HumanoidModel.ArmPose.CROSSBOW_CHARGE;
                }

                if (useanim == ItemUseAnimation.SPYGLASS) {
                    return HumanoidModel.ArmPose.SPYGLASS;
                }
            } else if (!recruit.swinging && itemstack.is(Items.CROSSBOW) && CrossbowItem.isCharged(itemstack) || isMusket) {
                return HumanoidModel.ArmPose.CROSSBOW_HOLD;
            }

            if (useForgeExtensions) {
                HumanoidModel.ArmPose forgeArmPose = net.minecraftforge.client.extensions.common.IClientItemExtensions.of(itemstack).getArmPose(recruit, hand, itemstack);
                if (forgeArmPose != null) return forgeArmPose;
            }

            return HumanoidModel.ArmPose.ITEM;
        }
    }
}
