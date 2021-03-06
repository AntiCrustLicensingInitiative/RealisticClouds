package com.martmists.realisticclouds.mixin;

import com.martmists.realisticclouds.WorldRendererMixinKt;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.GlAllocationUtils;
import net.minecraft.client.util.math.MatrixStack;
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

    @Inject(method = "renderClouds(Lnet/minecraft/client/util/math/MatrixStack;FDDD)V",
            at = @At("HEAD"), cancellable = true)
    private void renderClouds(MatrixStack stack, float tickDelta, double x, double y, double z, CallbackInfo ci) {
        if (world.dimension.getType() == DimensionType.OVERWORLD) {
            WorldRendererMixinKt.tessellator = Tessellator.getInstance();
            GlStateManager.disableTexture();
            // Blend disabled since clouds are not getting culled
            // GlStateManager.enableBlend();
            WorldRendererMixinKt.renderClouds(tickDelta, x, y - 1, z, world);
            WorldRendererMixinKt.tessellator.draw();
            // GlStateManager.disableBlend();
            GlStateManager.enableTexture();
            ci.cancel();
        }
    }
}
