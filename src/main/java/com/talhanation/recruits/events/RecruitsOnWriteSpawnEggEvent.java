package com.talhanation.recruits.events;

import com.talhanation.recruits.entities.AbstractRecruitEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.eventbus.api.bus.CancellableEventBus;
import net.minecraftforge.eventbus.api.bus.EventBus;
import net.minecraftforge.eventbus.api.event.MutableEvent;
import net.minecraftforge.eventbus.api.event.characteristic.Cancellable;
public class RecruitsOnWriteSpawnEggEvent extends MutableEvent {
    public static final EventBus<RecruitsOnWriteSpawnEggEvent> BUS = EventBus.create(RecruitsOnWriteSpawnEggEvent.class);
    public final AbstractRecruitEntity recruit;
    public final CompoundTag tag;

    public RecruitsOnWriteSpawnEggEvent(AbstractRecruitEntity recruit, CompoundTag tag) {
        this.recruit = recruit;
        this.tag = tag;
    }
}
