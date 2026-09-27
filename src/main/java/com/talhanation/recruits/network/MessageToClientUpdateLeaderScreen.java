package com.talhanation.recruits.network;

import com.talhanation.recruits.client.gui.PatrolLeaderScreen;
import com.talhanation.recruits.network.Message;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.network.CustomPayloadEvent;

import java.util.List;


public class MessageToClientUpdateLeaderScreen implements Message<MessageToClientUpdateLeaderScreen> {
    public List<BlockPos> waypoints;
    public List<ItemStack> waypointItems;
    public int size;

    public MessageToClientUpdateLeaderScreen() {
    }

    public MessageToClientUpdateLeaderScreen(List<BlockPos> waypoints, List<ItemStack> waypointItems, int size) {
        this.waypoints = waypoints;
        this.waypointItems = waypointItems;
        this.size = size;
    }

    @Override
    public Dist getExecutingSide() {
        return Dist.CLIENT;
    }

    @Override
    public void executeClientSide(CustomPayloadEvent.Context context) {

    }

    @Override
    public MessageToClientUpdateLeaderScreen fromBytes(FriendlyByteBuf buf) {
        this.waypoints = buf.readList(b -> b.readBlockPos());
        this.waypointItems = buf.readList(b -> com.talhanation.recruits.util.NbtCompat.loadItem(b.readNbt()));
        this.size = buf.readInt();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeCollection(waypoints, (b, v) -> b.writeBlockPos(v));
        buf.writeCollection(waypointItems, (b, v) -> b.writeNbt(com.talhanation.recruits.util.NbtCompat.saveItem(v)));
        buf.writeInt(this.size);
    }
}

