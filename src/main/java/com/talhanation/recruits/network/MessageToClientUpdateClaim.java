package com.talhanation.recruits.network;

import com.talhanation.recruits.client.ClientManager;
import com.talhanation.recruits.client.api.ClientClaimEvent;
import com.talhanation.recruits.client.gui.worldmap.claim.WorldMapClaimIndex;
import com.talhanation.recruits.network.codec.ClaimNetworkCodec;
import com.talhanation.recruits.world.RecruitsClaim;
import com.talhanation.recruits.network.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class MessageToClientUpdateClaim implements Message<MessageToClientUpdateClaim> {
    private RecruitsClaim claim;

    public MessageToClientUpdateClaim() {
    }

    public MessageToClientUpdateClaim(RecruitsClaim claim) {
        this.claim = claim;
    }

    @Override
    public Dist getExecutingSide() {
        return Dist.CLIENT;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void executeClientSide(CustomPayloadEvent.Context context) {
        this.updateOrAddClaim(claim);
    }

    @OnlyIn(Dist.CLIENT)
    private void updateOrAddClaim(RecruitsClaim newClaim) {
        if (newClaim == null) return;

        if (newClaim.isRemoved) {
            removeClaim(newClaim);
            return;
        }

        for (int i = 0; i < ClientManager.recruitsClaims.size(); i++) {
            RecruitsClaim existing = ClientManager.recruitsClaims.get(i);
            if (existing.getUUID().equals(newClaim.getUUID())) {
                ClientManager.recruitsClaims.set(i, newClaim);
                WorldMapClaimIndex.invalidate();

                boolean isCurrentClaim = ClientManager.currentClaim != null
                        && ClientManager.currentClaim.getUUID().equals(newClaim.getUUID());

                // Aktuellen Claim-Zeiger ebenfalls aktualisieren
                if (isCurrentClaim) {
                    ClientManager.currentClaim = newClaim;
                }

                ClientManager.updateActiveSiege(newClaim);

                ClientClaimEvent.DataUpdated.BUS.post(new ClientClaimEvent.DataUpdated(newClaim, isCurrentClaim));
                return;
            }
        }

        ClientManager.recruitsClaims.add(newClaim);
        WorldMapClaimIndex.invalidate();
        ClientManager.updateActiveSiege(newClaim);
        ClientClaimEvent.DataUpdated.BUS.post(new ClientClaimEvent.DataUpdated(newClaim, false));
    }

    @OnlyIn(Dist.CLIENT)
    private void removeClaim(RecruitsClaim removedClaim) {
        boolean wasCurrentClaim = ClientManager.currentClaim != null
                && ClientManager.currentClaim.getUUID().equals(removedClaim.getUUID());

        ClientManager.recruitsClaims.removeIf(
                claim -> claim != null && claim.getUUID().equals(removedClaim.getUUID()));
        ClientManager.activeSiegeClaims.remove(removedClaim.getUUID());
        if (wasCurrentClaim) {
            ClientManager.currentClaim = null;
        }

        WorldMapClaimIndex.invalidate();
        ClientClaimEvent.DataUpdated.BUS.post(new ClientClaimEvent.DataUpdated(removedClaim, wasCurrentClaim));
    }

    @Override
    public MessageToClientUpdateClaim fromBytes(FriendlyByteBuf buf) {
        this.claim = ClaimNetworkCodec.readNullableClaim(buf);

        return this;
    }
    @Override
    public void toBytes(FriendlyByteBuf buf) {
        ClaimNetworkCodec.writeNullableClaim(buf, this.claim);
    }
}
