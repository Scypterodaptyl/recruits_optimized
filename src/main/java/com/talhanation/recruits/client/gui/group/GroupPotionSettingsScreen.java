package com.talhanation.recruits.client.gui.group;

import com.mojang.blaze3d.systems.RenderSystem;
import com.talhanation.recruits.Main;
import com.talhanation.recruits.config.RecruitsServerConfig;
import com.talhanation.recruits.network.MessageUpdateGroup;
import com.talhanation.recruits.world.RecruitsGroup;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.widget.ExtendedButton;

public class GroupPotionSettingsScreen extends Screen {
    private static final int FONT_COLOR = 4210752;
    private static final ResourceLocation TEXTURE = new ResourceLocation(Main.MOD_ID, "textures/gui/gui_big.png");
    private final Screen parent;
    private final RecruitsGroup group;
    private final int imageWidth = 195;
    private final int imageHeight = 160;
    private int leftPos;
    private int topPos;
    private final Button[] valueButtons = new Button[2];
    private Button combatButton;

    public GroupPotionSettingsScreen(Screen parent, RecruitsGroup group) {
        super(Component.literal(""));
        this.parent = parent;
        this.group = group;
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;

        for (int i = 0; i < 2; i++) {
            final int slot = i;
            int y = topPos + 35 + i * 40;
            addRenderableWidget(new ExtendedButton(leftPos + 7, y, 20, 20, Component.literal("-"), b -> change(slot, -1)));
            valueButtons[i] = new ExtendedButton(leftPos + 29, y, 137, 20, Component.empty(), b -> {});
            valueButtons[i].active = false;
            addRenderableWidget(valueButtons[i]);
            addRenderableWidget(new ExtendedButton(leftPos + 168, y, 20, 20, Component.literal("+"), b -> change(slot, 1)));
        }

        combatButton = new ExtendedButton(leftPos + 7, topPos + 115, 181, 20, Component.empty(), b -> {
            group.potionCombatOnly = combatOnly() ? 0 : 1;
            refresh();
        });
        addRenderableWidget(combatButton);

        addRenderableWidget(new ExtendedButton(leftPos + 7, topPos + 137, 181, 20, Component.translatable("gui.recruits.groups.potions.back"), b -> onClose()));
        refresh();
    }

    private int configDefault(int slot) {
        return slot == 0 ? RecruitsServerConfig.PotionSlot1MinHealth.get() : RecruitsServerConfig.PotionSlot2MinHealth.get();
    }

    private boolean combatOnly() {
        return group.potionCombatOnly >= 0 ? group.potionCombatOnly == 1 : RecruitsServerConfig.PotionOnlyInCombat.get();
    }

    // -1 default (server config), 0 off, 1..40 half hearts
    private void change(int slot, int delta) {
        int value = Math.max(-1, Math.min(40, group.potionMinHealth[slot] + delta));
        group.potionMinHealth[slot] = value;
        refresh();
    }

    private void refresh() {
        for (int i = 0; i < 2; i++) {
            int v = group.potionMinHealth[i];
            String text = v < 0 ? Component.translatable("gui.recruits.groups.potions.default").getString() + " (" + configDefault(i) + ")"
                    : v == 0 ? Component.translatable("gui.recruits.groups.potions.off").getString() : String.valueOf(v);
            valueButtons[i].setMessage(Component.literal(text));
        }
        combatButton.setMessage(Component.translatable("gui.recruits.groups.potions.combat",
                Component.translatable(combatOnly() ? "gui.recruits.groups.potions.yes" : "gui.recruits.groups.potions.no")));
    }

    // Esc and Back both save the settings.
    @Override
    public void onClose() {
        Main.SIMPLE_CHANNEL.sendToServer(new MessageUpdateGroup(group));
        minecraft.setScreen(parent);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        guiGraphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        guiGraphics.drawString(font, Component.translatable("gui.recruits.groups.potions.title"), leftPos + 7, topPos + 5, FONT_COLOR, false);
        for (int i = 0; i < 2; i++) {
            guiGraphics.drawString(font, Component.translatable("gui.recruits.groups.potions.slot", i + 1), leftPos + 7, topPos + 24 + i * 40, FONT_COLOR, false);
        }
        super.render(guiGraphics, mouseX, mouseY, delta);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics) {
    }
}
