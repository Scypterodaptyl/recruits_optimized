package com.talhanation.recruits.client.gui.component;

import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.CharacterEvent;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RecruitsMultiLineEditBox extends MultiLineEditBox {

    public boolean enableEditing;
    public RecruitsMultiLineEditBox(Font p_239008_, int p_239009_, int p_239010_, int p_239011_, int p_239012_, Component p_239013_, Component p_239014_) {
        super(p_239008_, p_239009_, p_239010_, p_239011_, p_239012_, p_239013_, p_239014_, -2039584, true, -3092272, true, true);
        this.enableEditing = false;

    }
    public boolean keyPressed(KeyEvent event) {
        int p_239433_ = event.key();
        int p_239434_ = event.scancode();
        int p_239435_ = event.modifiers();
        if(enableEditing) return super.keyPressed(event);
        else return false;
    }

    public boolean charTyped(CharacterEvent event) {
        char p_239387_ = (char) event.codepoint();
        int p_239388_ = event.modifiers();
        if(enableEditing) return super.charTyped(event);
        else return false;
    }

    public void setEnableEditing(boolean enableEditing) {
        this.enableEditing = enableEditing;
    }

    protected boolean scrollbarVisible() {
        return false;
    }

    protected void renderBackground(GuiGraphics p_282207_) {
        this.renderBorder(p_282207_, this.getX(), this.getY(), this.getWidth(), this.getHeight());
    }
    @Override
    protected void renderBorder(GuiGraphics p_289776_, int p_289792_, int p_289795_, int p_289775_, int p_289762_) {
        p_289776_.fill(p_289792_, p_289795_, p_289792_ + p_289775_, p_289795_ + p_289762_, -6250336);
        p_289776_.fill(p_289792_ + 1, p_289795_ + 1, p_289792_ + p_289775_ - 1, p_289795_ + p_289762_ - 1, -6250336);
    }

    public void setScrollAmount(double x) {
        super.setScrollAmount(x);
    }
}
