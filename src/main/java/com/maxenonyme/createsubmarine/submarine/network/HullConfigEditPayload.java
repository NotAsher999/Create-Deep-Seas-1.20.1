package com.maxenonyme.createsubmarine.submarine.network;

import com.maxenonyme.createsubmarine.submarine.config.HullStrengthConfig;
import net.minecraft.ResourceLocationException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public record HullConfigEditPayload(Map<String, HullStrengthConfig.HullProperty> changed) {
    public static void encode(HullConfigEditPayload payload, FriendlyByteBuf buf) {
        buf.writeVarInt(payload.changed.size());
        for (Map.Entry<String, HullStrengthConfig.HullProperty> entry : payload.changed.entrySet()) {
            buf.writeUtf(entry.getKey());
            buf.writeVarInt(entry.getValue().maxWaterDepth());
            buf.writeFloat(entry.getValue().implosionChance());
        }
    }

    public static HullConfigEditPayload decode(FriendlyByteBuf buf) {
        int size = Math.max(0, Math.min(buf.readVarInt(), 10_000));
        Map<String, HullStrengthConfig.HullProperty> map = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            map.put(buf.readUtf(256), new HullStrengthConfig.HullProperty(buf.readVarInt(), buf.readFloat()));
        }
        return new HullConfigEditPayload(map);
    }

    public static void handle(HullConfigEditPayload payload, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            if (!player.hasPermissions(2) && !player.server.isSingleplayerOwner(player.getGameProfile())) return;
            payload.changed().forEach((key, property) -> {
                if (key == null || key.isEmpty()) return;
                try {
                    new ResourceLocation(key);
                } catch (ResourceLocationException exception) {
                    return;
                }
                float chance = property.implosionChance();
                if (!Float.isFinite(chance)) return;
                HullStrengthConfig.update(key, Math.max(0, Math.min(property.maxWaterDepth(), 100_000)), chance);
            });
            HullStrengthConfig.save();
            HullConfigSyncPayload sync = new HullConfigSyncPayload(HullStrengthConfig.getValues());
            for (ServerPlayer target : player.server.getPlayerList().getPlayers()) {
                SubmarineNetwork.sendToPlayer(target, sync);
            }
        });
        context.setPacketHandled(true);
    }
}
