package com.talhanation.recruits.client.render.pip;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelBakery;

public class ScaledBannerRenderer extends PictureInPictureRenderer<ScaledBannerRenderState> {

    public ScaledBannerRenderer(MultiBufferSource.BufferSource bufferSource) {
        super(bufferSource);
    }

    @Override
    public Class<ScaledBannerRenderState> getRenderStateClass() {
        return ScaledBannerRenderState.class;
    }

    @Override
    protected void renderToTexture(ScaledBannerRenderState state, PoseStack poseStack) {
        Minecraft mc = Minecraft.getInstance();
        mc.gameRenderer.getLighting().setupFor(Lighting.Entry.ITEMS_FLAT);
        poseStack.translate(0.0F, 0.25F, 0.0F);
        FeatureRenderDispatcher dispatcher = mc.gameRenderer.getFeatureRenderDispatcher();
        BannerRenderer.submitPatterns(mc.getAtlasManager(), poseStack, dispatcher.getSubmitNodeStorage(),
                15728880, OverlayTexture.NO_OVERLAY, state.flag(), 0.0F, ModelBakery.BANNER_BASE, true,
                state.baseColor(), state.resultBannerPatterns(), false, null, 0);
        dispatcher.renderAllFeatures();
    }

    @Override
    protected String getTextureLabel() {
        return "recruits scaled banner";
    }
}
