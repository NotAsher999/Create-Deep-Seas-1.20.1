package com.maxenonyme.createsubmarine.submarine.network;

import com.maxenonyme.AbyssDimension.network.StruggleSharkPayload;
import com.maxenonyme.createsubmarine.CreateSubmarine;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

public final class SubmarineNetwork {
    private static final String PROTOCOL_VERSION = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(CreateSubmarine.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);
    private static int nextPacketId;
    private static boolean registered;

    private SubmarineNetwork() {
    }

    public static synchronized void register() {
        if (registered) return;
        registered = true;
        register(SubLevelBoundsPayload.class, SubLevelBoundsPayload::encode, SubLevelBoundsPayload::decode,
                SubLevelBoundsPayload::handle, NetworkDirection.PLAY_TO_CLIENT);
        register(SubCrackPayload.class, SubCrackPayload::encode, SubCrackPayload::decode,
                SubCrackPayload::handle, NetworkDirection.PLAY_TO_CLIENT);
        register(ElectrolyzerTogglePayload.class, ElectrolyzerTogglePayload::encode,
                ElectrolyzerTogglePayload::decode, ElectrolyzerTogglePayload::handle,
                NetworkDirection.PLAY_TO_SERVER);
        register(HullConfigSyncPayload.class, HullConfigSyncPayload::encode, HullConfigSyncPayload::decode,
                HullConfigSyncPayload::handle, NetworkDirection.PLAY_TO_CLIENT);
        register(HullConfigEditPayload.class, HullConfigEditPayload::encode, HullConfigEditPayload::decode,
                HullConfigEditPayload::handle, NetworkDirection.PLAY_TO_SERVER);
        register(CameraShakePayload.class, CameraShakePayload::encode, CameraShakePayload::decode,
                CameraShakePayload::handle, NetworkDirection.PLAY_TO_CLIENT);
        register(StruggleSharkPayload.class, StruggleSharkPayload::encode, StruggleSharkPayload::decode,
                StruggleSharkPayload::handle, NetworkDirection.PLAY_TO_SERVER);
    }

    private static <T> void register(Class<T> type,
                                     java.util.function.BiConsumer<T, net.minecraft.network.FriendlyByteBuf> encoder,
                                     java.util.function.Function<net.minecraft.network.FriendlyByteBuf, T> decoder,
                                     java.util.function.BiConsumer<T, java.util.function.Supplier<net.minecraftforge.network.NetworkEvent.Context>> handler,
                                     NetworkDirection direction) {
        CHANNEL.registerMessage(nextPacketId++, type, encoder, decoder, handler, Optional.of(direction));
    }

    public static void sendToPlayer(ServerPlayer player, Object message) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static void sendToServer(Object message) {
        CHANNEL.sendToServer(message);
    }
}
