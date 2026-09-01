package com.maxenonyme.createsubmarine.submarine.mixin.compat;

import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Invoker;

@Pseudo
@Mixin(targets = "me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderContext", remap = false)
public interface SodiumBlockRenderContextAccessor {
    @Invoker(value = "pos", remap = false)
    BlockPos createsubmarine$position();
}
