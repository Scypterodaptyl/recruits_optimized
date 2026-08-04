package com.talhanation.recruits.entities.ai.async;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.Team;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;

public record AsyncAttackContext(Map<Entity, Team> teams, Set<String> blacklist, @Nullable Player owner) {
}
