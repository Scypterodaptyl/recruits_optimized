package com.talhanation.recruits.network;

import com.talhanation.recruits.FactionEvents;
import com.talhanation.recruits.world.RecruitsDiplomacyManager;
import com.talhanation.recruits.world.RecruitsFaction;
import com.talhanation.recruits.network.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.network.CustomPayloadEvent;


public class MessageChangeDiplomacyStatus implements Message<MessageChangeDiplomacyStatus> {
    private String ownTeam;
    private String otherTeam;
    private byte status;

    public MessageChangeDiplomacyStatus(){
    }

    public MessageChangeDiplomacyStatus(RecruitsFaction ownTeam, RecruitsFaction otherTeam, RecruitsDiplomacyManager.DiplomacyStatus status) {
        this.status = status.getByteValue();
        this.ownTeam = ownTeam.getStringID();
        this.otherTeam = otherTeam.getStringID();
    }

    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    public void executeServerSide(CustomPayloadEvent.Context context){
        RecruitsDiplomacyManager.DiplomacyStatus status = RecruitsDiplomacyManager.DiplomacyStatus.fromByte(this.status);

        FactionEvents.recruitsDiplomacyManager.setRelation(ownTeam, otherTeam, status, (ServerLevel) context.getSender().level());

    }
    public MessageChangeDiplomacyStatus fromBytes(FriendlyByteBuf buf) {
        this.ownTeam = buf.readUtf();
        this.otherTeam = buf.readUtf();
        this.status = buf.readByte();
        return this;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(ownTeam);
        buf.writeUtf(otherTeam);
        buf.writeByte(status);
    }
}
