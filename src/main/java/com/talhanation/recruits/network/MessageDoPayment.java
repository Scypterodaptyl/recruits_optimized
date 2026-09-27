package com.talhanation.recruits.network;

import com.talhanation.recruits.FactionEvents;
import com.talhanation.recruits.network.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.network.CustomPayloadEvent;

import java.util.UUID;


public class MessageDoPayment implements Message<MessageDoPayment> {

    private int amount;
    private UUID uuid;
    public MessageDoPayment(){

    }

    public MessageDoPayment(UUID uuid, int amount) {
        this.amount = amount;
        this.uuid = uuid;
    }

    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    public void executeServerSide(CustomPayloadEvent.Context context){
        ServerPlayer serverPlayer = context.getSender();
        if(serverPlayer == null) return;

        if(!serverPlayer.getUUID().equals(uuid)) return;
        if(this.amount <= 0) return;

        if(serverPlayer.isCreative() && serverPlayer.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER)){
            return;
        }
        if(!FactionEvents.playerHasEnoughEmeralds(serverPlayer, this.amount)) return;

        FactionEvents.doPayment(serverPlayer, this.amount);
    }
    public MessageDoPayment fromBytes(FriendlyByteBuf buf) {
        this.uuid = buf.readUUID();
        this.amount = buf.readInt();
        return this;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(uuid);
        buf.writeInt(amount);
    }
}
