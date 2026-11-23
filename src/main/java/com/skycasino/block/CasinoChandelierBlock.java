package com.skycasino.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CasinoChandelierBlock extends Block {
    private static final VoxelShape SHAPE = Block.box(1.0D, 2.0D, 1.0D, 15.0D, 16.0D, 15.0D);

    public CasinoChandelierBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(4) == 0) {
            double x = pos.getX() + 0.25 + random.nextDouble() * 0.5;
            double y = pos.getY() + 0.35 + random.nextDouble() * 0.25;
            double z = pos.getZ() + 0.25 + random.nextDouble() * 0.5;
            level.addParticle(ParticleTypes.END_ROD, x, y, z, 0.0D, -0.01D, 0.0D);
        }
    }
}
