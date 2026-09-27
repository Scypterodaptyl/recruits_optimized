package com.talhanation.recruits.init;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraft.resources.Identifier;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import com.talhanation.recruits.Main;
import org.lwjgl.glfw.GLFW;

public class ModShortcuts {
    public static KeyMapping COMMAND_SCREEN_KEY;
    public static KeyMapping TEAM_SCREEN_KEY;
    public static KeyMapping MAP_SCREEN_KEY;
    public static KeyMapping.Category CATEGORY;

    @OnlyIn(Dist.CLIENT)
    public static void registerModBusListeners(BusGroup modBusGroup) {
        RegisterKeyMappingsEvent.getBus(modBusGroup).addListener(ModShortcuts::registerBindings);
    }

    @OnlyIn(Dist.CLIENT)
    public static void registerBindings(RegisterKeyMappingsEvent event) {
        if (CATEGORY == null) CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(Main.MOD_ID, "main"));
        COMMAND_SCREEN_KEY = new KeyMapping("key.recruits.command_screen_key", GLFW.GLFW_KEY_R, CATEGORY);
        TEAM_SCREEN_KEY =  new KeyMapping("key.recruits.team_screen_key", GLFW.GLFW_KEY_U, CATEGORY);
        MAP_SCREEN_KEY =  new KeyMapping("key.recruits.map_screen_key", GLFW.GLFW_KEY_M, CATEGORY);

        event.register(COMMAND_SCREEN_KEY);
        event.register(TEAM_SCREEN_KEY);
        event.register(MAP_SCREEN_KEY);
    }
}
