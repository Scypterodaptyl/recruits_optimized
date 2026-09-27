package com.talhanation.recruits.entities;

import net.minecraft.world.entity.monster.illager.AbstractIllager;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import com.talhanation.recruits.entities.ai.FleeFire;
import com.talhanation.recruits.entities.ai.FleeTNT;
import com.talhanation.recruits.entities.ai.FleeTarget;
import com.talhanation.recruits.entities.ai.UseShield;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

import javax.annotation.Nullable;
import java.util.Optional;

public abstract class AbstractOrderAbleEntity extends AbstractInventoryEntity{
    private static final EntityDataAccessor<Integer> DATA_REMAINING_ANGER_TIME = SynchedEntityData.defineId(AbstractOrderAbleEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Optional<BlockPos>> HOME_POS = SynchedEntityData.defineId(AbstractOrderAbleEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
    private static final EntityDataAccessor<Integer> XP = SynchedEntityData.defineId(AbstractOrderAbleEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> LEVEL = SynchedEntityData.defineId(AbstractOrderAbleEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> KILLS = SynchedEntityData.defineId(AbstractOrderAbleEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> isEating = SynchedEntityData.defineId(AbstractOrderAbleEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> isInOrder = SynchedEntityData.defineId(AbstractOrderAbleEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> FLEEING = SynchedEntityData.defineId(AbstractOrderAbleEntity.class, EntityDataSerializers.BOOLEAN);

    //private static final DataParameter<ItemStack> OFFHAND_ITEM_SAVE = EntityDataManager.defineId(AbstractRecruitEntity.class, DataSerializers.ITEM_STACK);

    public ItemStack beforeFoodItem;

    public AbstractOrderAbleEntity(EntityType<? extends AbstractOrderAbleEntity> entityType, Level world) {
        super(entityType, world);
        //this.setOwned(false);
        this.xpReward = 12;
    }

    ///////////////////////////////////TICK/////////////////////////////////////////

    public double getMyRidingOffset() {
        return -0.35D;
    }

    @Override
    public void aiStep() {
        super.aiStep();
    }

    public void tick() {
        super.tick();
        updateSwingTime();
        updateSwimming();

        if (this.getIsEating() && !this.isUsingItem()) {
            if (beforeFoodItem != null) resetItemInHand();
            setIsEating(false);
        }

    }

    public void rideTick() {
        super.rideTick();
    }

    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance diff, EntitySpawnReason reason, @Nullable SpawnGroupData spawnData) {
        return spawnData;
    }


    public void setDropEquipment(){
        if (this.level() instanceof ServerLevel serverLevel) this.dropEquipment(serverLevel);
    }

    ////////////////////////////////////REGISTER////////////////////////////////////

    protected void registerGoals() {
        //this.goalSelector.addGoal(0, new RecruitQuaffGoal(this));

        this.goalSelector.addGoal(0, new FleeTNT(this));
        this.goalSelector.addGoal(0, new FleeTarget(this));
        this.goalSelector.addGoal(0, new FleeFire(this));
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(1, new UseShield(this));
        //this.goalSelector.addGoal(1, new RecruitEatGoal(this));
        this.goalSelector.addGoal(5, new MeleeAttackGoal(this, 1.15D, true));
        this.goalSelector.addGoal(7, new MoveTowardsTargetGoal(this, 1.15D, 32.0F));
        //this.goalSelector.addGoal(8, new RecruitPickupWantedItemGoal(this));
        this.goalSelector.addGoal(11, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(12, new RandomLookAroundGoal(this));
        /*
        this.targetSelector.addGoal(0, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(1, (new HurtByTargetGoal(this)).setAlertOthers());
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));


         */
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, net.minecraft.world.entity.monster.illager.AbstractIllager.class, false));
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Monster.class, false));
    }

    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_REMAINING_ANGER_TIME, 0);
        builder.define(FLEEING, false);
        builder.define(XP, 0);
        builder.define(KILLS, 0);
        builder.define(LEVEL, 1);
        builder.define(isEating, true);
        builder.define(isInOrder,false);
        //IS IN ORDER
        //IS LEADER
        //UUID OPFER optional

    }
    @Override
    public void saveRecruitData(CompoundTag nbt) {
        super.saveRecruitData(nbt);
        nbt.putBoolean("isInOrder", this.getIsInOrder());
        nbt.putBoolean("Fleeing", this.getFleeing());
        nbt.putBoolean("isEating", this.getIsEating());
        nbt.putInt("Xp", this.getXp());
        nbt.putInt("Level", this.getXpLevel());
        nbt.putInt("Kills", this.getKills());

    }

    @Override
    public void loadRecruitData(CompoundTag nbt) {
        super.loadRecruitData(nbt);
        this.setIsInOrder(nbt.getBooleanOr("isInOrder", false));
        this.setXpLevel(nbt.getIntOr("Level", 0));
        this.setFleeing(nbt.getBooleanOr("Fleeing", false));
        this.setIsEating(nbt.getBooleanOr("isEating", false));
        this.setXp(nbt.getIntOr("Xp", 0));
        this.setKills(nbt.getIntOr("Kills", 0));
    }


    ////////////////////////////////////GET////////////////////////////////////

    public boolean getIsInOrder() {
        return entityData.get(isInOrder);
    }

    public BlockPos getAssassinOnPos(){
        return getOnPos();
    }

    public boolean getFleeing() {
        return entityData.get(FLEEING);
    }

    public int getKills() {
        return entityData.get(KILLS);
    }

    public int getXpLevel() {
        return entityData.get(LEVEL);
    }

    public int getXp() {
        return entityData.get(XP);
    }

    public boolean getIsEating() {
        return entityData.get(isEating);
    }


    public SoundEvent getHurtSound(DamageSource ds) {
        if (this.isBlocking())
            return SoundEvents.SHIELD_BLOCK.value();
        return SoundEvents.VILLAGER_HURT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.VILLAGER_DEATH;
    }

    protected float getSoundVolume() {
        return 0.4F;
    }


    public int getMaxHeadXRot() {
        return super.getMaxHeadXRot();
    }

    public int getMaxSpawnClusterSize() {
        return 8;
    }

    public BlockPos getHomePos(){
        return entityData.get(HOME_POS).orElse(null);
    }

    ////////////////////////////////////SET////////////////////////////////////

    public void setIsInOrder(boolean bool){
        entityData.set(isInOrder, bool);
    }


    public void setFleeing(boolean bool){
        entityData.set(FLEEING, bool);
    }

    public void addXpLevel(int level){
        int currentLevel = this.getXpLevel();
        int newLevel = currentLevel + level;
        makelevelUpSound();
        this.entityData.set(LEVEL, newLevel);
    }

    public void setKills(int kills){
        this.entityData.set(KILLS, kills);
    }

    public void setXpLevel(int XpLevel){
        this.entityData.set(LEVEL, XpLevel);
    }

    public void setXp(int xp){
        this. entityData.set(XP, xp);
    }

    public void addXp(int xp){
        int currentXp = this.getXp();
        int newXp = currentXp + xp;

        this. entityData.set(XP, newXp);
    }

    public void setIsEating(boolean bool){
        entityData.set(isEating, bool);
    }

    public void setEquipment(){}



    ////////////////////////////////////ATTACK FUNCTIONS////////////////////////////////////

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource dmg, float amt) {
        if (this.isInvulnerableTo(serverLevel, dmg)) {
            return false;
        } else {
            Entity entity = dmg.getEntity();
            if (entity != null && !(entity instanceof Player) && !(entity instanceof AbstractArrow)) {
                amt = (amt + 1.0F) / 2.0F;
            }
            this.addXp(1);
            this.checkLevel();

            return super.hurtServer(serverLevel, dmg, amt);
        }
    }

    public boolean doHurtTarget(Entity entity) {
        return this.level() instanceof ServerLevel serverLevel && this.doHurtTarget(serverLevel, entity);
    }

    @Override
    public boolean doHurtTarget(ServerLevel serverLevel, Entity entity) {
        DamageSource damageSource = this.damageSources().mobAttack(this);
        boolean flag = entity.hurtServer(serverLevel, damageSource, (float)((int)this.getAttributeValue(Attributes.ATTACK_DAMAGE)));
        if (flag) {
            EnchantmentHelper.doPostAttackEffects(serverLevel, entity, damageSource);
        }
        this.addXp(2);
        this.checkLevel();
        this.damageMainHandItem();
        return flag;
    }

    public void addLevelBuffs(){
        int level = getXpLevel();
        if(level <= 10){
            getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(new AttributeModifier(com.talhanation.recruits.util.AttributeUtil.uniqueId("heath_bonus_level"), 3D, AttributeModifier.Operation.ADD_VALUE));
            getAttribute(Attributes.ATTACK_DAMAGE).addPermanentModifier(new AttributeModifier(com.talhanation.recruits.util.AttributeUtil.uniqueId("attack_bonus_level"), 0.15D, AttributeModifier.Operation.ADD_VALUE));
            getAttribute(Attributes.KNOCKBACK_RESISTANCE).addPermanentModifier(new AttributeModifier(com.talhanation.recruits.util.AttributeUtil.uniqueId("knockback_bonus_level"), 0.01D, AttributeModifier.Operation.ADD_VALUE));
            getAttribute(Attributes.MOVEMENT_SPEED).addPermanentModifier(new AttributeModifier(com.talhanation.recruits.util.AttributeUtil.uniqueId("speed_bonus_level"), 0.01D, AttributeModifier.Operation.ADD_VALUE));
        }
        if(level > 10){
            getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(new AttributeModifier(com.talhanation.recruits.util.AttributeUtil.uniqueId("heath_bonus_level"), 2D, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    public boolean wantsToAttack(LivingEntity target, LivingEntity owner) {
        if (!(target instanceof Creeper) && !(target instanceof Ghast)) {
            if (target instanceof AbstractOrderAbleEntity) {
                AbstractOrderAbleEntity otherRecruit = (AbstractOrderAbleEntity)target;
                return false;// otherRecruit.getOwner() != owner;
            } else if (target instanceof Player && owner instanceof Player && !((Player)owner).canHarmPlayer((Player)target)) {
                return false;
            } else if (target instanceof AbstractHorse && ((AbstractHorse)target).isTamed()) {
                return false;
            } else {
                return !(target instanceof TamableAnimal) || !((TamableAnimal)target).isTame();
            }
        } else {
            return false;
        }
    }


    public void die(DamageSource dmg) {
        super.die(dmg);
    }


    ////////////////////////////////////OTHER FUNCTIONS////////////////////////////////////


    public void checkLevel(){
        int currentXp = this.getXp();
        if (currentXp >= 1000){
            this.addXpLevel(1);
            this.setXp(0);
            this.addLevelBuffs();
            this.heal(10F);
        }
    }

    public void makelevelUpSound() {
        this.level().playSound(null, this.getX(), this.getY() + 4 , this.getZ(), SoundEvents.VILLAGER_YES, this.getSoundSource(), 15.0F, 0.8F + 0.4F * this.random.nextFloat());
        this.level().playSound(null, this.getX(), this.getY() + 4 , this.getZ(), SoundEvents.PLAYER_LEVELUP, this.getSoundSource(), 15.0F, 0.8F + 0.4F * this.random.nextFloat());
    }

    @Override
    public boolean canBeLeashed() {
        return false;
    }



    protected void hurtArmor(DamageSource damageSource, float damage) {
        if (damage >= 0.0F) {
            damage = damage / 4.0F;
            if (damage < 1.0F) {
                damage = 1.0F;
            }
            for (int i = 11; i < 15; ++i) {//11,12,13,14 = armor
                ItemStack itemstack = this.inventory.getItem(i);
                if ((!(damageSource.is(DamageTypes.IN_FIRE) && (damageSource.is(DamageTypes.ON_FIRE))) || !itemstack.has(net.minecraft.core.component.DataComponents.DAMAGE_RESISTANT)) && com.talhanation.recruits.util.ItemCompat.isArmor(itemstack)) {
                    itemstack.setDamageValue((int) damage);
                }
            }
        }
    }

    protected void damageMainHandItem() {
        ItemStack itemstack = this.inventory.getItem(9);// 10 = hoffhand slot
        if (itemstack.isDamageableItem()) {
            itemstack.setDamageValue(1);
        }
    }

    @Override
    public boolean killedEntity(ServerLevel p_241847_1_, LivingEntity p_241847_2_, DamageSource damageSource) {
        this.addXp(2);
        this.setKills(this.getKills() + 1);
        return super.killedEntity(p_241847_1_, p_241847_2_, damageSource);
    }

    protected void hurtCurrentlyUsedShield(float damage) {
        if (this.useItem.getItem() instanceof ShieldItem) {
            int i = 1 + Mth.floor(damage);
            InteractionHand hand = this.getUsedItemHand();
            this.useItem.hurtAndBreak(i, this, hand.asEquipmentSlot());
            if (this.useItem.isEmpty()) {
                if (hand == InteractionHand.MAIN_HAND) {
                    this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                    this.getSlot(9).set(ItemStack.EMPTY);
                } else {
                    this.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
                    this.getSlot(10).set(ItemStack.EMPTY);
                }
                this.useItem = ItemStack.EMPTY;
                this.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
                this.getSlot(10).set(ItemStack.EMPTY);
                this.playSound(SoundEvents.SHIELD_BREAK.value(), 0.8F, 0.8F + this.level().random.nextFloat() * 0.4F);
            }

            ItemStack itemstack = this.inventory.getItem(10);// 10 = hoffhand slot
            if (itemstack.getItem() instanceof ShieldItem) {
                itemstack.setDamageValue((int) damage);
            }
        }
    }

    @Override
    public abstract void openGUI(Player player);

    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        Item item = itemstack.getItem();
        if (this.level().isClientSide()) {
            boolean flag = false; //this.isOwnedBy(player) || this.isOwned() || item == Items.BONE && !this.isOwned();
            return flag ? InteractionResult.CONSUME : InteractionResult.PASS;
        } else {
                if (player.isCrouching()) {
                    openGUI(player);
                    return InteractionResult.SUCCESS;
                }
                if(!player.isCrouching()) {
                    int i = this.random.nextInt(5);
                    switch (i) {
                        case 0:
                            player.displayClientMessage(Component.literal(this.getName().getString() + ": " + " Hello my Friend."), false);
                            break;
                        case 1:
                            player.displayClientMessage(Component.literal(this.getName().getString() + ": " +"Life is only worth as much as emeralds..."), false);
                            break;
                        default:
                            player.displayClientMessage(Component.literal(this.getName().getString() + ": " +"Pay me I'll get rid of your headache!"), false);
                            break;
                    }
                    return InteractionResult.SUCCESS;
                }
            return super.mobInteract(player, hand);
        }
    }

}
