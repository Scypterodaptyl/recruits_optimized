package com.talhanation.recruits.client.gui.worldmap.render.tile;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

/**
 * Uploads tile pixels into the atlas textures. The pixels are packed as RGBA bytes
 * (0xAABBGGRR as little endian int), which is what the RGBA8 texture expects.
 */
final class WorldMapTextureUploader implements AutoCloseable {
    private static final int PIXEL_COUNT =
            WorldMapRenderTileKey.PIXEL_SIZE * WorldMapRenderTileKey.PIXEL_SIZE;

    private final ByteBuffer stagingBytes = BufferUtils.createByteBuffer(PIXEL_COUNT * Integer.BYTES).order(ByteOrder.nativeOrder());
    private final IntBuffer stagingBuffer = stagingBytes.asIntBuffer();

    void prepare() {
        RenderSystem.assertOnRenderThread();
    }

    void upload(GpuTexture texture, int xOffset, int yOffset, int[] pixels) {
        uploadRegion(
                texture,
                xOffset,
                yOffset,
                pixels,
                WorldMapRenderTileKey.PIXEL_SIZE,
                0,
                0,
                WorldMapRenderTileKey.PIXEL_SIZE,
                WorldMapRenderTileKey.PIXEL_SIZE);
    }

    void uploadRegion(
            GpuTexture texture,
            int xOffset,
            int yOffset,
            int[] pixels,
            int sourceWidth,
            int sourceX,
            int sourceY,
            int width,
            int height) {
        if (pixels == null || pixels.length != PIXEL_COUNT) {
            throw new IllegalArgumentException("Invalid world map render tile pixel count");
        }
        if (sourceWidth != WorldMapRenderTileKey.PIXEL_SIZE
                || sourceX < 0
                || sourceY < 0
                || width <= 0
                || height <= 0
                || sourceX + width > sourceWidth
                || sourceY + height > WorldMapRenderTileKey.PIXEL_SIZE) {
            throw new IllegalArgumentException("Invalid world map render tile upload area");
        }

        RenderSystem.assertOnRenderThread();

        stagingBuffer.clear();
        for (int row = 0; row < height; row++) {
            stagingBuffer.put(pixels, (sourceY + row) * sourceWidth + sourceX, width);
        }
        stagingBytes.clear();
        stagingBytes.limit(width * height * Integer.BYTES);

        RenderSystem.getDevice().createCommandEncoder().writeToTexture(
                texture, stagingBytes, NativeImage.Format.RGBA, 0, 0, xOffset, yOffset, width, height);
    }

    @Override
    public void close() {
    }
}
