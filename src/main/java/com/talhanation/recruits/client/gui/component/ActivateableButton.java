package com.talhanation.recruits.client.gui.component;

import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.widget.ExtendedButton;

public class ActivateableButton extends ExtendedButton {
    public ActivateableButton(int p_93728_, int p_93729_, int p_93730_, int p_93731_, Component p_93732_, OnPress p_93733_) {
        super(p_93728_, p_93729_, p_93730_, p_93731_, p_93732_, p_93733_);
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
}
