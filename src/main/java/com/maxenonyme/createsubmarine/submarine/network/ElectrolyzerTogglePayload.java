package com.maxenonyme.createsubmarine.submarine.network;

import com.maxenonyme.createsubmarine.submarine.block.entity.ElectrolyzerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ElectrolyzerTogglePayload(BlockPos pos) {
    public static void encode(ElectrolyzerTogglePayload payload, FriendlyByteBuf buf) {
        buf.writeBlockPos(payload.pos());
    }

    public static ElectrolyzerTogglePayload decode(FriendlyByteBuf buf) {
        return new ElectrolyzerTogglePayload(buf.readBlockPos());
    }

    public static void handle(ElectrolyzerTogglePayload payload,
                              Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            var player = context.getSender();
            if (player == null) return;
            if (player.containerMenu instanceof com.maxenonyme.createsubmarine.submarine.gui.ElectrolyzerMenu menu
                    && menu.pos.equals(payload.pos())
                    && player.level().getBlockEntity(payload.pos()) instanceof ElectrolyzerBlockEntity be) {
                be.toggleEnabled();
            }
        });
        context.setPacketHandled(true);
    }
}
