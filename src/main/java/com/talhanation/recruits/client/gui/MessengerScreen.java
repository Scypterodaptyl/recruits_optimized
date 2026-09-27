package com.talhanation.recruits.client.gui;

import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;

import com.talhanation.recruits.Main;
import com.talhanation.recruits.client.gui.player.PlayersList;
import com.talhanation.recruits.client.gui.player.SelectPlayerScreen;
import com.talhanation.recruits.client.gui.widgets.SelectedPlayerWidget;
import com.talhanation.recruits.entities.MessengerEntity;
import com.talhanation.recruits.network.MessageSendMessenger;
import com.talhanation.recruits.world.RecruitsPlayerInfo;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.gui.widget.ExtendedButton;
import org.lwjgl.glfw.GLFW;


public class MessengerScreen extends RecruitsScreenBase {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Main.MOD_ID, "textures/gui/professions/blank_gui.png");
    protected static final int PLAYER_NAME_COLOR = ARGB.color(255, 255, 255, 255);
    private final Player player;
    public static RecruitsPlayerInfo playerInfo;
    private final MessengerEntity messenger;
    private MultiLineEditBox textFieldMessage;
    private SelectedPlayerWidget selectedPlayerWidget;
    private static final MutableComponent TOOLTIP_MESSENGER = Component.translatable("gui.recruits.inv.tooltip.messenger");
    private static final MutableComponent BUTTON_SEND_MESSENGER = Component.translatable("gui.recruits.inv.text.send_messenger");
    private static final int fontColor = 4210752;
    public MessengerScreen(MessengerEntity messenger, Player player) {
        super(Component.literal(""), 197,250);
        this.player = player;
        this.messenger = messenger;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int key = event.key();
        int a = event.scancode();
        int b = event.modifiers();
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            this.onClose();
            return true;
        }

        setFocused(textFieldMessage);
        return textFieldMessage.keyPressed(new KeyEvent(key, a, b)) || textFieldMessage.isFocused() || super.keyPressed(event);
    }
    @Override
    protected void init() {
        super.init();

        setButtons();
    }
    public void tick() {
        super.tick();
        
    }

    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double p_100753_ = event.x();
        double p_100754_ = event.y();
        int p_100755_ = event.button();
        if (this.textFieldMessage.isFocused()) {
            this.textFieldMessage.mouseClicked(new MouseButtonEvent(p_100753_, p_100754_, new MouseButtonInfo(p_100755_, 0)), false);
        }
        return super.mouseClicked(event, doubleClick);
    }

    private void setButtons() {
        clearWidgets();

        this.textFieldMessage = MultiLineEditBox.builder().setX(guiLeft + 3).setY(guiTop + ySize - 203).setPlaceholder(Component.literal("")).build(font, 186, 150, Component.literal(""));
        this.textFieldMessage.setValue(messenger.getMessage());
        addRenderableWidget(textFieldMessage);

        Button sendButton = addRenderableWidget(new ExtendedButton(guiLeft + 33, guiTop + ySize - 52 , 128, 20, BUTTON_SEND_MESSENGER,
                button -> {
                    Main.SIMPLE_CHANNEL.sendToServer(new MessageSendMessenger(messenger.getUUID(), playerInfo, textFieldMessage.getValue(), true));
                    this.onClose();
                }
        ));
        sendButton.setTooltip(Tooltip.create(TOOLTIP_MESSENGER));
        sendButton.active = playerInfo != null;

        if(playerInfo != null){
            this.selectedPlayerWidget = new SelectedPlayerWidget(font, guiLeft + 33, guiTop + ySize - 235, 128, 20, Component.literal("x"), // Button label
                    () -> {
                        playerInfo = null;
                        Main.SIMPLE_CHANNEL.sendToServer(new MessageSendMessenger(messenger.getUUID(), playerInfo, textFieldMessage.getValue(), false));
                        this.selectedPlayerWidget.setPlayer(null, null);
                        this.setButtons();
                    }
            );

            this.selectedPlayerWidget.setPlayer(playerInfo.getUUID(), playerInfo.getName());
            addRenderableWidget(this.selectedPlayerWidget);
        }
        else
        {
            Button selectPlayerButton = addRenderableWidget(new ExtendedButton(guiLeft + 33, guiTop + ySize - 235, 128, 20, SelectPlayerScreen.TITLE,
                    button -> {
                        Main.SIMPLE_CHANNEL.sendToServer(new MessageSendMessenger(messenger.getUUID(), playerInfo, textFieldMessage.getValue(), false));
                        minecraft.setScreen(new SelectPlayerScreen(this, player, SelectPlayerScreen.TITLE, SelectPlayerScreen.BUTTON_SELECT, SelectPlayerScreen.BUTTON_SELECT_TOOLTIP, false, PlayersList.FilterType.NONE,
                                (playerInfo) -> {
                                    MessengerScreen.playerInfo = playerInfo;
                                    minecraft.setScreen(this);
                                }
                        ));

                    }
            ));
            selectPlayerButton.setTooltip(Tooltip.create(TOOLTIP_MESSENGER));
        }
    }

    public void onClose(){
        super.onClose();
        Main.SIMPLE_CHANNEL.sendToServer(new MessageSendMessenger(messenger.getUUID(), playerInfo, textFieldMessage.getValue(), false));
    }

    @Override
    public void renderRecruitsBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, guiLeft, guiTop, (float) (0), (float) (0), xSize, ySize, 256, 256);
    }
    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        int fontColor = 4210752;

        guiGraphics.drawString(font, "Player:", guiLeft + 5, guiTop + 5, com.talhanation.recruits.client.gui.util.GuiCompat.opaque(fontColor), false);
        guiGraphics.drawString(font, "Message:", guiLeft + 5, guiTop + 35, com.talhanation.recruits.client.gui.util.GuiCompat.opaque(fontColor), false);

        if(!messenger.getMainHandItem().isEmpty()){
            guiGraphics.renderFakeItem(messenger.getMainHandItem(), guiLeft + 140, guiTop + ySize - 48);
            guiGraphics.renderItemDecorations(font, messenger.getMainHandItem(),guiLeft + 140, guiTop + ySize - 48);
        }
    }
}