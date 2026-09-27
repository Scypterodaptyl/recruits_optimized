package com.talhanation.recruits.util;

import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;

import javax.annotation.Nullable;

final class ClientRegistries {

    private ClientRegistries() {
    }

    @Nullable
    static HolderLookup.Provider get() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft != null && minecraft.level != null ? minecraft.level.registryAccess() : null;
    }
}
