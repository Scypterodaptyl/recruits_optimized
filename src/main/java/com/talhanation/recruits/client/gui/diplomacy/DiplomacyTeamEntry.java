package com.talhanation.recruits.client.gui.diplomacy;

import net.minecraft.client.renderer.RenderPipelines;

import com.talhanation.recruits.Main;
import com.talhanation.recruits.client.gui.component.BannerRenderer;
import com.talhanation.recruits.client.gui.widgets.ListScreenEntryBase;
import com.talhanation.recruits.client.gui.widgets.ListScreenListBase;
import com.talhanation.recruits.world.RecruitsDiplomacyManager;
import com.talhanation.recruits.world.RecruitsFaction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public class DiplomacyTeamEntry extends ListScreenEntryBase<DiplomacyTeamEntry> {
    protected static final int SKIN_SIZE = 24;
    protected static final int PADDING = 4;
    protected static final int BG_FILL = ARGB.color(255, 60, 60, 60);
    protected static final int BG_FILL_HOVERED = ARGB.color(255, 100, 100, 100);
    protected static final int BG_FILL_SELECTED = ARGB.color(255, 10, 10, 10);
    protected static final int PLAYER_NAME_COLOR = ARGB.color(255, 255, 255, 255);

    protected final Minecraft minecraft;
    protected final DiplomacyTeamListScreen screen;
    protected final @NotNull RecruitsFaction team;
    protected final BannerRenderer bannerRenderer;
    protected final RecruitsDiplomacyManager.DiplomacyStatus status;

    public DiplomacyTeamEntry(DiplomacyTeamListScreen screen, @NotNull RecruitsFaction team, RecruitsDiplomacyManager.DiplomacyStatus status) {
        this.minecraft = Minecraft.getInstance();
        this.screen = screen;
        this.team = team;
        this.bannerRenderer = new BannerRenderer(team);
        this.status = status;
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

    public void renderElement(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float delta, int skinX, int skinY, int textX, int textY) {
        boolean selected = team.equalsFaction(screen.getSelected());
        if (selected) {
            guiGraphics.fill( left, top, left + width, top + height, BG_FILL_SELECTED);
        } else if (hovered) {
            guiGraphics.fill( left, top, left + width, top + height, BG_FILL_HOVERED);
        } else {
            guiGraphics.fill( left, top, left + width, top + height, BG_FILL);
        }
        int iconX = 178;
        int iconY = 5;
        bannerRenderer.renderBanner(guiGraphics, left, top, width, height, 15);
        if(status != null){
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, getStatusTextureLocation(), left + iconX, top + iconY, (float) (0), (float) (0), 21, 21, 21, 21);
        }

        /*
        Integer teamColor = ChatFormatting.getById(team.getTeamColor()).getColor();
        int unitColor = TeamCreationScreen.RecruitColorID.get(team.getUnitColor());

        GuiComponent.fill(poseStack, left + 10, top , left + 200,top + 10, 0xFFFF0000);

        GuiComponent.fill(poseStack, left + 10, top + 20, left + 200,top + 10, 0x8000FF00);
         */
       guiGraphics.drawString(minecraft.font, team.getTeamDisplayName(), (int) ((float) textX + 10), (int) ((float) textY), com.talhanation.recruits.client.gui.util.GuiCompat.opaque(PLAYER_NAME_COLOR), false);
    }

    @Nullable
    public RecruitsFaction getTeamInfo() {
        return team;
    }

    @Override
    public ListScreenListBase<DiplomacyTeamEntry> getList() {
        return screen.list;
    }

    private Identifier getStatusTextureLocation() {
        Identifier location;

        switch (this.status){
            default -> location = Identifier.fromNamespaceAndPath(Main.MOD_ID, "textures/gui/image/neutral.png");
            case ALLY ->  location = Identifier.fromNamespaceAndPath(Main.MOD_ID, "textures/gui/image/ally.png");
            case ENEMY ->  location = Identifier.fromNamespaceAndPath(Main.MOD_ID, "textures/gui/image/enemy.png");
        }
        return location;
    }

}