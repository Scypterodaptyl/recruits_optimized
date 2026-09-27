package com.talhanation.recruits.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.talhanation.recruits.Main;
import com.talhanation.recruits.client.events.ClientEvent;
import com.talhanation.recruits.client.models.RecruitVillagerModel;
import com.talhanation.recruits.client.render.layer.RecruitVillagerBiomeLayer;
import com.talhanation.recruits.client.render.layer.RecruitVillagerCompanionLayer;
import com.talhanation.recruits.client.render.layer.RecruitVillagerTeamColorLayer;
import com.talhanation.recruits.entities.AbstractRecruitEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.VillagerRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;

public class RecruitVillagerRenderer extends HumanoidMobRenderer<AbstractRecruitEntity, RecruitRenderState, HumanoidModel<RecruitRenderState>> {

    private static final Identifier[] TEXTURE = {
            Identifier.fromNamespaceAndPath(Main.MOD_ID,"textures/entity/villager/villager_1.png"),
    };


    public RecruitVillagerRenderer(EntityRendererProvider.Context context) {
        super(context, new RecruitVillagerModel(context.bakeLayer(ClientEvent.RECRUIT)), new RecruitVillagerModel(context.bakeLayer(ClientEvent.RECRUIT)), 0.5F, VillagerRenderer.CUSTOM_HEAD_TRANSFORMS);
        this.addLayer(new HumanoidArmorLayer<>(this, ArmorModelSet.bake(ClientEvent.RECRUIT_ARMOR, context.getModelSet(), HumanoidModel::new), context.getEquipmentRenderer()));
        this.addLayer(new RecruitVillagerTeamColorLayer(this));
        this.addLayer(new RecruitVillagerBiomeLayer(this));
        this.addLayer(new RecruitVillagerCompanionLayer(this));
    }

    @Override
    public RecruitRenderState createRenderState() {
        return new RecruitRenderState();
    }

    @Override
    public void extractRenderState(AbstractRecruitEntity recruit, RecruitRenderState state, float partialTick) {
        super.extractRenderState(recruit, state, partialTick);
        RecruitArmPoses.extract(recruit, state);
    }

    @Override
    public Identifier getTextureLocation(RecruitRenderState state) {
        return TEXTURE[0];
    }

    @Override
    protected HumanoidModel.ArmPose getArmPose(AbstractRecruitEntity recruit, HumanoidArm arm) {
        return RecruitArmPoses.getArmPose(recruit, arm, false);
    }

    @Override
    protected void scale(RecruitRenderState state, PoseStack poseStack) {
        poseStack.scale(0.9375F, 0.9375F, 0.9375F);
    }
}
