package com.talhanation.recruits.network;

import com.talhanation.recruits.CommandEvents;
import com.talhanation.recruits.entities.AbstractRecruitEntity;
import com.talhanation.recruits.network.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.network.CustomPayloadEvent;

import java.util.Objects;
import java.util.UUID;

public class MessageDismount implements Message<MessageDismount> {

    private UUID uuid;
    private UUID group;

    public MessageDismount(){
    }

    public MessageDismount(UUID uuid, UUID group) {
        this.uuid = uuid;
        this.group = group;

    }

    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    public void executeServerSide(CustomPayloadEvent.Context context){
        ServerPlayer player = Objects.requireNonNull(context.getSender());
        player.level().getEntitiesOfClass(
                AbstractRecruitEntity.class,
                context.getSender().getBoundingBox().inflate(100)
        ).forEach((recruit) -> CommandEvents.onDismountButton(uuid, recruit, group));
    }
    public MessageDismount fromBytes(FriendlyByteBuf buf) {
        this.uuid = buf.readUUID();
        this.group = buf.readUUID();
        return this;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(uuid);
        buf.writeUUID(group);
    }

}