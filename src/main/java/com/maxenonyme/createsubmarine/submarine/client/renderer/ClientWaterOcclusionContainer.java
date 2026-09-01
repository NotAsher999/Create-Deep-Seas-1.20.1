package com.maxenonyme.createsubmarine.submarine.client.renderer;

import dev.ryanhcode.sable.sublevel.water_occlusion.WaterOcclusionContainer;
import dev.ryanhcode.sable.sublevel.water_occlusion.WaterOcclusionRegion;
import dev.ryanhcode.sable.util.BoundedBitVolume3i;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.List;

/** Client counterpart omitted by the formal Sable 1.20.1 artifact. */
public final class ClientWaterOcclusionContainer
        extends WaterOcclusionContainer<ClientWaterOcclusionContainer.ClientRegion> {
    public ClientWaterOcclusionContainer(Level level) {
        super(level);
    }

    @Override
    public void removeRegion(WaterOcclusionRegion region) {
        if (!(region instanceof ClientRegion clientRegion)) return;
        regions.remove(clientRegion);
        WaterOcclusionRenderer.INSTANCE.removeRegion(clientRegion.renderRegion);
    }

    @Override
    public ClientRegion addRegion(BoundedBitVolume3i volume) {
        ClientRegion region = new ClientRegion(volume);
        regions.add(region);
        List<BlockPos> blocks = BlockPos.betweenClosedStream(volume.getMinBlockPos(), volume.getMaxBlockPos())
                .map(BlockPos::immutable)
                .filter(pos -> volume.getOccupied(pos.getX(), pos.getY(), pos.getZ()))
                .toList();
        region.renderRegion = WaterOcclusionRenderer.INSTANCE.addRegion(blocks);
        return region;
    }

    public static final class ClientRegion extends WaterOcclusionRegion {
        private WaterOcclusionRenderRegion renderRegion;

        private ClientRegion(BoundedBitVolume3i volume) {
            super(volume);
        }
    }
}
