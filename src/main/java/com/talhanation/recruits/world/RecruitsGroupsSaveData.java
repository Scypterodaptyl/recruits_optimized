package com.talhanation.recruits.world;

import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

public class RecruitsGroupsSaveData extends SavedData {
    private static final String FILE_ID = "recruitsGroups";
    private List<RecruitsGroup> groups = new ArrayList<>();
    private Map<UUID, UUID> redirects = new HashMap<>();
    private Map<UUID, UUID> recruitRedirects = new HashMap<>();
    public static RecruitsGroupsSaveData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public static RecruitsGroupsSaveData load(CompoundTag nbt) {
        RecruitsGroupsSaveData data = new RecruitsGroupsSaveData();
        if (nbt.contains("groups")) {
            ListTag list = nbt.getListOrEmpty("groups");
            for (Tag t : list) {
                data.groups.add(RecruitsGroup.fromNBT((CompoundTag) t));
            }
        }

        if (nbt.contains("redirects")) {
            ListTag redirectList = nbt.getListOrEmpty("redirects");
            for (Tag t : redirectList) {
                CompoundTag tag = (CompoundTag) t;
                UUID oldId = tag.read("old", UUIDUtil.CODEC).orElse(null);
                UUID newId = tag.read("new", UUIDUtil.CODEC).orElse(null);
                data.redirects.put(oldId, newId);
            }
        }

        if (nbt.contains("recruitRedirects")) {
            ListTag recruitRedirectList = nbt.getListOrEmpty("recruitRedirects");
            for (Tag t : recruitRedirectList) {
                CompoundTag tag = (CompoundTag) t;
                UUID oldId = tag.read("recruit", UUIDUtil.CODEC).orElse(null);
                UUID newId = tag.read("group", UUIDUtil.CODEC).orElse(null);
                data.recruitRedirects.put(oldId, newId);
            }
        }

        return data;
    }

        public CompoundTag save(CompoundTag nbt) {
        ListTag list = new ListTag();
        for (RecruitsGroup group : this.groups) {
            list.add(group.toNBT());
        }
        nbt.put("groups", list);

        ListTag redirectList = new ListTag();
        for (Map.Entry<UUID, UUID> e : redirects.entrySet()) {
            CompoundTag tag = new CompoundTag();
            tag.store("old", UUIDUtil.CODEC, e.getKey());
            tag.store("new", UUIDUtil.CODEC, e.getValue());
            redirectList.add(tag);
        }
        nbt.put("redirects", redirectList);

        ListTag recruitRedirectList = new ListTag();
        for (Map.Entry<UUID, UUID> e : recruitRedirects.entrySet()) {
            CompoundTag tag = new CompoundTag();
            tag.store("recruit", UUIDUtil.CODEC, e.getKey());
            tag.store("group", UUIDUtil.CODEC, e.getValue());
            recruitRedirectList.add(tag);
        }
        nbt.put("recruitRedirects", recruitRedirectList);

        return nbt;
    }

    public List<RecruitsGroup> getAllGroups() {
        return this.groups;
    }

    public void setAllGroups(List<RecruitsGroup> groups) {
        this.groups = groups;
    }

    public Map<UUID, UUID> getRedirects() {
        return redirects;
    }

    public Map<UUID, UUID> getRecruitRedirects() {
        return recruitRedirects;
    }

    public void setRedirects(Map<UUID, UUID> map){
        this.redirects = map;
    }

    public void setRecruitRedirects(Map<UUID, UUID> map){
        this.recruitRedirects = map;
    }

    public static final net.minecraft.world.level.saveddata.SavedDataType<RecruitsGroupsSaveData> TYPE = com.talhanation.recruits.util.SavedDataUtil.type(FILE_ID, RecruitsGroupsSaveData::new, RecruitsGroupsSaveData::load, data -> data.save(new CompoundTag()));
}
