package gg.itzkatze.katze.client.mixin;

import gg.itzkatze.katze.client.RunMusicPlayer;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.sounds.Music;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MusicManager.class)
public abstract class MusicManagerMixin {

    @Shadow
    private int nextSongDelay;

    @Shadow
    private MusicManager.MusicFrequency gameMusicFrequency;

    @Shadow
    @Final
    private RandomSource random;

    // This mirrors stopPlaying(), so the countdown after a run is a fresh one.
    @Inject(method = "startPlaying", at = @At("HEAD"), cancellable = true)
    private void katzegames$holdOffDuringRunMusic(Music music, CallbackInfo callback) {
        if (!RunMusicPlayer.isPlaying()) return;

        nextSongDelay = ((MusicFrequencyInvoker) (Object) gameMusicFrequency).katzegames$getNextSongDelay(music, random);
        callback.cancel();
    }
}
