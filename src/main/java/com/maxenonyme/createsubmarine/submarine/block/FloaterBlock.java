package com.maxenonyme.createsubmarine.submarine.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import com.maxenonyme.createsubmarine.CreateSubmarine;
import com.maxenonyme.createsubmarine.submarine.block.entity.FloaterBlockEntity;

public class FloaterBlock extends Block implements EntityBlock {
    public static final EnumProperty<DyeColor> COLOR = EnumProperty.create("color", DyeColor.class);

    public FloaterBlock(Properties properties) {
        super(properties.sound(net.minecraft.world.level.block.SoundType.WOOL));
        this.registerDefaultState(this.stateDefinition.any().setValue(COLOR, DyeColor.WHITE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(COLOR);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hitResult) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof DyeItem dye) {
            DyeColor color = dye.getDyeColor();
            if (state.getValue(COLOR) != color) {
                level.setBlock(pos, state.setValue(COLOR, color), 3);
                level.playSound(null, pos, net.minecraft.sounds.SoundEvents.DYE_USE, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
                level.playSound(null, pos, net.minecraft.sounds.SoundEvents.WOOL_PLACE, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FloaterBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : (l, p, s, be) -> {
            if (be instanceof FloaterBlockEntity fbe) FloaterBlockEntity.serverTick(l, p, fbe);
        };
    }

    @Override
    public ItemStack getCloneItemStack(net.minecraft.world.level.BlockGetter level, BlockPos pos, BlockState state) {
        return createColoredStack(state);
    }

    private ItemStack createColoredStack(BlockState state) {
        ItemStack stack = new ItemStack(this);
        DyeColor color = state.getValue(COLOR);
        stack.getOrCreateTagElement("BlockStateTag").putString(COLOR.getName(), color.getName());
        stack.getOrCreateTag().putInt("CustomModelData", getColorIndex(color));
        return stack;
    }

    @Override
    public java.util.List<ItemStack> getDrops(BlockState state, net.minecraft.world.level.storage.loot.LootParams.Builder params) {
        java.util.List<ItemStack> drops = new java.util.ArrayList<>();
        drops.add(createColoredStack(state));
        return drops;
    }

    private int getColorIndex(DyeColor color) {
        return switch (color) {
            case WHITE -> 1;
            case ORANGE -> 2;
            case MAGENTA -> 3;
            case LIGHT_BLUE -> 4;
            case YELLOW -> 5;
            case LIME -> 6;
            case PINK -> 7;
            case GRAY -> 8;
            case LIGHT_GRAY -> 9;
            case CYAN -> 10;
            case PURPLE -> 11;
            case BLUE -> 12;
            case BROWN -> 13;
            case GREEN -> 14;
            case RED -> 15;
            case BLACK -> 16;
        };
    }
}

