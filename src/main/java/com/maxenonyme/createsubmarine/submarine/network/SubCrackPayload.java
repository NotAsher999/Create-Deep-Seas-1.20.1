package com.maxenonyme.createsubmarine.submarine.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public record SubCrackPayload(UUID subId, BlockPos plotPos, int crackLevel, int blockId) {
    public static void encode(SubCrackPayload payload, FriendlyByteBuf buf) {
        buf.writeUUID(payload.subId());
        buf.writeBlockPos(payload.plotPos());
        buf.writeInt(payload.crackLevel());
        buf.writeInt(payload.blockId());
    }

    public static SubCrackPayload decode(FriendlyByteBuf buf) {
        return new SubCrackPayload(buf.readUUID(), buf.readBlockPos(), buf.readInt(), buf.readInt());
    }

    public static void handle(SubCrackPayload payload, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> ClientHandler.handle(payload));
        context.setPacketHandled(true);
    }

    private static final class ClientHandler {
        private static void handle(SubCrackPayload payload) {
            com.maxenonyme.createsubmarine.submarine.client.SubLevelCrackRenderer.updateCrack(
                    payload.subId(), payload.plotPos(), payload.crackLevel(), payload.blockId());
        }
    }
}
