package com.talhanation.recruits.client.gui.worldmap.route;

import com.talhanation.recruits.client.gui.worldmap.storage.WorldMapCacheManager;
import com.talhanation.recruits.world.RecruitsRoute;
import com.talhanation.recruits.world.RecruitsRoute.Waypoint;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ChunkPos;
import org.joml.Matrix4f;

import javax.annotation.Nullable;
import java.util.List;

public class RouteRenderer {
    private static final Identifier MAP_ICONS = Identifier.withDefaultNamespace("textures/map/decorations/player_off_map.png");

    private static final int COLOR_NORMAL = 0xFFFFFFFF; // white
    private static final int COLOR_NOT_LOADED = 0xFFFF4444; // red
    private static final int ROUTE_LINE_COLOR = 0xE6FFFFFF;
    private static final int ROUTE_LINE_SHADOW_COLOR = 0xAA000000;
    private static final int INSERT_LINE_COLOR = 0xFF00FFFF;
    private static final int ICON_INDEX = 6;

    // -------------------------------------------------------------------------

    public static void renderRoute(
            GuiGraphics guiGraphics, RecruitsRoute route, double offsetX, double offsetZ, double scale) {
        renderRoute(guiGraphics, route, offsetX, offsetZ, scale, null, -1);
    }

    public static void renderRoute(
            GuiGraphics guiGraphics,
            RecruitsRoute route,
            double offsetX,
            double offsetZ,
            double scale,
            @Nullable Waypoint draggingWaypoint,
            int dragInsertIndex) {
        if (route == null || route.getWaypoints().isEmpty()) return;

        List<Waypoint> waypoints = route.getWaypoints();

        renderLines(guiGraphics, waypoints, offsetX, offsetZ, scale, draggingWaypoint);
        renderInsertionIndicator(
                guiGraphics, waypoints, offsetX, offsetZ, scale, draggingWaypoint, dragInsertIndex);

        for (int i = 0; i < waypoints.size(); i++) {
            boolean isDragging = waypoints.get(i) == draggingWaypoint;
            renderWaypointIcon(
                    guiGraphics, waypoints.get(i), i + 1, offsetX, offsetZ, scale, 0xFF, isDragging);
        }
    }

    public static void renderDragGhost(
            GuiGraphics guiGraphics, Waypoint waypoint, int mouseX, int mouseY) {
        if (waypoint == null) return;
        renderIconAt(guiGraphics, mouseX, mouseY, ICON_INDEX, COLOR_NORMAL, 0xFF);
        if (waypoint.getAction() != null) {
            String label = getActionLabel(waypoint.getAction());
            int textWidth = Minecraft.getInstance().font.width(label);
            guiGraphics.drawString(
                    Minecraft.getInstance().font,
                    label,
                    mouseX - textWidth / 2,
                    mouseY + 6, com.talhanation.recruits.client.gui.util.GuiCompat.opaque(COLOR_NORMAL),
                    false);
        }
    }

    // -------------------------------------------------------------------------

    private static void renderLines(
            GuiGraphics guiGraphics,
            List<Waypoint> waypoints,
            double offsetX,
            double offsetZ,
            double scale,
            @Nullable Waypoint draggingWaypoint) {
        for (int i = 0; i < waypoints.size() - 1; i++) {
            Waypoint a = waypoints.get(i);
            Waypoint b = waypoints.get(i + 1);
            if (a == draggingWaypoint || b == draggingWaypoint) continue;
            double x1 = offsetX + a.getPosition().getX() * scale;
            double z1 = offsetZ + a.getPosition().getZ() * scale;
            double x2 = offsetX + b.getPosition().getX() * scale;
            double z2 = offsetZ + b.getPosition().getZ() * scale;
            renderRouteLine(guiGraphics, x1, z1, x2, z2, ROUTE_LINE_COLOR);
        }
    }

    private static void renderInsertionIndicator(
            GuiGraphics guiGraphics,
            List<Waypoint> waypoints,
            double offsetX,
            double offsetZ,
            double scale,
            @Nullable Waypoint draggingWaypoint,
            int insertIndex) {
        if (draggingWaypoint == null || insertIndex < 0) return;

        java.util.List<Waypoint> without = new java.util.ArrayList<>(waypoints);
        without.remove(draggingWaypoint);

        int clampedIdx = Math.max(0, Math.min(insertIndex, without.size()));
        Waypoint prev = clampedIdx > 0 ? without.get(clampedIdx - 1) : null;
        Waypoint next = clampedIdx < without.size() ? without.get(clampedIdx) : null;

        if (prev != null) {
            renderRouteLine(
                    guiGraphics,
                    offsetX + prev.getPosition().getX() * scale,
                    offsetZ + prev.getPosition().getZ() * scale,
                    offsetX + draggingWaypoint.getPosition().getX() * scale,
                    offsetZ + draggingWaypoint.getPosition().getZ() * scale,
                    INSERT_LINE_COLOR);
        }
        if (next != null) {
            renderRouteLine(
                    guiGraphics,
                    offsetX + draggingWaypoint.getPosition().getX() * scale,
                    offsetZ + draggingWaypoint.getPosition().getZ() * scale,
                    offsetX + next.getPosition().getX() * scale,
                    offsetZ + next.getPosition().getZ() * scale,
                    INSERT_LINE_COLOR);
        }
    }

    private static void renderRouteLine(
            GuiGraphics guiGraphics, double x1, double z1, double x2, double z2, int color) {
        if (Math.hypot(x2 - x1, z2 - z1) < 0.0001) return;

        com.talhanation.recruits.client.gui.worldmap.render.MapRenderUtil.line(guiGraphics, x1, z1, x2, z2, 3.0, ROUTE_LINE_SHADOW_COLOR);
        com.talhanation.recruits.client.gui.worldmap.render.MapRenderUtil.line(guiGraphics, x1, z1, x2, z2, 1.25, color);
    }

    private static void renderWaypointIcon(
            GuiGraphics guiGraphics,
            Waypoint waypoint,
            int number,
            double offsetX,
            double offsetZ,
            double scale,
            int alpha,
            boolean isDragging) {
        double pixelX = offsetX + waypoint.getPosition().getX() * scale;
        double pixelZ = offsetZ + waypoint.getPosition().getZ() * scale;

        boolean loaded = isChunkLoaded(waypoint);
        int color = loaded ? COLOR_NORMAL : COLOR_NOT_LOADED;
        int argb = (alpha << 24) | (color & 0x00FFFFFF);

        renderIconAt(guiGraphics, pixelX, pixelZ, ICON_INDEX, argb, alpha);

        // Number label
        String numStr = String.valueOf(number);
        renderCenteredTextAt(guiGraphics, numStr, pixelX, pixelZ - 10.0, argb);

        // Status / action label below icon, only when zoomed in and not being dragged
        if (scale > 2.0 && !isDragging) {
            String label = null;
            if (!loaded) {
                label = Component.translatable("gui.recruits.map.waypoint.not_loaded").getString();
            } else if (waypoint.getAction() != null) {
                label = getActionLabel(waypoint.getAction());
            }
            if (label != null) {
                renderCenteredTextAt(guiGraphics, label, pixelX, pixelZ + 5.0, argb);
            }
        }
    }

    private static String getActionLabel(RecruitsRoute.WaypointAction action) {
        if (action.getType() == RecruitsRoute.WaypointAction.Type.WAIT) {
            return Component.translatable(
                    "gui.recruits.map.waypoint.action.wait_label",
                    action.getWaitSeconds()).getString();
        }
        return action.toString();
    }

    private static void renderCenteredTextAt(
            GuiGraphics guiGraphics, String text, double centerX, double y, int color) {
        Font font = Minecraft.getInstance().font;
        org.joml.Matrix3x2fStack pose = guiGraphics.pose();
        pose.pushMatrix();
        pose.translate((float) (centerX), (float) (y));
        guiGraphics.drawString(font, text, -font.width(text) / 2, 0, com.talhanation.recruits.client.gui.util.GuiCompat.opaque(color), false);
        pose.popMatrix();
    }

    private static boolean isChunkLoaded(Waypoint waypoint) {
        net.minecraft.client.multiplayer.ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return false;
        ChunkPos chunk = new ChunkPos(waypoint.getPosition().getX() >> 4, waypoint.getPosition().getZ() >> 4);
        // Must be both explored on the map AND currently loaded in the level
        // so that surface Y can be resolved accurately when patrolling starts.
        if (!WorldMapCacheManager.getInstance().isChunkExplored(chunk)) return false;
        return level.getChunkSource().getChunk(chunk.x, chunk.z, false) != null;
    }

    private static void renderIconAt(
            GuiGraphics guiGraphics, double pixelX, double pixelZ, int iconIndex, int color, int alpha) {
        org.joml.Matrix3x2fStack pose = guiGraphics.pose();
        pose.pushMatrix();
        pose.translate((float) (pixelX), (float) (pixelZ));
        pose.scale((float) (3.0f), (float) (3.0f));


        // map decorations are single sprite files now, the old icon index is kept for reference
        com.talhanation.recruits.client.gui.worldmap.render.MapRenderUtil.texturedQuad(guiGraphics, MAP_ICONS, -1f, -1f, 1f, 1f, 0f, 1f, 1f, 0f, color);

        pose.popMatrix();
    }

    // -------------------------------------------------------------------------

    @Nullable
    public static Waypoint getWaypointAt(
            RecruitsRoute route,
            double mouseX,
            double mouseY,
            double offsetX,
            double offsetZ,
            double scale) {
        if (route == null) return null;
        int hitRadius = Math.max(5, (int) (8 * scale / 2.0));
        for (Waypoint wp : route.getWaypoints()) {
            int px = (int) (offsetX + wp.getPosition().getX() * scale);
            int pz = (int) (offsetZ + wp.getPosition().getZ() * scale);
            if (Math.abs(mouseX - px) <= hitRadius && Math.abs(mouseY - pz) <= hitRadius) return wp;
        }
        return null;
    }

    public static int computeInsertIndex(
            RecruitsRoute route,
            Waypoint dragging,
            double mouseX,
            double mouseY,
            double offsetX,
            double offsetZ,
            double scale) {
        if (route == null) return 0;

        List<Waypoint> without = new java.util.ArrayList<>(route.getWaypoints());
        without.remove(dragging);
        if (without.isEmpty()) return 0;

        int bestIndex = 0;
        double bestDist = Double.MAX_VALUE;

        double fx = offsetX + without.get(0).getPosition().getX() * scale;
        double fz = offsetZ + without.get(0).getPosition().getZ() * scale;
        double d = Math.hypot(mouseX - fx, mouseY - fz);
        double bias = (mouseX < fx) ? 0 : 20;
        if (d + bias < bestDist) {
            bestDist = d + bias;
            bestIndex = 0;
        }

        for (int i = 0; i < without.size() - 1; i++) {
            double mx =
                    (offsetX
                                    + without.get(i).getPosition().getX() * scale
                                    + offsetX
                                    + without.get(i + 1).getPosition().getX() * scale)
                            / 2.0;
            double mz =
                    (offsetZ
                                    + without.get(i).getPosition().getZ() * scale
                                    + offsetZ
                                    + without.get(i + 1).getPosition().getZ() * scale)
                            / 2.0;
            d = Math.hypot(mouseX - mx, mouseY - mz);
            if (d < bestDist) {
                bestDist = d;
                bestIndex = i + 1;
            }
        }

        double lx = offsetX + without.get(without.size() - 1).getPosition().getX() * scale;
        double lz = offsetZ + without.get(without.size() - 1).getPosition().getZ() * scale;
        d = Math.hypot(mouseX - lx, mouseY - lz);
        bias = (mouseX > lx) ? 0 : 20;
        if (d + bias < bestDist) bestIndex = without.size();

        return bestIndex;
    }
}
