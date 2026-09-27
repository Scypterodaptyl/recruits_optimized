package com.talhanation.recruits.client.gui;

import net.minecraft.client.renderer.RenderPipelines;

import com.talhanation.recruits.Main;
import com.talhanation.recruits.client.gui.component.ActivateableButton;
import com.talhanation.recruits.client.gui.widgets.RecruitsCheckBox;
import com.talhanation.recruits.entities.ScoutEntity;
import com.talhanation.recruits.network.MessageScoutTask;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

public class ScoutScreen extends RecruitsScreenBase {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Main.MOD_ID, "textures/gui/gui_big.png");
    private static final Component TITLE = Component.translatable("gui.recruits.more_screen.title");
    private final Player player;
    private final ScoutEntity scout;
    private ScoutEntity.State task;
    private static final MutableComponent SCOUTING = Component.translatable("gui.recruits.inv.text.scoutScoutTask");
    private static final MutableComponent TOOLTIP_SCOUTING = Component.translatable("gui.recruits.inv.tooltip.scoutScoutTask");
    private RecruitsCheckBox checkBoxScouting;
    public boolean scouting;
    public ScoutScreen(ScoutEntity scout, Player player) {
        super(TITLE, 195,160);
        this.player = player;
        this.scout = scout;
    }

    @Override
    protected void init() {
        super.init();
        this.task = ScoutEntity.State.fromIndex(scout.getTaskState());
        this.scouting = task == ScoutEntity.State.SCOUTING;
        setButtons();
    }

    private void setButtons(){
        clearWidgets();

        checkBoxScouting = new RecruitsCheckBox(guiLeft + 32, guiTop + ySize - 120 - 7, 130, 20, SCOUTING, this.scouting,
        (bool) -> {
                this.scouting = bool;
                Main.SIMPLE_CHANNEL.sendToServer(new MessageScoutTask(scout.getUUID(), scouting ? 1 : 0));
            }
        );
        checkBoxScouting.setTooltip(Tooltip.create(TOOLTIP_SCOUTING));
        addRenderableWidget(checkBoxScouting);
    }

    @Override
    public void renderRecruitsBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, guiLeft, guiTop, (float) (0), (float) (0), xSize, ySize, 256, 256);
    }

    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
    }
}
