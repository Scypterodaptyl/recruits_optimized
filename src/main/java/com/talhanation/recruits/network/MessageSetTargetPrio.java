package com.talhanation.recruits.network;

import com.talhanation.recruits.entities.AbstractRecruitEntity;
import com.talhanation.recruits.entities.IHasTargetPriority;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class MessageSetTargetPrio implements Message<MessageSetTargetPrio> {

    private UUID recruit;
    private int state;

    public MessageSetTargetPrio() {
    }
    public MessageSetTargetPrio(UUID recruit, int state) {
        this.recruit = recruit;
        this.state = state;
    }

    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    public void executeServerSide(NetworkEvent.Context context){
        ServerPlayer player = Objects.requireNonNull(context.getSender());
        List<AbstractRecruitEntity> list = player.getCommandSenderWorld().getEntitiesOfClass(AbstractRecruitEntity.class, player.getBoundingBox().inflate(16D));
        for (AbstractRecruitEntity recruitEntity : list){

            if (recruitEntity.getUUID().equals(this.recruit) && recruitEntity.isOwnedBy(player) && recruitEntity instanceof IHasTargetPriority specialRecruit){

                specialRecruit.setTargetPriority(IHasTargetPriority.TargetPriority.fromIndex(state));
                break;
            }
        }
    }
    public MessageSetTargetPrio fromBytes(FriendlyByteBuf buf) {
        this.recruit = buf.readUUID();
        this.state = buf.readInt();
        return this;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(recruit);
        buf.writeInt(state);
    }
}
