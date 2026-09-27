package com.talhanation.recruits.network;

import net.minecraft.resources.Identifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;

public class RecruitsChannel {

    private final SimpleChannel channel;

    public RecruitsChannel(Identifier name, int protocolVersion) {
        this.channel = ChannelBuilder.named(name)
                .networkProtocolVersion(protocolVersion)
                .simpleChannel();
    }

    public <T extends Message<?>> void registerMessage(int id, Class<T> type) {
        channel.messageBuilder(type, id)
                .encoder(Message::toBytes)
                .decoder(buf -> {
                    try {
                        Message<?> msg = type.getDeclaredConstructor().newInstance();
                        return type.cast(msg.fromBytes(buf));
                    } catch (ReflectiveOperationException e) {
                        throw new RuntimeException(e);
                    }
                })
                .consumerMainThread((msg, context) -> {
                    if (msg.getExecutingSide() == Dist.CLIENT) {
                        msg.executeClientSide(context);
                    } else if (msg.getExecutingSide() == Dist.DEDICATED_SERVER) {
                        msg.executeServerSide(context);
                    }
                    context.setPacketHandled(true);
                })
                .add();
    }

    public void send(PacketDistributor.PacketTarget target, Object message) {
        channel.send(message, target);
    }

    public void sendToServer(Object message) {
        channel.send(message, PacketDistributor.SERVER.noArg());
    }

    public SimpleChannel getChannel() {
        return channel;
    }
}
