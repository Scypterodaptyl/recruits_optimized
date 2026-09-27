package com.talhanation.recruits.world;

import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.UUID;

public class RecruitsWorldSaveData extends SavedData {
    private static final String FILE_ID = "recruitsWorldId";
    private UUID worldId;

    public RecruitsWorldSaveData() {
        this.worldId = UUID.randomUUID();
        this.setDirty();
    }

    private RecruitsWorldSaveData(UUID worldId) {
        this.worldId = worldId == null ? UUID.randomUUID() : worldId;
        if (worldId == null) this.setDirty();
    }

    public static RecruitsWorldSaveData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public static RecruitsWorldSaveData load(CompoundTag nbt) {
        UUID worldId = nbt.read("WorldId", UUIDUtil.CODEC).isPresent() ? nbt.read("WorldId", UUIDUtil.CODEC).orElse(null) : null;
        return new RecruitsWorldSaveData(worldId);
    }

        public CompoundTag save(CompoundTag nbt) {
        nbt.store("WorldId", UUIDUtil.CODEC, worldId);
        return nbt;
    }

    public UUID getWorldId() {
        return worldId;
    }

    public static final net.minecraft.world.level.saveddata.SavedDataType<RecruitsWorldSaveData> TYPE = com.talhanation.recruits.util.SavedDataUtil.type(FILE_ID, RecruitsWorldSaveData::new, RecruitsWorldSaveData::load, data -> data.save(new CompoundTag()));
}
