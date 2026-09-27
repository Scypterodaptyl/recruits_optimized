package com.talhanation.recruits.client.gui.worldmap.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;
import org.joml.Vector2f;

import javax.annotation.Nullable;

/**
 * A free form quad for the GUI render state. GuiGraphics only offers axis aligned integer
 * rectangles, the world map needs rotated lines and sub pixel positioned tiles.
 */
public record GuiQuadRenderState(
        RenderPipeline pipeline,
        TextureSetup textureSetup,
        Matrix3x2fc pose,
        float[] positions,
        @Nullable float[] uvs,
        int color,
        @Nullable ScreenRectangle scissorArea,
        @Nullable ScreenRectangle bounds
) implements GuiElementRenderState {

    @Override
    public void buildVertices(VertexConsumer consumer) {
        for (int i = 0; i < 4; i++) {
            VertexConsumer vertex = consumer.addVertexWith2DPose(this.pose, positions[i * 2], positions[i * 2 + 1]);
            if (uvs != null) {
                vertex.setUv(uvs[i * 2], uvs[i * 2 + 1]);
            }
            vertex.setColor(this.color);
        }
    }

    public static void submitColored(GuiGraphics graphics, float[] positions, int color) {
        Matrix3x2f pose = new Matrix3x2f(graphics.pose());
        ScreenRectangle scissor = graphics.getScissorStack().peek();
        graphics.getRenderState().submitGuiElement(new GuiQuadRenderState(
                RenderPipelines.GUI, TextureSetup.noTexture(), pose, positions, null, color, scissor, bounds(positions, pose, scissor)));
    }

    public static void submitTextured(GuiGraphics graphics, Identifier texture, float[] positions, float[] uvs, int color) {
        AbstractTexture abstractTexture = Minecraft.getInstance().getTextureManager().getTexture(texture);
        Matrix3x2f pose = new Matrix3x2f(graphics.pose());
        ScreenRectangle scissor = graphics.getScissorStack().peek();
        graphics.getRenderState().submitGuiElement(new GuiQuadRenderState(
                RenderPipelines.GUI_TEXTURED,
                TextureSetup.singleTexture(abstractTexture.getTextureView(), abstractTexture.getSampler()),
                pose, positions, uvs, color, scissor, bounds(positions, pose, scissor)));
    }

    @Nullable
    private static ScreenRectangle bounds(float[] positions, Matrix3x2fc pose, @Nullable ScreenRectangle scissor) {
        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
        Vector2f v = new Vector2f();
        for (int i = 0; i < 4; i++) {
            pose.transformPosition(positions[i * 2], positions[i * 2 + 1], v);
            minX = Math.min(minX, v.x);
            minY = Math.min(minY, v.y);
            maxX = Math.max(maxX, v.x);
            maxY = Math.max(maxY, v.y);
        }
        int x = (int) Math.floor(minX);
        int y = (int) Math.floor(minY);
        ScreenRectangle rect = new ScreenRectangle(x, y, Math.max(1, (int) Math.ceil(maxX) - x), Math.max(1, (int) Math.ceil(maxY) - y));
        return scissor != null ? scissor.intersection(rect) : rect;
    }
}
