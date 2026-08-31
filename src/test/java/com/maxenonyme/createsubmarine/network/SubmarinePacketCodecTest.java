package com.maxenonyme.createsubmarine.network;

import com.maxenonyme.AbyssDimension.network.StruggleSharkPayload;
import com.maxenonyme.createsubmarine.submarine.config.HullStrengthConfig;
import com.maxenonyme.createsubmarine.submarine.network.CameraShakePayload;
import com.maxenonyme.createsubmarine.submarine.network.ElectrolyzerTogglePayload;
import com.maxenonyme.createsubmarine.submarine.network.HullConfigEditPayload;
import com.maxenonyme.createsubmarine.submarine.network.HullConfigSyncPayload;
import com.maxenonyme.createsubmarine.submarine.network.SubCrackPayload;
import com.maxenonyme.createsubmarine.submarine.network.SubLevelBoundsPayload;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SubmarinePacketCodecTest {
    @Test
    void allSevenForgePacketsRoundTripWithoutTrailingBytes() {
        UUID subId = UUID.fromString("b514ad8c-8328-4c02-a0b3-c178132a58b6");
        assertRoundTrip(new SubLevelBoundsPayload(subId, -64, 320),
                SubLevelBoundsPayload::encode, SubLevelBoundsPayload::decode);
        assertRoundTrip(new SubCrackPayload(subId, new BlockPos(12, 34, -56), 7, 4096),
                SubCrackPayload::encode, SubCrackPayload::decode);
        assertRoundTrip(new ElectrolyzerTogglePayload(new BlockPos(-4, 80, 9)),
                ElectrolyzerTogglePayload::encode, ElectrolyzerTogglePayload::decode);

        Map<String, HullStrengthConfig.HullProperty> hull = new LinkedHashMap<>();
        hull.put("minecraft:iron_block", new HullStrengthConfig.HullProperty(256, 0.125f));
        hull.put("create_submarine:ballast_tank", new HullStrengthConfig.HullProperty(640, 0.025f));
        assertRoundTrip(new HullConfigSyncPayload(hull), HullConfigSyncPayload::encode,
                HullConfigSyncPayload::decode);
        assertRoundTrip(new HullConfigEditPayload(hull), HullConfigEditPayload::encode,
                HullConfigEditPayload::decode);
        assertRoundTrip(new CameraShakePayload(0.75f, 48), CameraShakePayload::encode,
                CameraShakePayload::decode);
        assertRoundTrip(new StruggleSharkPayload(12345), StruggleSharkPayload::encode,
                StruggleSharkPayload::decode);
    }

    private static <T> void assertRoundTrip(T expected,
                                            BiConsumer<T, FriendlyByteBuf> encoder,
                                            Function<FriendlyByteBuf, T> decoder) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            encoder.accept(expected, buffer);
            assertEquals(expected, decoder.apply(buffer));
            assertEquals(0, buffer.readableBytes(), "Codec left unread packet bytes");
        } finally {
            buffer.release();
        }
    }
}
