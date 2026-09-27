package com.talhanation.recruits.client.gui.worldmap.render;

import net.minecraft.client.gui.GuiGraphics;

/**
 * The map used to be rendered into an off screen framebuffer at an integer scale and then blitted
 * with the remaining fractional scale. GUI rendering is deferred now, so the same transform is
 * applied to the GUI pose instead.
 */
public final class MapFramebufferPass implements AutoCloseable {

    public void prepare() {
    }

    public Frame begin(
            GuiGraphics guiGraphics,
            double offsetX,
            double offsetZ,
            double scale,
            int screenWidth,
            int screenHeight) {
        Frame frame = Frame.fromView(offsetX, offsetZ, scale, screenWidth, screenHeight);
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().scale((float) frame.secondaryScale(), (float) frame.secondaryScale());
        guiGraphics.pose().translate((float) -frame.secondaryOffsetX(), (float) -frame.secondaryOffsetZ());
        return frame;
    }

    public void endAndBlit(GuiGraphics guiGraphics, Frame frame) {
        guiGraphics.pose().popMatrix();
    }

    @Override
    public void close() {
    }

    public record Frame(
            double fboScale,
            double secondaryScale,
            double renderOffsetX,
            double renderOffsetZ,
            double secondaryOffsetX,
            double secondaryOffsetZ,
            double leftWorld,
            double topWorld,
            double rightWorld,
            double bottomWorld,
            int screenWidth,
            int screenHeight) {
        private static Frame fromView(
                double offsetX, double offsetZ, double scale, int screenWidth, int screenHeight) {
            double effectiveScale = Math.max(0.01, scale);

            double fboScale =
                    effectiveScale >= 1.0 ? Math.max(1.0, Math.floor(effectiveScale)) : effectiveScale;
            double secondaryScale = effectiveScale / fboScale;

            double leftWorld = -offsetX / effectiveScale;
            double topWorld = -offsetZ / effectiveScale;
            double rightWorld = leftWorld + screenWidth / effectiveScale;
            double bottomWorld = topWorld + screenHeight / effectiveScale;

            double renderOffsetX;
            double renderOffsetZ;
            double secondaryOffsetX;
            double secondaryOffsetZ;

            if (fboScale < 1.0) {
                double pixelInBlocks = 1.0 / fboScale;
                double anchorWorldX = Math.floor(leftWorld / pixelInBlocks) * pixelInBlocks;
                double anchorWorldZ = Math.floor(topWorld / pixelInBlocks) * pixelInBlocks;
                renderOffsetX = -anchorWorldX * fboScale;
                renderOffsetZ = -anchorWorldZ * fboScale;
                secondaryOffsetX = (leftWorld - anchorWorldX) * fboScale;
                secondaryOffsetZ = (topWorld - anchorWorldZ) * fboScale;
            } else {
                double anchorWorldX = Math.floor(leftWorld);
                double anchorWorldZ = Math.floor(topWorld);
                secondaryOffsetX = (leftWorld - anchorWorldX) * fboScale;
                secondaryOffsetZ = (topWorld - anchorWorldZ) * fboScale;

                int wholeOffsetX = (int) Math.floor(secondaryOffsetX);
                int wholeOffsetZ = (int) Math.floor(secondaryOffsetZ);
                renderOffsetX = -anchorWorldX * fboScale - wholeOffsetX;
                renderOffsetZ = -anchorWorldZ * fboScale - wholeOffsetZ;
                secondaryOffsetX -= wholeOffsetX;
                secondaryOffsetZ -= wholeOffsetZ;
            }

            double padding = 2.0 / effectiveScale;
            return new Frame(
                    fboScale,
                    secondaryScale,
                    renderOffsetX,
                    renderOffsetZ,
                    secondaryOffsetX,
                    secondaryOffsetZ,
                    leftWorld - padding,
                    topWorld - padding,
                    rightWorld + padding,
                    bottomWorld + padding,
                    screenWidth,
                    screenHeight);
        }
    }
}
