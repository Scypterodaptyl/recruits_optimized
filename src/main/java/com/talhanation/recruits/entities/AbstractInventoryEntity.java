package com.talhanation.recruits.entities;

import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import com.talhanation.recruits.util.ItemCompat;
import net.minecraft.server.level.ServerLevel;
import com.talhanation.recruits.util.NbtCompat;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.network.syncher.SynchedEntityData;
import com.talhanation.recruits.Main;
import com.talhanation.recruits.FactionEvents;
import com.talhanation.recruits.config.RecruitsServerConfig;
import com.talhanation.recruits.inventory.RecruitSimpleContainer;
import com.talhanation.recruits.pathfinding.AsyncPathfinderMob;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractSkullBlock;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.function.Predicate;

import static net.minecraft.world.entity.EquipmentSlot.*;

public abstract class AbstractInventoryEntity extends AsyncPathfinderMob {


    //iv slots
    //4 = offhand
    //5 = mainhand
    //0,1,2,3 = armor
    //rest = inv

    public RecruitSimpleContainer inventory;
    private int beforeItemSlot = -1;
    private net.minecraftforge.common.util.LazyOptional<?> itemHandler = null;

    public AbstractInventoryEntity(EntityType<? extends AbstractInventoryEntity> entityType, Level world) {
        super(entityType, world);
        this.createInventory();
        this.setCanPickUpLoot(true);
    }

    ///////////////////////////////////TICK/////////////////////////////////////////

    public void aiStep() {
        super.aiStep();
    }

    public void tick() {
        super.tick();
    }

    ////////////////////////////////////DATA////////////////////////////////////

    protected void defineSynchedData(SynchedEntityData.Builder builder) {

        super.defineSynchedData(builder);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        CompoundTag nbt = new CompoundTag();
        this.saveRecruitData(nbt);
        NbtCompat.writeAll(output, nbt);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.loadRecruitData(NbtCompat.readAll(input));
    }

    public void saveRecruitData(CompoundTag nbt) {
        ListTag listnbt = new ListTag();
        for (int i = 0; i < this.inventory.getContainerSize(); ++i) {
            ItemStack itemstack = this.inventory.getItem(i);
            if (!itemstack.isEmpty()) {
                CompoundTag compoundnbt = new CompoundTag();
                compoundnbt.putByte("Slot", (byte) i);
                NbtCompat.saveItem(this.registryAccess(), itemstack, compoundnbt);
                listnbt.add(compoundnbt);
            }
        }

        nbt.put("Items", listnbt);
        nbt.putInt("BeforeItemSlot", this.getBeforeItemSlot());
    }

    public void loadRecruitData(CompoundTag nbt) {
        ListTag listnbt = nbt.getListOrEmpty("Items");
        this.createInventory();

        for (int i = 0; i < listnbt.size(); ++i) {
            CompoundTag compoundnbt = listnbt.getCompoundOrEmpty(i);
            int j = compoundnbt.getByteOr("Slot", (byte) 0) & 255;
            if (j < this.inventory.getContainerSize()) {
                this.inventory.setItem(j, NbtCompat.loadItem(this.registryAccess(), compoundnbt));
            }
        }

        // equipment is loaded by vanilla, mirror it into the recruit inventory
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR || slot.getType() == EquipmentSlot.Type.HAND) {
                ItemStack equipped = this.getItemBySlot(slot);
                if (!equipped.isEmpty()) {
                    this.inventory.setItem(this.getInventorySlotIndex(slot), equipped);
                }
            }
        }

        int beforeItemSlot = nbt.getIntOr("BeforeItemSlot", 0);
        this.setBeforeItemSlot(beforeItemSlot);
        if(getBeforeItemSlot() != -1) resetItemInHand();// fail-safe in case eating is interrupted
    }


    ////////////////////////////////////GET////////////////////////////////////

    public SimpleContainer getInventory() {
        return this.inventory;
    }

    public int getInventorySize() {
        return 15;
    }

    public int getInventoryColumns() {
        return 3;
    }

    public int getInventorySlotIndex(EquipmentSlot slot) {
        switch (slot) {
            case HEAD -> {
                return 0;
            }
            case CHEST -> {
                return 1;
            }
            case LEGS -> {
                return 2;
            }
            case FEET -> {
                return 3;
            }
            case OFFHAND -> {
                return 4;
            }
            case MAINHAND -> {
                return 5;
            }
        }
        return 6;
    }
    public EquipmentSlot getEquipmentSlotIndex(int id) {
        switch (id) {
            case 0 -> {return HEAD;}
            case 1 -> {return CHEST;}
            case 2 -> {return LEGS;}
            case 3 -> {return FEET;}
            case 4 -> {return OFFHAND;}
            case 5 -> {return MAINHAND;}
        }
        return null;
    }

    ////////////////////////////////////SET////////////////////////////////////

    public void setItemInHand(@NotNull InteractionHand hand, @NotNull ItemStack itemStack) {
        if (hand == InteractionHand.MAIN_HAND) {
            this.setItemSlot(EquipmentSlot.MAINHAND, itemStack);
            this.inventory.setItem(5, itemStack);//5 == MAINHAND
        } else {
            if (hand != InteractionHand.OFF_HAND) {
                throw new IllegalArgumentException("Invalid hand " + hand);
            }

            this.setItemSlot(EquipmentSlot.OFFHAND, itemStack);
            this.inventory.setItem(4, itemStack);//4 == MAINHAND
        }

    }

    @Override
    public void setItemSlot(@NotNull EquipmentSlot slotIn, @NotNull ItemStack stack) {
        super.setItemSlot(slotIn, stack);
        switch (slotIn) {
            case HEAD ->{
                if (this.inventory.getItem(0).isEmpty())
                    this.inventory.setItem(0, stack);
            }
            case CHEST-> {
                if (this.inventory.getItem(1).isEmpty())
                    this.inventory.setItem(1, stack);
            }
            case LEGS-> {
                if (this.inventory.getItem(2).isEmpty())
                    this.inventory.setItem(2, stack);
            }
            case FEET-> {
                if (this.inventory.getItem(3).isEmpty())
                    this.inventory.setItem(3, stack);
            }
            case OFFHAND-> {
                if (this.inventory.getItem(4).isEmpty())
                    this.inventory.setItem(4, stack);
            }
            case MAINHAND-> {
                if (this.inventory.getItem(5).isEmpty())
                    this.inventory.setItem(5, stack);
            }
        }
    }
    public @NotNull SlotAccess getSlot(int slot) {
        return slot == 499 ? new SlotAccess() {
            public ItemStack get() {
                return new ItemStack(Items.CHEST);
            }

            public boolean set(ItemStack stack) {
                if (stack.isEmpty()) {

                    AbstractInventoryEntity.this.createInventory();

                    return true;
                } else {
                    return false;
                }
            }
        } : super.getSlot(slot);
    }



    ////////////////////////////////////OTHER FUNCTIONS////////////////////////////////////

    public void onInventoryChanged(){}
    public void onItemStackAdded(ItemStack itemStack){}
    public void createInventory() {
        SimpleContainer inventory = this.inventory;
        this.inventory = new RecruitSimpleContainer(this.getInventorySize(), this){

        };
        if (inventory != null) {
            int i = Math.min(inventory.getContainerSize(), this.inventory.getContainerSize());

            for (int j = 0; j < i; ++j) {
                ItemStack itemstack = inventory.getItem(j);
                if (!itemstack.isEmpty()) {
                    this.inventory.setItem(j, itemstack.copy());
                }
            }
        }
        this.itemHandler = net.minecraftforge.common.util.LazyOptional.of(() -> new net.minecraftforge.items.wrapper.InvWrapper(this.inventory));
    }

    public void die(DamageSource dmg) {
        super.die(dmg);

        // The Corpse mod is not available for Forge 1.21.11, so the corpse compat was removed.

        if (this.level() instanceof ServerLevel serverLevel && serverLevel.getGameRules().get(GameRules.ENTITY_DROPS)) {
            for (int i = 0; i < this.inventory.getContainerSize(); i++) {
                this.spawnAtLocation(serverLevel, this.inventory.getItem(i));// Containers.dropItemStack(this.level(), getX(), getY(), getZ(), );
            }
        }
    }

    @Override
    protected void pickUpItem(ServerLevel level, ItemEntity itemEntity) {
        this.pickUpItem(itemEntity);
    }

    protected void pickUpItem(ItemEntity itemEntity) {
        ItemStack itemstack = itemEntity.getItem();

        if (this.canEquipItem(itemstack)) {
            this.equipItem(itemstack);
            this.onItemPickup(itemEntity);
            this.take(itemEntity, itemstack.getCount());
            itemEntity.discard();
        }
        else {
            RecruitSimpleContainer inventory = this.inventory;
            boolean flag = inventory.canAddItem(itemstack);
            if (!flag) {
                return;
            }
            this.onItemPickup(itemEntity);
            this.take(itemEntity, itemstack.getCount());
            ItemStack itemstack1 = inventory.addItem(itemstack);
            if (itemstack1.isEmpty()) {
                itemEntity.remove(RemovalReason.KILLED);
            } else {
                itemstack.setCount(itemstack1.getCount());
            }
        }
    }
    public void equipItem(ItemStack itemStack) {
        EquipmentSlot equipmentslot = getSlotForItem(itemStack);
        ItemStack currentArmor = this.getItemBySlot(equipmentslot);
        if (this.level() instanceof ServerLevel serverLevel) this.spawnAtLocation(serverLevel, currentArmor);
        this.setItemSlot(equipmentslot, itemStack);
        this.inventory.setItem(getInventorySlotIndex(equipmentslot), itemStack);
        net.minecraft.core.Holder<net.minecraft.sounds.SoundEvent> equipSound = ItemCompat.getEquipSound(itemStack);
        if(equipSound != null)
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), equipSound, this.getSoundSource(), 1.0F, 1.0F);
    }
    public boolean canEquipItem(@NotNull ItemStack itemStack) {
        if(!itemStack.isEmpty()) {
            EquipmentSlot equipmentslot = getSlotForItem(itemStack);
                ItemStack currentArmor = this.getItemBySlot(equipmentslot);
                boolean flag = this.canReplaceCurrentItem(itemStack, currentArmor);
                return flag && this.canHoldItem(itemStack);
        }
        return false;
    }

    public boolean hasSameTypeOfItem(ItemStack stack) {
        return this.getInventory().items.stream().anyMatch(itemStack -> itemStack.getItem().getDescriptionId().equals(stack.getItem().getDescriptionId()));
    }
    @Nullable
    public ItemStack getMatchingItem(Predicate<ItemStack> predicate) {
        for (ItemStack stack : this.getInventory().items) {
            if (!stack.isEmpty() && predicate.test(stack)) {
                return stack;
            }
        }
        return null;
    }
    public boolean canEquipItemToSlot(@NotNull ItemStack itemStack, EquipmentSlot slot) {
        if(!itemStack.isEmpty()) {
            ItemStack currentArmor = this.getItemBySlot(slot);
            boolean flag = this.canReplaceCurrentItem(itemStack, currentArmor);

            return flag && this.canHoldItem(itemStack) && itemStack.canEquip(slot, this);
        }
        return false;
    }


    @Override
    public boolean wantsToPickUp(ServerLevel level, @NotNull ItemStack itemStack) {
        return this.wantsToPickUp(itemStack);
    }

    public boolean wantsToPickUp(@NotNull ItemStack itemStack){
       if (ItemCompat.isArmor(itemStack)){
           EquipmentSlot equipmentslot = getSlotForItem(itemStack);

           return this.getItemBySlot(equipmentslot).isEmpty() && !hasSameTypeOfItem(itemStack) && canEquipItem(itemStack);
       }
       else
           return ItemCompat.isEdible(itemStack);
    }
    @NotNull
    public static EquipmentSlot getSlotForItem(ItemStack itemStack) {
        EquipmentSlot armorSlot = ItemCompat.getArmorSlot(itemStack);
        if (armorSlot != null) return armorSlot;
        if (itemStack.is(Items.CARVED_PUMPKIN) || (itemStack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof AbstractSkullBlock)) {
            return EquipmentSlot.HEAD;
        }
        if (ItemCompat.isSword(itemStack)) {
            return EquipmentSlot.MAINHAND;
        }
        return itemStack.canPerformAction(net.minecraftforge.common.ToolActions.SHIELD_BLOCK) ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND;
    }

    @Override
    protected boolean canReplaceCurrentItem(@NotNull ItemStack replacer, @NotNull ItemStack current, @NotNull EquipmentSlot slot) {
        return this.canReplaceCurrentItem(replacer, current);
    }

    protected boolean canReplaceCurrentItem(@NotNull ItemStack replacer, ItemStack current) {
        if (current.isEmpty()) {
            return true;
        } else if (ItemCompat.isDigger(current) && ItemCompat.isSword(replacer)) {
            double diggerDamage = ItemCompat.getAttackDamage(current);
            double swordDamage = ItemCompat.getAttackDamage(replacer);
            if (diggerDamage != swordDamage) {
                return diggerDamage < swordDamage;
            }
            return this.canReplaceEqualItem(replacer, current);
        }

        else if (ItemCompat.isSword(replacer)) {
            if (!ItemCompat.isSword(current)) {
                return true;
            } else {
                double damage = ItemCompat.getAttackDamage(replacer);
                double damage1 = ItemCompat.getAttackDamage(current);
                if (damage != damage1) {
                    return damage > damage1;
                } else {
                    return this.canReplaceEqualItem(replacer, current);
                }
            }
        }

        else if (replacer.getItem() instanceof BowItem && current.getItem() instanceof BowItem) {
            return this.canReplaceEqualItem(replacer, current);
        }

        else if (replacer.getItem() instanceof CrossbowItem && current.getItem() instanceof CrossbowItem) {
            return this.canReplaceEqualItem(replacer, current);
        }

        else if (ItemCompat.isArmor(replacer)) {
            if (EnchantmentHelper.has(current, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE)) {
                return false;
            } else if (!ItemCompat.isArmor(current)) {
                return true;
            } else {
                double defense = ItemCompat.getArmorDefense(replacer);
                double defense1 = ItemCompat.getArmorDefense(current);
                double toughness = ItemCompat.getArmorToughness(replacer);
                double toughness1 = ItemCompat.getArmorToughness(current);
                if (defense != defense1) {
                    return defense > defense1;
                } else if (toughness != toughness1) {
                    return toughness > toughness1;
                } else {
                    return this.canReplaceEqualItem(replacer, current);
                }
            }
        } else {
            if (ItemCompat.isDigger(replacer)) {
                if (current.getItem() instanceof BlockItem) {
                    return true;
                }

                if (ItemCompat.isDigger(current)) {
                    double damage = ItemCompat.getAttackDamage(replacer);
                    double damage1 = ItemCompat.getAttackDamage(current);
                    if (damage != damage1) {
                        return damage > damage1;
                    }

                    return this.canReplaceEqualItem(replacer, current);
                }
            }

            return false;
        }
    }


    public abstract Predicate<ItemEntity> getAllowedItems();

    public abstract void openGUI(Player player);

    public <T> net.minecraftforge.common.util.LazyOptional<T> getCapability(net.minecraftforge.common.capabilities.Capability<T> capability, @Nullable net.minecraft.core.Direction facing) {
        if (this.isAlive() && capability == net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER && itemHandler != null)
            return itemHandler.cast();
        return super.getCapability(capability, facing);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        if (itemHandler != null) {
            net.minecraftforge.common.util.LazyOptional<?> oldHandler = itemHandler;
            itemHandler = null;
            oldHandler.invalidate();
        }
    }

    public void consumeArrow(){
        for(ItemStack itemStack : this.inventory.items){
            if(itemStack.is(ItemTags.ARROWS)){
                itemStack.shrink(1);
                break;
            }
        }
    }

    public boolean canTakeArrows() {
        int count = 0;
        for(ItemStack itemstack : this.inventory.items){
             if(itemstack.is(ItemTags.ARROWS)){
                 count += itemstack.getCount();
             }
        }

        return count < 32;
    }

    public boolean canTakeCannonBalls() {
        int count = 0;
        for(ItemStack itemstack : this.inventory.items){
            if(itemstack.getItem().getDescriptionId().contains("cannon_ball")){
                count += itemstack.getCount();
            }
        }

        return count < 50;
    }

    public boolean canTakePlanks() {
        int count = 0;
        for(ItemStack itemstack : this.inventory.items){
            if(itemstack.is(ItemTags.PLANKS)){
                count += itemstack.getCount();
            }
        }

        return count < 64;
    }

    public boolean canTakeIronNuggets() {
        int count = 0;
        for(ItemStack itemstack : this.inventory.items){
            if(itemstack.is(Items.IRON_NUGGET)){
                count += itemstack.getCount();
            }
        }

        return count < 64;
    }

    public boolean canTakeCartridge() {
        int count = 0;
        for(ItemStack itemstack : this.inventory.items){
            if(itemstack.getItem().getDescriptionId().contains("cartridge")){
                count += itemstack.getCount();
            }
        }

        return count < 32;
    }

    public void resetItemInHand() {
        //food is in offhand
        //before-item is in inventory slot

        //get OffhandItem (food)
        ItemStack foodStack = this.getOffhandItem().copy();
        //get Before Item from saved slot
        ItemStack beforeItem = this.inventory.getItem(getBeforeItemSlot()).copy();
        //remove item from this slot
        inventory.removeItemNoUpdate(getBeforeItemSlot());

        //remove offhand item
        this.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        //this.inventory.setItem(4, ItemStack.EMPTY);//set hand slot empty

        //set before item in hand and slot
        this.setItemInHand(InteractionHand.OFF_HAND, beforeItem.copy());
        this.inventory.setItem(getBeforeItemSlot(), foodStack.copy());

        this.setBeforeItemSlot(-1); // means eating was successfully without interrupt
    }


    public boolean isPaymentInContainer(Container container){
        int amount = 0;
        for(int i = 0; i < container.getContainerSize(); i++) {
            ItemStack itemStack = container.getItem(i);
            if(itemStack.is(FactionEvents.getCurrency().getItem())){
                amount += itemStack.getCount();
            }
        }
        return amount >= RecruitsServerConfig.RecruitsPaymentAmount.get();
    }

    @Nullable
    public ItemStack getCurrencyFromInv(Container inv){
        ItemStack currency = null;
        for(int i = 0; i < inv.getContainerSize(); i++){
            ItemStack itemStack = inv.getItem(i);
            if(itemStack.is(FactionEvents.getCurrency().getItem())){
                currency = itemStack;
                break;
            }
        }
        return currency;
    }

    public void doPayment(Container container){
        int amount = RecruitsServerConfig.RecruitsPaymentAmount.get();
        for (int i = 0; i < amount; i++) {
            ItemStack currency = this.getCurrencyFromInv(container);
            if (currency != null) {
                currency.shrink(amount);
            }
        }
    }


    public void setBeforeItemSlot(int i) {
        beforeItemSlot = i;
    }

    public int getBeforeItemSlot(){
        return beforeItemSlot;
    }

    public void switchMainHandItem(Predicate<ItemStack> predicate) {
        if (!this.isAlive() || predicate == null) return;

        SimpleContainer inventory = this.getInventory();
        ItemStack mainHand = this.getMainHandItem();
        if (predicate.test(mainHand)) return;

        for (int i = 6; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (predicate.test(stack)) {

                inventory.setItem(i, mainHand);
                this.setItemInHand(InteractionHand.MAIN_HAND, stack);
                return;
            }
        }
    }
    public void switchOffHandItem(Predicate<ItemStack> predicate) {
        if (!this.isAlive() || predicate == null) return;

        SimpleContainer inventory = this.getInventory();
        ItemStack offHand = this.getOffhandItem();
        if (predicate.test(offHand)) return;

        for (int i = 6; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (predicate.test(stack)) {

                inventory.setItem(i, offHand);
                this.setItemInHand(InteractionHand.OFF_HAND, stack);
                return;
            }
        }
    }
}
