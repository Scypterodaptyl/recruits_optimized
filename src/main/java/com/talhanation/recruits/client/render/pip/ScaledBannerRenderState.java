package com.talhanation.recruits.client.render.pip;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import net.minecraft.client.model.object.banner.BannerFlagModel;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.jspecify.annotations.Nullable;

/** Like the vanilla loom banner state, but with a configurable scale. */
public record ScaledBannerRenderState(
        BannerFlagModel flag,
        DyeColor baseColor,
        BannerPatternLayers resultBannerPatterns,
        int x0, int y0, int x1, int y1,
        float scale,
        @Nullable ScreenRectangle scissorArea,
        @Nullable ScreenRectangle bounds
) implements PictureInPictureRenderState {

    public ScaledBannerRenderState(BannerFlagModel flag, DyeColor baseColor, BannerPatternLayers patterns,
                                   int x0, int y0, int x1, int y1, float scale, @Nullable ScreenRectangle scissorArea) {
        this(flag, baseColor, patterns, x0, y0, x1, y1, scale, scissorArea,
                PictureInPictureRenderState.getBounds(x0, y0, x1, y1, scissorArea));
    }
}
