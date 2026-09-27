package com.talhanation.recruits.client.gui.worldmap.render.tile;

import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.TextureFormat;
import com.talhanation.recruits.Main;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;

import java.util.BitSet;
import java.util.concurrent.atomic.AtomicLong;



/**
 * Keeps many map tiles in one persistent texture. Besides avoiding texture allocation churn, this
 * lets the renderer submit one batch per atlas.
 */
final class WorldMapTextureAtlas implements AutoCloseable {
    static final int ATLAS_SIZE = 1024;
    private static final int SLOTS_PER_SIDE = ATLAS_SIZE / WorldMapRenderTileKey.PIXEL_SIZE;
    private static final int CAPACITY = SLOTS_PER_SIDE * SLOTS_PER_SIDE;
    private static final AtomicLong ATLAS_SEQUENCE = new AtomicLong();

    private final PixelTexture texture = new PixelTexture();
    private final Identifier textureId =
            Identifier.fromNamespaceAndPath(Main.MOD_ID, "worldmap/atlas_" + ATLAS_SEQUENCE.incrementAndGet());
    private final BitSet usedSlots = new BitSet(CAPACITY);

    WorldMapTextureAtlas() {
        Minecraft.getInstance().getTextureManager().register(textureId, texture);
    }

    Slot allocate() {
        int index = usedSlots.nextClearBit(0);
        if (index >= CAPACITY) return null;

        usedSlots.set(index);
        return new Slot(this, index);
    }

    void release(Slot slot) {
        if (slot != null && slot.atlas == this) {
            usedSlots.clear(slot.index);
        }
    }

    void upload(Slot slot, int[] pixels, WorldMapTextureUploader uploader) {
        uploadRegion(
                slot,
                pixels,
                0,
                0,
                WorldMapRenderTileKey.PIXEL_SIZE,
                WorldMapRenderTileKey.PIXEL_SIZE,
                uploader);
    }

    void uploadRegion(
            Slot slot,
            int[] pixels,
            int x,
            int y,
            int width,
            int height,
            WorldMapTextureUploader uploader) {
        if (slot == null || slot.atlas != this || !usedSlots.get(slot.index)) {
            throw new IllegalStateException("Invalid world map atlas slot");
        }
        uploader.uploadRegion(
                texture.getTexture(),
                slot.pixelX() + x,
                slot.pixelY() + y,
                pixels,
                WorldMapRenderTileKey.PIXEL_SIZE,
                x,
                y,
                width,
                height);
    }

    Identifier textureId() {
        return textureId;
    }

    boolean isEmpty() {
        return usedSlots.isEmpty();
    }

    @Override
    public void close() {
        usedSlots.clear();
        try {
            Minecraft.getInstance().getTextureManager().release(textureId);
        } catch (Exception ignored) {
            texture.close();
        }
    }

    record Slot(WorldMapTextureAtlas atlas, int index) {
        private int pixelX() {
            return (index % SLOTS_PER_SIDE) * WorldMapRenderTileKey.PIXEL_SIZE;
        }

        private int pixelY() {
            return (index / SLOTS_PER_SIDE) * WorldMapRenderTileKey.PIXEL_SIZE;
        }

        float u1() {
            return (float) pixelX() / ATLAS_SIZE;
        }

        float v1() {
            return (float) pixelY() / ATLAS_SIZE;
        }

        float u2() {
            return (float) (pixelX() + WorldMapRenderTileKey.PIXEL_SIZE) / ATLAS_SIZE;
        }

        float v2() {
            return (float) (pixelY() + WorldMapRenderTileKey.PIXEL_SIZE) / ATLAS_SIZE;
        }
    }

    private static final class PixelTexture extends AbstractTexture {
        private PixelTexture() {
            RenderSystem.assertOnRenderThread();
            GpuDevice device = RenderSystem.getDevice();
            this.texture = device.createTexture(
                    () -> "Recruits world map atlas",
                    GpuTexture.USAGE_COPY_DST | GpuTexture.USAGE_TEXTURE_BINDING | GpuTexture.USAGE_RENDER_ATTACHMENT,
                    TextureFormat.RGBA8,
                    ATLAS_SIZE,
                    ATLAS_SIZE,
                    1,
                    1);
            this.textureView = device.createTextureView(this.texture);
            this.sampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST);
            device.createCommandEncoder().clearColorTexture(this.texture, 0);
        }
    }
}
