package com.talhanation.recruits.client.gui.component;

import com.talhanation.recruits.world.RecruitsFaction;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RecruitsTeamImageToast extends ImageToast {

    private final BannerRenderer bannerRenderer;
    public RecruitsTeamImageToast(Identifier image, Component title, Component description, RecruitsFaction recruitsFaction) {
        super(image, title, description);
        this.bannerRenderer = new BannerRenderer(recruitsFaction);
    }

    @Override
    public void render(GuiGraphics guiGraphics, Font font, long deltaTime) {
        super.render(guiGraphics, font, deltaTime);

        int bannerX = 139;
        int bannerY = 3;
        if(image == null){
            bannerX = 3;
        }

        if (bannerRenderer != null) {
            bannerRenderer.renderBanner(guiGraphics, bannerX, bannerY, width(), height(), 14);
        }
    }
}
