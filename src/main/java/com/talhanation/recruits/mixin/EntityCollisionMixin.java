package com.talhanation.recruits.mixin;

import com.talhanation.recruits.entities.AbstractRecruitEntity;
import com.talhanation.recruits.util.CollidableEntityTracker;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(Entity.class)
public class EntityCollisionMixin {
    @Redirect(method = "collide", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getEntityCollisions(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;"))
    private List<VoxelShape> talhanation$skipEmptyCollisionScan(Level level, Entity entity, AABB aabb) {
        if (entity instanceof AbstractRecruitEntity && !CollidableEntityTracker.hasCollidableEntities(level)) {
            return List.of();
        }
        return level.getEntityCollisions(entity, aabb);
    }
}
