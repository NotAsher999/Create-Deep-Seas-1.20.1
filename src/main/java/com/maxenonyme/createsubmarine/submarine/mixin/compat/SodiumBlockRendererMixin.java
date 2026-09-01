package com.maxenonyme.createsubmarine.submarine.mixin.compat;

import com.maxenonyme.createsubmarine.submarine.compartment.CompartmentTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer", remap = false)
public abstract class SodiumBlockRendererMixin {
    @Inject(
            method = "renderModel(Lme/jellysquid/mods/sodium/client/render/chunk/compile/pipeline/BlockRenderContext;Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuildBuffers;)V",
            at = @At("HEAD"),
            cancellable = true,
            remap = false)
    private void createsubmarine$skipOccludedBlocks(@Coerce Object context, @Coerce Object buffers, CallbackInfo ci) {
        BlockPos pos = ((SodiumBlockRenderContextAccessor) context).createsubmarine$position();
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && CompartmentTracker.isOccluded(mc.level, pos)) {
            ci.cancel();
        }
    }
}
