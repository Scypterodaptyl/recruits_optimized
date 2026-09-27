package com.talhanation.recruits.client.render.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.talhanation.recruits.Main;
import com.talhanation.recruits.client.render.RecruitRenderState;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.Identifier;

public class RecruitVillagerCompanionLayer extends RenderLayer<RecruitRenderState, HumanoidModel<RecruitRenderState>> {

    private static final Identifier LOCATION = Identifier.fromNamespaceAndPath(Main.MOD_ID,"textures/entity/villager/villager_assassin_cloth.png");

    public RecruitVillagerCompanionLayer(RenderLayerParent<RecruitRenderState, HumanoidModel<RecruitRenderState>> renderer) {
        super(renderer);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int packedLight, RecruitRenderState state, float yRot, float xRot) {
        if(!state.isInvisible && state.isCompanion){
            renderColoredCutoutModel(this.getParentModel(), LOCATION, poseStack, collector, packedLight, state, -1, 3);
        }
    }
}
