package com.maxenonyme.createsubmarine.submarine.system;

import com.maxenonyme.createsubmarine.submarine.compartment.CompartmentTracker;
import com.maxenonyme.createsubmarine.submarine.network.SubCrackPayload;
import com.maxenonyme.createsubmarine.submarine.network.SubLevelBoundsPayload;
import com.maxenonyme.createsubmarine.submarine.util.SubLevelRegistry;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;

import java.util.Map;
import java.util.UUID;

public final class SubmarineLifecycleHandler {

    private SubmarineLifecycleHandler() {}

    public static void onServerStopping(ServerStoppingEvent event) {
        SubmarineSinkingSystem.clearCrashed();
        SubmarinePressureSystem.clearAll();
        SubLevelRegistry.clearAll();
        CompartmentTracker.clearAll();
        SubmarineDriverRegistry.clearAll();
        SubmarineInteractionSystem.clearAll();
        com.maxenonyme.createsubmarine.submarine.block.entity.UnderwaterMineBlockEntity.clearAll();
    }

    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel sl) {
            SubLevelRegistry.clearForLevel(sl);
        }
    }

    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        com.maxenonyme.createsubmarine.submarine.network.SubmarineNetwork.sendToPlayer(player,
                new com.maxenonyme.createsubmarine.submarine.network.HullConfigSyncPayload(
                        com.maxenonyme.createsubmarine.submarine.config.HullStrengthConfig.getValues()));

        for (Map.Entry<UUID, SubLevelAccess> entry : SubLevelRegistry.getAll().entrySet()) {
            UUID subId = entry.getKey();

            SubLevelRegistry.PlotBounds bounds = SubLevelRegistry.getBounds(subId);
            if (bounds != null) {
                com.maxenonyme.createsubmarine.submarine.network.SubmarineNetwork.sendToPlayer(player,
                        new SubLevelBoundsPayload(subId, bounds.minY(), bounds.maxY()));
            }

            Map<BlockPos, Integer> cracks = SubmarinePressureSystem.getAllCracks().get(subId);
            if (cracks == null) continue;
            for (Map.Entry<BlockPos, Integer> c : cracks.entrySet()) {
                com.maxenonyme.createsubmarine.submarine.network.SubmarineNetwork.sendToPlayer(player,
                        new SubCrackPayload(subId, c.getKey(), c.getValue(), 0));
            }
        }
    }
}
