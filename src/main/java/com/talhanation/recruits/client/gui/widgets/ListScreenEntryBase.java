package com.talhanation.recruits.client.gui.widgets;

import net.minecraft.client.input.MouseButtonEvent;


import com.google.common.collect.Lists;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;

import java.util.List;

public abstract class ListScreenEntryBase<T extends ContainerObjectSelectionList.Entry<T>> extends ContainerObjectSelectionList.Entry<T> {

    protected final List<AbstractWidget> children;

    public ListScreenEntryBase() {
        this.children = Lists.newArrayList();
    }

    @Override
    public List<? extends NarratableEntry> narratables() {
        return children;
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return children;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();
        if (button == 0) {
            getList().setFocused(this);
            return true;
        }
        return false;
    }

    public abstract ListScreenListBase<?> getList();

}
