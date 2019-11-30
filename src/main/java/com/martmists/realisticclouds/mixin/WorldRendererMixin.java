package com.martmists.realisticclouds.mixin;

import com.martmists.realisticclouds.WorldRendererMixinKt;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.GlAllocationUtils;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.util.Identifier;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
class WorldRendererMixin {
    @Shadow
    private ClientWorld world;
    @Shadow
    private int cloudsDisplayList;
    @Shadow
    private TextureManager textureManager;

    @Inject(method = "renderClouds(FDDD)V",
            at = @At("HEAD"), cancellable = true)
    private void renderClouds(float tickDelta, double x, double y, double z, CallbackInfo ci) {
        if (world.dimension.getType() == DimensionType.OVERWORLD) {
            if (this.cloudsDisplayList >= 0) {
                GlAllocationUtils.deleteSingletonList(this.cloudsDisplayList);
                this.cloudsDisplayList = -1;
            }

            WorldRendererMixinKt.tessellator = Tessellator.getInstance();
            this.cloudsDisplayList = GlAllocationUtils.genLists(1);
            GlStateManager.newList(this.cloudsDisplayList, 4864);
            WorldRendererMixinKt.renderClouds(tickDelta, x, y - 1, z, world);
            WorldRendererMixinKt.tessellator.draw();
            GlStateManager.endList();
            GlStateManager.disableCull();
            this.textureManager.bindTexture(new Identifier("textures/environment/clouds.png"));
            GlStateManager.enableBlend();
            GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.pushMatrix();
            GlStateManager.colorMask(true, true, true, true);
            GlStateManager.callList(this.cloudsDisplayList);
            GlStateManager.popMatrix();
            GlStateManager.clearCurrentColor();
            GlStateManager.disableBlend();
            GlStateManager.enableCull();
            ci.cancel();
        }
    }
}
