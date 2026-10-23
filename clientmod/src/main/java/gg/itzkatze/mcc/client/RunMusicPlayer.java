package gg.itzkatze.mcc.client;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public final class RunMusicPlayer {

    private static @Nullable RunMusic current;

    private RunMusicPlayer() {
    }

    public static void play(Identifier sound, boolean file) {
        stop();
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.getMusicManager().stopPlaying();
        current = new RunMusic(sound, file);
        minecraft.getSoundManager().play(current);
    }

    public static void fade(int ticks) {
        if (current != null) {
            current.fadeOut(ticks);
        }
    }

    public static void stop() {
        if (current != null) {
            Minecraft.getInstance().getSoundManager().stop(current);
            current = null;
        }
    }

    // Asks the sound manager, so music that a fade, the server's record stop or a world change ended lets the background music back in.
    public static boolean isPlaying() {
        return current != null && Minecraft.getInstance().getSoundManager().isActive(current);
    }
}
