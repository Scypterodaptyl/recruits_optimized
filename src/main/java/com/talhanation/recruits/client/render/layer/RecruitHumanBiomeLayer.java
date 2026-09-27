package com.talhanation.recruits.client.render.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.talhanation.recruits.Main;
import com.talhanation.recruits.client.render.RecruitRenderState;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.Identifier;

public class RecruitHumanBiomeLayer extends RenderLayer<RecruitRenderState, HumanoidModel<RecruitRenderState>> {

    private static final Identifier[] TEXTURE = {
            Identifier.fromNamespaceAndPath(Main.MOD_ID,"textures/entity/human/biome/human_desert.png"),
            Identifier.fromNamespaceAndPath(Main.MOD_ID,"textures/entity/human/biome/human_jungle.png"),
            Identifier.fromNamespaceAndPath(Main.MOD_ID,"textures/entity/human/biome/human_plains.png"),
            Identifier.fromNamespaceAndPath(Main.MOD_ID,"textures/entity/human/biome/human_savanna.png"),
            Identifier.fromNamespaceAndPath(Main.MOD_ID,"textures/entity/human/biome/human_snowy_tundra.png"),
            Identifier.fromNamespaceAndPath(Main.MOD_ID,"textures/entity/human/biome/human_swamp.png"),
            Identifier.fromNamespaceAndPath(Main.MOD_ID,"textures/entity/human/biome/human_taiga.png"),
    };

    private final HumanoidModel<RecruitRenderState> overlayModel;

    public RecruitHumanBiomeLayer(RenderLayerParent<RecruitRenderState, HumanoidModel<RecruitRenderState>> renderer, ModelPart overlayRoot) {
        super(renderer);
        this.overlayModel = new HumanoidModel<>(overlayRoot);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int packedLight, RecruitRenderState state, float yRot, float xRot) {
        if(!state.isInvisible){
            renderColoredCutoutModel(this.overlayModel, TEXTURE[Math.floorMod(state.biome, TEXTURE.length)], poseStack, collector, packedLight, state, -1, 2);
        }
    }
}
