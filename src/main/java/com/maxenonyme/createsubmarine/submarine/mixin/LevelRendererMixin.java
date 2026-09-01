package com.maxenonyme.createsubmarine.submarine.mixin;

import com.maxenonyme.createsubmarine.submarine.client.renderer.WaterOcclusionRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.LevelRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    /**
     * Keep the Sable renderer-owner stage boundary: build the water depth masks
     * after entities and block entities (including Flywheel visuals) have
     * rendered, immediately before vanilla flushes the destruction overlay
     * and enters the translucent terrain pass. This keeps its framebuffer and
     * shader transitions out of the earlier opaque/Flywheel portion of the
     * frame on Forge 1.20.1.
     */
    @Inject(
            method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/RenderBuffers;crumblingBufferSource()Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;",
                    ordinal = 2,
                    shift = At.Shift.AFTER))
    private void createsubmarine$preRenderTranslucent(PoseStack poseStack, float partialTick, long finishTimeNano,
            boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture,
            Matrix4f projection,
            CallbackInfo ci) {
        if (!WaterOcclusionRenderer.isEnabled())
            return;
        WaterOcclusionRenderer.INSTANCE.preRenderTranslucent(
                new Matrix4f(poseStack.last().pose()), new Matrix4f(projection));
    }
}
