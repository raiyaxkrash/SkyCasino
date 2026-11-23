package com.skycasino.block;

import com.skycasino.client.ClientScreenOpener;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class CasinoMachineBlock extends HorizontalDirectionalBlock {

    public enum MachineType {
        CASHIER,
        SLOT_MACHINE,
        BLACKJACK,
        ROULETTE,
        FORTUNE_WHEEL,
        CRASH,
        MINES,
        PRIZE_SHOP
    }

    private static final VoxelShape TABLE_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 15.0D, 16.0D);
    private static final VoxelShape MACHINE_SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 16.0D, 15.0D);
    private static final VoxelShape WHEEL_SHAPE = Block.box(1.0D, 0.0D, 2.0D, 15.0D, 16.0D, 14.0D);

    private final MachineType machineType;

    public CasinoMachineBlock(MachineType machineType, Properties properties) {
        super(properties);
        this.machineType = machineType;
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    public MachineType getMachineType() {
        return machineType;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (machineType) {
            case BLACKJACK, ROULETTE -> TABLE_SHAPE;
            case FORTUNE_WHEEL -> WHEEL_SHAPE;
            default -> MACHINE_SHAPE;
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientScreenOpener.openMachineScreen(machineType, pos));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
