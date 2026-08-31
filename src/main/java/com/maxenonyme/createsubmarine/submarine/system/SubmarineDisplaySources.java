package com.maxenonyme.createsubmarine.submarine.system;

import com.maxenonyme.createsubmarine.CreateSubmarine;
import com.simibubi.create.api.behaviour.display.DisplaySource;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class SubmarineDisplaySources {
    public static final DeferredRegister<DisplaySource> DISPLAY_SOURCES = DeferredRegister.create(
            ResourceKey.createRegistryKey(new ResourceLocation("create", "display_source")),
            CreateSubmarine.MOD_ID
    );

    public static final RegistryObject<BarometerDisplaySource> BAROMETER = DISPLAY_SOURCES.register(
            "barometer",
            BarometerDisplaySource::new
    );

    public static void register(IEventBus modEventBus) {
        DISPLAY_SOURCES.register(modEventBus);
    }
}
