package com.talhanation.recruits.client.gui.player;

import net.minecraft.client.renderer.RenderPipelines;

import com.talhanation.recruits.client.gui.component.BannerRenderer;
import com.talhanation.recruits.client.gui.widgets.ListScreenEntryBase;
import com.talhanation.recruits.client.gui.widgets.ListScreenListBase;
import com.talhanation.recruits.util.GameProfileUtils;
import com.talhanation.recruits.world.RecruitsPlayerInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.ARGB;
import org.jetbrains.annotations.NotNull;


public class RecruitsPlayerEntry extends ListScreenEntryBase<RecruitsPlayerEntry> {
    protected static final int SKIN_SIZE = 24;
    protected static final int PADDING = 4;
    protected static final int BG_FILL = ARGB.color(255, 60, 60, 60);
    protected static final int BG_FILL_HOVERED = ARGB.color(255, 100, 100, 100);
    protected static final int BG_FILL_SELECTED = ARGB.color(255, 10, 10, 10);
    protected static final int PLAYER_NAME_COLOR = ARGB.color(255, 255, 255, 255);
    protected static final int PLAYER_NAME_COLOR_OFFLINE = ARGB.color(255, 140, 140, 140);

    protected final Minecraft minecraft;
    protected final IPlayerSelection screen;
    protected final @NotNull RecruitsPlayerInfo player;
    protected final BannerRenderer bannerRenderer;

    public RecruitsPlayerEntry(IPlayerSelection screen, @NotNull RecruitsPlayerInfo player) {
        this.minecraft = Minecraft.getInstance();
        this.screen = screen;
        this.player = player;
        this.bannerRenderer = new BannerRenderer(player.getFaction());
    }

    @Override
    public void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY, boolean hovered, float delta) {
        int index = 0;
        int top = this.getY();
        // Pre-1.21.9 lists inset rows by 2px and shrank them by 4px.
        int left = this.getX() + 2;
        int width = this.getWidth() - 4;
        int height = this.getHeight() - 4;
        int skinX = left + PADDING;
        int skinY = top + (height - SKIN_SIZE) / 2;
        int textX = skinX + SKIN_SIZE + PADDING;
        int textY = top + (height - minecraft.font.lineHeight) / 2;

        guiGraphics.fill(left, top, left + width, top + height, BG_FILL);

        renderElement(guiGraphics, index, top, left, width, height, mouseX, mouseY, hovered, delta, skinX, skinY, textX, textY);
    }

    public void renderElement(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float delta, int skinX, int skinY, int textX, int textY){
        boolean selected = screen.getSelected() != null && player.getUUID().equals(screen.getSelected().getUUID());
        if (selected) {
            guiGraphics.fill(left, top, left + width, top + height, BG_FILL_SELECTED);
        } else if (hovered) {
            guiGraphics.fill(left, top, left + width, top + height, BG_FILL_HOVERED);
        } else {
            guiGraphics.fill(left, top, left + width, top + height, BG_FILL);
        }

        int nameColor = player.isOnline() ? PLAYER_NAME_COLOR : PLAYER_NAME_COLOR_OFFLINE;

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GameProfileUtils.getSkin(player.getUUID()), skinX, skinY, (float) (8), (float) (8), SKIN_SIZE, SKIN_SIZE, 8, 8, 64, 64);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GameProfileUtils.getSkin(player.getUUID()), skinX, skinY, (float) (40), (float) (8), SKIN_SIZE, SKIN_SIZE, 8, 8, 64, 64);
        if (!player.isOnline()) {
            guiGraphics.fill(skinX, skinY, skinX + SKIN_SIZE, skinY + SKIN_SIZE, ARGB.color(120, 0, 0, 0));
        }
        guiGraphics.drawString(minecraft.font, player.getName(), (int) ((float) textX), (int) ((float) textY), com.talhanation.recruits.client.gui.util.GuiCompat.opaque(nameColor), false);

        if(bannerRenderer != null){
            bannerRenderer.renderBanner(guiGraphics, left + 185, top, width, height, 15);
        }
    }

    @NotNull
    public RecruitsPlayerInfo getPlayerInfo() {
        return player;
    }

    @Override
    public ListScreenListBase<RecruitsPlayerEntry> getList() {
        return screen.getPlayerList();
    }
}
