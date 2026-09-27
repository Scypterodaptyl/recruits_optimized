package com.talhanation.recruits.client.gui.widgets;

import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;

import com.talhanation.recruits.client.gui.component.BannerRenderer;
import com.talhanation.recruits.world.RecruitsFaction;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraftforge.client.gui.widget.ExtendedButton;
import org.jetbrains.annotations.Nullable;

public class SelectedFactionWidget extends AbstractWidget {

    private static final int NAME_COLOR = ARGB.color(255, 255, 255, 255);
    private static final int LEADER_COLOR = ARGB.color(255, 180, 180, 180);
    private static final int BACKGROUND_COLOR = ARGB.color(255, 0, 0, 0);

    private final Font font;
    private final Button actionButton;

    @Nullable
    private RecruitsFaction faction;
    @Nullable
    private BannerRenderer bannerRenderer;

    public SelectedFactionWidget(Font font, int x, int y, int width, int height, Component buttonLabel, Runnable onPress) {
        super(x, y, width, height, Component.literal(""));
        this.font = font;
        this.actionButton = new ExtendedButton(x + width - 20, y, 20, height, buttonLabel, button -> onPress.run());
    }

    public void setFaction(@Nullable RecruitsFaction faction) {
        this.faction = faction;
        this.bannerRenderer = faction != null ? new BannerRenderer(faction) : null;
    }

    public void setButtonActive(boolean active) {
        this.actionButton.active = active;
    }

    public void setButtonVisible(boolean visible) {
        this.actionButton.visible = visible;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (faction == null) return;

        int x = getX();
        int y = getY();
        int w = getWidth();
        int h = getHeight();

        guiGraphics.fill(x, y, x + w, y + h, BACKGROUND_COLOR);

        // Banner on the left (small, 18px wide area)
        if (bannerRenderer != null) {
            bannerRenderer.renderBanner(guiGraphics, x, y, 20, h, 10);
        }

        // Faction display name
        int textX = x + 22;
        int nameY = h > 20 ? y + (h / 2) - font.lineHeight : y + (h - font.lineHeight) / 2;
        guiGraphics.drawString(font, faction.getTeamDisplayName(), textX, nameY, com.talhanation.recruits.client.gui.util.GuiCompat.opaque(NAME_COLOR), false);

        // Leader name in smaller/lighter text if there's enough space
        if (h > 20 && faction.getTeamLeaderName() != null && !faction.getTeamLeaderName().isEmpty()) {
            guiGraphics.drawString(font, "Leader: " + faction.getTeamLeaderName(), textX, nameY + font.lineHeight + 1, com.talhanation.recruits.client.gui.util.GuiCompat.opaque(LEADER_COLOR), false);
        }

        actionButton.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mx = event.x();
        double my = event.y();
        int button = event.button();
        if (actionButton.isMouseOver(mx, my) && actionButton.active && actionButton.visible) {
            actionButton.onClick(new MouseButtonEvent(mx, my, new MouseButtonInfo(0, 0)), false);
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        double mx = event.x();
        double my = event.y();
        if (actionButton.isMouseOver(mx, my) && actionButton.active && actionButton.visible) {
            actionButton.onClick(new MouseButtonEvent(mx, my, new MouseButtonInfo(0, 0)), false);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
    }
}
