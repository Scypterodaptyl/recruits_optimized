package com.talhanation.recruits.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.function.Function;
import java.util.function.Supplier;

public final class SavedDataUtil {

    private SavedDataUtil() {
    }

    /**
     * Builds a SavedDataType that keeps using the old CompoundTag based load/save methods.
     */
    public static <T extends SavedData> SavedDataType<T> type(String id, Supplier<T> constructor, Function<CompoundTag, T> load, Function<T, CompoundTag> save) {
        return new SavedDataType<>(id, constructor, CompoundTag.CODEC.xmap(load, save), null);
    }
}
