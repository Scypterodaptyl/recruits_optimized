package com.talhanation.recruits.client.events;


import com.talhanation.recruits.Main;
import com.talhanation.recruits.client.models.RecruitVillagerModel;
import com.talhanation.recruits.client.gui.worldmap.storage.WorldMapCacheManager;
import com.talhanation.recruits.client.render.RecruitHumanRenderer;
import com.talhanation.recruits.client.render.RecruitVillagerRenderer;
import com.talhanation.recruits.client.render.layer.RecruitArmorLayer;
import com.talhanation.recruits.config.RecruitsClientConfig;
import com.talhanation.recruits.init.ModEntityTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.AddGuiOverlayLayersEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import com.talhanation.recruits.client.gui.overlay.ClaimOverlayManager;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraft.client.renderer.entity.ArmorModelSet;

import javax.annotation.Nullable;

public class ClientEvent {

    public static ModelLayerLocation RECRUIT = new ModelLayerLocation(Identifier.parse(Main.MOD_ID + "recruit"), "recruit");
    public static final ArmorModelSet<ModelLayerLocation> RECRUIT_ARMOR = new ArmorModelSet<>(
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(Main.MOD_ID, "recruit_armor"), "helmet"),
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(Main.MOD_ID, "recruit_armor"), "chestplate"),
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(Main.MOD_ID, "recruit_armor"), "leggings"),
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(Main.MOD_ID, "recruit_armor"), "boots"));

    public static void register(BusGroup modBusGroup) {
        net.minecraftforge.client.event.RegisterPictureInPictureRendererEvent.BUS.addListener(e ->
                e.register(new com.talhanation.recruits.client.render.pip.ScaledBannerRenderer(e.getBufferSource())));
        EntityRenderersEvent.RegisterRenderers.getBus(modBusGroup).addListener(ClientEvent::entityRenderersEvent);
        EntityRenderersEvent.RegisterLayerDefinitions.getBus(modBusGroup).addListener(ClientEvent::layerDefinitions);
        ModelEvent.BakingCompleted.getBus(modBusGroup).addListener(ClientEvent::modelBakingCompleted);
        TextureStitchEvent.Post.getBus(modBusGroup).addListener(ClientEvent::textureStitchCompleted);
        AddGuiOverlayLayersEvent.BUS.addListener(event -> event.getLayeredDraw().add(
                Identifier.fromNamespaceAndPath(Main.MOD_ID, "claim_overlay"),
                (guiGraphics, deltaTracker) -> {
                    if (ClaimOverlayManager.INSTANCE != null) ClaimOverlayManager.INSTANCE.onRenderGameOverlay(guiGraphics);
                }));
    }
    public static final ModelLayerLocation RECRUIT_HUMAN_TEAM_LAYER = humanOverlayLayer("team");
    public static final ModelLayerLocation RECRUIT_HUMAN_BIOME_LAYER = humanOverlayLayer("biome");
    public static final ModelLayerLocation RECRUIT_HUMAN_COMPANION_LAYER = humanOverlayLayer("companion");

    public static void entityRenderersEvent(EntityRenderersEvent.RegisterRenderers event){
        if(RecruitsClientConfig.RecruitsLookLikeVillagers.get()){
            event.registerEntityRenderer(ModEntityTypes.RECRUIT.get(), RecruitVillagerRenderer::new );
            event.registerEntityRenderer(ModEntityTypes.BOWMAN.get(), RecruitVillagerRenderer::new );
            event.registerEntityRenderer(ModEntityTypes.NOMAD.get(), RecruitVillagerRenderer::new );
            event.registerEntityRenderer(ModEntityTypes.HORSEMAN.get(), RecruitVillagerRenderer::new );
            event.registerEntityRenderer(ModEntityTypes.CROSSBOWMAN.get(), RecruitVillagerRenderer::new );
            event.registerEntityRenderer(ModEntityTypes.RECRUIT_SHIELDMAN.get(), RecruitVillagerRenderer::new );

            //COMPANIONS
            event.registerEntityRenderer(ModEntityTypes.MESSENGER.get(), RecruitVillagerRenderer::new );
            event.registerEntityRenderer(ModEntityTypes.SCOUT.get(), RecruitVillagerRenderer::new );
            event.registerEntityRenderer(ModEntityTypes.PATROL_LEADER.get(), RecruitVillagerRenderer::new );
            event.registerEntityRenderer(ModEntityTypes.CAPTAIN.get(), RecruitVillagerRenderer::new );
            event.registerEntityRenderer(ModEntityTypes.SIEGE_ENGINEER.get(), RecruitVillagerRenderer::new );

            //OTHER
            event.registerEntityRenderer(ModEntityTypes.VILLAGER_NOBLE.get(), RecruitVillagerRenderer::new );

        }
        else{
            event.registerEntityRenderer(ModEntityTypes.RECRUIT.get(), RecruitHumanRenderer::new);
            event.registerEntityRenderer(ModEntityTypes.BOWMAN.get(), RecruitHumanRenderer::new );
            event.registerEntityRenderer(ModEntityTypes.NOMAD.get(), RecruitHumanRenderer::new );
            event.registerEntityRenderer(ModEntityTypes.HORSEMAN.get(), RecruitHumanRenderer::new );
            event.registerEntityRenderer(ModEntityTypes.CROSSBOWMAN.get(), RecruitHumanRenderer::new );
            event.registerEntityRenderer(ModEntityTypes.RECRUIT_SHIELDMAN.get(), RecruitHumanRenderer::new );

            //COMPANIONS
            event.registerEntityRenderer(ModEntityTypes.MESSENGER.get(), RecruitHumanRenderer::new );
            event.registerEntityRenderer(ModEntityTypes.SCOUT.get(), RecruitHumanRenderer::new );
            event.registerEntityRenderer(ModEntityTypes.PATROL_LEADER.get(), RecruitHumanRenderer::new );
            event.registerEntityRenderer(ModEntityTypes.CAPTAIN.get(), RecruitHumanRenderer::new );
            event.registerEntityRenderer(ModEntityTypes.SIEGE_ENGINEER.get(), RecruitHumanRenderer::new );
			
            //OTHER
            event.registerEntityRenderer(ModEntityTypes.VILLAGER_NOBLE.get(), RecruitHumanRenderer::new );

  

        }
    }

    public static void layerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ClientEvent.RECRUIT, RecruitVillagerModel::createLayerDefinition);
        ArmorModelSet<LayerDefinition> armorLayers = RecruitArmorLayer.createArmorLayerSet();
        event.registerLayerDefinition(RECRUIT_ARMOR.head(), armorLayers::head);
        event.registerLayerDefinition(RECRUIT_ARMOR.chest(), armorLayers::chest);
        event.registerLayerDefinition(RECRUIT_ARMOR.legs(), armorLayers::legs);
        event.registerLayerDefinition(RECRUIT_ARMOR.feet(), armorLayers::feet);
        event.registerLayerDefinition(RECRUIT_HUMAN_TEAM_LAYER, () -> humanOverlayLayerDefinition(0.10F));
        event.registerLayerDefinition(RECRUIT_HUMAN_BIOME_LAYER, () -> humanOverlayLayerDefinition(0.15F));
        event.registerLayerDefinition(RECRUIT_HUMAN_COMPANION_LAYER, () -> humanOverlayLayerDefinition(0.20F));
    }

    private static ModelLayerLocation humanOverlayLayer(String name) {
        return new ModelLayerLocation(Identifier.fromNamespaceAndPath(Main.MOD_ID, "recruit_human_" + name), name);
    }

    private static LayerDefinition humanOverlayLayerDefinition(float deformation) {
        return LayerDefinition.create(HumanoidModel.createMesh(new CubeDeformation(deformation), 0.0F), 64, 64);
    }

    public static void modelBakingCompleted(ModelEvent.BakingCompleted event) {
        WorldMapCacheManager.getInstance().onClientModelsReloaded();
    }

    public static void textureStitchCompleted(TextureStitchEvent.Post event) {
        if (TextureAtlas.LOCATION_BLOCKS.equals(event.getAtlas().location())) {
            WorldMapCacheManager.getInstance().onClientBlockAtlasStitched();
        }
    }

    @Nullable
    public static Entity getEntityByLooking() {
        HitResult hit = Minecraft.getInstance().hitResult;

        if (hit instanceof EntityHitResult entityHitResult){
            return entityHitResult.getEntity();
        }
        return null;
    }
}
