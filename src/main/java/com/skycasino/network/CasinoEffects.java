package com.skycasino.network;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public class CasinoEffects {
    public static void playWinEffect(ServerPlayer player, BlockPos pos, boolean jackpot) {
        ServerLevel level = player.serverLevel();
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 1.2;
        double z = pos.getZ() + 0.5;

        if (jackpot) {
            level.playSound(null, pos, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.playSound(null, pos, SoundEvents.FIREWORK_ROCKET_LARGE_BLAST, SoundSource.BLOCKS, 0.9F, 1.2F);
            level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, x, y, z, 40, 0.6, 0.7, 0.6, 0.15);
            level.sendParticles(ParticleTypes.FIREWORK, x, y + 0.3, z, 25, 0.5, 0.5, 0.5, 0.1);
        } else {
            level.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 0.9F, 1.2F);
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, x, y, z, 12, 0.4, 0.4, 0.4, 0.05);
        }
    }

    public static void playLoseEffect(ServerPlayer player, BlockPos pos) {
        ServerLevel level = player.serverLevel();
        level.playSound(null, pos, SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.BLOCKS, 0.6F, 0.6F);
    }
}
