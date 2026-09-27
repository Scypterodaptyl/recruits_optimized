package com.talhanation.recruits.network;

import com.talhanation.recruits.FactionEvents;
import com.talhanation.recruits.network.Message;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class MessageCreateTeam implements Message<MessageCreateTeam> {

    private String teamName;
    private String displayName;
    private ChatFormatting color;
    private ItemStack banner;
    private int index;

    public MessageCreateTeam(){
    }

    public MessageCreateTeam(String name, String displayName, ItemStack banner, ChatFormatting color, int index) {
        this.teamName = name;
        this.displayName = displayName;
        this.banner = banner;
        this.color = color;
        this.index = index;
    }

    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    public void executeServerSide(CustomPayloadEvent.Context context) {
        ServerPlayer player = context.getSender();
        ServerLevel world = player.level();
        FactionEvents.createTeam(true, context.getSender(), world, this.teamName, this.displayName, player.getName().getString(), this.banner, this.color, (byte) index);
    }

    public MessageCreateTeam fromBytes(FriendlyByteBuf buf) {
        this.teamName = buf.readUtf();
        this.displayName = buf.readUtf();
        this.banner = com.talhanation.recruits.util.NbtCompat.loadItem(buf.readNbt());
        this.color = ChatFormatting.getById(buf.readInt());
        this.index = buf.readInt();
        return this;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(this.teamName);
        buf.writeUtf(this.displayName);
        buf.writeNbt(com.talhanation.recruits.util.NbtCompat.saveItem(this.banner));
        buf.writeInt(this.color.getId());
        buf.writeInt(this.index);
    }
}
