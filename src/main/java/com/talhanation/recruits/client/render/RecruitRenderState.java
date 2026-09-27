package com.talhanation.recruits.client.render;

import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

/**
 * Render state of recruits, filled from the entity every frame.
 */
public class RecruitRenderState extends HumanoidRenderState {
    public int variant;
    public int color;
    public int biome;
    public boolean hasTeam;
    public boolean isCompanion;
}
