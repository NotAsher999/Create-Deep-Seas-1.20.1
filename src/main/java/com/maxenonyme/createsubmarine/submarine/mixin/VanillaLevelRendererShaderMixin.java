package com.maxenonyme.createsubmarine.submarine.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.maxenonyme.createsubmarine.submarine.client.renderer.WaterOcclusionRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Vanilla chunk-shader hook. The Mixin plugin excludes this class when
 * Embeddium owns {@code LevelRenderer.renderChunkLayer}; the Embeddium path
 * applies equivalent uniforms in {@code SodiumChunkRendererMixin}.
 */
@Mixin(LevelRenderer.class)
public class VanillaLevelRendererShaderMixin {
    @Inject(method = "renderChunkLayer", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/ShaderInstance;apply()V", shift = At.Shift.BEFORE))
    private void createsubmarine$setupTranslucentShader(RenderType renderType, PoseStack poseStack,
            double cameraX, double cameraY, double cameraZ, Matrix4f projection, CallbackInfo ci,
            @Local ShaderInstance shader) {
        if (renderType == RenderType.translucent()) {
            WaterOcclusionRenderer.INSTANCE.setupTranslucentShader(shader);
        }
    }
}
