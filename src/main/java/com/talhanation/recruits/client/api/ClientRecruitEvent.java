package com.talhanation.recruits.client.api;

import com.talhanation.recruits.entities.AbstractRecruitEntity;
import com.talhanation.recruits.world.RecruitsFaction;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.bus.CancellableEventBus;
import net.minecraftforge.eventbus.api.bus.EventBus;
import net.minecraftforge.eventbus.api.event.MutableEvent;
import net.minecraftforge.eventbus.api.event.characteristic.Cancellable;

import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public abstract class ClientRecruitEvent extends MutableEvent {

    private final AbstractRecruitEntity recruit;

    protected ClientRecruitEvent(AbstractRecruitEntity recruit) {
        this.recruit = recruit;
    }

    public AbstractRecruitEntity getRecruit() { return recruit; }

    public static class LevelUp extends ClientRecruitEvent {
        public static final EventBus<ClientRecruitEvent.LevelUp> BUS = EventBus.create(ClientRecruitEvent.LevelUp.class);
        private final int newLevel;

        public LevelUp(AbstractRecruitEntity recruit, int newLevel) {
            super(recruit);
            this.newLevel = newLevel;
        }

        public int getNewLevel() { return newLevel; }
    }

    public static class Died extends ClientRecruitEvent {
        public static final EventBus<ClientRecruitEvent.Died> BUS = EventBus.create(ClientRecruitEvent.Died.class);
        @Nullable
        private final Player owner;

        public Died(AbstractRecruitEntity recruit, @Nullable Player owner) {
            super(recruit);
            this.owner = owner;
        }

        @Nullable
        public Player getOwner() { return owner; }
    }


    public static class Spawned extends ClientRecruitEvent {
        public static final EventBus<ClientRecruitEvent.Spawned> BUS = EventBus.create(ClientRecruitEvent.Spawned.class);
        public Spawned(AbstractRecruitEntity recruit) {
            super(recruit);
        }
    }


    public static class FactionChanged extends ClientRecruitEvent {
        public static final EventBus<ClientRecruitEvent.FactionChanged> BUS = EventBus.create(ClientRecruitEvent.FactionChanged.class);
        @Nullable private final RecruitsFaction previousFaction;
        @Nullable private final RecruitsFaction newFaction;

        public FactionChanged(AbstractRecruitEntity recruit,
                               @Nullable RecruitsFaction previousFaction,
                               @Nullable RecruitsFaction newFaction) {
            super(recruit);
            this.previousFaction = previousFaction;
            this.newFaction = newFaction;
        }

        @Nullable public RecruitsFaction getPreviousFaction() { return previousFaction; }
        @Nullable public RecruitsFaction getNewFaction() { return newFaction; }
    }
}
