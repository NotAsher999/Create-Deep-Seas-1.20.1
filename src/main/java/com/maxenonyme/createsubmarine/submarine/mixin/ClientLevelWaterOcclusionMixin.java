package com.maxenonyme.createsubmarine.submarine.mixin;

import com.maxenonyme.createsubmarine.submarine.client.renderer.ClientWaterOcclusionContainer;
import dev.ryanhcode.sable.mixinterface.water_occlusion.WaterOcclusionContainerHolder;
import dev.ryanhcode.sable.platform.SablePlatform;
import dev.ryanhcode.sable.sublevel.water_occlusion.WaterOcclusionContainer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ClientLevel.class)
public abstract class ClientLevelWaterOcclusionMixin implements WaterOcclusionContainerHolder {
    @Unique
    private final WaterOcclusionContainer<?> createsubmarine$waterOcclusionContainer =
            createsubmarine$createWaterOcclusionContainer();

    @Unique
    private WaterOcclusionContainer<?> createsubmarine$createWaterOcclusionContainer() {
        Level self = (Level) (Object) this;
        return SablePlatform.INSTANCE.isWrappedLevel(self) ? null : new ClientWaterOcclusionContainer(self);
    }

    @Override
    public WaterOcclusionContainer<?> sable$getWaterOcclusionContainer() {
        return createsubmarine$waterOcclusionContainer;
    }
}
