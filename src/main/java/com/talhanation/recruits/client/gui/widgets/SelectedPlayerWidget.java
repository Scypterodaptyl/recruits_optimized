package com.talhanation.recruits.client.gui.widgets;

import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.renderer.RenderPipelines;

import com.talhanation.recruits.util.GameProfileUtils;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraftforge.client.gui.widget.ExtendedButton;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class SelectedPlayerWidget extends AbstractWidget {
    private final int x, y, width, height;
    private final Button actionButton;
    private final int PLAYER_NAME_COLOR = ARGB.color(255, 255, 255, 255);
    private final int BACKGROUND_COLOR = ARGB.color(255, 0, 0, 0);

    private final Font font;
    @Nullable
    private UUID playerUUID;
    @Nullable
    private String playerName;

    public SelectedPlayerWidget(Font font, int x, int y, int width, int height, Component buttonLabel, Runnable onPress) {
        super(x, y, width, height, Component.literal(""));
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.font = font;
        this.actionButton = new ExtendedButton(x + width - 20, y, 20, 20, buttonLabel, button ->{
            onPress.run();
        });
    }

    public void setButtonActive(boolean x){
        this.actionButton.active = x;
    }

    public void setButtonVisible(boolean x){
        this.actionButton.visible = x;
    }

    public void setPlayer(@Nullable UUID playerUUID, @Nullable String playerName) {
        this.playerUUID = playerUUID;
        this.playerName = playerName;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (playerUUID != null && playerName != null) {
            guiGraphics.fill(x, y, x + width, y + height, BACKGROUND_COLOR);

            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GameProfileUtils.getSkin(playerUUID), x, y, (float) (8), (float) (8), 20, 20, 8, 8, 64, 64);
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GameProfileUtils.getSkin(playerUUID), x, y, (float) (40), (float) (8), 20, 20, 8, 8, 64, 64);

            guiGraphics.drawString(font, playerName, x + 25, y + 6, com.talhanation.recruits.client.gui.util.GuiCompat.opaque(PLAYER_NAME_COLOR), false);

            actionButton.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double x = event.x();
        double y = event.y();
        int i = event.button();
        if(actionButton.isMouseOver(x,y) && actionButton.active && actionButton.visible) actionButton.onClick(new MouseButtonEvent(x, y, new MouseButtonInfo(0, 0)), false);
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        double x = event.x();
        double y = event.y();
        if(actionButton.isMouseOver(x,y) && actionButton.active && actionButton.visible) actionButton.onClick(new MouseButtonEvent(x, y, new MouseButtonInfo(0, 0)), false);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput p_259858_) {

    }
}

