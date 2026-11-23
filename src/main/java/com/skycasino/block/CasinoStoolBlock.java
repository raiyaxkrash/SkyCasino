package com.skycasino.block;

import com.skycasino.entity.SeatEntity;
import com.skycasino.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

public class CasinoStoolBlock extends Block {
    private static final VoxelShape SHAPE = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 11.0D, 13.0D);

    public CasinoStoolBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && !player.isShiftKeyDown()) {
            List<SeatEntity> existing = level.getEntitiesOfClass(SeatEntity.class, new AABB(pos));
            if (existing.isEmpty()) {
                SeatEntity seat = ModEntities.SEAT.get().create(level);
                if (seat != null) {
                    seat.setPos(pos.getX() + 0.5D, pos.getY() + 0.45D, pos.getZ() + 0.5D);
                    level.addFreshEntity(seat);
                    player.startRiding(seat);
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
