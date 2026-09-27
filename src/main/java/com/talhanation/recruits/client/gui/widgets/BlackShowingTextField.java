package com.talhanation.recruits.client.gui.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;

public class BlackShowingTextField extends AbstractWidget {
    protected static final int BG_FILL_SELECTED = ARGB.color(255, 10, 10, 10);
    private final String text;
    private final int textXOffset;
    private final int textYOffset;
    public BlackShowingTextField(int x, int y, int width, int height, Component component) {
        this(x, y, width, height, 2, 0, component);
    }

    public BlackShowingTextField(int x, int y, int width, int height, int textXOffset, int textYOffset, Component component){
        super(x, y, width, height, Component.empty());
        this.text = component.getString();
        this.textXOffset = textXOffset;
        this.textYOffset = textYOffset;
    }
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, BG_FILL_SELECTED);
        guiGraphics.drawString(Minecraft.getInstance().font, text, this.getX() + textXOffset, this.getY() + textYOffset + (this.height - 8) / 2, 0xFFFFFFFF, false);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput p_259858_) {

    }
}
