package com.talhanation.recruits.network;

import com.talhanation.recruits.Main;
import com.talhanation.recruits.entities.AbstractLeaderEntity;
import com.talhanation.recruits.network.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.Objects;
import java.util.UUID;

public class MessagePatrolLeaderRemoveWayPoint implements Message<MessagePatrolLeaderRemoveWayPoint> {
    private UUID worker;

    public MessagePatrolLeaderRemoveWayPoint() {
    }

    public MessagePatrolLeaderRemoveWayPoint(UUID recruit) {
        this.worker = recruit;
    }

    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    public void executeServerSide(CustomPayloadEvent.Context context) {
        ServerPlayer player = Objects.requireNonNull(context.getSender());
        player.level().getEntitiesOfClass(
                AbstractLeaderEntity.class,
                player.getBoundingBox().inflate(100.0D),
                v -> v.getUUID().equals(this.worker) && v.isAlive() && v.isEffectedByCommand(player.getUUID())
        ).forEach((merchant) -> this.removeLastWayPoint(player, merchant));
    }

    private void removeLastWayPoint(ServerPlayer player, AbstractLeaderEntity leaderEntity) {
        if (!leaderEntity.WAYPOINTS.isEmpty()) leaderEntity.WAYPOINTS.pop();
        if (!leaderEntity.WAYPOINT_ITEMS.isEmpty()) leaderEntity.WAYPOINT_ITEMS.pop();

        Main.SIMPLE_CHANNEL.send(PacketDistributor.PLAYER.with(player), new MessageToClientUpdateLeaderScreen(leaderEntity.WAYPOINTS, leaderEntity.WAYPOINT_ITEMS, leaderEntity.getArmySize()));
    }

    public MessagePatrolLeaderRemoveWayPoint fromBytes(FriendlyByteBuf buf) {
        this.worker = buf.readUUID();
        return this;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(this.worker);
    }
}
