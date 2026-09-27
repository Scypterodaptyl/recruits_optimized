package com.talhanation.recruits.client.render;

import com.talhanation.recruits.Main;
import com.talhanation.recruits.client.events.ClientEvent;
import com.talhanation.recruits.client.render.layer.RecruitHumanBiomeLayer;
import com.talhanation.recruits.client.render.layer.RecruitHumanCompanionLayer;
import com.talhanation.recruits.client.render.layer.RecruitHumanTeamColorLayer;
import com.talhanation.recruits.entities.AbstractRecruitEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;

public class RecruitHumanRenderer extends HumanoidMobRenderer<AbstractRecruitEntity, RecruitRenderState, HumanoidModel<RecruitRenderState>> {

    private static final Identifier[] TEXTURE = {
            Identifier.fromNamespaceAndPath(Main.MOD_ID,"textures/entity/human/human_0.png"),
            Identifier.fromNamespaceAndPath(Main.MOD_ID,"textures/entity/human/human_1.png"),
            Identifier.fromNamespaceAndPath(Main.MOD_ID,"textures/entity/human/human_2.png"),
            Identifier.fromNamespaceAndPath(Main.MOD_ID,"textures/entity/human/human_3.png"),
            Identifier.fromNamespaceAndPath(Main.MOD_ID,"textures/entity/human/human_4.png"),
            Identifier.fromNamespaceAndPath(Main.MOD_ID,"textures/entity/human/human_5.png"),
            Identifier.fromNamespaceAndPath(Main.MOD_ID,"textures/entity/human/human_6.png"),
            Identifier.fromNamespaceAndPath(Main.MOD_ID,"textures/entity/human/human_7.png"),
            Identifier.fromNamespaceAndPath(Main.MOD_ID,"textures/entity/human/human_8.png"),
            Identifier.fromNamespaceAndPath(Main.MOD_ID,"textures/entity/human/human_9.png"),
            Identifier.fromNamespaceAndPath(Main.MOD_ID,"textures/entity/human/human_10.png"),
            Identifier.fromNamespaceAndPath(Main.MOD_ID,"textures/entity/human/human_11.png"),
            Identifier.fromNamespaceAndPath(Main.MOD_ID,"textures/entity/human/human_12.png"),
            Identifier.fromNamespaceAndPath(Main.MOD_ID,"textures/entity/human/human_13.png"),
            Identifier.fromNamespaceAndPath(Main.MOD_ID,"textures/entity/human/human_14.png"),
            Identifier.fromNamespaceAndPath(Main.MOD_ID,"textures/entity/human/human_15.png"),
            Identifier.fromNamespaceAndPath(Main.MOD_ID,"textures/entity/human/human_16.png"),
            Identifier.fromNamespaceAndPath(Main.MOD_ID,"textures/entity/human/human_17.png"),
            Identifier.fromNamespaceAndPath(Main.MOD_ID,"textures/entity/human/human_18.png"),
            Identifier.fromNamespaceAndPath(Main.MOD_ID,"textures/entity/human/human_19.png")
    };


    public RecruitHumanRenderer(EntityRendererProvider.Context mgr) {
        super(mgr, new HumanoidModel<>(mgr.bakeLayer(ModelLayers.PLAYER)), 0.5F);
        this.addLayer(new HumanoidArmorLayer<>(this, ArmorModelSet.bake(ModelLayers.PLAYER_ARMOR, mgr.getModelSet(), HumanoidModel::new), mgr.getEquipmentRenderer()));
        this.addLayer(new RecruitHumanTeamColorLayer(this, mgr.bakeLayer(ClientEvent.RECRUIT_HUMAN_TEAM_LAYER)));
        this.addLayer(new RecruitHumanBiomeLayer(this, mgr.bakeLayer(ClientEvent.RECRUIT_HUMAN_BIOME_LAYER)));
        this.addLayer(new RecruitHumanCompanionLayer(this, mgr.bakeLayer(ClientEvent.RECRUIT_HUMAN_COMPANION_LAYER)));
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
        return TEXTURE[Math.floorMod(state.variant, TEXTURE.length)];
    }

    @Override
    protected HumanoidModel.ArmPose getArmPose(AbstractRecruitEntity recruit, HumanoidArm arm) {
        return RecruitArmPoses.getArmPose(recruit, arm, true);
    }
}
