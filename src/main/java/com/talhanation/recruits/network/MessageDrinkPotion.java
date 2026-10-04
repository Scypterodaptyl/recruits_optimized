package com.talhanation.recruits.network;

import com.talhanation.recruits.entities.AbstractRecruitEntity;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class MessageDrinkPotion implements Message<MessageDrinkPotion> {

    private UUID player;
    private UUID group;
    private int slot;

    public MessageDrinkPotion() {
    }

    public MessageDrinkPotion(UUID player, UUID group, int slot) {
        this.player = player;
        this.group = group;
        this.slot = slot;
    }

    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    public void executeServerSide(NetworkEvent.Context context) {
        ServerPlayer serverPlayer = Objects.requireNonNull(context.getSender());
        if (slot < 0 || slot > 1) return;
        List<AbstractRecruitEntity> list = serverPlayer.getCommandSenderWorld().getEntitiesOfClass(AbstractRecruitEntity.class, serverPlayer.getBoundingBox().inflate(100));
        for (AbstractRecruitEntity recruit : list) {
            if (recruit.isEffectedByCommand(serverPlayer.getUUID(), group)) {
                recruit.forcedPotionSlot = slot;
            }
        }
    }

    public MessageDrinkPotion fromBytes(FriendlyByteBuf buf) {
        this.player = buf.readUUID();
        this.group = buf.readUUID();
        this.slot = buf.readInt();
        return this;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(this.player);
        buf.writeUUID(this.group);
        buf.writeInt(this.slot);
    }
}
