package gg.itzkatze.mcc.client.mixin;

import net.minecraft.client.sounds.MusicManager;
import net.minecraft.sounds.Music;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MusicManager.MusicFrequency.class)
public interface MusicFrequencyInvoker {

    @Invoker("getNextSongDelay")
    int minecraftchallenges$getNextSongDelay(Music music, RandomSource random);
}
