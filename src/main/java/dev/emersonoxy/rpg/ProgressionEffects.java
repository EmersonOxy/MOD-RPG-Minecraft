package dev.emersonoxy.rpg;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public final class ProgressionEffects {
    private ProgressionEffects() {}
    public static void levelUp(ServerPlayer player) {
        var level = (ServerLevel) player.level();
        for (int i = 0; i < 24; i++) {
            double angle = Math.PI * 2 * i / 24;
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, player.getX() + Math.cos(angle), player.getY() + .3 + i / 24.0,
                player.getZ() + Math.sin(angle), 2, .05, .08, .05, .02);
        }
        level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, .7f, 1.1f);
    }
    public static void skillPoint(ServerPlayer player) {
        var level = (ServerLevel) player.level();
        for (int i = 0; i < 20; i++) {
            double angle = Math.PI * 2 * i / 20;
            level.sendParticles(ParticleTypes.END_ROD, player.getX() + Math.cos(angle) * .7, player.getY() + 1.4,
                player.getZ() + Math.sin(angle) * .7, 1, .05, .2, .05, .03);
        }
        level.sendParticles(ParticleTypes.ENCHANT, player.getX(), player.getY() + 1, player.getZ(), 35, .6, .8, .6, .4);
        level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, .9f, 1.5f);
    }
}
