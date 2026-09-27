package com.talhanation.recruits.client.api;

import com.talhanation.recruits.world.RecruitsClaim;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.bus.CancellableEventBus;
import net.minecraftforge.eventbus.api.bus.EventBus;
import net.minecraftforge.eventbus.api.event.MutableEvent;
import net.minecraftforge.eventbus.api.event.characteristic.Cancellable;

import javax.annotation.Nullable;


@OnlyIn(Dist.CLIENT)
public abstract class ClientClaimEvent extends MutableEvent {

    private final RecruitsClaim claim;

    protected ClientClaimEvent(RecruitsClaim claim) {
        this.claim = claim;
    }

    public RecruitsClaim getClaim() {
        return claim;
    }


    public static class Enter extends ClientClaimEvent {
        public static final EventBus<ClientClaimEvent.Enter> BUS = EventBus.create(ClientClaimEvent.Enter.class);

        @Nullable
        private final RecruitsClaim previousClaim;

        public Enter(RecruitsClaim newClaim, @Nullable RecruitsClaim previousClaim) {
            super(newClaim);
            this.previousClaim = previousClaim;
        }

        @Nullable
        public RecruitsClaim getPreviousClaim() {
            return previousClaim;
        }
    }

    public static class Leave extends ClientClaimEvent {
        public static final EventBus<ClientClaimEvent.Leave> BUS = EventBus.create(ClientClaimEvent.Leave.class);
        @Nullable
        private final RecruitsClaim nextClaim;

        public Leave(RecruitsClaim leftClaim, @Nullable RecruitsClaim nextClaim) {
            super(leftClaim);
            this.nextClaim = nextClaim;
        }

        @Nullable
        public RecruitsClaim getNextClaim() {
            return nextClaim;
        }
    }

    public static class DataUpdated extends ClientClaimEvent {
        public static final EventBus<ClientClaimEvent.DataUpdated> BUS = EventBus.create(ClientClaimEvent.DataUpdated.class);

        private final boolean isCurrentClaim;

        public DataUpdated(RecruitsClaim claim, boolean isCurrentClaim) {
            super(claim);
            this.isCurrentClaim = isCurrentClaim;
        }

        public boolean isCurrentClaim() {
            return isCurrentClaim;
        }
    }

        public static class SiegeStarted extends ClientClaimEvent implements Cancellable {
        public static final CancellableEventBus<ClientClaimEvent.SiegeStarted> BUS = CancellableEventBus.create(ClientClaimEvent.SiegeStarted.class);
        public SiegeStarted(RecruitsClaim claim) {
            super(claim);
        }
    }

    public static class SiegeEnded extends ClientClaimEvent {
        public static final EventBus<ClientClaimEvent.SiegeEnded> BUS = EventBus.create(ClientClaimEvent.SiegeEnded.class);
        private final boolean wasConquered;

        public SiegeEnded(RecruitsClaim claim, boolean wasConquered) {
            super(claim);
            this.wasConquered = wasConquered;
        }

        public boolean wasConquered() {
            return wasConquered;
        }
    }

    public static class HealthChanged extends ClientClaimEvent {
        public static final EventBus<ClientClaimEvent.HealthChanged> BUS = EventBus.create(ClientClaimEvent.HealthChanged.class);
        private final int previousHealth;
        private final int newHealth;

        public HealthChanged(RecruitsClaim claim, int previousHealth, int newHealth) {
            super(claim);
            this.previousHealth = previousHealth;
            this.newHealth = newHealth;
        }

        public int getPreviousHealth() { return previousHealth; }
        public int getNewHealth() { return newHealth; }

        public boolean isDamage() { return newHealth < previousHealth; }
    }
}
