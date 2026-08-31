package com.maxenonyme.createsubmarine.submarine.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.fml.loading.FMLEnvironment;

public class WatermarkOverlay {
    public static void register(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "watermark",
                (forgeGui, guiGraphics, partialTick, width, height) -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.options.hideGui || FMLEnvironment.production) return;

            String text = "[Create Deep Seas: In development]";
            int x = 10;
            int y = guiGraphics.guiHeight() - 15;

            guiGraphics.drawString(mc.font, text, x, y, 0xAAFFFFFF, true);
                });
    }
}
