package com.talhanation.recruits.client.render.layer;

import com.talhanation.recruits.config.RecruitsClientConfig;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

public class RecruitArmorLayer extends HumanoidModel<HumanoidRenderState> {
    public RecruitArmorLayer(ModelPart part) {
        super(part);
    }

    /**
     * Armor layers of the villager looking recruits. The outer layer gets a villager shaped head.
     */
    public static ArmorModelSet<LayerDefinition> createArmorLayerSet() {
        return createArmorMeshSet(RecruitArmorLayer::createBaseArmorMesh, new CubeDeformation(0.51F), new CubeDeformation(1.0F))
                .map(mesh -> LayerDefinition.create(mesh, 64, 32));
    }

    private static MeshDefinition createBaseArmorMesh(CubeDeformation deformation) {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(deformation, 0.0F);
        PartDefinition partdefinition = meshdefinition.getRoot();

        if (RecruitsClientConfig.RecruitsLookLikeVillagers.get()) {
            partdefinition.addOrReplaceChild("head",
                    CubeListBuilder.create()
                            .texOffs(0, 0)
                            .addBox(-4.0F, -10.0F, -4.0F, 8.0F, 8.0F, 8.0F, deformation),
                    PartPose.offset(0.0F, 1.0F, 0.0F));
        }
        return meshdefinition;
    }
}
