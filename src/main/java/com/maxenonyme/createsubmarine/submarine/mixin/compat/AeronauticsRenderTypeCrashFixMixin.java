package com.maxenonyme.createsubmarine.submarine.mixin.compat;

import dev.eriksonn.aeronautics.neoforge.events.AeroNeoForgeClientEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = AeroNeoForgeClientEvents.ModBusEvents.class, remap = false)
public class AeronauticsRenderTypeCrashFixMixin {

    @Redirect(
        method = "clientSetup",
        at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/ChunkRenderTypeSet;of([Lnet/minecraft/client/renderer/RenderType;)Lnet/minecraftforge/client/ChunkRenderTypeSet;", remap = false),
        remap = false,
        require = 0
    )
    private static net.minecraftforge.client.ChunkRenderTypeSet createsubmarine$redirectChunkRenderType(net.minecraft.client.renderer.RenderType[] renderTypes) {
        try {
            return net.minecraftforge.client.ChunkRenderTypeSet.of(renderTypes);
        } catch (IllegalArgumentException e) {
            return net.minecraftforge.client.ChunkRenderTypeSet.of(net.minecraft.client.renderer.RenderType.translucent());
        }
    }
}
