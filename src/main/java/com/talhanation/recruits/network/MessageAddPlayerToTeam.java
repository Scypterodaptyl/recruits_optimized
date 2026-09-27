package com.talhanation.recruits.network;

import com.talhanation.recruits.FactionEvents;
import com.talhanation.recruits.network.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.network.CustomPayloadEvent;

import java.util.Objects;

public class MessageAddPlayerToTeam implements Message<MessageAddPlayerToTeam> {

    private String teamName;
    private String namePlayerToAdd;

    public MessageAddPlayerToTeam(){
    }

    public MessageAddPlayerToTeam(String teamName, String namePlayerToAdd) {
        this.teamName = teamName;
        this.namePlayerToAdd = namePlayerToAdd;
    }

    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    public void executeServerSide(CustomPayloadEvent.Context context) {
        ServerPlayer player = Objects.requireNonNull(context.getSender());
        ServerLevel world = player.level();

        FactionEvents.addPlayerToTeam(player, world, this.teamName, this.namePlayerToAdd);
    }

    public MessageAddPlayerToTeam fromBytes(FriendlyByteBuf buf) {
        this.teamName = buf.readUtf();
        this.namePlayerToAdd = buf.readUtf();
        return this;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(this.teamName);
        buf.writeUtf(this.namePlayerToAdd);
    }
}
