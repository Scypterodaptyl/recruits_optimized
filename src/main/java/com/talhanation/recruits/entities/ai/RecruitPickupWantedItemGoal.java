package com.talhanation.recruits.entities.ai;

import com.talhanation.recruits.entities.AbstractRecruitEntity;
import com.talhanation.recruits.entities.ai.async.NearbyEntityCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static com.talhanation.recruits.entities.ai.RecruitPickupWantedItemGoal.State.*;


public class RecruitPickupWantedItemGoal extends Goal {

    public AbstractRecruitEntity recruit;
    public State state;
    public List<ItemEntity> itemEntityList = new ArrayList<>();
    public ItemEntity itemEntity;
    private byte timer;
    private int scanDelay;

    public RecruitPickupWantedItemGoal(AbstractRecruitEntity recruit) {
        this.recruit = recruit;
        this.scanDelay = recruit.getRandom().nextInt(10);
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    private boolean baseConditionsMet() {
        return recruit.getTarget() == null && !recruit.isFollowing() && !recruit.getFleeing() && !recruit.needsToGetFood() && !recruit.getShouldMount() && !recruit.getShouldMovePos() && !recruit.getShouldHoldPos();
    }

    @Override
    public boolean canUse() {
        if (!baseConditionsMet()) return false;
        refreshNearbyItems();
        return !itemEntityList.isEmpty();
    }

    @Override
    public boolean canContinueToUse() {
        if (!baseConditionsMet()) return false;
        if (itemEntity != null && itemEntity.isAlive()) return true;
        refreshNearbyItems();
        return !itemEntityList.isEmpty();
    }

    private void refreshNearbyItems() {
        if (--scanDelay > 0) return;
        scanDelay = 10 + recruit.getRandom().nextInt(5);

        itemEntityList.clear();
        if (!(recruit.getCommandSenderWorld() instanceof ServerLevel serverLevel)) return;

        AABB aabb = recruit.getBoundingBox().inflate(16.0D, 3.0D, 16.0D);
        for (Entity nearby : NearbyEntityCache.allEntities(serverLevel)) {
            if (!(nearby instanceof ItemEntity item)) continue;
            if (!item.isAlive()) continue;
            if (!aabb.contains(item.getX(), item.getY(), item.getZ())) continue;
            if (!recruit.getAllowedItems().test(item)) continue;
            if (recruit.distanceTo(item) >= 25) continue;
            boolean wantedForFood = item.getItem().isEdible() && recruit.getHunger() < 30;
            if (wantedForFood || recruit.wantsToPickUp(item.getItem())) {
                itemEntityList.add(item);
            }
        }
    }

    @Override
    public void start() {
        super.start();
        timer = 0;
        state = SEARCH;
    }

    @Override
    public void stop() {
        super.stop();
        recruit.setCanPickUpLoot(false);
        itemEntity = null;
        itemEntityList.clear();
    }

    @Override
    public void tick() {
        switch (state) {
            case SEARCH -> state = itemEntityList.isEmpty() ? SEARCH : SELECT;

            case SELECT -> {
                if (!itemEntityList.isEmpty()) {
                    ItemEntity result = null;
                    double d0 = -1.0D;

                    for (ItemEntity item : itemEntityList) {
                        double d1 = recruit.distanceToSqr(item);
                        if (d0 == -1.0D || d1 < d0) {
                            result = item;
                            d0 = d1;
                        }
                    }

                    itemEntity = result;
                    state = MOVE;
                } else state = SEARCH;
            }

            case MOVE -> {
                if (itemEntity != null && itemEntity.isAlive()) {
                    recruit.getNavigation().moveTo(itemEntity, 1F);
                    recruit.setMaxUpStep(1.25F);
                    if (recruit.distanceTo(itemEntity) < 3F) {
                        state = PICKUP;
                        recruit.setMaxUpStep(1F);
                    }
                } else state = SELECT;
            }

            case PICKUP -> {
                recruit.getNavigation().moveTo(itemEntity, 1F);
                recruit.setCanPickUpLoot(true);
                if (++timer > 30) {
                    itemEntityList.clear();
                    itemEntity = null;
                    recruit.setCanPickUpLoot(false);
                    timer = 0;
                    state = SELECT;
                }
            }
        }
    }


    enum State {
        SEARCH,
        SELECT,
        MOVE,
        PICKUP
    }
}
