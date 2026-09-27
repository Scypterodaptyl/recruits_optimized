package com.talhanation.recruits.entities;

import com.talhanation.recruits.config.RecruitsServerConfig;
import com.talhanation.recruits.entities.ai.HorsemanAttackAI;
import com.talhanation.recruits.pathfinding.AsyncGroundPathNavigation;
import net.minecraft.util.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.animal.equine.Markings;
import net.minecraft.world.entity.animal.equine.Variant;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.common.ForgeMod;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class HorsemanEntity extends RecruitShieldmanEntity {

    private final Predicate<ItemEntity> ALLOWED_ITEMS = (item) ->
            (!item.hasPickUpDelay() && item.isAlive() && getInventory().canAddItem(item.getItem()) && this.wantsToPickUp(item.getItem()));
    private static final EntityDataAccessor<Boolean> HAD_HORSE = SynchedEntityData.defineId(HorsemanEntity.class, EntityDataSerializers.BOOLEAN);

    public boolean isPatrol = false;

    public HorsemanEntity(EntityType<? extends AbstractRecruitEntity> entityType, Level world) {
        super(entityType, world);
    }

    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(HAD_HORSE, false);
    }

    @Override
    public void saveRecruitData(CompoundTag nbt) {
        super.saveRecruitData(nbt);
        nbt.putBoolean("hadHorse", this.getHadHorse());
    }

    @Override
    public void loadRecruitData(CompoundTag nbt) {
        super.loadRecruitData(nbt);
        this.setHadHorse(nbt.getBooleanOr("hadHorse", false));
    }

    private void setHadHorse(boolean hadHorse) {
        entityData.set(HAD_HORSE, hadHorse);
    }

    private boolean getHadHorse() {
        return entityData.get(HAD_HORSE);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new HorsemanAttackAI(this));
    }

    //ATTRIBUTES
    public static AttributeSupplier.Builder setAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.35D)
                .add(ForgeMod.SWIM_SPEED.getHolder().get(), 0.3D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.05D)
                .add(Attributes.ATTACK_DAMAGE, 1.0D)
                .add(Attributes.FOLLOW_RANGE, 64.0D)
                .add(Attributes.ENTITY_INTERACTION_RANGE, 0D)
                .add(Attributes.ATTACK_SPEED);

    }

    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficultyInstance, EntitySpawnReason reason, @Nullable SpawnGroupData data) {
        RandomSource randomsource = world.getRandom();
        SpawnGroupData ilivingentitydata = super.finalizeSpawn(world, difficultyInstance, reason, data);
        ((AsyncGroundPathNavigation)this.getNavigation()).setCanOpenDoors(true);
        this.populateDefaultEquipmentEnchantments(world, randomsource, difficultyInstance);

        this.initSpawn();

        return ilivingentitydata;
    }

    @Override
    public void initSpawn() {
        this.setCustomName(Component.literal("Horseman"));
        this.setCost(RecruitsServerConfig.HorsemanCost.get());

        this.setEquipment();
        this.setRandomSpawnBonus();
        this.setPersistenceRequired();

        AbstractRecruitEntity.applySpawnValues(this);
    }

    @Override
    public void tick() {
        super.tick();

        if (!getHadHorse() && (RecruitsServerConfig.RecruitHorseUnitsHorse.get() || isPatrol)){
            boolean hasHorse = this.getVehicle() != null && this.getVehicle() instanceof AbstractHorse;
            if (!hasHorse){
                boolean isDesert = this.getBiome() == 0;
                boolean spawnCamel = isDesert && this.random.nextInt(2) == 0;

                if (spawnCamel) {
                    Camel camel = new Camel(EntityType.CAMEL, this.level());
                    camel.setPos(this.getX(), this.getY(), this.getZ());
                    camel.setTamed(true);
                    camel.setItemSlot(net.minecraft.world.entity.EquipmentSlot.SADDLE, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.SADDLE));
                    this.startRiding(camel);
                    this.level().addFreshEntity(camel);
                    this.setHadHorse(true);
                    this.setMountUUID(Optional.of(camel.getUUID()));
                } else {
                    Horse horse = new Horse(EntityType.HORSE, this.level());
                    horse.setPos(this.getX(), this.getY(), this.getZ());
                    horse.setTamed(true);
                    horse.setItemSlot(net.minecraft.world.entity.EquipmentSlot.SADDLE, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.SADDLE));

                    Variant variant = Util.getRandom(Variant.values(), this.random);
                    Markings markings = Util.getRandom(Markings.values(), this.random);
                    horse.setVariantAndMarkings(variant, markings);

                    this.startRiding(horse);
                    this.level().addFreshEntity(horse);
                    this.setHadHorse(true);
                    this.setMountUUID(Optional.of(horse.getUUID()));
                }
            }
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if(this.getVehicle() instanceof AbstractHorse abstractHorse){
            abstractHorse.setDeltaMovement(abstractHorse.getDeltaMovement().add(this.getDeltaMovement().scale(2)));
        }
    }

    @Override
    public Predicate<ItemEntity> getAllowedItems() {
        return ALLOWED_ITEMS;
    }

    public enum State {
        //IDLE,
        SELECT_TARGET,
        CHARGE_TARGET,
        MOVE_TO_POS
    }

    public List<List<String>> getEquipment(){
        return RecruitsServerConfig.HorsemanStartEquipments.get();
    }
}
