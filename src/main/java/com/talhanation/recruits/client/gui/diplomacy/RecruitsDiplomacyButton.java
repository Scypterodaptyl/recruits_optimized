package com.talhanation.recruits.client.gui.diplomacy;

import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.renderer.RenderPipelines;

import com.talhanation.recruits.Main;
import com.talhanation.recruits.world.RecruitsDiplomacyManager;
import com.talhanation.recruits.world.RecruitsGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.widget.ExtendedButton;

@OnlyIn(Dist.CLIENT)
public class RecruitsDiplomacyButton extends ExtendedButton {

    private final RecruitsDiplomacyManager.DiplomacyStatus status;

    public RecruitsDiplomacyButton(RecruitsDiplomacyManager.DiplomacyStatus status, int xPos, int yPos, int width, int height, Component displayString, OnPress handler) {
        super(xPos, yPos, width, height, displayString, handler);
        this.status = status;
    }

    private static Component createDisplayString(RecruitsGroup group) {
        return Component.literal(group.getName() + " (" + group.getCount() + ")");
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double p_93641_ = event.x();
        double p_93642_ = event.y();
        int p_93643_ = event.button();
        if (this.visible) {
            if (this.isValidClickButton(event.buttonInfo())) {
                boolean flag = this.clicked(p_93641_, p_93642_);
                if (flag) {
                    this.playDownSound(Minecraft.getInstance().getSoundManager());
                    this.onClick(new MouseButtonEvent(p_93641_, p_93642_, new MouseButtonInfo(0, 0)), false);
                    return true;
                }
            }

            return false;
        } else {
            return false;
        }
    }

    protected boolean clicked(double p_93681_, double p_93682_) {
        return this.visible && p_93681_ >= (double) this.getX() && p_93682_ >= (double) this.getY() && p_93681_ < (double)(this.getX() + this.width) && p_93682_ < (double)(this.getY() + this.height);
    }

    @Override
    public void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float f) {
        super.renderContents(guiGraphics, mouseX, mouseY, f);

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, getTextureLocation(), this.getX(), this.getY(), (float) (0), (float) (0), 21, 21, 21, 21);
    }

    private Identifier getTextureLocation() {
        Identifier location;

        switch (this.status){
            default -> location = Identifier.fromNamespaceAndPath(Main.MOD_ID, "textures/gui/image/neutral.png");
            case ALLY ->  location = Identifier.fromNamespaceAndPath(Main.MOD_ID, "textures/gui/image/ally.png");
            case ENEMY ->  location = Identifier.fromNamespaceAndPath(Main.MOD_ID, "textures/gui/image/enemy.png");
        }
        return location;
    }


}
