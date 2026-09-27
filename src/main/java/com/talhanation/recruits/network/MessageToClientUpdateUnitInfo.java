package com.talhanation.recruits.network;

import com.talhanation.recruits.client.ClientManager;
import com.talhanation.recruits.network.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.network.CustomPayloadEvent;


public class MessageToClientUpdateUnitInfo implements Message<MessageToClientUpdateUnitInfo> {
    private boolean configValueNobleNeedsVillagers;
    private int availableRecruitsToHire;
    public MessageToClientUpdateUnitInfo() {

    }

    public MessageToClientUpdateUnitInfo(boolean configValueNobleNeedsVillagers, int availableRecruitsToHire) {
        this.configValueNobleNeedsVillagers = configValueNobleNeedsVillagers;
        this.availableRecruitsToHire = availableRecruitsToHire;
    }

    @Override
    public Dist getExecutingSide() {
        return Dist.CLIENT;
    }

    @Override
    public void executeClientSide(CustomPayloadEvent.Context context) {
        ClientManager.configValueNobleNeedsVillagers = configValueNobleNeedsVillagers;
        ClientManager.availableRecruitsToHire = availableRecruitsToHire;
    }

    @Override
    public MessageToClientUpdateUnitInfo fromBytes(FriendlyByteBuf buf) {
        this.configValueNobleNeedsVillagers = buf.readBoolean();
        this.availableRecruitsToHire = buf.readInt();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(this.configValueNobleNeedsVillagers);
        buf.writeInt(this.availableRecruitsToHire);
    }

}