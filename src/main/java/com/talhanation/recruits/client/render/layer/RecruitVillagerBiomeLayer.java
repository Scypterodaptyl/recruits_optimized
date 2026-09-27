package com.talhanation.recruits.client.render.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.talhanation.recruits.Main;
import com.talhanation.recruits.client.render.RecruitRenderState;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.Identifier;

public class RecruitVillagerBiomeLayer extends RenderLayer<RecruitRenderState, HumanoidModel<RecruitRenderState>> {

    private static final Identifier[] BIOME_TEXTURE = {
            Identifier.fromNamespaceAndPath(Main.MOD_ID,"textures/entity/villager/biome/villager_desert.png"),
            Identifier.fromNamespaceAndPath(Main.MOD_ID,"textures/entity/villager/biome/villager_jungle.png"),
            Identifier.fromNamespaceAndPath(Main.MOD_ID,"textures/entity/villager/biome/villager_plains.png"),
            Identifier.fromNamespaceAndPath(Main.MOD_ID,"textures/entity/villager/biome/villager_savanna.png"),
            Identifier.fromNamespaceAndPath(Main.MOD_ID,"textures/entity/villager/biome/villager_snowy_tundra.png"),
            Identifier.fromNamespaceAndPath(Main.MOD_ID,"textures/entity/villager/biome/villager_swamp.png"),
            Identifier.fromNamespaceAndPath(Main.MOD_ID,"textures/entity/villager/biome/villager_taiga.png"),
    };

    public RecruitVillagerBiomeLayer(RenderLayerParent<RecruitRenderState, HumanoidModel<RecruitRenderState>> renderer) {
        super(renderer);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int packedLight, RecruitRenderState state, float yRot, float xRot) {
        if(!state.isInvisible){
            renderColoredCutoutModel(this.getParentModel(), BIOME_TEXTURE[Math.floorMod(state.biome, BIOME_TEXTURE.length)], poseStack, collector, packedLight, state, -1, 2);
        }
    }
}
