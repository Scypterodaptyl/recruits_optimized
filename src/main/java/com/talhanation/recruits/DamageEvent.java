package com.talhanation.recruits;

import com.talhanation.recruits.config.RecruitsServerConfig;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;

public class DamageEvent {


    public static boolean onPlayerAttack(AttackEntityEvent event) {
        if (true) {
            Player player = event.getEntity();
            if (player.level().isClientSide()) {
                return false;
            }
            float str = player.getAttackStrengthScale(0);
            if (str <= 0.1) {
                return true;
            }
            if (str <= 0.75) {
                Entity target = event.getTarget();
                if (target != null && target instanceof LivingEntity) {
                    ((LivingEntity)target).swinging = true;
                }
            }
        }
        return false;
    }


    public static boolean onKnockback(LivingKnockBackEvent event) {
        if (true) {
            LivingEntity entity = event.getEntity();
            if (entity.swinging) {
                entity.swinging = false;
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent
    public void onEntityHurt(LivingHurtEvent event) {
        if (true) {

            LivingEntity target = event.getEntity();

            if (target.level().isClientSide()) {
                return;
            }

            DamageSource source = event.getSource();
            Entity sourceEntity = event.getEntity();

            if(!RecruitsServerConfig.NoDamageImmunity.get()) return;

            if (target.level().isClientSide()) {
                return;
            }

            //Velocity Damage
            if(source != null && sourceEntity != null){

            }

            //NO Damage Immunity
            if(!RecruitsServerConfig.NoDamageImmunity.get()) return;


            if (source != null && RecruitsServerConfig.AcceptedDamagesourceImmunity.get().contains(source.getMsgId())) {
                return;
            }
            target.invulnerableTime = 0;
        }
    }

    @SubscribeEvent
    public boolean onEntityHurtByPlayer(AttackEntityEvent event) {
        if (true) {
            Player player = event.getEntity();
            Entity target = event.getTarget();

            if(target.getFirstPassenger() instanceof LivingEntity passenger){
                if (!RecruitEvents.canHarmTeam(player, passenger)) {
                    return true;
                }
            }
        }
        return false;
    }
}