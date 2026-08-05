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
    }

    @Override
    public boolean canUse() {
        return recruit.getTarget() == null && !this.recruit.isFollowing() && !recruit.getFleeing() && !recruit.needsToGetFood() && !recruit.getShouldMount() && !recruit.getShouldMovePos() && !recruit.getShouldHoldPos();
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
    }

    @Override
    public void tick() {
        switch (state) {
            case SEARCH -> {
                if (--scanDelay <= 0) {
                    scanDelay = 10 + recruit.getRandom().nextInt(5);

                    if (recruit.getCommandSenderWorld() instanceof ServerLevel serverLevel) {
                        AABB aabb = recruit.getBoundingBox().inflate(16.0D, 3.0D, 16.0D);
                        for (Entity nearby : NearbyEntityCache.allEntities(serverLevel)) {
                            if (!(nearby instanceof ItemEntity item)) continue;
                            if (!aabb.contains(item.getX(), item.getY(), item.getZ())) continue;
                            if (!recruit.getAllowedItems().test(item)) continue;
                            if (recruit.distanceTo(item) >= 25) continue;
                            boolean wantedForFood = item.getItem().isEdible() && recruit.getHunger() < 30;
                            if (wantedForFood || recruit.wantsToPickUp(item.getItem())) {
                                this.itemEntityList.add(item);
                            }
                        }
                    }
                }

                if (itemEntityList.isEmpty()) {
                    state = SEARCH;
                } else {
                    state = SELECT;
                }
            }

            case SELECT -> {
                if (!itemEntityList.isEmpty()) {
                    ItemEntity result = null;
                    double d0 = -1.0D;

                    for (ItemEntity item : itemEntityList) {
                        double d1 = recruit.distanceToSqr(item);
                        if (d0 == -1.0D || d0 < d1) {
                            result = item;
                            d0 = d1;
                        }
                    }

                    this.itemEntity = result;
                    this.state = MOVE;
                } else state = SEARCH;
            }

            case MOVE -> {
                if (itemEntity != null) {
                    recruit.getNavigation().moveTo(itemEntity, 1F);
                    recruit.setMaxUpStep(1.25F);
                    if (recruit.distanceTo(itemEntity) < 3F) {
                        this.state = PICKUP;
                        recruit.setMaxUpStep(1F);

                    }
                } else state = SELECT;
            }

            case PICKUP -> {
                recruit.getNavigation().moveTo(itemEntity, 1F);
                this.recruit.setCanPickUpLoot(true);
                if (++this.timer > 30) {
                    this.itemEntityList.clear();
                    this.recruit.setCanPickUpLoot(false);
                    this.timer = 0;
                    this.state = SELECT;
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