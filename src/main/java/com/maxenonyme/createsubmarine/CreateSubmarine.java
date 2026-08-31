package com.maxenonyme.createsubmarine;

import com.mojang.logging.LogUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraftforge.fluids.FluidType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.DeferredRegister;
import org.slf4j.Logger;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import com.maxenonyme.createsubmarine.submarine.block.*;
import com.maxenonyme.createsubmarine.submarine.block.entity.*;
import com.maxenonyme.createsubmarine.submarine.effect.SuffocationEffect;
import com.maxenonyme.createsubmarine.submarine.system.*;
import com.maxenonyme.createsubmarine.submarine.config.HullStrengthConfig;
import com.maxenonyme.createsubmarine.submarine.config.SubmarineConfig;
import net.minecraft.network.chat.Component;
import dev.ryanhcode.sable.api.physics.force.ForceGroup;
import dev.ryanhcode.sable.api.physics.force.ForceGroups;
import net.minecraftforge.fml.config.ModConfig;

@Mod(CreateSubmarine.MOD_ID)
public class CreateSubmarine {
        public static final String MOD_ID = "create_submarine";
        public static final DeferredRegister<ForceGroup> FORCE_GROUP_REGISTER = DeferredRegister
                        .create(ForceGroups.REGISTRY_KEY, MOD_ID);
        public static final Supplier<ForceGroup> BALLAST_FORCE_GROUP = FORCE_GROUP_REGISTER.register(
                        "ballast",
                        () -> new ForceGroup(
                                        Component.translatable("create_submarine.force_group.ballast"),
                                        Component.translatable("create_submarine.force_group.ballast.description"),
                                        0x00008B,
                                        true));
        public static final Supplier<ForceGroup> FLOATER_FORCE_GROUP = FORCE_GROUP_REGISTER.register(
                        "floater",
                        () -> new ForceGroup(
                                        Component.translatable("create_submarine.force_group.floater"),
                                        Component.translatable("create_submarine.force_group.floater.description"),
                                        0xADD8E6,
                                        true));
        public static final Logger LOGGER = LogUtils.getLogger();
        public static final boolean DISABLE_WATER_OCCLUSION = false;
        public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(net.minecraftforge.registries.ForgeRegistries.BLOCKS, MOD_ID);
        public static java.util.function.Function<dev.ryanhcode.sable.companion.SubLevelAccess, dev.ryanhcode.sable.companion.math.Pose3dc> clientPoseGetter = (
                        sub) -> sub.logicalPose();
        public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(net.minecraftforge.registries.ForgeRegistries.ITEMS, MOD_ID);
        public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister
                        .create(net.minecraftforge.registries.ForgeRegistries.BLOCK_ENTITY_TYPES, MOD_ID);
        public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(net.minecraftforge.registries.ForgeRegistries.SOUND_EVENTS,
                        MOD_ID);
        public static final DeferredRegister<net.minecraft.world.inventory.MenuType<?>> MENUS = DeferredRegister
                        .create(net.minecraftforge.registries.ForgeRegistries.MENU_TYPES, MOD_ID);
        public static final DeferredRegister<net.minecraft.world.effect.MobEffect> MOB_EFFECTS = DeferredRegister
                        .create(net.minecraftforge.registries.ForgeRegistries.MOB_EFFECTS, MOD_ID);
        public static final DeferredRegister<net.minecraft.world.level.material.Fluid> FLUIDS = DeferredRegister
                        .create(net.minecraftforge.registries.ForgeRegistries.FLUIDS, MOD_ID);

        public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister
                        .create(net.minecraftforge.registries.ForgeRegistries.Keys.FLUID_TYPES, MOD_ID);

        public static final DeferredRegister<com.mojang.serialization.Codec<? extends net.minecraft.world.level.levelgen.DensityFunction>> DENSITY_FUNCTIONS = DeferredRegister
                        .create(Registries.DENSITY_FUNCTION_TYPE, MOD_ID);

        public static final Supplier<com.mojang.serialization.Codec<com.maxenonyme.createsubmarine.worldgen.OceanDepthOffset>> OCEAN_DEPTH_OFFSET = DENSITY_FUNCTIONS
                        .register("ocean_depth_offset",
                                        () -> com.maxenonyme.createsubmarine.worldgen.OceanDepthOffset.CODEC_HOLDER.codec());

        public static final net.minecraftforge.registries.RegistryObject<FluidType> OXYGEN_TYPE = FLUID_TYPES
                        .register("oxygen",
                                        () -> new FluidType(net.minecraftforge.fluids.FluidType.Properties.create()
                                                        .descriptionId("fluid.create_submarine.oxygen")
                                                        .density(-1000)
                                                        .viscosity(1000)) {
                                                @Override
                                                public void initializeClient(
                                                                java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions> consumer) {
                                                        consumer.accept(new net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions() {
                                                                @Override
                                                                public ResourceLocation getStillTexture() {
                                                                        return new ResourceLocation(
                                                                                        "block/water_still");
                                                                }

                                                                @Override
                                                                public ResourceLocation getFlowingTexture() {
                                                                        return new ResourceLocation(
                                                                                        "block/water_flow");
                                                                }

                                                                @Override
                                                                public int getTintColor() {
                                                                        return 0x88FFFFFF;
                                                                }
                                                        });
                                                }
                                        });

        public static final net.minecraftforge.registries.RegistryObject<net.minecraft.world.level.material.FlowingFluid> OXYGEN = FLUIDS
                        .register("oxygen", () -> new net.minecraftforge.fluids.ForgeFlowingFluid.Source(
                                        makeOxygenProperties()));

        public static final net.minecraftforge.registries.RegistryObject<net.minecraft.world.level.material.FlowingFluid> OXYGEN_FLOWING = FLUIDS
                        .register("oxygen_flowing", () -> new net.minecraftforge.fluids.ForgeFlowingFluid.Flowing(
                                        makeOxygenProperties()));

        private static net.minecraftforge.fluids.ForgeFlowingFluid.Properties makeOxygenProperties() {
                return new net.minecraftforge.fluids.ForgeFlowingFluid.Properties(
                                OXYGEN_TYPE, OXYGEN, OXYGEN_FLOWING);
        }

        public static final net.minecraftforge.registries.RegistryObject<net.minecraft.world.effect.MobEffect> SUFFOCATION = MOB_EFFECTS
                        .register("suffocation",
                                        SuffocationEffect::new);
        public static final Supplier<Block> BAROMETER = BLOCKS.register("barometer",
                        () -> new com.maxenonyme.createsubmarine.submarine.block.BarometerBlock(
                                        BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                                                        .requiresCorrectToolForDrops().noOcclusion()));
        public static final Supplier<Item> BAROMETER_ITEM = ITEMS.register("barometer",
                        () -> new com.maxenonyme.createsubmarine.submarine.block.BarometerItem(BAROMETER.get(),
                                        new net.minecraft.world.item.Item.Properties()));
        public static final Supplier<BlockEntityType<com.maxenonyme.createsubmarine.submarine.block.entity.BarometerBlockEntity>> BAROMETER_BE = BLOCK_ENTITIES
                        .register(
                                        "barometer",
                                        () -> BlockEntityType.Builder.of(
                                                        com.maxenonyme.createsubmarine.submarine.block.entity.BarometerBlockEntity::new,
                                                        BAROMETER.get()).build(null));
        public static final Supplier<Block> CREATIVE_OXYGENATOR = BLOCKS.register("creative_oxygenator",
                        () -> new HullControllerBlock(BlockBehaviour.Properties.copy(Blocks.OBSIDIAN)));
        public static final Supplier<Item> CREATIVE_OXYGENATOR_ITEM = ITEMS.register("creative_oxygenator",
                        () -> new com.maxenonyme.createsubmarine.submarine.block.CreativeOxygenatorItem(
                                        CREATIVE_OXYGENATOR.get(), new net.minecraft.world.item.Item.Properties()
                                                        .rarity(net.minecraft.world.item.Rarity.EPIC)));
        public static final Supplier<BlockEntityType<HullControllerBlockEntity>> CREATIVE_OXYGENATOR_BE = BLOCK_ENTITIES
                        .register("creative_oxygenator",
                                        () -> BlockEntityType.Builder
                                                        .of(HullControllerBlockEntity::new, CREATIVE_OXYGENATOR.get())
                                                        .build(null));
        public static final Supplier<Block> BALLAST_TANK = BLOCKS.register("ballast_tank",
                        () -> new BallastTankBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)));
        public static final Supplier<Item> BALLAST_TANK_ITEM = ITEMS.register("ballast_tank",
                        () -> new com.maxenonyme.createsubmarine.submarine.block.BallastTankItem(BALLAST_TANK.get(),
                                        new Item.Properties()));
        public static final Supplier<BlockEntityType<BallastTankBlockEntity>> BALLAST_TANK_BE = BLOCK_ENTITIES.register(
                        "ballast_tank",
                        () -> BlockEntityType.Builder.of(BallastTankBlockEntity::new, BALLAST_TANK.get()).build(null));
        public static final Supplier<Block> BALLAST_VENT = BLOCKS.register("ballast_vent",
                        () -> new BallastVentBlock(
                                        BlockBehaviour.Properties.copy(Blocks.COPPER_BLOCK).noOcclusion()));
        public static final Supplier<Item> BALLAST_VENT_ITEM = ITEMS.register("ballast_vent",
                        () -> new net.minecraft.world.item.BlockItem(BALLAST_VENT.get(), new Item.Properties()));
        public static final Supplier<BlockEntityType<BallastVentBlockEntity>> BALLAST_VENT_BE = BLOCK_ENTITIES.register(
                        "ballast_vent",
                        () -> BlockEntityType.Builder.of(BallastVentBlockEntity::new, BALLAST_VENT.get()).build(null));
        public static final Supplier<Block> DECOMPRESSION_CHAMBER = BLOCKS.register("decompression_chamber",
                        () -> new com.maxenonyme.createsubmarine.submarine.block.DecompressionChamberBlock(
                                        BlockBehaviour.Properties.copy(Blocks.COPPER_BLOCK).noOcclusion()));
        public static final Supplier<Item> DECOMPRESSION_CHAMBER_ITEM = ITEMS.register("decompression_chamber",
                        () -> new com.maxenonyme.createsubmarine.submarine.block.DecompressionChamberItem(DECOMPRESSION_CHAMBER.get(),
                                        new Item.Properties()));
        public static final Supplier<BlockEntityType<com.maxenonyme.createsubmarine.submarine.block.entity.DecompressionChamberBlockEntity>> DECOMPRESSION_CHAMBER_BE = BLOCK_ENTITIES
                        .register(
                                        "decompression_chamber",
                                        () -> BlockEntityType.Builder.of(
                                                        com.maxenonyme.createsubmarine.submarine.block.entity.DecompressionChamberBlockEntity::new,
                                                        DECOMPRESSION_CHAMBER.get()).build(null));
        public static final Supplier<Block> OXYGENE_DIFFUSER = BLOCKS.register("oxygene_diffuser",
                        () -> new OxygeneDiffuserBlock(
                                        BlockBehaviour.Properties.copy(Blocks.COPPER_BLOCK).noOcclusion()));
        public static final Supplier<Item> OXYGENE_DIFFUSER_ITEM = ITEMS.register("oxygene_diffuser",
                        () -> new net.minecraft.world.item.BlockItem(OXYGENE_DIFFUSER.get(), new Item.Properties()));
        public static final Supplier<BlockEntityType<OxygeneDiffuserBlockEntity>> OXYGENE_DIFFUSER_BE = BLOCK_ENTITIES
                        .register("oxygene_diffuser",
                                        () -> BlockEntityType.Builder
                                                        .of(OxygeneDiffuserBlockEntity::new, OXYGENE_DIFFUSER.get())
                                                        .build(null));
        public static final Supplier<SoundEvent> IMPLOSION_SOUND = SOUNDS.register("implosion",
                        () -> SoundEvent.createVariableRangeEvent(
                                        new ResourceLocation(MOD_ID, "implosion")));
        public static final Supplier<SoundEvent> UNDERWATER_EXPLOSION_SOUND = SOUNDS.register("explosionunderwater",
                        () -> SoundEvent.createVariableRangeEvent(
                                        new ResourceLocation(MOD_ID, "explosionunderwater")));
        public static final Supplier<SoundEvent> IMPACT_EXPLOSION_SOUND = SOUNDS.register("impact_explosion_03",
                        () -> SoundEvent.createVariableRangeEvent(
                                        new ResourceLocation(MOD_ID, "impact_explosion_03")));
        public static final Supplier<Block> ELECTROLYZER = BLOCKS.register("electrolyzer",
                        () -> new ElectrolyzerBlock(BlockBehaviour.Properties.copy(Blocks.COPPER_BLOCK)
                                        .noOcclusion()
                                        .isViewBlocking((state, level, pos) -> false)
                                        .isSuffocating((state, level, pos) -> false)));
        public static final Supplier<Item> ELECTROLYZER_ITEM = ITEMS.register("electrolyzer",
                        () -> new net.minecraft.world.item.BlockItem(ELECTROLYZER.get(), new Item.Properties()));
        public static final Supplier<BlockEntityType<ElectrolyzerBlockEntity>> ELECTROLYZER_BE = BLOCK_ENTITIES
                        .register(
                                        "electrolyzer",
                                        () -> BlockEntityType.Builder
                                                        .of(ElectrolyzerBlockEntity::new, ELECTROLYZER.get())
                                                        .build(null));
        public static final Supplier<Block> INDUSTRIAL_ALARM = BLOCKS.register("industrial_alarm",
                        () -> new com.maxenonyme.createsubmarine.submarine.block.IndustrialAlarmBlock(
                                        BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).noOcclusion()));
        public static final Supplier<Item> INDUSTRIAL_ALARM_ITEM = ITEMS.register("industrial_alarm",
                        () -> new net.minecraft.world.item.BlockItem(INDUSTRIAL_ALARM.get(), new Item.Properties()));
        public static final Supplier<BlockEntityType<com.maxenonyme.createsubmarine.submarine.block.entity.IndustrialAlarmBlockEntity>> INDUSTRIAL_ALARM_BE = BLOCK_ENTITIES
                        .register(
                                        "industrial_alarm",
                                        () -> BlockEntityType.Builder.of(
                                                        com.maxenonyme.createsubmarine.submarine.block.entity.IndustrialAlarmBlockEntity::new,
                                                        INDUSTRIAL_ALARM.get()).build(null));
        public static final Supplier<Block> WATER_THRUSTER = BLOCKS.register("water_thruster",
                        () -> new WaterThrusterBlock(
                                        BlockBehaviour.Properties.copy(Blocks.COPPER_BLOCK).noOcclusion()));
        public static final Supplier<Item> WATER_THRUSTER_ITEM = ITEMS.register("water_thruster",
                        () -> new net.minecraft.world.item.BlockItem(WATER_THRUSTER.get(), new Item.Properties()));
        public static final Supplier<BlockEntityType<WaterThrusterBlockEntity>> WATER_THRUSTER_BE = BLOCK_ENTITIES
                        .register(
                                        "water_thruster",
                                        () -> BlockEntityType.Builder
                                                        .of(WaterThrusterBlockEntity::new, WATER_THRUSTER.get())
                                                        .build(null));
        public static final Supplier<net.minecraft.world.inventory.MenuType<com.maxenonyme.createsubmarine.submarine.gui.ElectrolyzerMenu>> ELECTROLYZER_MENU = MENUS
                        .register("electrolyzer",
                                        () -> net.minecraftforge.common.extensions.IForgeMenuType.create(
                                                        com.maxenonyme.createsubmarine.submarine.gui.ElectrolyzerMenu::new));
        public static final Supplier<Block> IRON_PRESSURIZER = BLOCKS.register("iron_pressurizer",
                        () -> new HalfTransparentBlock(BlockBehaviour.Properties.copy(Blocks.GLASS)
                                        .strength(5.0F, 1200.0F)
                                        .requiresCorrectToolForDrops()
                                        .noOcclusion()
                                        .isViewBlocking((state, level, pos) -> false)
                                        .isSuffocating((state, level, pos) -> false)));
        public static final Supplier<Item> IRON_PRESSURIZER_ITEM = ITEMS.register("iron_pressurizer",
                        () -> new com.maxenonyme.createsubmarine.submarine.block.PressurizerItem(IRON_PRESSURIZER.get(),
                                        new Item.Properties()));

        public static final Supplier<Block> COPPER_PRESSURIZER = BLOCKS.register("copper_pressurizer",
                        () -> new HalfTransparentBlock(BlockBehaviour.Properties.copy(Blocks.GLASS)
                                        .strength(5.0F, 1200.0F)
                                        .requiresCorrectToolForDrops()
                                        .noOcclusion()
                                        .isViewBlocking((state, level, pos) -> false)
                                        .isSuffocating((state, level, pos) -> false)));
        public static final Supplier<Item> COPPER_PRESSURIZER_ITEM = ITEMS.register("copper_pressurizer",
                        () -> new com.maxenonyme.createsubmarine.submarine.block.PressurizerItem(
                                        COPPER_PRESSURIZER.get(), new Item.Properties()));

        public static final Supplier<Block> FLOATER = BLOCKS.register("floater",
                        () -> new FloaterBlock(BlockBehaviour.Properties.copy(Blocks.WHITE_WOOL).noOcclusion()));
        public static final Supplier<Item> FLOATER_ITEM = ITEMS.register("floater",
                        () -> new com.maxenonyme.createsubmarine.submarine.block.FloaterItem(FLOATER.get(),
                                        new Item.Properties()));
        public static final Supplier<BlockEntityType<FloaterBlockEntity>> FLOATER_BE = BLOCK_ENTITIES.register(
                        "floater",
                        () -> BlockEntityType.Builder.of(FloaterBlockEntity::new, FLOATER.get()).build(null));
        public static final Supplier<Item> PHYCOLOGICAL_MEMBRANE = ITEMS.register("phycological_membrane",
                        () -> new com.maxenonyme.createsubmarine.submarine.block.PhycologicalMembraneItem(
                                        new net.minecraft.world.item.Item.Properties()
                                                        .rarity(net.minecraft.world.item.Rarity.UNCOMMON)));
        public static final Supplier<Item> STEEL_CABLE = ITEMS.register("steel_cable",
                        () -> new com.maxenonyme.createsubmarine.submarine.block.SteelCableItem(
                                        new net.minecraft.world.item.Item.Properties()));

        public static final Supplier<Block> PULLEY = BLOCKS.register("pulley",
                        () -> new PulleyBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                                        .requiresCorrectToolForDrops().noOcclusion()));
        public static final Supplier<Item> PULLEY_ITEM = ITEMS.register("pulley",
                        () -> new net.minecraft.world.item.BlockItem(PULLEY.get(), new Item.Properties()));
        public static final Supplier<BlockEntityType<PulleyBlockEntity>> PULLEY_BE = BLOCK_ENTITIES.register(
                        "pulley",
                        () -> BlockEntityType.Builder.of(PulleyBlockEntity::new, PULLEY.get()).build(null));

        public static final Supplier<Block> ARRESTING_HOOK = BLOCKS.register("arresting_hook",
                        () -> new com.maxenonyme.createsubmarine.submarine.block.ArrestingHookBlock(
                                        BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).noOcclusion()));
        public static final Supplier<Item> ARRESTING_HOOK_ITEM = ITEMS.register("arresting_hook",
                        () -> new com.maxenonyme.createsubmarine.submarine.block.ArrestingHookItem(ARRESTING_HOOK.get(),
                                        new Item.Properties()));
        public static final Supplier<BlockEntityType<com.maxenonyme.createsubmarine.submarine.block.entity.ArrestingHookBlockEntity>> ARRESTING_HOOK_BE = BLOCK_ENTITIES
                        .register(
                                        "arresting_hook",
                                        () -> BlockEntityType.Builder.of(
                                                        com.maxenonyme.createsubmarine.submarine.block.entity.ArrestingHookBlockEntity::new,
                                                        ARRESTING_HOOK.get()).build(null));

        public static final Supplier<Block> UNDERWATER_MINE = BLOCKS.register("underwater_mine",
                        () -> new UnderwaterMineBlock(
                                        BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).noOcclusion()));
        public static final Supplier<Item> UNDERWATER_MINE_ITEM = ITEMS.register("underwater_mine",
                        () -> new net.minecraft.world.item.BlockItem(UNDERWATER_MINE.get(), new Item.Properties()));
        public static final Supplier<BlockEntityType<UnderwaterMineBlockEntity>> UNDERWATER_MINE_BE = BLOCK_ENTITIES
                        .register(
                                        "underwater_mine",
                                        () -> BlockEntityType.Builder
                                                        .of(UnderwaterMineBlockEntity::new, UNDERWATER_MINE.get())
                                                        .build(null));

        public static final Supplier<Block> SUBMARINE_PROPELLER = BLOCKS.register("submarine_propeller",
                        () -> new com.maxenonyme.createsubmarine.submarine.block.propeller.submarine_propeller.SubmarinePropellerBlock(
                                        BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)));
        public static final Supplier<Item> SUBMARINE_PROPELLER_ITEM = ITEMS.register("submarine_propeller",
                        () -> new net.minecraft.world.item.BlockItem(SUBMARINE_PROPELLER.get(), new Item.Properties()));
        public static final Supplier<BlockEntityType<com.maxenonyme.createsubmarine.submarine.block.propeller.submarine_propeller.SubmarinePropellerBlockEntity>> SUBMARINE_PROPELLER_BE = BLOCK_ENTITIES
                        .register(
                                        "submarine_propeller",
                                        () -> BlockEntityType.Builder.of(
                                                        com.maxenonyme.createsubmarine.submarine.block.propeller.submarine_propeller.SubmarinePropellerBlockEntity::new,
                                                        SUBMARINE_PROPELLER.get()).build(null));

        public CreateSubmarine() {
                IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
                ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SubmarineConfig.SPEC);
                BLOCKS.register(modEventBus);
                FORCE_GROUP_REGISTER.register(modEventBus);
                ITEMS.register(modEventBus);
                BLOCK_ENTITIES.register(modEventBus);
                SOUNDS.register(modEventBus);
                MOB_EFFECTS.register(modEventBus);
                FLUID_TYPES.register(modEventBus);
                FLUIDS.register(modEventBus);
                MENUS.register(modEventBus);
                DENSITY_FUNCTIONS.register(modEventBus);
                net.minecraftforge.common.crafting.CraftingHelper.register(
                                com.maxenonyme.createsubmarine.submarine.system.ConfigCondition.SERIALIZER);
                com.maxenonyme.createsubmarine.submarine.network.SubmarineNetwork.register();
                MinecraftForge.EVENT_BUS.addGenericListener(
                                net.minecraft.world.level.block.entity.BlockEntity.class,
                                com.maxenonyme.createsubmarine.submarine.system.SubmarineCapabilities::attach);
                com.maxenonyme.createsubmarine.submarine.system.SubmarineDisplaySources.register(modEventBus);
                modEventBus.addListener(this::onCommonSetup);
                modEventBus.addListener(this::onConfigLoaded);
                MinecraftForge.EVENT_BUS.addListener(SubmarinePressureSystem::onServerTick);
                MinecraftForge.EVENT_BUS.addListener(SubmarinePressureSystem::onBlockBroken);
                MinecraftForge.EVENT_BUS.addListener(SubmarineSinkingSystem::onServerTick);
                MinecraftForge.EVENT_BUS.addListener(SubmarineInteractionSystem::onServerTick);
                MinecraftForge.EVENT_BUS.addListener(
                                com.maxenonyme.createsubmarine.submarine.system.SteelCablePhysicsSystem::onServerTick);
                MinecraftForge.EVENT_BUS.addListener(
                                com.maxenonyme.createsubmarine.submarine.system.CableElectrificationSystem::onServerTick);
                MinecraftForge.EVENT_BUS.addListener(
                                com.maxenonyme.createsubmarine.submarine.system.SubmarineInfoCommand::register);
                MinecraftForge.EVENT_BUS.addListener(net.minecraftforge.eventbus.api.EventPriority.HIGH,
                                com.maxenonyme.createsubmarine.submarine.system.WrenchRepairHandler::onRightClickBlock);
                MinecraftForge.EVENT_BUS.addListener(this::onBlockPlaceAboveSensor);
                MinecraftForge.EVENT_BUS.addListener(
                                com.maxenonyme.createsubmarine.submarine.system.SubmarineLifecycleHandler::onServerStopping);
                MinecraftForge.EVENT_BUS.addListener(
                                com.maxenonyme.createsubmarine.submarine.system.SubmarineLifecycleHandler::onLevelUnload);
                MinecraftForge.EVENT_BUS.addListener(
                                com.maxenonyme.createsubmarine.submarine.system.SubmarineLifecycleHandler::onPlayerLoggedIn);

                if (net.minecraftforge.fml.loading.FMLEnvironment.dist.isClient()) {
                        CreateSubmarineClient.init(modEventBus);
                }
        }

        private void onBlockPlaceAboveSensor(net.minecraftforge.event.level.BlockEvent.EntityPlaceEvent event) {
                net.minecraft.world.level.block.state.BlockState below = event.getLevel()
                                .getBlockState(event.getPos().below());
                if (below.is(ELECTROLYZER.get()) || below.is(OXYGENE_DIFFUSER.get())) {
                        event.setCanceled(true);
                }
        }



        private void onConfigLoaded(net.minecraftforge.fml.event.config.ModConfigEvent event) {
                if (event.getConfig().getSpec() == SubmarineConfig.SPEC) {
                        com.maxenonyme.createsubmarine.worldgen.OceanDepthOffset.refreshConfig();
                }
        }

        private void onCommonSetup(FMLCommonSetupEvent event) {
                event.enqueueWork(() -> {
                        HullStrengthConfig.load();
                        registerToSimulatedTab();
                        com.simibubi.create.api.stress.BlockStressValues.IMPACTS.register(SUBMARINE_PROPELLER.get(),
                                        () -> 4.0);
                        com.simibubi.create.foundation.item.TooltipModifier.REGISTRY.register(
                                        SUBMARINE_PROPELLER_ITEM.get(),
                                        com.simibubi.create.foundation.item.TooltipModifier.mapNull(
                                                        com.simibubi.create.foundation.item.KineticStats
                                                                        .create(SUBMARINE_PROPELLER_ITEM.get())));
                        com.simibubi.create.api.behaviour.display.DisplaySource.BY_BLOCK_ENTITY.register(
                                        BAROMETER_BE.get(),
                                        java.util.List.of(
                                                        com.maxenonyme.createsubmarine.submarine.system.SubmarineDisplaySources.BAROMETER
                                                                        .get()));
                });
        }

        @SuppressWarnings("unchecked")
        private void registerToSimulatedTab() {
                try {
                        Class<?> regClass = Class
                                        .forName("dev.simulated_team.simulated.registrate.SimulatedRegistrate");
                        List<Supplier<Item>> tabItems = (List<Supplier<Item>>) regClass.getField("TAB_ITEMS").get(null);
                        Map<ResourceLocation, ResourceLocation> itemToSection = (Map<ResourceLocation, ResourceLocation>) regClass
                                        .getField("ITEM_TO_SECTION").get(null);
                        tabItems.add(CREATIVE_OXYGENATOR_ITEM::get);
                        tabItems.add(BALLAST_TANK_ITEM::get);
                        tabItems.add(BALLAST_VENT_ITEM::get);
                        tabItems.add(DECOMPRESSION_CHAMBER_ITEM::get);
                        tabItems.add(OXYGENE_DIFFUSER_ITEM::get);
                        ResourceLocation subSection = new ResourceLocation(MOD_ID, "submarine");
                        itemToSection.put(new ResourceLocation(MOD_ID, "creative_oxygenator"),
                                        subSection);
                        itemToSection.put(new ResourceLocation(MOD_ID, "ballast_tank"), subSection);
                        itemToSection.put(new ResourceLocation(MOD_ID, "ballast_vent"), subSection);
                        itemToSection.put(new ResourceLocation(MOD_ID, "decompression_chamber"),
                                        subSection);
                        itemToSection.put(new ResourceLocation(MOD_ID, "oxygene_diffuser"),
                                        subSection);
                        tabItems.add(ELECTROLYZER_ITEM::get);
                        itemToSection.put(new ResourceLocation(MOD_ID, "electrolyzer"), subSection);
                        tabItems.add(WATER_THRUSTER_ITEM::get);
                        itemToSection.put(new ResourceLocation(MOD_ID, "water_thruster"), subSection);
                        tabItems.add(IRON_PRESSURIZER_ITEM::get);
                        itemToSection.put(new ResourceLocation(MOD_ID, "iron_pressurizer"),
                                        subSection);
                        tabItems.add(COPPER_PRESSURIZER_ITEM::get);
                        itemToSection.put(new ResourceLocation(MOD_ID, "copper_pressurizer"),
                                        subSection);
                        tabItems.add(FLOATER_ITEM::get);
                        itemToSection.put(new ResourceLocation(MOD_ID, "floater"), subSection);
                        tabItems.add(PHYCOLOGICAL_MEMBRANE::get);
                        itemToSection.put(new ResourceLocation(MOD_ID, "phycological_membrane"),
                                        subSection);
                        tabItems.add(STEEL_CABLE::get);
                        itemToSection.put(new ResourceLocation(MOD_ID, "steel_cable"), subSection);
                        tabItems.add(PULLEY_ITEM::get);
                        itemToSection.put(new ResourceLocation(MOD_ID, "pulley"), subSection);
                        tabItems.add(UNDERWATER_MINE_ITEM::get);
                        itemToSection.put(new ResourceLocation(MOD_ID, "underwater_mine"), subSection);
                        tabItems.add(SUBMARINE_PROPELLER_ITEM::get);
                        itemToSection.put(new ResourceLocation(MOD_ID, "submarine_propeller"),
                                        subSection);
                        tabItems.add(BAROMETER_ITEM::get);
                        itemToSection.put(new ResourceLocation(MOD_ID, "barometer"), subSection);
                        tabItems.add(ARRESTING_HOOK_ITEM::get);
                        itemToSection.put(new ResourceLocation(MOD_ID, "arresting_hook"), subSection);
                } catch (Exception ignored) {
                }
        }
}
