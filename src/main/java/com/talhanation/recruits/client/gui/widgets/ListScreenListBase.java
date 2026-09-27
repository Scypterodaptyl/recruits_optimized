package com.talhanation.recruits.client.gui.widgets;


import net.minecraft.client.Minecraft;
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

}
