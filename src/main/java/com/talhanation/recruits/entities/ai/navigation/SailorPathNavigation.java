package com.talhanation.recruits.entities.ai.navigation;

import com.google.common.collect.ImmutableSet;
import com.talhanation.recruits.config.RecruitsServerConfig;
import com.talhanation.recruits.entities.CaptainEntity;
import com.talhanation.recruits.pathfinding.AsyncPathfinder;
import com.talhanation.recruits.pathfinding.AsyncWaterBoundPathNavigation;
import com.talhanation.recruits.pathfinding.NodeEvaluatorGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class SailorPathNavigation extends AsyncWaterBoundPathNavigation {
    CaptainEntity captain;

    private static final NodeEvaluatorGenerator nodeEvaluatorGenerator = SailorNodeEvaluator::new;

    public SailorPathNavigation(CaptainEntity sailor, Level level) {
        super(sailor, level);
        this.captain = sailor;
    }

    protected @NotNull PathFinder createPathFinder(int maxVisitedNodes) {
        this.nodeEvaluator = new SailorNodeEvaluator();
        if (RecruitsServerConfig.UseAsyncPathfinding.get()) {
            return new AsyncPathfinder(this.nodeEvaluator, maxVisitedNodes, nodeEvaluatorGenerator, this.level);
        }
        return new PathFinder(this.nodeEvaluator, maxVisitedNodes);
    }

    @Override
    protected boolean canUpdatePath() {
        return true;
    }

    @Nullable
    public Path createPath(@NotNull BlockPos blockPos, int additionalOffsetXYZ, boolean targetBlockAbove, int accuracy) {
        return this.createPath(ImmutableSet.of(blockPos), additionalOffsetXYZ, targetBlockAbove, accuracy, (float)this.mob.getAttributeValue(Attributes.FOLLOW_RANGE));
    }
}
