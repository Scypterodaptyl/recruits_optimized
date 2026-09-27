package com.talhanation.recruits.client.gui.worldmap.render;

import com.talhanation.recruits.Main;
import com.talhanation.recruits.compat.smallships.SmallShips;
import net.minecraft.client.gui.Font;
import org.joml.Matrix3x2fStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class WorldMapPlayerRenderer {
    private static final Identifier MAP_ICONS = Identifier.withDefaultNamespace("textures/map/decorations/player.png");
    private static final ItemStack BOAT_STACK = new ItemStack(Items.OAK_BOAT);
    private static final float PLAYER_ARROW_BASE_SCALE = 0.55F;
    private static final float PLAYER_ARROW_MIN_SCALE = 0.18F;
    private static final float PLAYER_ARROW_MAX_SCALE = 0.55F;
    private static final int[][] PLAYER_ARROW_SPANS = {
            {0, 2, 23, 25}, {0, 4, 21, 25}, {0, 6, 19, 25}, {1, 8, 17, 24}, {1, 10, 15, 24},
            {2, 23, -1, -1}, {2, 23, -1, -1}, {3, 22, -1, -1}, {3, 22, -1, -1},
            {4, 21, -1, -1}, {4, 21, -1, -1}, {5, 20, -1, -1}, {5, 20, -1, -1},
            {6, 19, -1, -1}, {6, 19, -1, -1}, {7, 18, -1, -1}, {7, 18, -1, -1},
            {7, 18, -1, -1}, {8, 17, -1, -1}, {9, 16, -1, -1}, {9, 16, -1, -1},
            {10, 15, -1, -1}, {10, 15, -1, -1}, {10, 15, -1, -1}, {11, 14, -1, -1},
            {11, 14, -1, -1}, {12, 13, -1, -1}
    };

    private WorldMapPlayerRenderer() {}

    public static void render(
            GuiGraphics guiGraphics,
            Font font,
            Player player,
            double offsetX,
            double offsetZ,
            double scale,
            boolean usePlayerArrow) {
        if (player == null) return;

        double playerWorldX = player.getX();
        double playerWorldZ = player.getZ();
        double pixelX = offsetX + playerWorldX * scale;
        double pixelZ = offsetZ + playerWorldZ * scale;

        org.joml.Matrix3x2fStack pose = guiGraphics.pose();
        pose.pushMatrix();
        pose.translate((float) (pixelX), (float) (pixelZ));
        if (player.getVehicle() instanceof Boat) renderBoat(pose, guiGraphics, player);
        else renderIcon(pose, guiGraphics, player, scale, usePlayerArrow);
        pose.popMatrix();
        renderNameTag(guiGraphics, font, player, pixelX, pixelZ, scale);
    }

    private static void renderBoat(Matrix3x2fStack pose, GuiGraphics guiGraphics, Player player) {
        float yaw = player.getYRot() % 360f;
        if (yaw < -180f) yaw += 360f;
        if (yaw >= 180f) yaw -= 360f;
        boolean flipX = yaw > 0;
        pose.pushMatrix();
        if (flipX) pose.scale((float) (-1f), (float) (1f));
        pose.scale((float) (1.5f), (float) (1.5f));
        ItemStack boat = BOAT_STACK;
        if (Main.isSmallShipsLoaded
                && player.getVehicle() != null
                && SmallShips.isSmallShip(player.getVehicle())) {
            boat = SmallShips.getSmallShipsItem();
        }
        guiGraphics.renderItem(boat, -8, -8);
        pose.popMatrix();
    }

    private static void renderIcon(
            Matrix3x2fStack pose, GuiGraphics guiGraphics, Player player, double scale, boolean usePlayerArrow) {
        if (usePlayerArrow) {
            renderPlayerArrowIcon(pose, guiGraphics, player, scale);
            return;
        }
        renderVanillaIcon(pose, guiGraphics, player);
    }

    private static void renderVanillaIcon(Matrix3x2fStack pose, GuiGraphics guiGraphics, Player player) {
        pose.rotate((float) Math.toRadians(player.getYRot()));
        pose.scale(5.0f, 5.0f);
        // the player decoration is a single sprite file now instead of a 16x16 icon sheet
        MapRenderUtil.texturedQuad(guiGraphics, MAP_ICONS, -1f, -1f, 1f, 1f, 0f, 1f, 1f, 0f, 0xFFFFFFFF);
    }

    private static void renderPlayerArrowIcon(
            Matrix3x2fStack pose, GuiGraphics guiGraphics, Player player, double mapScale) {
        float arrowScale = getPlayerArrowScale(mapScale);

        pose.pushMatrix();
        pose.translate((float) (0), (float) (2.0F * arrowScale));
        pose.rotate((float) Math.toRadians(player.getYRot()));
        pose.scale((float) (arrowScale), (float) (arrowScale));
        renderPlayerArrowMask(guiGraphics, 0xE0000000);
        pose.popMatrix();

        pose.pushMatrix();
        pose.rotate((float) Math.toRadians(player.getYRot()));
        pose.scale((float) (arrowScale), (float) (arrowScale));
        renderPlayerArrowMask(guiGraphics, 0xFF2BEA68);
        pose.popMatrix();
    }

    private static float getPlayerArrowScale(double mapScale) {
        float scale = (float) mapScale * PLAYER_ARROW_BASE_SCALE;
        return Math.max(PLAYER_ARROW_MIN_SCALE, Math.min(PLAYER_ARROW_MAX_SCALE, scale));
    }

    private static void renderPlayerArrowMask(GuiGraphics guiGraphics, int color) {
        for (int row = 0; row < PLAYER_ARROW_SPANS.length; row++) {
            int[] spans = PLAYER_ARROW_SPANS[row];
            drawPlayerArrowSpan(guiGraphics, spans[0], spans[1], row, color);
            if (spans[2] >= 0) drawPlayerArrowSpan(guiGraphics, spans[2], spans[3], row, color);
        }
    }

    private static void drawPlayerArrowSpan(GuiGraphics guiGraphics, int startX, int endX, int row, int color) {
        MapRenderUtil.fill(guiGraphics, startX - 13, row - 5, endX - 12, row - 4, color);
    }

    private static void renderNameTag(
            GuiGraphics guiGraphics,
            Font font,
            Player player,
            double pixelX,
            double pixelZ,
            double scale) {
        if (scale <= 1.5) return;

        String playerName = player.getName().getString();
        float textScale = (float) Math.min(1.0, scale / 1.25);
        int textWidth = font.width(playerName);
        int textHeight = font.lineHeight;
        guiGraphics.pose().pushMatrix();
        guiGraphics
                .pose()
                .translate(
                        (float) (pixelX - (textWidth * textScale) / 2.0),
                        (float) (pixelZ - (textHeight * textScale) / 2.0 - 10));
        guiGraphics.pose().scale((float) (textScale), (float) (textScale));
        guiGraphics.drawString(font, playerName, 0, 0, 0xFFFFFFFF, false);
        guiGraphics.pose().popMatrix();
    }
}
