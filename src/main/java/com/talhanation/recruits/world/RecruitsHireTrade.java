package com.talhanation.recruits.world;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public class RecruitsHireTrade {

    public int cost;
    public int maxUses;
    public int uses;
    public Identifier resourceLocation;
    public Component title;
    public Component description;
    public RecruitsHireTrade(Identifier resourceLocation, int cost) {
        this(resourceLocation, cost, Component.empty(),  Component.empty());
    }
    public RecruitsHireTrade(Identifier resourceLocation, int cost, Component title, Component description){
        this.resourceLocation = resourceLocation;
        this.cost = cost;
        this.maxUses = 1;
        this.uses = maxUses;
        this.title = title;
        this.description = description;
    }

    public RecruitsHireTrade copy(){
        return new RecruitsHireTrade(this.resourceLocation, this.cost, this.title, this.description);
    }

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("cost", cost);
        tag.putString("recruitType", resourceLocation.toString());
        tag.putInt("maxUses", maxUses);
        tag.putInt("uses", uses);

        return tag;
    }

    public static RecruitsHireTrade fromNbt(CompoundTag tag) {
        int cost = tag.getIntOr("cost", 0);
        Identifier recruitType = Identifier.parse(tag.getStringOr("recruitType", ""));
        int maxUses = tag.getIntOr("maxUses", 0);
        int uses = tag.getIntOr("uses", 0);

        RecruitsHireTrade trade = new RecruitsHireTrade(recruitType, cost);
        trade.maxUses = maxUses;
        trade.uses = uses;
        return trade;
    }
    public static CompoundTag listToNbt(List<RecruitsHireTrade> trades) {
        CompoundTag compound = new CompoundTag();
        if (trades == null) return compound;

        ListTag list = new ListTag();
        for (RecruitsHireTrade t : trades) {
            list.add(t.toNbt());
        }
        compound.put("Trades", list);
        return compound;
    }

    /** liest Liste aus CompoundTag */
    public static List<RecruitsHireTrade> listFromNbt(CompoundTag compound) {
        List<RecruitsHireTrade> out = new ArrayList<>();
        if (compound == null || !compound.contains("Trades")) {
            return out;
        }

        ListTag list = compound.getListOrEmpty("Trades");
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompoundOrEmpty(i);
            out.add(RecruitsHireTrade.fromNbt(entry));
        }
        return out;
    }
}


