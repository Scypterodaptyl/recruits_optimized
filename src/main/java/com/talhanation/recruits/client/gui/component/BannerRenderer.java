package com.talhanation.recruits.client.gui.component;

import com.talhanation.recruits.world.RecruitsFaction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.banner.BannerFlagModel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.jetbrains.annotations.Nullable;

public class BannerRenderer {
    private BannerPatternLayers resultBannerPatterns = BannerPatternLayers.EMPTY;
    private DyeColor baseColor = DyeColor.WHITE;
    private final BannerFlagModel flag;
    private ItemStack bannerItem = ItemStack.EMPTY;
    private RecruitsFaction recruitsFaction;

    public BannerRenderer(@Nullable RecruitsFaction team) {
        this.flag = new BannerFlagModel(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.STANDING_BANNER_FLAG));
        this.setRecruitsFaction(team);
    }

    /**
     * Renders the banner. The old code rendered the flag model in a scaled pose,
     * scale 24 matches the 20x40 preview of the loom screen.
     */
    public void renderBanner(GuiGraphics guiGraphics, int left, int top, int width, int height, int scale0) {
        if (bannerItem.isEmpty() || this.flag == null) return;

        float s = scale0 / 24.0F;
        int x0 = left + 10 + Math.round(2 * s);
        int y0 = top + 20 - Math.round(44 * s);
        int x1 = x0 + Math.max(1, Math.round(20 * s));
        int y1 = y0 + Math.max(1, Math.round(40 * s));
        guiGraphics.submitBannerPatternRenderState(this.flag, this.baseColor, this.resultBannerPatterns, x0, y0, x1, y1);
    }

    public void setBannerItem(ItemStack bannerItem) {
        if(bannerItem.getItem() instanceof BannerItem item){
            this.bannerItem = bannerItem;
            this.baseColor = item.getColor();
            this.resultBannerPatterns = bannerItem.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY);
        }
    }

    public void setRecruitsFaction(@Nullable RecruitsFaction faction){
        if (faction == null || faction.getBanner() == null) {
            this.recruitsFaction = null;
            this.bannerItem = ItemStack.EMPTY;
            this.resultBannerPatterns = BannerPatternLayers.EMPTY;
            return;
        }

        this.recruitsFaction = faction;
        ItemStack itemStack = com.talhanation.recruits.util.NbtCompat.loadItem(faction.getBanner());
        if (itemStack.getItem() instanceof BannerItem) {
            this.setBannerItem(itemStack);
        } else {
            this.bannerItem = ItemStack.EMPTY;
            this.resultBannerPatterns = BannerPatternLayers.EMPTY;
        }
    }
}
