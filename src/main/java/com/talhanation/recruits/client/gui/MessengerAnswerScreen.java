package com.talhanation.recruits.client.gui;

import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;

import com.talhanation.recruits.Main;
import com.talhanation.recruits.client.gui.component.RecruitsMultiLineEditBox;
import com.talhanation.recruits.entities.MessengerEntity;
import com.talhanation.recruits.network.MessageAnswerMessenger;
import com.talhanation.recruits.world.RecruitsPlayerInfo;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.gui.widget.ExtendedButton;

public class MessengerAnswerScreen extends RecruitsScreenBase {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Main.MOD_ID, "textures/gui/professions/blank_gui.png");
    private final Player player;
    private final MessengerEntity messenger;
    private RecruitsMultiLineEditBox textFieldMessage;

    private final String message;

    private final RecruitsPlayerInfo playerInfo;
    private static final MutableComponent BUTTON_OK = Component.translatable("gui.recruits.inv.text.ok_messenger");
    public MessengerAnswerScreen(MessengerEntity messenger, Player player, String message, RecruitsPlayerInfo playerInfo) {
        super(Component.literal(""), 197,250);
        this.player = player;
        this.messenger = messenger;
        this.message = message;
        this.playerInfo = playerInfo;
    }

    @Override
    protected void init() {
        super.init();
        this.textFieldMessage = new RecruitsMultiLineEditBox(font, guiLeft + 3, guiTop + ySize - 215, 186, 165, Component.empty(), Component.empty());
        this.textFieldMessage.setValue(message);
        this.textFieldMessage.setEnableEditing(false);

        addRenderableWidget(textFieldMessage);

        setOKButton();
    }
    public void tick() {
        super.tick();
        
    }

    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double p_100753_ = event.x();
        double p_100754_ = event.y();
        int p_100755_ = event.button();
        return super.mouseClicked(event, doubleClick);
    }

    private void setOKButton() {
        Button sendButton = addRenderableWidget(new ExtendedButton(guiLeft + 33, guiTop + ySize - 50, 128, 20, BUTTON_OK,
                button -> {
                    Main.SIMPLE_CHANNEL.sendToServer(new MessageAnswerMessenger(messenger.getUUID()));

                    onClose();
                }
        ));
    }

    @Override
    public void onClose() {
        super.onClose();

    }


    @Override
    public void renderRecruitsBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, guiLeft, guiTop, (float) (0), (float) (0), xSize, ySize, 256, 256);
    }

    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        String targetPlayer = playerInfo.getName();
        String owner = this.messenger.getOwnerName();
        String unit = "min";
        int rawtime = this.messenger.getWaitingTime();
        int time = rawtime / 20;
        if (time <= 100) unit = "sec";
        else time = time / 60;

        //Info
        int fontColor = 4210752;
        guiGraphics.drawString(font, "From:", guiLeft + 9, guiTop + 9, com.talhanation.recruits.client.gui.util.GuiCompat.opaque(fontColor), false);
        guiGraphics.drawString(font, "To:", guiLeft + 9,  guiTop + 20, com.talhanation.recruits.client.gui.util.GuiCompat.opaque(fontColor), false);
        guiGraphics.drawString(font, "" + owner, guiLeft + 50,  guiTop + 9, com.talhanation.recruits.client.gui.util.GuiCompat.opaque(fontColor), false);
        guiGraphics.drawString(font, "" + targetPlayer, guiLeft + 50,  guiTop + 20, com.talhanation.recruits.client.gui.util.GuiCompat.opaque(fontColor), false);

        guiGraphics.drawString(font, "Time: " + time + unit, guiLeft + 130, guiTop + 9, com.talhanation.recruits.client.gui.util.GuiCompat.opaque(fontColor), false);

        if(!messenger.getMainHandItem().isEmpty()){
            guiGraphics.renderFakeItem(messenger.getMainHandItem(), guiLeft + 120, guiTop + ySize - 48);
            guiGraphics.renderItemDecorations(font, messenger.getMainHandItem(),guiLeft + 120, guiTop + ySize - 48);
        }
    }
}