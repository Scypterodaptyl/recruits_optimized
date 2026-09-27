package com.talhanation.recruits.client.gui.worldmap.render;

import net.minecraft.client.gui.GuiGraphics;

public final class MapRenderUtil {
    private MapRenderUtil() {}

    public static void fill(GuiGraphics graphics, double x1, double y1, double x2, double y2, int color) {
        double left = Math.min(x1, x2);
        double right = Math.max(x1, x2);
        double top = Math.min(y1, y2);
        double bottom = Math.max(y1, y2);

        if (right <= left) right = left + 0.5;
        if (bottom <= top) bottom = top + 0.5;

        quad(graphics, left, bottom, right, bottom, right, top, left, top, color);
    }

    public static void line(GuiGraphics graphics,
                            double x1, double y1,
                            double x2, double y2,
                            double thickness,
                            int color) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double length = Math.hypot(dx, dy);
        if (length < 0.0001) {
            double radius = thickness * 0.5;
            fill(graphics, x1 - radius, y1 - radius, x1 + radius, y1 + radius, color);
            return;
        }

        double half = thickness * 0.5;
        double nx = -dy / length * half;
        double ny = dx / length * half;
        quad(graphics, x1 - nx, y1 - ny, x2 - nx, y2 - ny, x2 + nx, y2 + ny, x1 + nx, y1 + ny, color);
    }

    private static void quad(GuiGraphics graphics,
                             double x1, double y1,
                             double x2, double y2,
                             double x3, double y3,
                             double x4, double y4,
                             int color) {
        GuiQuadRenderState.submitColored(graphics, new float[]{
                (float) x1, (float) y1,
                (float) x2, (float) y2,
                (float) x3, (float) y3,
                (float) x4, (float) y4
        }, color);
    }

    public static void texturedQuad(GuiGraphics graphics, net.minecraft.resources.Identifier texture,
                                    double x1, double y1, double x2, double y2,
                                    float u1, float v1, float u2, float v2, int color) {
        GuiQuadRenderState.submitTextured(graphics, texture,
                new float[]{(float) x1, (float) y2, (float) x2, (float) y2, (float) x2, (float) y1, (float) x1, (float) y1},
                new float[]{u1, v2, u2, v2, u2, v1, u1, v1},
                color);
    }
}
