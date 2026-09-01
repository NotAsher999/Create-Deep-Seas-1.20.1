package com.maxenonyme.createsubmarine.submarine.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record CameraShakePayload(float intensity, int ticks) {
    public static void encode(CameraShakePayload payload, FriendlyByteBuf buf) {
        buf.writeFloat(payload.intensity());
        buf.writeInt(payload.ticks());
    }

    public static CameraShakePayload decode(FriendlyByteBuf buf) {
        return new CameraShakePayload(buf.readFloat(), buf.readInt());
    }

    public static void handle(CameraShakePayload payload, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> ClientHandler.handle(payload));
        context.setPacketHandled(true);
    }

    private static final class ClientHandler {
        private static void handle(CameraShakePayload payload) {
            com.maxenonyme.AbyssDimension.client.CameraShake.shake(payload.intensity(), payload.ticks());
        }
    }
}
