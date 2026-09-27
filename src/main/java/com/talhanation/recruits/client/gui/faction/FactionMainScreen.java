package com.talhanation.recruits.client.gui.faction;

import net.minecraft.client.renderer.RenderPipelines;

import com.talhanation.recruits.Main;
import com.talhanation.recruits.FactionEvents;
import com.talhanation.recruits.client.ClientManager;
import net.minecraft.client.gui.GuiGraphics;
import com.talhanation.recruits.client.gui.RecruitsScreenBase;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.widget.ExtendedButton;

@OnlyIn(Dist.CLIENT)
public class FactionMainScreen extends RecruitsScreenBase {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Main.MOD_ID,"textures/gui/gui_small.png");
    private static final MutableComponent CREATE_TEAM = Component.translatable("gui.recruits.team_creation.create_team");
    private static final MutableComponent INSPECT_TEAM = Component.translatable("gui.recruits.team_creation.inspect_team");
    private static final MutableComponent TEAMS_LIST = Component.translatable("gui.recruits.team_creation.teams_list");
    private final Player player;
    public FactionMainScreen(Player player){
        super(Component.literal("TeamMainScreen"),246,84);
        this.player = player;
    }

    @Override
    protected void init() {
        super.init();

        MutableComponent mutableComponent = ClientManager.ownFaction != null ? INSPECT_TEAM : CREATE_TEAM;
        addRenderableWidget(new ExtendedButton(guiLeft + 20, guiTop + 29, 100, 20, mutableComponent, btn -> {
            if (ClientManager.ownFaction != null) {
                minecraft.setScreen(new FactionInspectionScreen(this, player));
            }
            else {
                FactionEvents.openTeamEditScreen(player);
            }
        }));

        addRenderableWidget(new ExtendedButton(guiLeft + 130, guiTop + 29, 100, 20, TEAMS_LIST, btn -> {
            minecraft.setScreen(new RecruitsFactionListScreen(this));
        }));
    }

    @Override
    public void renderRecruitsBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, guiLeft, guiTop, (float) (0), (float) (0), xSize, ySize, 256, 256);
    }

}
