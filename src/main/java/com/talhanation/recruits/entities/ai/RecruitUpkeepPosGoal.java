package com.talhanation.recruits.entities.ai;

import com.talhanation.recruits.ClaimEvents;
import com.talhanation.recruits.entities.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;


public class RecruitUpkeepPosGoal extends Goal {
    // BlockEntity#getPersistentData no longer exists, keep the transient chest state here instead
    private static final java.util.Map<ChestBlockEntity, CompoundTag> CHEST_STATE = new java.util.WeakHashMap<>();
    public AbstractRecruitEntity recruit;
    public BlockPos chestPos;
    public Container container;
    public boolean message;
    public boolean messageNotChest;
    public boolean messageNeedNewChest;
    public boolean messageNotInRange;
    public int timeToRecalcPath = 0;
    public int findPosCooldown = 0;
    public int timer = 0;
    public boolean setTimer = false;
    public boolean canResetPaymentTimer = false;
    public RecruitUpkeepPosGoal(AbstractRecruitEntity recruit) {
        this.recruit = recruit;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return recruit.needsToGetFood() && recruit.getUpkeepPos() != null;
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }
    @Override
    public void start() {
        super.start();
        this.timeToRecalcPath = 0;
        this.findPosCooldown = 0;
        message = true;
        messageNotChest = true;
        messageNeedNewChest = true;
        messageNotInRange = true;
        this.chestPos = recruit.getUpkeepPos();

        if(chestPos != null) {
            BlockEntity entity = recruit.level().getBlockEntity(chestPos);
            BlockState blockState = recruit.level().getBlockState(chestPos);
            if (blockState.getBlock() instanceof ChestBlock chestBlock) {
                this.container = ChestBlock.getContainer(chestBlock, blockState, recruit.level(), chestPos, false);
            } else if (entity instanceof Container containerEntity) {
                this.container = containerEntity;
            } else {
                if (recruit.getOwner() != null && messageNotChest) {
                    recruit.getOwner().displayClientMessage(TEXT_CANT_INTERACT(recruit.getName().getString()), false);
                    messageNotChest = false;
                }
                this.chestPos = null;
                recruit.clearUpkeepPos();
            }
            if(chestPos != null && !ClaimEvents.canRecruitAccessBlock(recruit, chestPos)){
                if(recruit.getOwner() != null){
                    recruit.getOwner().displayClientMessage(TEXT_CANT_INTERACT(recruit.getName().getString()), false);
                }
                this.chestPos = null;
                this.container = null;
                recruit.clearUpkeepPos();
            }
            if(chestPos != null){
                double distance = this.recruit.position().distanceToSqr(Vec3.atCenterOf(chestPos));
                if(distance > 10000){
                    if(recruit.getOwner() != null && messageNotInRange){
                        recruit.getOwner().displayClientMessage(TEXT_NOT_IN_RANGE(recruit.getName().getString()), false);
                        messageNotInRange = false;
                    }
                    recruit.clearUpkeepPos();
                    stop();
                }
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        if(this.chestPos != recruit.getUpkeepPos()){
            this.chestPos = recruit.getUpkeepPos();
            this.stop();
            return;
        }

        if (container != null && chestPos != null && !ClaimEvents.canRecruitAccessBlock(recruit, chestPos)) {
            recruit.clearUpkeepPos();
            this.stop();
            return;
        }

        if (container != null && chestPos != null){
            if (--this.timeToRecalcPath <= 0) {
                this.timeToRecalcPath = this.adjustedTickDelay(10);
                this.recruit.getNavigation().moveTo(chestPos.getX(), chestPos.getY(), chestPos.getZ(), 1.15D);
            }

            if (recruit.horizontalCollision || recruit.minorHorizontalCollision) {
                this.recruit.getJumpControl().jump();
            }

            if (chestPos.closerThan(recruit.getOnPos(), 3) && container != null) {

                this.recruit.getNavigation().stop();
                this.recruit.getLookControl().setLookAt(chestPos.getX(), chestPos.getY() + 1, chestPos.getZ(), 10.0F, (float) this.recruit.getMaxHeadXRot());

                if(!setTimer){
                    if(recruit.paymentTimer == 0){
                        recruit.checkPayment(container);
                        canResetPaymentTimer = true;
                    }
                    this.recruit.upkeepReequip(container);
                    timer = 30;
                    setTimer = true;

                    if (isFoodInContainer(container)) {
                        interactChest(container, true);
                        for (int i = 0; i < 3; i++) {
                            ItemStack foodItem = this.getFoodFromInv(container);
                            ItemStack food;
                            if (foodItem != null && canAddFood()){
                                food = foodItem.copy();
                                food.setCount(1);
                                recruit.getInventory().addItem(food);
                                foodItem.shrink(1);
                            } else {
                                if(recruit.getOwner() != null && message){
                                    recruit.getOwner().displayClientMessage(TEXT_NO_PLACE(recruit.getName().getString()), false);
                                    message = false;
                                }
                                this.stop();
                                return;
                            }
                        }
                    }
                    else {
                        if(recruit.getOwner() != null && message){
                            recruit.getOwner().displayClientMessage(TEXT_FOOD(recruit.getName().getString()), false);
                            message = false;
                        }
                        this.stop();
                        return;
                    }
                }
            }
        }
        else if (--this.findPosCooldown <= 0) {
            this.findPosCooldown = this.adjustedTickDelay(20);
            this.chestPos = findInvPos();

            if(chestPos == null){
                if(recruit.getOwner() != null && messageNeedNewChest){
                    recruit.getOwner().displayClientMessage(NEED_NEW_UPKEEP(recruit.getName().getString()), false);
                    messageNeedNewChest = false;
                }

                recruit.clearUpkeepPos();
                stop();
            }
            else recruit.setUpkeepPos(chestPos);
            //Main.LOGGER.debug("Chest not found"
        }


        if(setTimer){
            if(timer > 0) timer--;
            if(timer == 0) stop();
        }
    }
    @Override
    public void stop() {
        super.stop();
        recruit.setUpkeepTimer(recruit.getUpkeepCooldown());
        recruit.forcedUpkeep = false;
        timer = 0;
        setTimer = false;

        if(recruit.paymentTimer == 0 && canResetPaymentTimer){
            canResetPaymentTimer = false;
            recruit.resetPaymentTimer();
        }

        if(container != null) {
            interactChest(container, false);
            container.setChanged();
        }
    }

    @Nullable
    private BlockPos findInvPos() {
        List<BlockPos> list = new ArrayList<>();
        BlockPos chestPos;
        if(this.recruit.getUpkeepPos() != null) {
            int range = 8;
            for (int x = -range; x < range; x++) {
                for (int y = -range; y < range; y++) {
                    for (int z = -range; z < range; z++) {
                        chestPos = recruit.getUpkeepPos().offset(x, y, z);
                        BlockEntity block = recruit.level().getBlockEntity(chestPos);
                        if (block instanceof Container blockContainer && ClaimEvents.canRecruitAccessBlock(recruit, chestPos)){
                            if(isFoodInContainer(blockContainer)) return chestPos;
                            else list.add(chestPos);
                        }
                    }
                }
            }
        }

        if(list.isEmpty()) return null;
        else return list.get(recruit.getRandom().nextInt(list.size()));
    }

    private boolean isFoodInContainer(Container container){
        for(int i = 0; i < container.getContainerSize(); i++) {
            ItemStack foodItem = container.getItem(i);
            if(recruit.canEatItemStack(foodItem)){
                return true;
            }
        }
        return false;
    }
    @Nullable
    private ItemStack getFoodFromInv(Container inv){
        ItemStack itemStack = null;
        for(int i = 0; i < inv.getContainerSize(); i++){
            if(recruit.canEatItemStack(inv.getItem(i))){
                itemStack = inv.getItem(i);
                break;
            }
        }
        return itemStack;
    }

    private boolean canAddFood(){
        for(int i = 6; i < 14; i++){
            if(recruit.getInventory().getItem(i).isEmpty())
                return true;
        }
        return false;
    }

    public void interactChest(Container container, boolean open) {
        if(this.chestPos != null && (container instanceof CompoundContainer || container instanceof ChestBlockEntity)){
            BlockState state = this.recruit.level().getBlockState(this.chestPos);
            Block block = state.getBlock();
            boolean isOpened = false;
            CompoundTag compoundTag = new CompoundTag();
            if(recruit.level().getBlockEntity(chestPos) instanceof ChestBlockEntity chestBlockEntity){
                compoundTag = CHEST_STATE.computeIfAbsent(chestBlockEntity, be -> new CompoundTag());
                if(compoundTag.contains("isOpened"))
                    isOpened = compoundTag.getBooleanOr("isOpened", false);
                else
                    compoundTag.putBoolean("isOpened", false);
            }

            if (open) {
                if(!isOpened){
                    this.recruit.level().blockEvent(this.chestPos, block, 1, 1);
                    this.recruit.level().playSound(null, chestPos, SoundEvents.CHEST_OPEN, recruit.getSoundSource(), 0.7F, 0.8F + 0.4F * recruit.getRandom().nextFloat());
                    compoundTag.putBoolean("isOpened", true);
                }
            }
            else {
                if(isOpened){
                    this.recruit.level().blockEvent(this.chestPos, block, 1, 0);
                    this.recruit.level().playSound(null, chestPos, SoundEvents.CHEST_CLOSE, recruit.getSoundSource(), 0.7F, 0.8F + 0.4F * recruit.getRandom().nextFloat());
                    compoundTag.putBoolean("isOpened", false);
                }

            }
            this.recruit.level().gameEvent(this.recruit, open ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, chestPos);
        }
    }

    private MutableComponent TEXT_NO_PLACE(String name) {
        return Component.translatable("chat.recruits.text.noPlaceInInv", name);
    }

    private MutableComponent TEXT_CANT_INTERACT(String name) {
        return Component.translatable("chat.recruits.text.cantInteract", name);
    }

    private MutableComponent NEED_NEW_UPKEEP(String name) {
        return Component.translatable("chat.recruits.text.findMeNewChest", name);
    }

    private MutableComponent TEXT_FOOD(String name) {
        return Component.translatable("chat.recruits.text.noFoodInUpkeep", name);
    }

    private MutableComponent TEXT_NOT_IN_RANGE(String name) {
        return Component.translatable("chat.recruits.text.upkeepNotInRange", name);
    }
}
