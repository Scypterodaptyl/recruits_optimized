package com.talhanation.recruits.network;

import com.talhanation.recruits.FactionEvents;
import com.talhanation.recruits.network.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class MessageLeaveTeam implements Message<MessageLeaveTeam> {

    public MessageLeaveTeam(){
    }

    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    public void executeServerSide(CustomPayloadEvent.Context context) {
        ServerPlayer player = context.getSender();
        ServerLevel level = player.level();
        FactionEvents.leaveTeam(false, player, null, level, false);
    }

    public MessageLeaveTeam fromBytes(FriendlyByteBuf buf) {
        return this;
    }

    public void toBytes(FriendlyByteBuf buf) {

    }
}
