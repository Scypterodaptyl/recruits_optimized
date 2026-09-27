package com.talhanation.recruits.client.render.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.talhanation.recruits.Main;
import com.talhanation.recruits.client.render.RecruitRenderState;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.Identifier;

public class RecruitVillagerTeamColorLayer extends RenderLayer<RecruitRenderState, HumanoidModel<RecruitRenderState>> {

    private static final Identifier[] TEXTURE = {
            Identifier.fromNamespaceAndPath(Main.MOD_ID,"textures/entity/villager/villager_team_white.png"),
            Identifier.fromNamespaceAndPath(Main.MOD_ID,"textures/entity/villager/villager_team_black.png"),
            Identifier.fromNamespaceAndPath(Main.MOD_ID,"textures/entity/villager/villager_team_light_grey.png"),
            Identifier.fromNamespaceAndPath(Main.MOD_ID,"textures/entity/villager/villager_team_grey.png"),
            Identifier.fromNamespaceAndPath(Main.MOD_ID,"textures/entity/villager/villager_team_dark_grey.png"),
            Identifier.fromNamespaceAndPath(Main.MOD_ID,"textures/entity/villager/villager_team_light_blue.png"),
            Identifier.fromNamespaceAndPath(Main.MOD_ID,"textures/entity/villager/villager_team_blue.png"),
            Identifier.fromNamespaceAndPath(Main.MOD_ID,"textures/entity/villager/villager_team_dark_blue.png"),
            Identifier.fromNamespaceAndPath(Main.MOD_ID,"textures/entity/villager/villager_team_light_green.png"),
            Identifier.fromNamespaceAndPath(Main.MOD_ID,"textures/entity/villager/villager_team_green.png"),
            Identifier.fromNamespaceAndPath(Main.MOD_ID,"textures/entity/villager/villager_team_dark_green.png"),
            Identifier.fromNamespaceAndPath(Main.MOD_ID,"textures/entity/villager/villager_team_light_red.png"),
            Identifier.fromNamespaceAndPath(Main.MOD_ID,"textures/entity/villager/villager_team_red.png"),
            Identifier.fromNamespaceAndPath(Main.MOD_ID,"textures/entity/villager/villager_team_dark_red.png"),
            Identifier.fromNamespaceAndPath(Main.MOD_ID,"textures/entity/villager/villager_team_light_brown.png"),
            Identifier.fromNamespaceAndPath(Main.MOD_ID,"textures/entity/villager/villager_team_brown.png"),
            Identifier.fromNamespaceAndPath(Main.MOD_ID,"textures/entity/villager/villager_team_dark_brown.png"),
            Identifier.fromNamespaceAndPath(Main.MOD_ID,"textures/entity/villager/villager_team_light_cyan.png"),
            Identifier.fromNamespaceAndPath(Main.MOD_ID,"textures/entity/villager/villager_team_cyan.png"),
            Identifier.fromNamespaceAndPath(Main.MOD_ID,"textures/entity/villager/villager_team_dark_cyan.png"),
            Identifier.fromNamespaceAndPath(Main.MOD_ID,"textures/entity/villager/villager_team_yellow.png"),
            Identifier.fromNamespaceAndPath(Main.MOD_ID,"textures/entity/villager/villager_team_orange.png"),
            Identifier.fromNamespaceAndPath(Main.MOD_ID,"textures/entity/villager/villager_team_magenta.png"),
            Identifier.fromNamespaceAndPath(Main.MOD_ID,"textures/entity/villager/villager_team_purple.png"),
            Identifier.fromNamespaceAndPath(Main.MOD_ID,"textures/entity/villager/villager_team_gold.png")
    };
    private static final Identifier TEXTURE2 = Identifier.fromNamespaceAndPath(Main.MOD_ID,"textures/entity/villager/villager_base_cloth.png");

    public RecruitVillagerTeamColorLayer(RenderLayerParent<RecruitRenderState, HumanoidModel<RecruitRenderState>> renderer) {
        super(renderer);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int packedLight, RecruitRenderState state, float yRot, float xRot) {
        if(!state.isInvisible){
            renderColoredCutoutModel(this.getParentModel(), state.hasTeam ? TEXTURE[Math.floorMod(state.color, TEXTURE.length)] : TEXTURE2, poseStack, collector, packedLight, state, -1, 1);
        }
    }
}
