package com.talhanation.recruits.util;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Helpers to keep the old CompoundTag based save format working on top of the
 * ValueInput/ValueOutput and codec based APIs introduced in newer Minecraft versions.
 */
public final class NbtCompat {

    private static final MapCodec<CompoundTag> WHOLE_TAG = MapCodec.assumeMapUnsafe(CompoundTag.CODEC);

    private NbtCompat() {
    }

    public static CompoundTag readAll(ValueInput input) {
        return input.read(WHOLE_TAG).orElseGet(CompoundTag::new);
    }

    public static void writeAll(ValueOutput output, CompoundTag tag) {
        output.store(WHOLE_TAG, tag);
    }

    /**
     * Best effort registry lookup for code paths that have no level at hand.
     */
    public static HolderLookup.Provider registries() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) return server.registryAccess();
        if (FMLEnvironment.dist == Dist.CLIENT) {
            HolderLookup.Provider client = ClientRegistries.get();
            if (client != null) return client;
        }
        return VanillaRegistries.createLookup();
    }

    public static CompoundTag saveItem(ItemStack stack) {
        return saveItem(registries(), stack, new CompoundTag());
    }

    public static CompoundTag saveItem(ItemStack stack, CompoundTag into) {
        return saveItem(registries(), stack, into);
    }

    public static ItemStack loadItem(@Nullable Tag tag) {
        return loadItem(registries(), tag);
    }

    public static CompoundTag saveItem(HolderLookup.Provider provider, ItemStack stack) {
        return saveItem(provider, stack, new CompoundTag());
    }

    public static CompoundTag saveItem(HolderLookup.Provider provider, ItemStack stack, CompoundTag into) {
        if (stack.isEmpty()) return into;
        Tag encoded = ItemStack.CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), stack).result().orElse(null);
        if (encoded instanceof CompoundTag compound) {
            into.merge(compound);
        }
        return into;
    }

    public static ItemStack loadItem(HolderLookup.Provider provider, @Nullable Tag tag) {
        if (!(tag instanceof CompoundTag compound) || compound.isEmpty()) return ItemStack.EMPTY;
        return ItemStack.CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), compound).result().orElse(ItemStack.EMPTY);
    }

    @Nullable
    public static UUID getUUID(CompoundTag tag, String key) {
        return tag.read(key, UUIDUtil.CODEC).orElse(null);
    }

    public static boolean hasUUID(CompoundTag tag, String key) {
        return tag.read(key, UUIDUtil.CODEC).isPresent();
    }

    public static void putUUID(CompoundTag tag, String key, UUID uuid) {
        tag.store(key, UUIDUtil.CODEC, uuid);
    }

    public static Tag saveAttributes(AttributeMap attributes) {
        return AttributeInstance.Packed.LIST_CODEC.encodeStart(registries().createSerializationContext(NbtOps.INSTANCE), attributes.pack())
                .result().orElseGet(ListTag::new);
    }

    public static void loadAttributes(AttributeMap attributes, @Nullable Tag tag) {
        if (tag == null) return;
        AttributeInstance.Packed.LIST_CODEC.parse(registries().createSerializationContext(NbtOps.INSTANCE), tag)
                .result().ifPresent(attributes::apply);
    }

    public static void putComponent(CompoundTag tag, String key, Component component) {
        tag.store(key, ComponentSerialization.CODEC, registries().createSerializationContext(NbtOps.INSTANCE), component);
    }

    @Nullable
    public static Component getComponent(CompoundTag tag, String key) {
        return tag.read(key, ComponentSerialization.CODEC, registries().createSerializationContext(NbtOps.INSTANCE)).orElse(null);
    }

    /**
     * Old item stacks kept their extra data in the stack NBT, it lives in the custom data component now.
     */
    @Nullable
    public static CompoundTag getCustomTag(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data == null ? null : data.copyTag();
    }

    public static void setCustomTag(ItemStack stack, @Nullable CompoundTag tag) {
        if (tag == null || tag.isEmpty()) stack.remove(DataComponents.CUSTOM_DATA);
        else stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
}
