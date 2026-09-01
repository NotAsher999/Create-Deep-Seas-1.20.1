package com.maxenonyme.createsubmarine.submarine.network;

import com.maxenonyme.createsubmarine.submarine.config.HullStrengthConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public record HullConfigSyncPayload(Map<String, HullStrengthConfig.HullProperty> values) {
    public static void encode(HullConfigSyncPayload payload, FriendlyByteBuf buf) {
        buf.writeVarInt(payload.values.size());
        for (Map.Entry<String, HullStrengthConfig.HullProperty> entry : payload.values.entrySet()) {
            buf.writeUtf(entry.getKey());
            buf.writeVarInt(entry.getValue().maxWaterDepth());
            buf.writeFloat(entry.getValue().implosionChance());
        }
    }

    public static HullConfigSyncPayload decode(FriendlyByteBuf buf) {
        int size = Math.max(0, Math.min(buf.readVarInt(), 100_000));
        Map<String, HullStrengthConfig.HullProperty> map = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            map.put(buf.readUtf(256), new HullStrengthConfig.HullProperty(buf.readVarInt(), buf.readFloat()));
        }
        return new HullConfigSyncPayload(map);
    }

    public static void handle(HullConfigSyncPayload payload, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> HullStrengthConfig.applySynced(payload.values()));
        context.setPacketHandled(true);
    }
}
