package com.talhanation.recruits.mixin;

import com.talhanation.recruits.entities.AbstractRecruitEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Animal.class)
public class AnimalMixin {

    @SuppressWarnings("DataFlowIssue")
    @Inject(method = "actuallyHurt", at = @At(value = "HEAD"))
    private void hurtWhenRecruitsRides(net.minecraft.server.level.ServerLevel level, DamageSource source, float amount, CallbackInfo ci) {
        if (((Animal)(Object)this).isAlive() && ((Animal)(Object)this).isVehicle() && ((Animal)(Object)this).getControllingPassenger() instanceof AbstractRecruitEntity recruit) {
            if(source.getEntity() instanceof LivingEntity target && recruit.canAttack(target))
                recruit.setTarget(target);
        }
    }
}
