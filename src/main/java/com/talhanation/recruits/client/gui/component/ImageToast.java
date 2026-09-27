package com.talhanation.recruits.client.gui.component;

import net.minecraft.client.renderer.RenderPipelines;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public class ImageToast implements Toast {
    @Nullable
    protected final Identifier image;
    private final Component title;
    private final Component description;
    private long lastChanged;
    private boolean hasStarted;
    public final long SHOW_TIME = 20000L;

    private static final Identifier BACKGROUND_SPRITE = Identifier.withDefaultNamespace("toast/advancement");
    private Visibility wantedVisibility = Visibility.SHOW;

    public ImageToast(@Nullable Identifier image, Component title, Component description) {
        this.image = image;
        this.title = title;
        this.description = description;
    }

    @Override
    public Visibility getWantedVisibility() {
        return this.wantedVisibility;
    }

    @Override
    public void update(ToastManager toastManager, long deltaTime) {
        if (!this.hasStarted) {
            this.lastChanged = deltaTime;
            this.hasStarted = true;
        }
        this.wantedVisibility = deltaTime - this.lastChanged >= SHOW_TIME ? Visibility.HIDE : Visibility.SHOW;
    }

    @Override
    public void render(GuiGraphics guiGraphics, Font font, long deltaTime) {
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND_SPRITE, 0, 0, this.width(), this.height());

        if (image != null) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, this.image, 5, 5, (float) (0), (float) (0), 21, 21, 21, 21);
        }

        if (this.title != null) {
            guiGraphics.drawString(font, this.title, 30, 7, 0xFFFFFFFF, false);
        }

        if (this.description != null) {
            guiGraphics.pose().pushMatrix();
            try {
                guiGraphics.pose().translate((float) (30), (float) (18));
                guiGraphics.pose().scale((float) (0.5f), (float) (0.5f));
                guiGraphics.drawString(font, this.description, 0, 0, 0xFFCCCCCC, false);
            } finally {
                guiGraphics.pose().popMatrix();
            }
        }
    }

    @Override
    public int width() {
        return 160;
    }

    @Override
    public int height() {
        return 32;
    }
}
