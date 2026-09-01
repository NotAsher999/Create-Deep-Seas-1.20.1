package com.maxenonyme.AbyssDimension.network;

import com.maxenonyme.AbyssDimension.entities.CookiecutterSharkEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record StruggleSharkPayload(int sharkId) {
    public static void encode(StruggleSharkPayload payload, FriendlyByteBuf buf) {
        buf.writeVarInt(payload.sharkId());
    }

    public static StruggleSharkPayload decode(FriendlyByteBuf buf) {
        return new StruggleSharkPayload(buf.readVarInt());
    }

    public static void handle(StruggleSharkPayload payload, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            Entity entity = player.level().getEntity(payload.sharkId());
            if (entity instanceof CookiecutterSharkEntity shark && shark.isLatchedTo(player)) {
                shark.addStruggle();
            }
        });
        context.setPacketHandled(true);
    }
}
