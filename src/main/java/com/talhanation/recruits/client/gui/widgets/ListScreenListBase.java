package com.talhanation.recruits.client.gui.widgets;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;

public abstract class ListScreenListBase<T extends ListScreenEntryBase<T>> extends ContainerObjectSelectionList<T> {

    public ListScreenListBase(int width, int height, int top, int bottom, int size) {
        super(Minecraft.getInstance(), width, Math.max(0, bottom - top), top, size);
    }

    /**
     * Old list API: the list is placed between top and bottom.
     */
    public void updateSize(int width, int height, int top, int bottom) {
        this.updateSizeAndPosition(width, Math.max(0, bottom - top), top);
    }

    // The screens draw their own frame; 1.21 lists would paint a full-width background and separators.
    @Override
    protected void renderListBackground(GuiGraphics guiGraphics) {
    }

    @Override
    protected void renderListSeparators(GuiGraphics guiGraphics) {
    }

    // Pre-1.21.9 geometry: rows 216px wide, centered; scrollbar 124px right of the center.
    @Override
    public int getRowWidth() {
        return 216;
    }

    @Override
    protected int scrollBarX() {
        return this.getX() + this.width / 2 + 124;
    }
}
