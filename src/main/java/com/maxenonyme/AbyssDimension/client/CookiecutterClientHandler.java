package com.maxenonyme.AbyssDimension.client;

import com.maxenonyme.AbyssDimension.entities.CookiecutterSharkEntity;
import com.maxenonyme.AbyssDimension.network.StruggleSharkPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.util.List;

public final class CookiecutterClientHandler {

    @SubscribeEvent
    public static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        Player player = event.getEntity();
        if (player == null || !player.level().isClientSide) return;

        List<CookiecutterSharkEntity> sharks = player.level().getEntitiesOfClass(
                CookiecutterSharkEntity.class,
                player.getBoundingBox().inflate(3.0),
                shark -> shark.isLatchedTo(player));
        if (!sharks.isEmpty()) {
            com.maxenonyme.createsubmarine.submarine.network.SubmarineNetwork.sendToServer(new StruggleSharkPayload(sharks.get(0).getId()));
        }
    }
}
