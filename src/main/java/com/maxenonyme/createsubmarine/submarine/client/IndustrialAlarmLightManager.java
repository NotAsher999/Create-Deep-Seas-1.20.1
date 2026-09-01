package com.maxenonyme.createsubmarine.submarine.client;

import com.maxenonyme.createsubmarine.submarine.block.entity.IndustrialAlarmBlockEntity;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.deferred.light.PointLight;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Map;
import java.util.WeakHashMap;

@OnlyIn(Dist.CLIENT)
public final class IndustrialAlarmLightManager {
    private static final Map<IndustrialAlarmBlockEntity, PointLight> LIGHTS = new WeakHashMap<>();

    private IndustrialAlarmLightManager() {
    }

    public static void tick(IndustrialAlarmBlockEntity alarm, Level level, BlockState state) {
        if (!state.getValue(BlockStateProperties.LIT) || (level.getGameTime() / 20L) % 2L != 0L) {
            remove(alarm);
            return;
        }
        if (LIGHTS.containsKey(alarm)) return;

        var pos = alarm.getBlockPos();
        PointLight light = new PointLight()
                .setPosition(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)
                .setBrightness(7.200f)
                .setColor(1.0f, 0.0f, 0.0f)
                .setRadius(4.900f);
        VeilRenderSystem.renderer().getDeferredRenderer().getLightRenderer().addLight(light);
        LIGHTS.put(alarm, light);
    }

    public static void remove(IndustrialAlarmBlockEntity alarm) {
        PointLight light = LIGHTS.remove(alarm);
        if (light != null) {
            VeilRenderSystem.renderer().getDeferredRenderer().getLightRenderer().removeLight(light);
        }
    }
}
