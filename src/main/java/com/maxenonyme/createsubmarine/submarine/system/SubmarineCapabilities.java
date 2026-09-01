package com.maxenonyme.createsubmarine.submarine.system;

import com.maxenonyme.createsubmarine.CreateSubmarine;
import com.maxenonyme.createsubmarine.submarine.block.entity.BallastTankBlockEntity;
import com.maxenonyme.createsubmarine.submarine.block.entity.BallastVentBlockEntity;
import com.maxenonyme.createsubmarine.submarine.block.entity.DecompressionChamberBlockEntity;
import com.maxenonyme.createsubmarine.submarine.block.entity.ElectrolyzerBlockEntity;
import com.maxenonyme.createsubmarine.submarine.block.entity.OxygeneDiffuserBlockEntity;
import com.maxenonyme.createsubmarine.submarine.block.entity.WaterThrusterBlockEntity;
import dev.simulated_team.simulated.content.blocks.rope.rope_connector.RopeConnectorBlockEntity;
import dev.simulated_team.simulated.content.blocks.rope.rope_winch.RopeWinchBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public final class SubmarineCapabilities {
    private static final ResourceLocation PROVIDER_ID =
            new ResourceLocation(CreateSubmarine.MOD_ID, "block_entity_capabilities");

    private SubmarineCapabilities() {
    }

    public static void attach(AttachCapabilitiesEvent<BlockEntity> event) {
        BlockEntity blockEntity = event.getObject();
        if (!supportsCapabilities(blockEntity)) return;
        Provider provider = new Provider(blockEntity);
        event.addCapability(PROVIDER_ID, provider);
        event.addListener(provider::invalidate);
    }

    private static boolean supportsCapabilities(BlockEntity blockEntity) {
        return blockEntity instanceof RopeWinchBlockEntity
                || blockEntity instanceof RopeConnectorBlockEntity
                || blockEntity instanceof BallastTankBlockEntity
                || blockEntity instanceof BallastVentBlockEntity
                || blockEntity instanceof DecompressionChamberBlockEntity
                || blockEntity instanceof ElectrolyzerBlockEntity
                || blockEntity instanceof OxygeneDiffuserBlockEntity
                || blockEntity instanceof WaterThrusterBlockEntity;
    }

    private static final class Provider implements ICapabilityProvider {
        private final BlockEntity blockEntity;
        private final Map<Key, LazyOptional<?>> cached = new HashMap<>();

        private Provider(BlockEntity blockEntity) {
            this.blockEntity = blockEntity;
        }

        @Override
        public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> capability,
                                                          @Nullable Direction side) {
            Key key = new Key(capability, side);
            return cached.computeIfAbsent(key, ignored -> create(capability, side)).cast();
        }

        private LazyOptional<?> create(Capability<?> capability, @Nullable Direction side) {
            Object value = resolve(capability, side);
            return value == null ? LazyOptional.empty() : LazyOptional.of(() -> value);
        }

        private Object resolve(Capability<?> capability, @Nullable Direction side) {
            if (capability == ForgeCapabilities.ENERGY) {
                if (blockEntity instanceof RopeWinchBlockEntity || blockEntity instanceof RopeConnectorBlockEntity) {
                    return CableElectrificationSystem.getOrCreateStorage(blockEntity);
                }
                if (blockEntity instanceof ElectrolyzerBlockEntity electrolyzer
                        && side != null && side != Direction.UP && side != Direction.DOWN) {
                    return electrolyzer.energyStorage;
                }
                return null;
            }
            if (capability != ForgeCapabilities.FLUID_HANDLER) return null;
            if (blockEntity instanceof BallastTankBlockEntity ballastTank) {
                return ballastTank.getClusterFluidHandler(side);
            }
            if (blockEntity instanceof BallastVentBlockEntity ballastVent) {
                return ballastVent.getFluidHandlerForSide(side);
            }
            if (blockEntity instanceof DecompressionChamberBlockEntity chamber) {
                return chamber.getFluidHandlerForSide(side);
            }
            if (blockEntity instanceof ElectrolyzerBlockEntity electrolyzer) {
                return electrolyzer.combinedFluidHandler;
            }
            if (blockEntity instanceof OxygeneDiffuserBlockEntity diffuser) {
                return diffuser.oxygenTank;
            }
            if (blockEntity instanceof WaterThrusterBlockEntity thruster
                    && (side == null || side == thruster.getBlockState().getValue(DirectionalBlock.FACING).getOpposite())) {
                return thruster.waterTank;
            }
            return null;
        }

        private void invalidate() {
            cached.values().forEach(LazyOptional::invalidate);
            cached.clear();
        }
    }

    private record Key(Capability<?> capability, @Nullable Direction side) {
    }
}
