package gg.itzkatze.katze.client;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

final class RunMusic extends AbstractTickableSoundInstance {

    private final boolean file;
    private int fadeLength;
    private int fadeElapsed;

    // Relative to the listener at 0,0,0 without attenuation, the music plays at the listener, as the game's own music does.
    RunMusic(Identifier sound, boolean file) {
        super(SoundEvent.createVariableRangeEvent(sound), SoundSource.RECORDS, SoundInstance.createUnseededRandom());
        this.file = file;
        this.looping = true;
        this.delay = 0;
        this.volume = 1;
        this.pitch = 1;
        this.relative = true;
        this.x = 0;
        this.y = 0;
        this.z = 0;
        this.attenuation = SoundInstance.Attenuation.NONE;
    }

    // A game file has no sound event of its own, so it resolves to one holding just that file, streamed like the game's music.
    @Override
    public WeighedSoundEvents getOrResolve(SoundManager soundManager) {
        if (!file) {
            return super.getOrResolve(soundManager);
        }
        Sound track = new Sound(identifier, _ -> 1f, _ -> 1f, 1, Sound.Type.FILE, true, false, 16);
        soundEvent = new WeighedSoundEvents(identifier, null);
        soundEvent.addSound(track);
        sound = track;
        return soundEvent;
    }

    void fadeOut(int ticks) {
        if (fadeLength == 0) {
            fadeLength = Math.max(1, ticks);
        }
    }

    // The volume is the square of the remaining time, which sounds like an even fade.
    @Override
    public void tick() {
        if (fadeLength == 0) return;

        if (++fadeElapsed >= fadeLength) {
            stop();
            return;
        }
        float remaining = 1 - (float) fadeElapsed / fadeLength;
        volume = remaining * remaining;
    }
}
