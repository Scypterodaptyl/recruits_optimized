package com.talhanation.recruits.client.gui.group;

import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.renderer.RenderPipelines;

import com.talhanation.recruits.client.ClientManager;
import com.talhanation.recruits.world.RecruitsGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.Identifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.widget.ExtendedButton;
@OnlyIn(Dist.CLIENT)
public class RecruitsGroupButton extends ExtendedButton {

    private RecruitsGroup group;
    private Identifier image;

    public RecruitsGroupButton(RecruitsGroup group, int xPos, int yPos, int width, int height, Component displayString, OnPress handler) {
        super(xPos, yPos, width, height, displayString, handler);
        this.group = group;
        this.image = RecruitsGroup.IMAGES.get(group.getImage());
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
        return this.visible && p_93681_ >= (double)this.getX() && p_93682_ >= (double)this.getY() && p_93681_ < (double)(this.getX() + this.width) && p_93682_ < (double)(this.getY() + this.height);
    }

    public RecruitsGroup getGroup() {
        return group;
    }

    @Override
    public void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        int k = !this.active ? 0 : (this.isHoveredOrFocused() ? 2 : 1);
        com.talhanation.recruits.client.gui.util.GuiCompat.blitButton(guiGraphics, this.getX(), this.getY(), this.width, this.height, 46 + k * 20);

        if(this.image != null){
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, this.image, this.getX() + 10, this.getY() + 8, (float) (0), (float) (0), 21, 21, 21, 21);
        }

        // Get the group name and count
        String groupName = group.getName();

        String groupCount = "";
        if(group.getCount() == group.getSize()){
            groupCount = "[" +group.getSize() + "]";
        }
        else{
            groupCount = "[" + group.getCount() + "/" + group.getSize() + "]";
        }
        // Set the scale for the text
        float scale = 0.65f;

        // Calculate positions for the texts
        int nameWidth = (int)(mc.font.width(groupName) * scale);
        int countWidth = (int)(mc.font.width(groupCount) * scale);

        // Calculate x positions to center the text
        int nameX = this.getX() + (this.width - nameWidth) / 2;
        int countX = this.getX() + (this.width - countWidth) / 2 + 3;

        // Calculate y positions for the texts
        int nameY = this.getY() + 3;
        int countY = this.getY() + (int)(mc.font.lineHeight * scale) + 24; // Below the group name with some padding

        // Draw the texts with scaling
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate((float) (nameX), (float) (nameY));
        guiGraphics.pose().scale((float) (scale), (float) (scale));
        guiGraphics.drawString(mc.font, Language.getInstance().getVisualOrder(FormattedText.of(groupName)), 0, 0, com.talhanation.recruits.client.gui.util.GuiCompat.opaque(getFGColor()), false);
        guiGraphics.pose().popMatrix();

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate((float) (countX), (float) (countY));
        guiGraphics.pose().scale((float) (scale), (float) (scale));
        guiGraphics.drawString(mc.font, Language.getInstance().getVisualOrder(FormattedText.of(groupCount)), 0, 0, com.talhanation.recruits.client.gui.util.GuiCompat.opaque(getFGColor()), false);
        guiGraphics.pose().popMatrix();

        Component aggroState = ClientManager.getGroupAggroState(group.getUUID());
        Component moveState = ClientManager.getGroupMoveState(group.getUUID());
        java.util.Set<String> specialStates = ClientManager.getGroupSpecialStates(group.getUUID());

        float infoScale = 0.5f;
        int lineY = this.getY() + this.height + 1;

        if (aggroState != null) {
            lineY = drawStateLine(guiGraphics, mc, aggroState, infoScale, lineY, 0xFFB0B0B0);
        }
        if (moveState != null) {
            lineY = drawStateLine(guiGraphics, mc, moveState, infoScale, lineY, 0xFF9AC0E0);
        }
        for (String key : specialStates) {
            Component label = specialStateLabel(key);
            if (label != null) {
                lineY = drawStateLine(guiGraphics, mc, label, infoScale, lineY, 0xFFE0534A);
            }
        }
    }

    private static Component specialStateLabel(String key) {
        return switch (key) {
            case "hold_fire" -> Component.translatableWithFallback("gui.recruits.command.state.holding_fire", "Holding Fire");
            case "strategic_fire" -> Component.translatableWithFallback("gui.recruits.command.state.strategic_fire", "Strategic Fire");
            case "shields_up" -> Component.translatableWithFallback("gui.recruits.command.state.shields_up", "Shields Up");
            default -> null;
        };
    }

    private int drawStateLine(GuiGraphics guiGraphics, Minecraft mc, Component text, float scale, int y, int color) {
        int textWidth = (int)(mc.font.width(text) * scale);
        int textX = this.getX() + (this.width - textWidth) / 2;

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate((float) (textX), (float) (y));
        guiGraphics.pose().scale((float) (scale), (float) (scale));
        guiGraphics.drawString(mc.font, text.getVisualOrderText(), 0, 0, com.talhanation.recruits.client.gui.util.GuiCompat.opaque(color), false);
        guiGraphics.pose().popMatrix();

        return y + (int)(mc.font.lineHeight * scale) + 1;
    }
}