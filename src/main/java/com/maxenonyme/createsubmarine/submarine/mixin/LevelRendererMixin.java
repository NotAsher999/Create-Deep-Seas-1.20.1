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
    @Inject(method = "renderLevel", at = @At("HEAD"))
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
