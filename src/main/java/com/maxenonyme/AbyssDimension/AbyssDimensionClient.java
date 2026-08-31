package com.maxenonyme.AbyssDimension;

import com.maxenonyme.AbyssDimension.client.AbyssSpecialEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.minecraftforge.client.event.ViewportEvent;

@EventBusSubscriber(modid = "create_abyss", bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class AbyssDimensionClient {
    private static final ResourceLocation ABYSS_DIM = new ResourceLocation("create_abyss", "abyss");

    @SubscribeEvent
    public static void onRegisterDimensionEffects(RegisterDimensionSpecialEffectsEvent event) {
        event.register(
            ABYSS_DIM,
            new AbyssSpecialEffects()
        );
    }

    @EventBusSubscriber(modid = "create_abyss", bus = EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class GameEvents {

        @SubscribeEvent
        public static void onRenderFog(ViewportEvent.RenderFog event) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null && mc.level.dimension().location().equals(ABYSS_DIM)) {
                if (event.getCamera().getFluidInCamera() == net.minecraft.world.level.material.FogType.WATER) {
                    event.setNearPlaneDistance(-4.0F);
                    event.setFarPlaneDistance(32.0F);
                    event.setCanceled(true);
                } else {
                    event.setNearPlaneDistance(0.0F);
                    event.setFarPlaneDistance(64.0F);
                    event.setCanceled(true);
                }
            }
        }

        @SubscribeEvent
        public static void onComputeFogColor(ViewportEvent.ComputeFogColor event) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null && mc.level.dimension().location().equals(ABYSS_DIM)) {
                if (event.getCamera().getFluidInCamera() == net.minecraft.world.level.material.FogType.WATER) {
                    event.setRed(0.0F);
                    event.setGreen(0.0627F);
                    event.setBlue(0.1882F);
                }
            }
        }
    }
}
