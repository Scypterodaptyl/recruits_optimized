package com.talhanation.recruits.client.gui.util;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

/**
 * Helpers for GUI code that used the old widgets.png texture atlas, which was replaced by sprites.
 */
public final class GuiCompat {
    public static final Identifier BUTTON = Identifier.withDefaultNamespace("widget/button");
    public static final Identifier BUTTON_DISABLED = Identifier.withDefaultNamespace("widget/button_disabled");
    public static final Identifier BUTTON_HIGHLIGHTED = Identifier.withDefaultNamespace("widget/button_highlighted");

    private GuiCompat() {
    }

    /**
     * Maps the v offset of a button in the old widgets.png atlas to the matching sprite.
     */
    public static Identifier buttonSprite(int textureY) {
        if (textureY <= 46) return BUTTON_DISABLED;
        if (textureY >= 86) return BUTTON_HIGHLIGHTED;
        return BUTTON;
    }

    public static void blitButton(GuiGraphics guiGraphics, int x, int y, int width, int height, int textureY) {
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, buttonSprite(textureY), x, y, width, height);
    }

    /**
     * Text colors are ARGB now, old code used plain RGB values which would be fully transparent.
     */
    public static int opaque(int color) {
        return (color & 0xFF000000) == 0 ? color | 0xFF000000 : color;
    }

    /**
     * Replacement for the old InventoryScreen#renderEntityInInventoryFollowsMouse(x, y, scale, lookX, lookY, entity)
     * where x/y was the position of the feet of the entity.
     */
    public static void renderEntityFollowsMouse(GuiGraphics guiGraphics, int x, int y, int scale, float mouseX, float mouseY, net.minecraft.world.entity.LivingEntity entity) {
        int halfWidth = Math.round(scale * 0.82F);
        int height = Math.round(scale * 2.33F);
        net.minecraft.client.gui.screens.inventory.InventoryScreen.renderEntityInInventoryFollowsMouse(
                guiGraphics, x - halfWidth, y - height + 3, x + halfWidth, y + 3, scale, 0.0625F, mouseX, mouseY, entity);
    }
}
