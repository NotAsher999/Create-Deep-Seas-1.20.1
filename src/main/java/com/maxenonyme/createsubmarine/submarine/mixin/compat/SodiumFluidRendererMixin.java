package com.maxenonyme.createsubmarine.submarine.mixin.compat;

import com.maxenonyme.createsubmarine.submarine.client.renderer.SodiumWaterOcclusionBridge;
import com.maxenonyme.createsubmarine.submarine.compartment.CompartmentTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.FluidRenderer", remap = false)
public abstract class SodiumFluidRendererMixin {
    @Inject(
            method = "render(Lme/jellysquid/mods/sodium/client/world/WorldSlice;Lnet/minecraft/world/level/material/FluidState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuildBuffers;)V",
            at = @At("HEAD"),
            cancellable = true,
            remap = false)
    private void createsubmarine$onRenderFluid(@Coerce Object levelSlice, FluidState fluidState,
            BlockPos blockPos, BlockPos offset, @Coerce Object buffers, CallbackInfo ci) {
        if (SodiumWaterOcclusionBridge.PIXEL_PERFECT_ACTIVE)
            return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && CompartmentTracker.isOccluded(mc.level, blockPos)) {
            ci.cancel();
        }
    }
}
