package com.maxenonyme.AbyssDimension;

import com.mojang.logging.LogUtils;
import com.maxenonyme.AbyssDimension.entities.EntityRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.DeferredRegister;
import org.slf4j.Logger;
import java.util.function.Supplier;

@Mod(CreateAbyss.MOD_ID)
public class CreateAbyss {
    public static final String MOD_ID = "create_abyss";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(net.minecraftforge.registries.ForgeRegistries.BLOCKS, MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(net.minecraftforge.registries.ForgeRegistries.ITEMS, MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister
            .create(net.minecraftforge.registries.ForgeRegistries.BLOCK_ENTITY_TYPES, MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister
            .create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    public static final Supplier<CreativeModeTab> ABYSS_TAB = CREATIVE_MODE_TABS.register("abyss_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.create_abyss.abyss_tab"))
                    .icon(() -> new ItemStack(EntityRegistry.AMPHISTIUM_SPAWN_EGG.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(EntityRegistry.AMPHISTIUM_SPAWN_EGG.get());
                        output.accept(EntityRegistry.COOKIECUTTER_SHARK_SPAWN_EGG.get());
                    })
                    .build());

    public CreateAbyss() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        // Abyss still in development: content only exists in the dev environment
        if (net.minecraftforge.fml.loading.FMLEnvironment.production) {
            LOGGER.info("Create Abyss is in development, content disabled in production");
            return;
        }
        LianaRegistry.init();
        EntityRegistry.init(modEventBus);
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        MinecraftForge.EVENT_BUS.addListener(com.maxenonyme.AbyssDimension.system.LianaLODOptimizer::onServerTick);
        MinecraftForge.EVENT_BUS.addListener(com.maxenonyme.AbyssDimension.system.SubmarineLianaCommand::onServerTick);
        MinecraftForge.EVENT_BUS.addListener(com.maxenonyme.AbyssDimension.system.SubmarineLianaCommand::register);

        if (net.minecraftforge.fml.loading.FMLEnvironment.dist.isClient()) {
            CreateAbyssClient.init(modEventBus);
        }
    }

}
