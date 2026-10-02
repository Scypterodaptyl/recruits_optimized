package com.talhanation.recruits.util;

import com.talhanation.recruits.entities.AbstractRecruitEntity;
import com.talhanation.recruits.entities.CaptainEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FormationUtils {
    public static final double spacing = 1.75D;
    public static Vec3 calculateLineBlockPosition(Vec3 targetPos, Vec3 linePos, int size, int index, Level level) {
        Vec3 toTarget = linePos.vectorTo(targetPos).normalize();
        Vec3 rotation = toTarget.yRot(3.14F/2).normalize();
        Vec3 pos;
        if(index == 0 || size/index > size/2)
            pos = linePos.lerp(linePos.add(rotation), index * 1.50);
        else
            pos = linePos.lerp(linePos.add(rotation.reverse()), index * 1.50);

        BlockPos blockPos = FormationUtils.getPositionOrSurface(
                level,
                new BlockPos((int) Math.round(pos.x), (int) Math.round(pos.y), (int) Math.round(pos.z))
        );

        return new Vec3(blockPos.getX(), blockPos.getY(), blockPos.getZ());

    }

    public static void movementFormation(ServerPlayer player, List<AbstractRecruitEntity> recruits, Vec3 targetPos, boolean hold) {
        movementFormation(player, recruits, targetPos, 1.0, hold);
    }

    public static void movementFormation(ServerPlayer player, List<AbstractRecruitEntity> recruits, Vec3 targetPos, double spacingMultiplier, boolean hold) {
        float yaw = player.getYRot();
        Vec3 forward = new Vec3(-Math.sin(Math.toRadians(yaw)), 0, Math.cos(Math.toRadians(yaw)));
        lineFormation(forward, recruits, targetPos, 3, 2.0D * spacingMultiplier, hold);
    }

    public static void movementFormation(ServerPlayer player, List<AbstractRecruitEntity> recruits, Vec3 targetPos, boolean tight, boolean hold) {
        if (!tight) {
            movementFormation(player, recruits, targetPos, 1.0, hold);
            return;
        }

        Vec3 forward = nearestCardinalForward(player.getYRot());
        lineFormation(forward, recruits, targetPos, 3, 1.0D, hold, true);
    }

    public static void lineUpFormation(ServerPlayer player, List<AbstractRecruitEntity> recruits, Vec3 targetPos) {
        lineUpFormation(player, recruits, targetPos, 1.0, false);
    }

    public static void lineUpFormation(ServerPlayer player, List<AbstractRecruitEntity> recruits, Vec3 targetPos, double spacingMultiplier, boolean hold) {
        float yaw = player.getYRot();
        Vec3 forward = new Vec3(-Math.sin(Math.toRadians(yaw)), 0, Math.cos(Math.toRadians(yaw)));
        int maxInRow = recruits.size() <= 20 ? recruits.size() : (recruits.size() + 1) / 2;
        lineFormation(forward, recruits, targetPos, maxInRow, 1.75D * spacingMultiplier, hold);
    }

    public static void lineUpFormation(ServerPlayer player, List<AbstractRecruitEntity> recruits, Vec3 targetPos, boolean tight, boolean hold) {
        if (!tight) {
            lineUpFormation(player, recruits, targetPos, 1.0, hold);
            return;
        }

        Vec3 forward = nearestCardinalForward(player.getYRot());
        int maxInRow = recruits.size() <= 20 ? recruits.size() : (recruits.size() + 1) / 2;
        lineFormation(forward, recruits, targetPos, maxInRow, 1.0D, hold, true);
    }

    public static void lineFormation(Vec3 forward, List<AbstractRecruitEntity> recruits, Vec3 targetPos, int maxInRow, double spacing) {
        lineFormation(forward, recruits, targetPos, maxInRow, spacing, false);
    }
    public static void lineFormation(Vec3 forward, List<AbstractRecruitEntity> recruits, Vec3 targetPos, int maxInRow, double spacing, boolean hold) {
        lineFormation(forward, recruits, targetPos, maxInRow, spacing, hold, false);
    }
    public static void lineFormation(Vec3 forward, List<AbstractRecruitEntity> recruits, Vec3 targetPos, int maxInRow, double spacing, boolean hold, boolean tight) {
        Vec3 left = new Vec3(-forward.z, forward.y, forward.x);

        List<FormationPosition> possiblePositions = new ArrayList<>();

        for(AbstractRecruitEntity rec : recruits){
            if(rec instanceof CaptainEntity captain && captain.smallShipsController.ship != null && captain.smallShipsController.ship.isCaptainDriver()){
                spacing *= 10;
                break;
            }
        }

        double rowDistance = tight ? spacing : spacing * 1.75;

        for (int i = 0; i < recruits.size(); i++) {
            int row = i / maxInRow;
            int recruitsInCurrentRow = Math.min(maxInRow, recruits.size() - row * maxInRow);
            int positionInRow = i % maxInRow;

            double centerOffset = (recruitsInCurrentRow - 1) / 2.0;

            Vec3 basePos = targetPos.add(forward.scale(-rowDistance * row));
            Vec3 offset = left.scale((positionInRow - centerOffset) * spacing);

            Vec3 recruitPos = basePos.add(offset);
            possiblePositions.add(new FormationPosition(recruitPos, true));
        }

        for (AbstractRecruitEntity recruit : recruits) {
            Vec3 pos = null;

            if (recruit.formationPos >= 0 && recruit.formationPos < possiblePositions.size() && possiblePositions.get(recruit.formationPos).isFree) {
                FormationPosition position = possiblePositions.get(recruit.formationPos);
                position.isFree = false;
                pos = position.position;
            } else {
                for (int i = 0; i < possiblePositions.size(); i++) {
                    FormationPosition position = possiblePositions.get(i);
                    if (position.isFree) {
                        pos = possiblePositions.get(i).position;
                        recruit.formationPos = i;
                        position.isFree = false;
                        break;
                    }
                }
            }

            if (pos != null) {
                BlockPos blockPos = FormationUtils.getPositionOrSurface(
                        recruit.getCommandSenderWorld(),
                        new BlockPos((int) Math.round(pos.x), (int) Math.round(pos.y), (int) Math.round(pos.z))
                );

                recruit.setHoldPos(tight ? new Vec3(blockPos.getX(), blockPos.getY(), blockPos.getZ()) : new Vec3(pos.x, blockPos.getY(), pos.z));
                //recruit.ownerRot = player.getYRot();
                recruit.setFollowState(3);
                recruit.isInFormation = true;
                recruit.holdFormation = hold;
            }
        }
    }
    public static void squareFormation(ServerPlayer player, List<AbstractRecruitEntity> recruits, Vec3 targetPos) {
        squareFormation(player, recruits, targetPos, 1.0, false);
    }
    public static void squareFormation(ServerPlayer player, List<AbstractRecruitEntity> recruits, Vec3 targetPos, boolean hold) {
        squareFormation(player, recruits, targetPos, 1.0, hold);
    }

    public static void squareFormation(ServerPlayer player, List<AbstractRecruitEntity> recruits, Vec3 targetPos, double spacingMultiplier, boolean hold) {
        float yaw = player.getYRot();
        Vec3 forward = new Vec3(-Math.sin(Math.toRadians(yaw)), 0, Math.cos(Math.toRadians(yaw)));
        squareFormation(forward, recruits, targetPos, 2.5 * spacingMultiplier, hold);
    }

    public static void squareFormation(ServerPlayer player, List<AbstractRecruitEntity> recruits, Vec3 targetPos, boolean tight, boolean hold) {
        if (!tight) {
            squareFormation(player, recruits, targetPos, 1.0, hold);
            return;
        }

        Vec3 forward = nearestCardinalForward(player.getYRot());
        squareFormation(forward, recruits, targetPos, 1.0, hold, true);
    }

    private static Vec3 nearestCardinalForward(float yaw) {
        float normalizedYaw = ((yaw % 360F) + 360F) % 360F;
        int octant = Math.round(normalizedYaw / 90F) % 4;
        return switch (octant) {
            case 1 -> new Vec3(-1, 0, 0);
            case 2 -> new Vec3(0, 0, -1);
            case 3 -> new Vec3(1, 0, 0);
            default -> new Vec3(0, 0, 1);
        };
    }
    public static void squareFormation(Vec3 forward, List<AbstractRecruitEntity> recruits, Vec3 targetPos, double spacing) {
        squareFormation(forward, recruits, targetPos, spacing, false);
    }
    public static void squareFormation(Vec3 forward, List<AbstractRecruitEntity> recruits, Vec3 targetPos, double spacing, boolean hold) {
        squareFormation(forward, recruits, targetPos, spacing, hold, false);
    }
    public static void squareFormation(Vec3 forward, List<AbstractRecruitEntity> recruits, Vec3 targetPos, double spacing, boolean hold, boolean tight) {
        Vec3 left = new Vec3(-forward.z, forward.y, forward.x);

        for(AbstractRecruitEntity rec : recruits){
            if(rec instanceof CaptainEntity captain && captain.smallShipsController.ship != null && captain.smallShipsController.ship.isCaptainDriver()){
                spacing *= 10;
                break;
            }
        }

        int numRecruits = recruits.size();
        int sideLength = (int) Math.ceil(Math.sqrt(numRecruits));

        List<FormationPosition> possiblePositions = new ArrayList<>();

        for (int i = 0; i < numRecruits; i++) {
            int row = i / sideLength;
            int col = i % sideLength;

            Vec3 rowOffset = forward.scale(-row * spacing);
            Vec3 colOffset = left.scale((col - (sideLength - 1) / 2.0) * spacing);

            Vec3 recruitPos = targetPos.add(rowOffset).add(colOffset);
            possiblePositions.add(new FormationPosition(recruitPos, true));
        }

        for (AbstractRecruitEntity recruit : recruits) {
            Vec3 pos = null;

            if (recruit.formationPos >= 0 && recruit.formationPos < possiblePositions.size() && possiblePositions.get(recruit.formationPos).isFree) {
                FormationPosition position = possiblePositions.get(recruit.formationPos);
                position.isFree = false;
                pos = position.position;
            }
            else {
                for (int i = 0; i < possiblePositions.size(); i++) {
                    FormationPosition position = possiblePositions.get(i);
                    if (position.isFree) {
                        pos = position.position;
                        recruit.formationPos = i; // Remember this position for next time
                        position.isFree = false;
                        break;
                    }
                }
            }

            if (pos != null) {
                BlockPos blockPos = FormationUtils.getPositionOrSurface(
                        recruit.getCommandSenderWorld(),
                        new BlockPos((int) Math.round(pos.x), (int) Math.round(pos.y), (int) Math.round(pos.z))
                );

                recruit.setHoldPos(tight ? new Vec3(blockPos.getX(), blockPos.getY(), blockPos.getZ()) : new Vec3(pos.x, blockPos.getY(), pos.z));
                //recruit.ownerRot = forwar;
                recruit.setFollowState(3);
                recruit.isInFormation = true;
                recruit.holdFormation = hold;
            }
        }
    }



    public static void triangleFormation(ServerPlayer player, List<AbstractRecruitEntity> recruits, Vec3 targetPos) {
        triangleFormation(player, recruits, targetPos, 1.0, false);
    }

    public static void triangleFormation(ServerPlayer player, List<AbstractRecruitEntity> recruits, Vec3 targetPos, double spacingMultiplier, boolean hold) {
        float yaw = player.getYRot();
        Vec3 forward = new Vec3(-Math.sin(Math.toRadians(yaw)), 0, Math.cos(Math.toRadians(yaw)));
        triangleFormation(forward, recruits, targetPos, 2.5 * spacingMultiplier, hold, false, yaw);
    }

    public static void triangleFormation(ServerPlayer player, List<AbstractRecruitEntity> recruits, Vec3 targetPos, boolean tight, boolean hold) {
        if (!tight) {
            triangleFormation(player, recruits, targetPos, 1.0, hold);
            return;
        }

        float yaw = player.getYRot();
        Vec3 forward = nearestCardinalForward(yaw);
        triangleFormation(forward, recruits, targetPos, 1.0, hold, true, yaw);
    }

    private static void triangleFormation(Vec3 forward, List<AbstractRecruitEntity> recruits, Vec3 targetPos, double spacing, boolean hold, boolean tight, float ownerRot) {
        Vec3 left = new Vec3(-forward.z, forward.y, forward.x);
        int numRecruits = recruits.size();

        for(AbstractRecruitEntity rec : recruits){
            if(rec instanceof CaptainEntity captain && captain.smallShipsController.ship != null && captain.smallShipsController.ship.isCaptainDriver()){
                spacing *= 10;
                break;
            }
        }

        double rowDistance = tight ? spacing : spacing * 1.5;

        List<FormationPosition> possiblePositions = new ArrayList<>();

        int index = 0;
        int rowCount = 1;
        while (index < numRecruits) {
            for (int positionInRow = 0; positionInRow < rowCount && index < numRecruits; positionInRow++, index++) {
                Vec3 basePos = targetPos.add(forward.scale(-rowDistance * (rowCount - 1)));
                Vec3 offset = left.scale((positionInRow - (rowCount - 1) / 2F) * spacing);

                Vec3 recruitPos = basePos.add(offset);
                possiblePositions.add(new FormationPosition(recruitPos, true));
            }
            rowCount++;
        }

        for (AbstractRecruitEntity recruit : recruits) {
            Vec3 pos = null;

            if (recruit.formationPos >= 0 && recruit.formationPos < possiblePositions.size() && possiblePositions.get(recruit.formationPos).isFree) {
                FormationPosition position = possiblePositions.get(recruit.formationPos);
                position.isFree = false;
                pos = position.position;
            } else {
                for (int i = 0; i < possiblePositions.size(); i++) {
                    FormationPosition position = possiblePositions.get(i);
                    if (position.isFree) {
                        pos = possiblePositions.get(i).position;
                        recruit.formationPos = i;
                        position.isFree = false;
                        break;
                    }
                }
            }

            if (pos != null) {
                BlockPos blockPos = FormationUtils.getPositionOrSurface(
                        recruit.getCommandSenderWorld(),
                        new BlockPos((int) Math.round(pos.x), (int) Math.round(pos.y), (int) Math.round(pos.z))
                );

                Vec3 holdPos = tight ? new Vec3(blockPos.getX(), blockPos.getY(), blockPos.getZ()) : new Vec3(pos.x, blockPos.getY(), pos.z);
                recruit.setHoldPos(holdPos);
                recruit.ownerRot = ownerRot;
                recruit.setFollowState(3);
                recruit.isInFormation = true;
                recruit.holdFormation = hold;
            }
        }
    }

    public static void hollowCircleFormation(ServerPlayer player, List<AbstractRecruitEntity> recruits, Vec3 targetPos) {
        hollowCircleFormation(player, recruits, targetPos, 1.0, false);
    }

    public static void hollowCircleFormation(ServerPlayer player, List<AbstractRecruitEntity> recruits, Vec3 targetPos, double spacingMultiplier, boolean hold) {
        hollowCircleFormation(recruits, targetPos, 2.5 * spacingMultiplier, hold, false, player.getYRot());
    }

    public static void hollowCircleFormation(ServerPlayer player, List<AbstractRecruitEntity> recruits, Vec3 targetPos, boolean tight, boolean hold) {
        if (!tight) {
            hollowCircleFormation(player, recruits, targetPos, 1.0, hold);
            return;
        }

        hollowCircleFormation(recruits, targetPos, 1.0, hold, true, player.getYRot());
    }

    private static void hollowCircleFormation(List<AbstractRecruitEntity> recruits, Vec3 targetPos, double spacing, boolean hold, boolean tight, float ownerRot) {
        int numRecruits = recruits.size();

        for(AbstractRecruitEntity rec : recruits){
            if(rec instanceof CaptainEntity captain && captain.smallShipsController.ship != null && captain.smallShipsController.ship.isCaptainDriver()){
                spacing *= 10;
                break;
            }
        }

        double radius = spacing * numRecruits / (2 * Math.PI); // Calculate radius based on the number of recruits
        List<FormationPosition> possiblePositions = new ArrayList<>();

        for (int i = 0; i < numRecruits; i++) {
            double angle = (2 * Math.PI / numRecruits) * i; // Angle for each recruit

            // Calculate position for each recruit in the circle
            double x = targetPos.x + radius * Math.cos(angle);
            double z = targetPos.z + radius * Math.sin(angle);
            Vec3 recruitPos = new Vec3(x, targetPos.y, z);

            possiblePositions.add(new FormationPosition(recruitPos, true));
        }

        java.util.Set<Long> usedCells = new java.util.HashSet<>();
        for (AbstractRecruitEntity recruit : recruits) {
            Vec3 pos = null;

            if (recruit.formationPos >= 0 && recruit.formationPos < possiblePositions.size() && possiblePositions.get(recruit.formationPos).isFree) {
                FormationPosition position = possiblePositions.get(recruit.formationPos);
                position.isFree = false;
                pos = position.position;
            } else {
                for (int i = 0; i < possiblePositions.size(); i++) {
                    FormationPosition position = possiblePositions.get(i);
                    if (position.isFree) {
                        pos = possiblePositions.get(i).position;
                        recruit.formationPos = i;
                        position.isFree = false;
                        break;
                    }
                }
            }

            if (pos != null) {
                BlockPos blockPos = FormationUtils.getPositionOrSurface(
                        recruit.getCommandSenderWorld(),
                        roundedCell(pos, tight, usedCells)
                );

                Vec3 holdPos = tight ? new Vec3(blockPos.getX(), blockPos.getY(), blockPos.getZ()) : new Vec3(pos.x, blockPos.getY(), pos.z);
                recruit.setHoldPos(holdPos);
                recruit.ownerRot = ownerRot;
                recruit.setFollowState(3);
                recruit.isInFormation = true;
                recruit.holdFormation = hold;
            }
        }
    }

    public static void circleFormation(ServerPlayer player, List<AbstractRecruitEntity> recruits, Vec3 targetPos) {
        circleFormation(player, recruits, targetPos, 1.0, false);
    }

    public static void circleFormation(ServerPlayer player, List<AbstractRecruitEntity> recruits, Vec3 targetPos, double spacingMultiplier, boolean hold) {
        circleFormation(recruits, targetPos, 2.5 * spacingMultiplier, hold, false, player.getYRot());
    }

    public static void circleFormation(ServerPlayer player, List<AbstractRecruitEntity> recruits, Vec3 targetPos, boolean tight, boolean hold) {
        if (!tight) {
            circleFormation(player, recruits, targetPos, 1.0, hold);
            return;
        }

        circleFormation(recruits, targetPos, 1.0, hold, true, player.getYRot());
    }

    private static void circleFormation(List<AbstractRecruitEntity> recruits, Vec3 targetPos, double spacing, boolean hold, boolean tight, float ownerRot) {
        int numRecruits = recruits.size();

        for(AbstractRecruitEntity rec : recruits){
            if(rec instanceof CaptainEntity captain && captain.smallShipsController.ship != null && captain.smallShipsController.ship.isCaptainDriver()){
                spacing *= 10;
                break;
            }
        }

        // Aufteilen der Rekruten auf drei Ringe
        int innerRingCount = Math.min(5, numRecruits); // Innerer Ring hat max 5
        int middleRingCount = Math.min(10, numRecruits - innerRingCount); // Mittlerer Ring hat max 10
        int outerRingCount = numRecruits - innerRingCount - middleRingCount; // Äußerer Ring bekommt den Rest

        double innerRadius = spacing * innerRingCount / (2 * Math.PI); // Radius des inneren Rings
        double middleRadius = middleRingCount > 0 ? Math.max(spacing * middleRingCount / (2 * Math.PI), innerRadius + spacing) : 0;
        double outerRadius = outerRingCount > 0 ? Math.max(spacing * outerRingCount / (2 * Math.PI), Math.max(middleRadius, innerRadius) + spacing) : 0;

        List<FormationPosition> possiblePositions = new ArrayList<>();

        // Positionen für den inneren Ring
        for (int i = 0; i < innerRingCount; i++) {
            double angle = (2 * Math.PI / innerRingCount) * i;
            double x = targetPos.x + innerRadius * Math.cos(angle);
            double z = targetPos.z + innerRadius * Math.sin(angle);
            Vec3 recruitPos = new Vec3(x, targetPos.y, z);
            possiblePositions.add(new FormationPosition(recruitPos, true));
        }

        // Positionen für den mittleren Ring
        for (int i = 0; i < middleRingCount; i++) {
            double angle = (2 * Math.PI / middleRingCount) * i;
            double x = targetPos.x + middleRadius * Math.cos(angle);
            double z = targetPos.z + middleRadius * Math.sin(angle);
            Vec3 recruitPos = new Vec3(x, targetPos.y, z);
            possiblePositions.add(new FormationPosition(recruitPos, true));
        }

        // Positionen für den äußeren Ring
        for (int i = 0; i < outerRingCount; i++) {
            double angle = (2 * Math.PI / outerRingCount) * i;
            double x = targetPos.x + outerRadius * Math.cos(angle);
            double z = targetPos.z + outerRadius * Math.sin(angle);
            Vec3 recruitPos = new Vec3(x, targetPos.y, z);
            possiblePositions.add(new FormationPosition(recruitPos, true));
        }

        // Zuweisen der Positionen an die Rekruten
        java.util.Set<Long> usedCells = new java.util.HashSet<>();
        for (AbstractRecruitEntity recruit : recruits) {
            Vec3 pos = null;

            if (recruit.formationPos >= 0 && recruit.formationPos < possiblePositions.size() && possiblePositions.get(recruit.formationPos).isFree) {
                FormationPosition position = possiblePositions.get(recruit.formationPos);
                position.isFree = false;
                pos = position.position;
            } else {
                for (int i = 0; i < possiblePositions.size(); i++) {
                    FormationPosition position = possiblePositions.get(i);
                    if (position.isFree) {
                        pos = possiblePositions.get(i).position;
                        recruit.formationPos = i;
                        position.isFree = false;
                        break;
                    }
                }
            }

            if (pos != null) {
                BlockPos blockPos = FormationUtils.getPositionOrSurface(
                        recruit.getCommandSenderWorld(),
                        roundedCell(pos, tight, usedCells)
                );

                Vec3 holdPos = tight ? new Vec3(blockPos.getX(), blockPos.getY(), blockPos.getZ()) : new Vec3(pos.x, blockPos.getY(), pos.z);
                recruit.setHoldPos(holdPos);
                recruit.ownerRot = ownerRot;
                recruit.setFollowState(3);
                recruit.isInFormation = true;
                recruit.holdFormation = hold;
            }
        }
    }

    public static void hollowSquareFormation(ServerPlayer player, List<AbstractRecruitEntity> recruits, Vec3 targetPos) {
        hollowSquareFormation(player, recruits, targetPos, 1.0, false);
    }

    public static void hollowSquareFormation(ServerPlayer player, List<AbstractRecruitEntity> recruits, Vec3 targetPos, double spacingMultiplier, boolean hold) {
        float yaw = player.getYRot();
        Vec3 forward = new Vec3(-Math.sin(Math.toRadians(yaw)), 0, Math.cos(Math.toRadians(yaw)));
        hollowSquareFormation(forward, recruits, targetPos, 2.5 * spacingMultiplier, hold, false, yaw);
    }

    public static void hollowSquareFormation(ServerPlayer player, List<AbstractRecruitEntity> recruits, Vec3 targetPos, boolean tight, boolean hold) {
        if (!tight) {
            hollowSquareFormation(player, recruits, targetPos, 1.0, hold);
            return;
        }

        float yaw = player.getYRot();
        Vec3 forward = nearestCardinalForward(yaw);
        hollowSquareFormation(forward, recruits, targetPos, 1.0, hold, true, yaw);
    }

    private static void hollowSquareFormation(Vec3 forward, List<AbstractRecruitEntity> recruits, Vec3 targetPos, double spacing, boolean hold, boolean tight, float ownerRot) {
        Vec3 left = new Vec3(-forward.z, forward.y, forward.x);

        int recruitsPerSide = Math.max(2, recruits.size() / 4); // Ensure at least 2 recruits per side

        for(AbstractRecruitEntity rec : recruits){
            if(rec instanceof CaptainEntity captain && captain.smallShipsController.ship != null && captain.smallShipsController.ship.isCaptainDriver()){
                spacing *= 10;
                break;
            }
        }

        int totalRecruitsNeeded = recruitsPerSide * 4;
        if (totalRecruitsNeeded > recruits.size()) {
            recruitsPerSide = recruits.size() / 4;
            totalRecruitsNeeded = recruitsPerSide * 4;
        }

        List<FormationPosition> possiblePositions = new ArrayList<>();

        for (int row = 0; row < 2; row++) { // Two rows per side
            double offset = (spacing * recruitsPerSide) / 2.0;
            for (int i = 0; i < recruitsPerSide; i++) {
                double positionOffset = i * spacing - offset;

                possiblePositions.add(new FormationPosition(targetPos.add(forward.scale(-offset - row * spacing)).add(left.scale(positionOffset)), true));

                possiblePositions.add(new FormationPosition(targetPos.add(forward.scale(offset + row * spacing)).add(left.scale(positionOffset)), true));

                possiblePositions.add(new FormationPosition(targetPos.add(left.scale(-offset - row * spacing)).add(forward.scale(positionOffset)), true));

                possiblePositions.add(new FormationPosition(targetPos.add(left.scale(offset + row * spacing)).add(forward.scale(positionOffset)), true));
            }
        }

        for (AbstractRecruitEntity recruit : recruits) {
            Vec3 pos = null;

            if (recruit.formationPos >= 0 && recruit.formationPos < possiblePositions.size() && possiblePositions.get(recruit.formationPos).isFree) {
                FormationPosition position = possiblePositions.get(recruit.formationPos);
                position.isFree = false;
                pos = position.position;
            }

            else {
                for (int i = 0; i < possiblePositions.size(); i++) {
                    FormationPosition position = possiblePositions.get(i);
                    if (position.isFree) {
                        pos = position.position;
                        recruit.formationPos = i; // Remember this position for next time
                        position.isFree = false;
                        break;
                    }
                }
            }

            if (pos != null) {
                BlockPos blockPos = FormationUtils.getPositionOrSurface(
                        recruit.getCommandSenderWorld(),
                        new BlockPos((int) Math.round(pos.x), (int) Math.round(pos.y), (int) Math.round(pos.z))
                );

                Vec3 holdPos = tight ? new Vec3(blockPos.getX(), blockPos.getY(), blockPos.getZ()) : new Vec3(pos.x, blockPos.getY(), pos.z);
                recruit.setHoldPos(holdPos);
                recruit.ownerRot = ownerRot;
                recruit.setFollowState(3);
                recruit.isInFormation = true;
                recruit.holdFormation = hold;
            }
        }
    }


    public static void vFormation(ServerPlayer player, List<AbstractRecruitEntity> recruits, Vec3 targetPos) {
        vFormation(player, recruits, targetPos, 1.0, false);
    }

    public static void vFormation(ServerPlayer player, List<AbstractRecruitEntity> recruits, Vec3 targetPos, double spacingMultiplier, boolean hold) {
        float yaw = player.getYRot();
        Vec3 forward = new Vec3(-Math.sin(Math.toRadians(yaw)), 0, Math.cos(Math.toRadians(yaw)));
        vFormation(forward, recruits, targetPos, 2.5 * spacingMultiplier, hold, false, yaw);
    }

    public static void vFormation(ServerPlayer player, List<AbstractRecruitEntity> recruits, Vec3 targetPos, boolean tight, boolean hold) {
        if (!tight) {
            vFormation(player, recruits, targetPos, 1.0, hold);
            return;
        }

        float yaw = player.getYRot();
        Vec3 forward = nearestCardinalForward(yaw);
        vFormation(forward, recruits, targetPos, 1.0, hold, true, yaw);
    }

    private static void vFormation(Vec3 forward, List<AbstractRecruitEntity> recruits, Vec3 targetPos, double spacing, boolean hold, boolean tight, float ownerRot) {
        Vec3 left = new Vec3(-forward.z, forward.y, forward.x);

        int recruitsPerWing = recruits.size() / 2;

        for(AbstractRecruitEntity rec : recruits){
            if(rec instanceof CaptainEntity captain && captain.smallShipsController.ship != null && captain.smallShipsController.ship.isCaptainDriver()){
                spacing *= 10;
                break;
            }
        }

        List<FormationPosition> possiblePositions = new ArrayList<>();

        for (int i = 0; i < recruitsPerWing; i++) {
            double offset = i * spacing;


            Vec3 rightWingPos = targetPos.add(forward.scale(offset)).add(left.scale(offset));
            possiblePositions.add(new FormationPosition(rightWingPos, true));


            Vec3 leftWingPos = targetPos.add(forward.scale(offset)).subtract(left.scale(offset));
            possiblePositions.add(new FormationPosition(leftWingPos, true));
        }


        if (recruits.size() % 2 != 0) {
            possiblePositions.add(new FormationPosition(targetPos, true));
        }


        for (int i = 0; i < recruits.size() && i < possiblePositions.size(); i++) {
            AbstractRecruitEntity recruit = recruits.get(i);
            Vec3 pos = possiblePositions.get(i).position;

            BlockPos blockPos = FormationUtils.getPositionOrSurface(
                    recruit.getCommandSenderWorld(),
                    new BlockPos((int) Math.round(pos.x), (int) Math.round(pos.y), (int) Math.round(pos.z))
            );

            Vec3 holdPos = tight ? new Vec3(blockPos.getX(), blockPos.getY(), blockPos.getZ()) : new Vec3(pos.x, blockPos.getY(), pos.z);
            recruit.setHoldPos(holdPos);
            recruit.ownerRot = ownerRot;
            recruit.setFollowState(3);
            recruit.isInFormation = true;
            recruit.holdFormation = hold;
        }
    }

    public static class FormationPosition{
        public Vec3 position;
        public boolean isFree;

        FormationPosition(Vec3 position, boolean isFree){
            this.position = position;
            this.isFree = isFree;
        }
    }

    public static Vec3 getCenterOfPositions(List<LivingEntity> recruits, ServerLevel level) {
        if (recruits.isEmpty()) return Vec3.ZERO;

        double sumX = 0;
        double sumY = 0;
        double sumZ = 0;

        for (LivingEntity recruit : recruits) {
            Vec3 pos = recruit.position();
            sumX += pos.x;
            sumY += pos.y;
            sumZ += pos.z;
        }

        double centerX = sumX / recruits.size();
        double centerY = sumY / recruits.size();
        double centerZ = sumZ / recruits.size();

        BlockPos blockPos = FormationUtils.getPositionOrSurface(
                level,
                new BlockPos((int) Math.round(centerX), (int) Math.round(centerY), (int) Math.round(centerZ))
        );

        return new Vec3(centerX, blockPos.getY(), centerZ);
    }

    public static Vec3 getFarthestRecruitsCenter(List<AbstractRecruitEntity> recruits, ServerLevel level) {
        if (recruits.size() < 2) {
            return recruits.isEmpty() ? Vec3.ZERO : recruits.get(0).position();
        }

        AbstractRecruitEntity farthestRecruit1 = null;
        AbstractRecruitEntity farthestRecruit2 = null;
        double maxDistance = Double.MIN_VALUE;

        for (int i = 0; i < recruits.size() - 1; i++) {
            for (int j = i + 1; j < recruits.size(); j++) {
                double distance = recruits.get(i).distanceToSqr(recruits.get(j));
                if (distance > maxDistance) {
                    maxDistance = distance;
                    farthestRecruit1 = recruits.get(i);
                    farthestRecruit2 = recruits.get(j);
                }
            }
        }

        Vec3 pos1 = Objects.requireNonNull(farthestRecruit1).position();
        Vec3 pos2 = Objects.requireNonNull(farthestRecruit2).position();

        double centerX = (pos1.x + pos2.x) / 2.0;
        double centerY = (pos1.y + pos2.y) / 2.0;
        double centerZ = (pos1.z + pos2.z) / 2.0;

        BlockPos blockPos = FormationUtils.getPositionOrSurface(
                level,
                new BlockPos((int) Math.round(centerX), (int) Math.round(centerY), (int) Math.round(centerZ))
        );

        return new Vec3(centerX, blockPos.getY(), centerZ);
    }

    public static Vec3 getGeometricMedian(List<AbstractRecruitEntity> recruits, ServerLevel level) {
        if (recruits.isEmpty()) {
            return Vec3.ZERO;
        }

        // Initial guess: average position
        double sumX = 0, sumY = 0, sumZ = 0;
        for (AbstractRecruitEntity recruit : recruits) {
            Vec3 pos = recruit.position();
            sumX += pos.x;
            sumY += pos.y;
            sumZ += pos.z;
        }
        Vec3 currentGuess = new Vec3(sumX / recruits.size(), sumY / recruits.size(), sumZ / recruits.size());

        // Weiszfeld algorithm
        double tolerance = 1e-4;
        int maxIterations = 100;
        for (int iteration = 0; iteration < maxIterations; iteration++) {
            double numeratorX = 0, numeratorY = 0, numeratorZ = 0;
            double denominator = 0;

            for (AbstractRecruitEntity recruit : recruits) {
                Vec3 pos = recruit.position();
                double distance = currentGuess.distanceTo(pos);

                if (distance < tolerance) {
                    continue;
                }

                double weight = 1 / distance;
                numeratorX += pos.x * weight;
                numeratorY += pos.y * weight;
                numeratorZ += pos.z * weight;
                denominator += weight;
            }

            if (denominator == 0) {
                break;
            }

            Vec3 newGuess = new Vec3(numeratorX / denominator, numeratorY / denominator, numeratorZ / denominator);

            if (currentGuess.distanceTo(newGuess) < tolerance) {
                break;
            }

            currentGuess = newGuess;
        }

        BlockPos blockPos = FormationUtils.getPositionOrSurface(
                level,
                new BlockPos((int) Math.round(currentGuess.x), (int) Math.round(currentGuess.y), (int) Math.round(currentGuess.z))
        );

        return new Vec3(currentGuess.x, blockPos.getY(), currentGuess.z);
    }

    // Tight formations sit on the block grid; if two spots round to the same corner, take the nearest free one.
    private static BlockPos roundedCell(Vec3 pos, boolean tight, java.util.Set<Long> used) {
        BlockPos rounded = new BlockPos((int) Math.round(pos.x), (int) Math.round(pos.y), (int) Math.round(pos.z));
        if (!tight) return rounded;
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                BlockPos cell = rounded.offset(dx, 0, dz);
                if (used.contains(cell.asLong())) continue;
                double dist = (cell.getX() - pos.x) * (cell.getX() - pos.x) + (cell.getZ() - pos.z) * (cell.getZ() - pos.z);
                if (dist < bestDist) { bestDist = dist; best = cell; }
            }
        }
        if (best == null) return rounded;
        used.add(best.asLong());
        return best;
    }

    public static BlockPos getPositionOrSurface(Level level, BlockPos pos) {
        if (isFreeSpot(level, pos.above(2)) && isFreeSpot(level, pos.above()) && isFreeSpot(level, pos)) {
            return pos;
        }

        // Stay on the same floor: only 1-2 blocks up or down, never the roof.
        for (int d = 1; d <= 2; d++) {
            for (int sign : new int[]{1, -1}) {
                BlockPos candidate = pos.above(d * sign);
                if (isStandable(level, candidate)) return candidate;
            }
        }

        return pos;
    }

    private static boolean isFreeSpot(Level level, BlockPos pos) {
        return level.getBlockState(pos).isAir();
    }

    private static boolean isStandable(Level level, BlockPos pos) {
        return level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()
                && level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty()
                && !level.getBlockState(pos.below()).getCollisionShape(level, pos.below()).isEmpty();
    }
}
