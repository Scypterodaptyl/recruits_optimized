package com.talhanation.recruits.client.gui.group;

import net.minecraft.client.renderer.RenderPipelines;

import com.talhanation.recruits.Main;
import com.talhanation.recruits.client.gui.CommandScreen;
import net.minecraft.client.gui.GuiGraphics;
import com.talhanation.recruits.client.gui.component.ActivateableButton;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;


@OnlyIn(Dist.CLIENT)
public class RecruitsFormationButton extends ActivateableButton {

    private final CommandScreen.Formation formation;
    public RecruitsFormationButton(CommandScreen.Formation formation, int xPos, int yPos, OnPress handler) {
        super(xPos - 10, yPos - 10, 21, 21, Component.empty(), handler);
        this.formation = formation;
    }

    @Override
    public void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float f) {
        super.renderContents(guiGraphics, mouseX, mouseY, f);

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, getTextureLocation(), getX(), getY(), (float) (0), (float) (0), 21, 21, 21, 21);
    }

    private Identifier getTextureLocation() {
        Identifier location;
        switch (this.formation.getIndex()){
            default -> location = Identifier.fromNamespaceAndPath(Main.MOD_ID, "textures/gui/image/none.png");
            case 1 ->  location = Identifier.fromNamespaceAndPath(Main.MOD_ID, "textures/gui/image/line.png");
            case 2 ->  location = Identifier.fromNamespaceAndPath(Main.MOD_ID, "textures/gui/image/square.png");
            case 3 ->  location = Identifier.fromNamespaceAndPath(Main.MOD_ID, "textures/gui/image/triangle.png");
            case 4 ->  location = Identifier.fromNamespaceAndPath(Main.MOD_ID, "textures/gui/image/hcircle.png");
            case 5 ->  location = Identifier.fromNamespaceAndPath(Main.MOD_ID, "textures/gui/image/hsquare.png");
            case 6 ->  location = Identifier.fromNamespaceAndPath(Main.MOD_ID, "textures/gui/image/vform.png");
            case 7 ->  location = Identifier.fromNamespaceAndPath(Main.MOD_ID, "textures/gui/image/circle.png");
            case 8 ->  location = Identifier.fromNamespaceAndPath(Main.MOD_ID, "textures/gui/image/movement.png");
        }
        return location;
    }
}
