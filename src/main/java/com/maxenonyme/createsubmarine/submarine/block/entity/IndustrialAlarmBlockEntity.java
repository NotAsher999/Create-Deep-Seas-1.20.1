package com.maxenonyme.createsubmarine.submarine.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import com.maxenonyme.createsubmarine.CreateSubmarine;

public class IndustrialAlarmBlockEntity extends BlockEntity {

    public IndustrialAlarmBlockEntity(BlockPos pos, BlockState state) {
        super(CreateSubmarine.INDUSTRIAL_ALARM_BE.get(), pos, state);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        Level lvl = level;
        if (lvl != null && lvl.isClientSide) {
            com.maxenonyme.createsubmarine.submarine.client.IndustrialAlarmLightManager.remove(this);
        }
    }

    public void tickClient(Level level, BlockPos pos, BlockState state) {
        com.maxenonyme.createsubmarine.submarine.client.IndustrialAlarmLightManager.tick(this, level, state);
    }
}
