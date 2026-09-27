package com.talhanation.recruits.client.gui.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public class RecruitsCheckBox extends AbstractButton {

    private static final Identifier CHECKBOX_SELECTED_HIGHLIGHTED_SPRITE = Identifier.withDefaultNamespace("widget/checkbox_selected_highlighted");
    private static final Identifier CHECKBOX_SELECTED_SPRITE = Identifier.withDefaultNamespace("widget/checkbox_selected");
    private static final Identifier CHECKBOX_HIGHLIGHTED_SPRITE = Identifier.withDefaultNamespace("widget/checkbox_highlighted");
    private static final Identifier CHECKBOX_SPRITE = Identifier.withDefaultNamespace("widget/checkbox");
    private static final int TEXT_COLOR = 14737632;
    private final Consumer<Boolean> onToggle;
    private boolean selected;

    public RecruitsCheckBox(int x, int y, int width, int height, Component label, boolean selected, Consumer<Boolean> onToggle) {
        this(x, y, width, height, label, selected, true, onToggle);
    }

    public RecruitsCheckBox(int x, int y, int width, int height, Component label, boolean selected, boolean showLabel, Consumer<Boolean> onToggle) {
        super(x, y, width, height, label);
        this.selected = selected;
        this.onToggle = onToggle;
    }

    public boolean selected() {
        return this.selected;
    }

    @Override
    public void onPress(InputWithModifiers input) {
        this.selected = !this.selected;
        if (onToggle != null) {
            onToggle.accept(this.selected());
        }
    }

    @Override
    protected void renderContents(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;

        int alpha = Mth.ceil(this.alpha * 255.0F);
        int bgColor = (0x80 << 24); // 0x80 = 128 = 50% Alpha
        graphics.fill(getX(), getY(), getX() + width, getY() + height, bgColor);

        Identifier sprite;
        if (this.selected) {
            sprite = this.isFocused() ? CHECKBOX_SELECTED_HIGHLIGHTED_SPRITE : CHECKBOX_SELECTED_SPRITE;
        } else {
            sprite = this.isFocused() ? CHECKBOX_HIGHLIGHTED_SPRITE : CHECKBOX_SPRITE;
        }
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, this.getX(), this.getY(), 20, this.height);

        int textX = this.getX() + 24;
        int textY = this.getY() + (this.height - 8) / 2;
        graphics.drawString(font, this.getMessage(), textX, textY, (TEXT_COLOR & 0xFFFFFF) | (alpha << 24));
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, this.createNarrationMessage());
    }
}
