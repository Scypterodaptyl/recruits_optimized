package com.talhanation.recruits.client.gui.worldmap.render;

import com.talhanation.recruits.client.gui.worldmap.render.tile.WorldMapRenderTileCache;
import com.talhanation.recruits.client.gui.worldmap.render.tile.WorldMapRenderTileKey;
import com.talhanation.recruits.client.gui.worldmap.storage.WorldMapCacheManager;
import com.talhanation.recruits.config.RecruitsClientConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class WorldMapRenderer {
    private static final MapFramebufferPass FRAMEBUFFER_PASS = new MapFramebufferPass();
    private static final int MAX_VISIBLE_TEXTURE_LEVEL = WorldMapRenderTileKey.MAX_LEVEL;
    private static final double FIRST_LOD_SCALE_THRESHOLD = 0.22;
    private static final double SECOND_LOD_SCALE_THRESHOLD = 0.11;
    private static final int MAX_TILE_DRAWS_PER_FRAME = 768;

    private final WorldMapCacheManager mapCache;
    private final WorldMapRenderTileCache renderTileCache;
    private final List<VisibleTile> visibleTiles = new ArrayList<>();
    private final List<WorldMapRenderTileKey> preparedTileKeys = new ArrayList<>();
    private final Map<Identifier, DrawBatch> drawBatches = new LinkedHashMap<>();
    private int visibleLevel = -1;
    private int visibleStartX = Integer.MIN_VALUE;
    private int visibleEndX = Integer.MIN_VALUE;
    private int visibleStartZ = Integer.MIN_VALUE;
    private int visibleEndZ = Integer.MIN_VALUE;
    private int visibleCenterX = Integer.MIN_VALUE;
    private int visibleCenterZ = Integer.MIN_VALUE;

    public WorldMapRenderer(WorldMapCacheManager mapCache) {
        this.mapCache = mapCache;
        this.renderTileCache = mapCache.getRenderTileCache();
    }

    public void render(
            GuiGraphics guiGraphics,
            int screenWidth,
            int screenHeight,
            double offsetX,
            double offsetZ,
            double scale,
            MapOverlayRenderer overlayRenderer) {
        mapCache.beginRenderFrame();
        MapFramebufferPass.Frame frame =
                FRAMEBUFFER_PASS.begin(guiGraphics, offsetX, offsetZ, scale, screenWidth, screenHeight);

        renderVisibleTiles(guiGraphics, frame, scale, getMapBrightness());

        if (overlayRenderer != null) {
            overlayRenderer.render(guiGraphics, frame);
        }

        FRAMEBUFFER_PASS.endAndBlit(guiGraphics, frame);
    }

    public void close() {
        visibleTiles.clear();
        preparedTileKeys.clear();
        drawBatches.clear();
    }

    public static void releaseSharedResources() {
        FRAMEBUFFER_PASS.close();
    }

    public static void prepareSharedResources() {
        FRAMEBUFFER_PASS.prepare();
    }

    private void renderVisibleTiles(GuiGraphics guiGraphics, MapFramebufferPass.Frame frame,
                                    double scale, float brightness) {
        int level = selectTextureLevel(scale);
        List<VisibleTile> visible = collectVisibleTiles(
                level,
                frame.leftWorld(),
                frame.rightWorld(),
                frame.topWorld(),
                frame.bottomWorld()
        );
        boolean allowParentFallback = true;
        renderTileCache.prepareVisible(preparedTileKeys, allowParentFallback);

        RenderBudget budget = new RenderBudget(MAX_TILE_DRAWS_PER_FRAME);
        clearDrawBatches();

        try {
            for (VisibleTile visibleTile : visible) {
                renderBestAvailable(visibleTile.key(), budget, allowParentFallback);
            }
            drawBatches(guiGraphics, frame);
        } finally {
        }
    }

    private boolean renderBestAvailable(
            WorldMapRenderTileKey requested, RenderBudget budget, boolean allowParentFallback) {
        // Avoid black gaps while the exact LOD tile is still building.
        WorldMapRenderTileCache.TileView best =
                renderTileCache.findBestAvailable(requested, allowParentFallback);
        boolean rendered = false;
        if (best != null) {
            rendered = renderTile(requested, best, budget);
            if (!allowParentFallback || requested.level() <= 0) {
                return rendered;
            }
        }
        return renderAvailableChildren(requested, budget) || rendered;
    }

    private boolean renderAvailableChildren(WorldMapRenderTileKey parent, RenderBudget budget) {
        if (parent.level() <= 0) return false;

        boolean rendered = false;
        for (int childZ = 0; childZ < 2; childZ++) {
            for (int childX = 0; childX < 2; childX++) {
                WorldMapRenderTileKey child = parent.child(childX, childZ);
                WorldMapRenderTileCache.TileView exact = renderTileCache.findExact(child);
                if (exact != null) {
                    rendered |= renderTile(child, exact, budget);
                } else {
                    rendered |= renderAvailableChildren(child, budget);
                }
            }
        }
        return rendered;
    }

    private boolean renderTile(
            WorldMapRenderTileKey key, WorldMapRenderTileCache.TileView tile, RenderBudget budget) {
        return queueTile(key, tile, budget);
    }

    private List<VisibleTile> collectVisibleTiles(
            int level, double leftWorld, double rightWorld, double topWorld, double bottomWorld) {
        int tileSize = WorldMapRenderTileKey.PIXEL_SIZE << level;
        int startX = (int) Math.floor(leftWorld / tileSize);
        int endX = (int) Math.floor(rightWorld / tileSize);
        int startZ = (int) Math.floor(topWorld / tileSize);
        int endZ = (int) Math.floor(bottomWorld / tileSize);

        double centerWorldX = (leftWorld + rightWorld) * 0.5;
        double centerWorldZ = (topWorld + bottomWorld) * 0.5;
        int centerX = (int) Math.floor(centerWorldX / tileSize);
        int centerZ = (int) Math.floor(centerWorldZ / tileSize);

        if (level == visibleLevel
                && startX == visibleStartX
                && endX == visibleEndX
                && startZ == visibleStartZ
                && endZ == visibleEndZ
                && centerX == visibleCenterX
                && centerZ == visibleCenterZ) {
            return visibleTiles;
        }

        visibleLevel = level;
        visibleStartX = startX;
        visibleEndX = endX;
        visibleStartZ = startZ;
        visibleEndZ = endZ;
        visibleCenterX = centerX;
        visibleCenterZ = centerZ;
        visibleTiles.clear();
        preparedTileKeys.clear();
        for (int tileZ = startZ; tileZ <= endZ; tileZ++) {
            for (int tileX = startX; tileX <= endX; tileX++) {
                WorldMapRenderTileKey key = new WorldMapRenderTileKey(level, tileX, tileZ);
                visibleTiles.add(new VisibleTile(key, distanceToCenter(key, centerWorldX, centerWorldZ)));
            }
        }
        visibleTiles.sort(Comparator.comparingDouble(VisibleTile::centerDistance));
        for (VisibleTile visibleTile : visibleTiles) {
            preparedTileKeys.add(visibleTile.key());
        }
        return visibleTiles;
    }

    private static int selectTextureLevel(double scale) {
        if (scale >= 1.0) return 0;
        if (scale >= FIRST_LOD_SCALE_THRESHOLD) return 0;
        if (scale >= SECOND_LOD_SCALE_THRESHOLD) return Math.min(1, MAX_VISIBLE_TEXTURE_LEVEL);
        return MAX_VISIBLE_TEXTURE_LEVEL;
    }

    private static double distanceToCenter(
            WorldMapRenderTileKey key, double centerWorldX, double centerWorldZ) {
        double tileCenterWorldX = key.worldMinX() + key.worldSize() * 0.5;
        double tileCenterWorldZ = key.worldMinZ() + key.worldSize() * 0.5;
        double dx = tileCenterWorldX - centerWorldX;
        double dz = tileCenterWorldZ - centerWorldZ;
        return dx * dx + dz * dz;
    }

    private boolean queueTile(
            WorldMapRenderTileKey key, WorldMapRenderTileCache.TileView tile, RenderBudget budget) {
        if (!budget.tryDraw()) return false;
        drawBatches
                .computeIfAbsent(tile.textureId(), DrawBatch::new)
                .tiles
                .add(new DrawTile(key, tile));
        return true;
    }

    private void clearDrawBatches() {
        for (DrawBatch drawBatch : drawBatches.values()) {
            drawBatch.tiles.clear();
        }
    }

    private void drawBatches(GuiGraphics guiGraphics, MapFramebufferPass.Frame frame) {
        Iterator<DrawBatch> iterator = drawBatches.values().iterator();
        while (iterator.hasNext()) {
            DrawBatch drawBatch = iterator.next();
            if (drawBatch.tiles.isEmpty()) {
                iterator.remove();
                continue;
            }

            for (DrawTile drawTile : drawBatch.tiles) {
                appendWorldTile(guiGraphics, drawBatch.textureId, drawTile, frame);
            }
        }
    }

    private static void appendWorldTile(
            GuiGraphics guiGraphics, Identifier textureId, DrawTile drawTile, MapFramebufferPass.Frame frame) {
        WorldMapRenderTileKey key = drawTile.key;
        WorldMapRenderTileCache.TileView tile = drawTile.tile;
        double screenX = frame.renderOffsetX() + key.worldMinX() * frame.fboScale();
        double screenZ = frame.renderOffsetZ() + key.worldMinZ() * frame.fboScale();
        double screenSize = key.worldSize() * frame.fboScale();
        if (screenSize < 0.75) return;

        double x1 = screenX;
        double z1 = screenZ;
        double x2 = screenX + screenSize;
        double z2 = screenZ + screenSize;

        MapRenderUtil.texturedQuad(guiGraphics, textureId, x1, z1, x2, z2, tile.u1(), tile.v1(), tile.u2(), tile.v2(), 0xFFFFFFFF);
    }

    private static float getMapBrightness() {
        // the brightness is currently not applied to the tiles
        return 1.0F;
    }

    private record VisibleTile(WorldMapRenderTileKey key, double centerDistance) {}

    private record DrawTile(WorldMapRenderTileKey key, WorldMapRenderTileCache.TileView tile) {}

    @FunctionalInterface
    public interface MapOverlayRenderer {
        void render(GuiGraphics guiGraphics, MapFramebufferPass.Frame frame);
    }

    private static final class DrawBatch {
        private final Identifier textureId;
        private final List<DrawTile> tiles = new ArrayList<>();

        private DrawBatch(Identifier textureId) {
            this.textureId = textureId;
        }
    }

    private static final class RenderBudget {
        private int drawsLeft;

        private RenderBudget(int drawsLeft) {
            this.drawsLeft = drawsLeft;
        }

        private boolean tryDraw() {
            if (drawsLeft <= 0) {
                return false;
            }
            drawsLeft--;
            return true;
        }
    }
}
