package com.maxenonyme.createsubmarine.submarine.network;

import com.maxenonyme.createsubmarine.submarine.util.SubLevelRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public record SubLevelBoundsPayload(UUID id, int minY, int maxY) {
    public static void encode(SubLevelBoundsPayload payload, FriendlyByteBuf buf) {
        buf.writeUUID(payload.id());
        buf.writeInt(payload.minY());
        buf.writeInt(payload.maxY());
    }

    public static SubLevelBoundsPayload decode(FriendlyByteBuf buf) {
        return new SubLevelBoundsPayload(buf.readUUID(), buf.readInt(), buf.readInt());
    }

    public static void handle(SubLevelBoundsPayload payload, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> SubLevelRegistry.updateBounds(payload.id(), payload.minY(), payload.maxY()));
        context.setPacketHandled(true);
    }
}
