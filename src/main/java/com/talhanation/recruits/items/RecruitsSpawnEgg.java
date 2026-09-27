package com.talhanation.recruits.items;

import net.minecraft.core.UUIDUtil;
import com.talhanation.recruits.FactionEvents;
import com.talhanation.recruits.Main;
import com.talhanation.recruits.RecruitEvents;
import com.talhanation.recruits.entities.AbstractRecruitEntity;
import com.talhanation.recruits.entities.ICompanion;
import com.talhanation.recruits.entities.IHasTargetPriority;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.SpawnEggItem;
import com.talhanation.recruits.util.NbtCompat;
import net.minecraft.world.phys.Vec3;

import net.minecraft.world.scores.PlayerTeam;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;


public class RecruitsSpawnEgg extends SpawnEggItem {
    private final Supplier<? extends EntityType<? extends AbstractRecruitEntity>> entityType;

    private static final EquipmentSlot[] ARMOR_SLOTS = {EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD};

    public RecruitsSpawnEgg(Supplier<? extends EntityType<? extends AbstractRecruitEntity>> entityType, Properties properties) {
        super(properties.spawnEgg(entityType.get()));
        this.entityType = entityType;
    }

    public @NotNull EntityType<?> getType(CompoundTag compound){
        if(compound != null && compound.contains("EntityTag")) {
            CompoundTag entityTag = compound.getCompoundOrEmpty("EntityTag");

            if(entityTag.contains("id")) {
                return EntityType.byString(entityTag.getStringOr("id", "")).orElse(this.entityType.get());
            }
        }
        return this.entityType.get();
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        if (world.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        ItemStack stack = context.getItemInHand();
        CompoundTag itemTag = NbtCompat.getCustomTag(stack);
        if (itemTag == null || itemTag.getCompoundOrEmpty("EntityTag").isEmpty()) {
            return super.useOn(context);
        }

        BlockPos spawnPos = context.getClickedPos().relative(context.getClickedFace());
        AbstractRecruitEntity recruit = spawnRecruitCopy((ServerLevel) world, this.getType(itemTag), itemTag, spawnPos);
        if (recruit == null) {
            return InteractionResult.FAIL;
        }

        Player player = context.getPlayer();
        if (player == null || !player.isCreative()) {
            stack.shrink(1);
        }
        return InteractionResult.SUCCESS;
    }

    @Nullable
    public static AbstractRecruitEntity spawnRecruitCopy(ServerLevel level, EntityType<?> entityType, CompoundTag itemTag, BlockPos spawnPos) {
        Entity entity = entityType.create(level, net.minecraft.world.entity.EntitySpawnReason.SPAWN_ITEM_USE);
        if (!(entity instanceof AbstractRecruitEntity recruit)) {
            return null;
        }

        recruit.snapTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, 0.0F, 0.0F);
        recruit.finalizeSpawn(level, level.getCurrentDifficultyAt(spawnPos), EntitySpawnReason.SPAWN_ITEM_USE, null);
        fillRecruit(recruit, itemTag, spawnPos);

        if (!level.addFreshEntity(recruit)) {
            return null;
        }

        registerSpawnedCopy(level, recruit, itemTag.getCompoundOrEmpty("EntityTag"));
        return recruit;
    }

    public static void fillRecruit(AbstractRecruitEntity recruit, CompoundTag entityTag, BlockPos pos){
        CompoundTag nbt = entityTag.getCompoundOrEmpty("EntityTag");

        if(nbt.isEmpty()) return;

        if (nbt.contains("CustomName")) {
            try {
                Component customName = NbtCompat.getComponent(nbt, "CustomName");
                if (customName != null) recruit.setCustomName(customName);
            } catch (RuntimeException exception) {
                Main.LOGGER.warn("Could not read copied recruit name", exception);
            }
        } else if (nbt.contains("Name")) {
            recruit.setCustomName(Component.literal(nbt.getStringOr("Name", "")));
        }

        recruit.setXpLevel(nbt.getIntOr("Level", 0));
        recruit.setAggroState(nbt.getIntOr("AggroState", 0));
        recruit.setShouldFollow(nbt.getBooleanOr("ShouldFollow", false));
        recruit.setShouldBlock(nbt.getBooleanOr("ShouldBlock", false));
        recruit.setShouldRest(nbt.getBooleanOr("ShouldRest", false));
        recruit.setShouldRanged(nbt.getBooleanOr("ShouldRanged", false));
        recruit.setListen(nbt.getBooleanOr("Listen", false));
        recruit.setXp(nbt.getIntOr("Xp", 0));
        recruit.setKills(nbt.getIntOr("Kills", 0));
        recruit.setVariant(nbt.getIntOr("Variant", 0));
        recruit.setHunger(nbt.getFloatOr("Hunger", 0.0F));
        recruit.setMoral(nbt.getFloatOr("Moral", 0.0F));
        recruit.setCost(nbt.getIntOr("Cost", 0));
        recruit.setColor(nbt.getByteOr("Color", (byte) 0));
        recruit.setBiome(nbt.getByteOr("Biome", (byte) 0));

        if (nbt.contains("Attributes")) {
            NbtCompat.loadAttributes(recruit.getAttributes(), nbt.get("Attributes"));
        }

        recruit.setShouldMount(false);
        recruit.setMountUUID(Optional.empty());
        recruit.setShouldProtect(false);
        recruit.setProtectUUID(Optional.empty());
        recruit.setFleeing(false);
        recruit.setIsFollowing(false);
        recruit.setTarget(null);

        if (nbt.read("OwnerUUID", UUIDUtil.CODEC).isPresent()) {
            recruit.setOwnerUUID(Optional.of(nbt.read("OwnerUUID", UUIDUtil.CODEC).orElse(null)));
        } else {
            recruit.setOwnerUUID(Optional.empty());
        }
        recruit.setIsOwned(nbt.getBooleanOr("isOwned", false) && recruit.getOwnerUUID() != null);

        int followState = nbt.getIntOr("FollowState", 0);
        if (followState == 4) followState = 3;
        if (followState == 5) followState = 2;
        recruit.setFollowState(followState);

        if(nbt.contains("Group")){
            Tag tag = nbt.get("Group");
            if (tag != null && tag.getId() == Tag.TAG_INT) {
                if(recruit.getOwner() != null){
                    RecruitEvents.handleGroupBackwardCompatibility(recruit, nbt.getIntOr("Group", 0));
                }
                else recruit.setGroupUUID(null);
            }
            else if (nbt.read("Group", UUIDUtil.CODEC).isPresent()){
                recruit.setGroupUUID(nbt.read("Group", UUIDUtil.CODEC).orElse(null));
            }
        } else {
            recruit.setGroupUUID(null);
        }

        if (nbt.read("UpkeepUUID", UUIDUtil.CODEC).isPresent()){
            recruit.setUpkeepUUID(Optional.of(nbt.read("UpkeepUUID", UUIDUtil.CODEC).orElse(null)));
        } else {
            recruit.setUpkeepUUID(Optional.empty());
        }

        if (nbt.contains("UpkeepPosX") && nbt.contains("UpkeepPosY") && nbt.contains("UpkeepPosZ")) {
            recruit.setUpkeepPos(new BlockPos (
                    nbt.getIntOr("UpkeepPosX", 0),
                    nbt.getIntOr("UpkeepPosY", 0),
                    nbt.getIntOr("UpkeepPosZ", 0)));
        }

        if (recruit.getShouldHoldPos()) {
            recruit.setHoldPos(Vec3.atCenterOf(pos));
        }

        if (recruit instanceof ICompanion companion && nbt.contains("CompanionOwnerName")) {
            companion.setOwnerName(nbt.getStringOr("CompanionOwnerName", ""));
        }
        if (recruit instanceof IHasTargetPriority priorityRecruit && nbt.contains("TargetPriority")) {
            try {
                priorityRecruit.setTargetPriority(IHasTargetPriority.TargetPriority.fromIndex(nbt.getIntOr("TargetPriority", 0)));
            } catch (IllegalArgumentException ignored) {
                priorityRecruit.setTargetPriority(IHasTargetPriority.TargetPriority.CLOSEST);
            }
        }

        recruit.inventory.clearContent();
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            recruit.setItemSlot(slot, ItemStack.EMPTY);
        }
        recruit.setPersistenceRequired();

        ListTag listnbt = nbt.getListOrEmpty("Items");//muss 10 sein amk sonst nix save
        for (int i = 0; i < listnbt.size(); ++i) {
            CompoundTag compoundnbt = listnbt.getCompoundOrEmpty(i);
            int j = compoundnbt.getByteOr("Slot", (byte) 0) & 255;
            if (j < recruit.inventory.getContainerSize()) {
                recruit.inventory.setItem(j, com.talhanation.recruits.util.NbtCompat.loadItem(compoundnbt));
            }
        }

        ListTag armorItems = nbt.getListOrEmpty("ArmorItems");
        for (int i = 0; i < armorItems.size() && i < ARMOR_SLOTS.length; i++) {
            ItemStack item = com.talhanation.recruits.util.NbtCompat.loadItem(armorItems.getCompoundOrEmpty(i));
            if (!item.isEmpty()) {
                recruit.setItemSlot(ARMOR_SLOTS[i], item);
            }
        }

        ListTag handItems = nbt.getListOrEmpty("HandItems");
        for (int i = 0; i < handItems.size() && i < 2; i++) {
            ItemStack item = com.talhanation.recruits.util.NbtCompat.loadItem(handItems.getCompoundOrEmpty(i));
            if (!item.isEmpty()) {
                recruit.setItemSlot(i == 0 ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND, item);
            }
        }

        recruit.setHealth(recruit.getMaxHealth());
    }

    private static void registerSpawnedCopy(ServerLevel level, AbstractRecruitEntity recruit, CompoundTag nbt) {
        if (nbt.contains("Team")) {
            String teamName = nbt.getStringOr("Team", "");
            PlayerTeam team = level.getScoreboard().getPlayerTeam(teamName);
            if (team != null) {
                FactionEvents.addRecruitToTeam(recruit, team, level);
                if (FactionEvents.recruitsFactionManager != null
                        && FactionEvents.recruitsFactionManager.getFactionByStringID(teamName) != null) {
                    FactionEvents.addNPCToData(level, teamName, 1);
                }
            } else {
                Main.LOGGER.warn("Unable to add copied recruit to missing team \"{}\" (that team probably doesn't exist)", teamName);
            }
        }

        if (recruit.getGroup() != null && RecruitEvents.recruitsGroupsManager != null) {
            RecruitEvents.recruitsGroupsManager.addMember(recruit.getGroup(), recruit.getUUID(), level);
        }

        UUID ownerId = recruit.getOwnerUUID();
        if (recruit.isOwned() && ownerId != null && RecruitEvents.recruitsPlayerUnitManager != null) {
            RecruitEvents.recruitsPlayerUnitManager.addRecruits(ownerId, 1);
            Player owner = level.getPlayerByUUID(ownerId);
            if (owner != null) {
                RecruitEvents.recruitsPlayerUnitManager.broadCastUnitInfoToPlayer(owner);
            }
        }
    }
}
