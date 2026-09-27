package com.talhanation.recruits.network;

import com.talhanation.recruits.entities.MessengerEntity;
import com.talhanation.recruits.network.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.network.CustomPayloadEvent;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class MessageAnswerMessenger implements Message<MessageAnswerMessenger> {

    private UUID recruit;
    public MessageAnswerMessenger() {
    }
    public MessageAnswerMessenger(UUID recruit) {
        this.recruit = recruit;
    }

    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    public void executeServerSide(CustomPayloadEvent.Context context){
        ServerPlayer player = context.getSender();
        List<MessengerEntity> list = Objects.requireNonNull(context.getSender()).level().getEntitiesOfClass(
                MessengerEntity.class,
                context.getSender().getBoundingBox().inflate(16D)
        );
        for (MessengerEntity messenger : list){

            if (messenger.getUUID().equals(this.recruit)){

                messenger.teleportWaitTimer = 100;
                context.getSender().sendSystemMessage(messenger.MESSENGER_INFO_ON_MY_WAY());
                messenger.giveDeliverItem(player);

                messenger.setMessengerState(MessengerEntity.MessengerState.TELEPORT_BACK);
            }
        }

    }
    public MessageAnswerMessenger fromBytes(FriendlyByteBuf buf) {
        this.recruit = buf.readUUID();
        return this;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(recruit);
    }
}
