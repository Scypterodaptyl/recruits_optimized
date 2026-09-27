package com.talhanation.recruits.entities.ai.navigation;

import com.talhanation.recruits.mixin.PathNavigationRegionAccessor;
import com.talhanation.recruits.pathfinding.SharedBlockPathTypeCache;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import org.jetbrains.annotations.NotNull;

/**
 * Walk node evaluator used by recruits. Builds on the vanilla {@link WalkNodeEvaluator} and only
 * changes the path costs (prefer dirt paths, avoid leaves) and the block path types
 * (fence gates are treated like doors).
 */
public class RecruitsPathNodeEvaluator extends WalkNodeEvaluator {

    private int x;
    private int y;
    private int z;

    public RecruitsPathNodeEvaluator() {
        super();
    }

    @Override
    public void prepare(@NotNull PathNavigationRegion region, @NotNull Mob mob) {
        super.prepare(region, mob);
        if (mob.isVehicle()) {
            this.entityHeight = Mth.floor(mob.getBbHeight() + (float) getEntityHeight());
        }
        mob.setPathfindingMalus(PathType.WATER, 128.0F);
        mob.setPathfindingMalus(PathType.WATER_BORDER, 128.0F);
        mob.setPathfindingMalus(PathType.TRAPDOOR, -1.0F);
        mob.setPathfindingMalus(PathType.DAMAGE_FIRE, 32.0F);
        mob.setPathfindingMalus(PathType.DAMAGE_CAUTIOUS, 32.0F);
        mob.setPathfindingMalus(PathType.DANGER_POWDER_SNOW, -1.0F);
        mob.setPathfindingMalus(PathType.DOOR_WOOD_CLOSED, 0.0F);
        mob.setPathfindingMalus(PathType.FENCE, -1.0F);
        mob.setPathfindingMalus(PathType.LAVA, -1.0F);
        mob.setPathfindingMalus(PathType.LEAVES, -1.0F);
    }

    private int getEntityHeight() {
        return mob.isVehicle() ? 2 : 1;
    }

    public void setTarget(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    //prefer blocks that have empty neighbors
    //prefer walk on dirt path
    //prefer walk on stairs when goal pos is higher or lower current position >2 blocks
    //prefer walk on ways with no leaves
    @Override
    protected @NotNull Node getNode(int x, int y, int z) {
        Node node = super.getNode(x, y, z);
        if (this.currentContext == null || this.mob == null) return node;

        PathType pathType = this.getCachedPathType(x, y, z);
        float f = this.mob.getPathfindingMalus(pathType);

        if (f >= 0.0F) {
            node.type = pathType;
            node.costMalus = Math.max(node.costMalus, f);

            CollisionGetter level = this.currentContext.level();
            BlockPos pos = new BlockPos(x, y, z);
            BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

            BlockPos aboveEntityHeightPos = pos.above(getEntityHeight());

            for (Direction direction : Direction.Plane.HORIZONTAL) {
                mutablePos.set(pos).move(direction);
                BlockState belowStateNeighbors = level.getBlockState(mutablePos.below());

                if (!belowStateNeighbors.is(Blocks.DIRT_PATH)) {
                    node.costMalus += 2.0F;
                    continue;
                }

                BlockState aboveLeavesCheck = level.getBlockState(aboveEntityHeightPos.relative(direction, 2));
                if (aboveLeavesCheck.is(BlockTags.LEAVES)) {
                    node.costMalus = -1.0F;
                    break;
                }
            }
        }

        return node;
    }

    @Override
    public @NotNull PathType getPathType(@NotNull PathfindingContext context, int x, int y, int z) {
        BlockGetter level = context.level();
        if (level instanceof PathNavigationRegion region) {
            Level realLevel = ((PathNavigationRegionAccessor) region).talhanation$getLevel();
            return SharedBlockPathTypeCache.computeIfAbsent(realLevel, x, y, z,
                    () -> getBlockPathTypeStatic(level, new BlockPos.MutableBlockPos(x, y, z)));
        }
        return getBlockPathTypeStatic(level, new BlockPos.MutableBlockPos(x, y, z));
    }

    public static PathType getBlockPathTypeStatic(BlockGetter level, BlockPos.MutableBlockPos pos) {
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        PathType pathType = getBlockPathTypeRaw(level, pos);
        if (pathType == PathType.OPEN && j >= level.getMinY() + 1) {
            PathType below = getBlockPathTypeRaw(level, pos.set(i, j - 1, k));
            pathType = below != PathType.WALKABLE && below != PathType.OPEN && below != PathType.WATER && below != PathType.LAVA ? PathType.WALKABLE : PathType.OPEN;
            if (below == PathType.DAMAGE_FIRE) pathType = PathType.DAMAGE_FIRE;
            if (below == PathType.DAMAGE_OTHER) pathType = PathType.DAMAGE_OTHER;
            if (below == PathType.STICKY_HONEY) pathType = PathType.STICKY_HONEY;
            if (below == PathType.POWDER_SNOW) pathType = PathType.DANGER_POWDER_SNOW;
            if (below == PathType.DAMAGE_CAUTIOUS) pathType = PathType.DAMAGE_CAUTIOUS;
        }

        if (pathType == PathType.WALKABLE) {
            pathType = checkNeighbourBlocks(level, pos.set(i, j, k), pathType);
        }

        return pathType;
    }

    public static PathType checkNeighbourBlocks(BlockGetter level, BlockPos.MutableBlockPos pos, PathType pathType) {
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();

        for (int l = -1; l <= 1; ++l) {
            for (int i1 = -1; i1 <= 1; ++i1) {
                for (int j1 = -1; j1 <= 1; ++j1) {
                    if (l != 0 || j1 != 0) {
                        pos.set(i + l, j + i1, k + j1);
                        BlockState blockstate = level.getBlockState(pos);
                        PathType blockPathType = blockstate.getAdjacentBlockPathType(level, pos, null, pathType);
                        if (blockPathType != null) return blockPathType;
                        FluidState fluidState = blockstate.getFluidState();
                        PathType fluidPathType = fluidState.getAdjacentBlockPathType(level, pos, null, pathType);
                        if (fluidPathType != null) return fluidPathType;
                        if (blockstate.is(Blocks.CACTUS) || blockstate.is(Blocks.SWEET_BERRY_BUSH)) {
                            return PathType.DANGER_OTHER;
                        }

                        if (isBurningBlock(blockstate)) {
                            return PathType.DANGER_FIRE;
                        }

                        if (level.getFluidState(pos).is(FluidTags.WATER)) {
                            return PathType.WATER_BORDER;
                        }

                        if (blockstate.is(Blocks.WITHER_ROSE) || blockstate.is(Blocks.POINTED_DRIPSTONE)) {
                            return PathType.DAMAGE_CAUTIOUS;
                        }
                    }
                }
            }
        }

        return pathType;
    }

    protected static PathType getBlockPathTypeRaw(BlockGetter level, BlockPos pos) {
        BlockState blockstate = level.getBlockState(pos);
        PathType type = blockstate.getBlockPathType(level, pos, null);
        if (type != null) return type;
        Block block = blockstate.getBlock();
        if (blockstate.isAir()) {
            return PathType.OPEN;
        } else if (!blockstate.is(BlockTags.TRAPDOORS) && !blockstate.is(Blocks.LILY_PAD) && !blockstate.is(Blocks.BIG_DRIPLEAF)) {
            if (blockstate.is(Blocks.POWDER_SNOW)) {
                return PathType.POWDER_SNOW;
            } else if (!blockstate.is(Blocks.CACTUS) && !blockstate.is(Blocks.SWEET_BERRY_BUSH)) {
                if (blockstate.is(Blocks.HONEY_BLOCK)) {
                    return PathType.STICKY_HONEY;
                } else if (blockstate.is(Blocks.COCOA)) {
                    return PathType.COCOA;
                } else if (!blockstate.is(Blocks.WITHER_ROSE) && !blockstate.is(Blocks.POINTED_DRIPSTONE)) {
                    FluidState fluidstate = level.getFluidState(pos);
                    PathType nonLoggableFluidPathType = fluidstate.getBlockPathType(level, pos, null, false);
                    if (nonLoggableFluidPathType != null) return nonLoggableFluidPathType;
                    if (fluidstate.is(FluidTags.LAVA)) {
                        return PathType.LAVA;
                    } else if (isBurningBlock(blockstate)) {
                        return PathType.DAMAGE_FIRE;
                    } else if (block instanceof DoorBlock doorblock) {
                        if (blockstate.getValue(DoorBlock.OPEN)) {
                            return PathType.DOOR_OPEN;
                        } else {
                            return doorblock.type().canOpenByHand() ? PathType.DOOR_WOOD_CLOSED : PathType.DOOR_IRON_CLOSED;
                        }
                    } else if (block instanceof FenceGateBlock && !blockstate.getValue(FenceGateBlock.OPEN)) {
                        return PathType.DOOR_WOOD_CLOSED;
                    } else if (block instanceof FenceGateBlock && blockstate.getValue(FenceGateBlock.OPEN)) {
                        return PathType.DOOR_OPEN;
                    } else if (block instanceof BaseRailBlock) {
                        return PathType.RAIL;
                    } else if (block instanceof LeavesBlock) {
                        return PathType.LEAVES;
                    } else if (!blockstate.is(BlockTags.FENCES) && !blockstate.is(BlockTags.WALLS)) {
                        if (!blockstate.isPathfindable(PathComputationType.LAND)) {
                            return PathType.BLOCKED;
                        } else {
                            PathType loggableFluidPathType = fluidstate.getBlockPathType(level, pos, null, true);
                            if (loggableFluidPathType != null) return loggableFluidPathType;
                            return fluidstate.is(FluidTags.WATER) ? PathType.WATER : PathType.OPEN;
                        }
                    } else {
                        return PathType.FENCE;
                    }
                } else {
                    return PathType.DAMAGE_CAUTIOUS;
                }
            } else {
                return PathType.DAMAGE_OTHER;
            }
        } else {
            return PathType.TRAPDOOR;
        }
    }
}
