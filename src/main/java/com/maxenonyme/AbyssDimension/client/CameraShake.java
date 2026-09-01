package com.maxenonyme.AbyssDimension.client;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.client.event.ViewportEvent;

public class CameraShake {
    private static float shakeIntensity = 0.0f;
    private static int shakeTicks = 0;
    private static int maxShakeTicks = 0;

    public static void shake(float intensity, int ticks) {
        shakeIntensity = intensity;
        shakeTicks = ticks;
        maxShakeTicks = ticks;
    }

    public static void stop() {
        shakeTicks = 0;
    }

    public static void tick() {
        if (Minecraft.getInstance().isPaused()) {
            return;
        }
        if (shakeTicks > 0) {
            shakeTicks--;
        }
    }

    public static class GameEvents {
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            CameraShake.tick();
        }

        @SubscribeEvent
        public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
            if (shakeTicks > 0 && shakeIntensity > 0) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null && !mc.isPaused()) {
                    float progress = maxShakeTicks > 0 ? (float) shakeTicks / maxShakeTicks : 1.0f;
                    float currentIntensity = shakeIntensity * progress;

                    float time = mc.player.tickCount + (float) event.getPartialTick();

                    float pitchShake = Mth.sin(time * 2.5F) * currentIntensity;
                    float yawShake = Mth.cos(time * 3.1F) * currentIntensity;
                    float rollShake = Mth.sin(time * 3.7F) * currentIntensity;

                    event.setPitch(event.getPitch() + pitchShake);
                    event.setYaw(event.getYaw() + yawShake);
                    event.setRoll(event.getRoll() + rollShake);
                }
            }
        }
    }
}
