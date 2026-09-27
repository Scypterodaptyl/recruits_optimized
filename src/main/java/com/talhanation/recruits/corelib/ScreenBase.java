package com.talhanation.recruits.corelib;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class ScreenBase<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
    public static final int FONT_COLOR = 0xFF404040;
    protected Identifier texture;
    protected List<HoverArea> hoverAreas;

    public ScreenBase(Identifier texture, T container, Inventory playerInventory, Component title) {
        super(container, playerInventory, title);
        this.texture = texture;
        this.hoverAreas = new ArrayList<>();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int x, int y, float partialTicks) {
        super.render(guiGraphics, x, y, partialTicks);
        this.renderTooltip(guiGraphics, x, y);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, this.texture, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
    }

    public void drawHoverAreas(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        for (HoverArea hoverArea : this.hoverAreas) {
            if (hoverArea.tooltip != null && hoverArea.isHovered(this.leftPos, this.topPos, mouseX, mouseY)) {
                guiGraphics.setTooltipForNextFrame(this.font, hoverArea.tooltip.get(), mouseX - this.leftPos, mouseY - this.topPos);
            }
        }
    }

    public int getBlitSize(int amount, int max, int size) {
        return size - (int) ((float) amount / max * size);
    }

    public void drawCentered(GuiGraphics guiGraphics, Component text, int y, int color) {
        this.drawCentered(guiGraphics, text, this.imageWidth / 2, y, color);
    }

    public void drawCentered(GuiGraphics guiGraphics, Component text, int x, int y, int color) {
        drawCentered(this.font, guiGraphics, text, x, y, color);
    }

    public static void drawCentered(Font font, GuiGraphics guiGraphics, Component text, int x, int y, int color) {
        int width = font.width(text);
        guiGraphics.drawString(font, text, x - width / 2, y, com.talhanation.recruits.client.gui.util.GuiCompat.opaque(color), false);
    }

    public static class HoverArea {
        private final int posX;
        private final int posY;
        private final int width;
        private final int height;
        @Nullable
        private final Supplier<List<FormattedCharSequence>> tooltip;

        public HoverArea(int posX, int posY, int width, int height) {
            this(posX, posY, width, height, null);
        }

        public HoverArea(int posX, int posY, int width, int height, Supplier<List<FormattedCharSequence>> tooltip) {
            this.posX = posX;
            this.posY = posY;
            this.width = width;
            this.height = height;
            this.tooltip = tooltip;
        }

        public int getPosX() { return this.posX; }
        public int getPosY() { return this.posY; }
        public int getWidth() { return this.width; }
        public int getHeight() { return this.height; }

        @Nullable
        public Supplier<List<FormattedCharSequence>> getTooltip() { return this.tooltip; }

        public boolean isHovered(int guiLeft, int guiTop, int mouseX, int mouseY) {
            return mouseX >= guiLeft + this.posX && mouseX < guiLeft + this.posX + this.width
                    && mouseY >= guiTop + this.posY && mouseY < guiTop + this.posY + this.height;
        }
    }
}
