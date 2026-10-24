package gg.itzkatze.katze.game;

import gg.itzkatze.katze.KatzeGames;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;

public final class StartCountdown {

    public static final int SECONDS = 5;

    private static final Sound TICK = Sound.sound(Key.key("block.note_block.pling"), Sound.Source.MASTER, 1f, 1f);
    private static final Title.Times ONE_SECOND = Title.Times.times(Duration.ZERO, Duration.ofSeconds(1), Duration.ZERO);

    private static @Nullable BukkitTask task;

    private StartCountdown() {
    }

    public static boolean isRunning() {
        return task != null;
    }

    public static void begin(@NotNull Runnable onFinished) {
        Countdown countdown = new Countdown(onFinished);
        countdown.run();
        task = Bukkit.getScheduler().runTaskTimer(KatzeGames.get(), countdown, 20L, 20L);
    }

    public static void cancel() {
        if (stop()) {
            Audience.audience(Bukkit.getOnlinePlayers()).clearTitle();
        }
    }

    private static boolean stop() {
        if (task == null) return false;

        task.cancel();
        task = null;
        return true;
    }

    private static final class Countdown implements Runnable {

        private final Runnable onFinished;
        private int remaining = SECONDS;

        private Countdown(@NotNull Runnable onFinished) {
            this.onFinished = onFinished;
        }

        @Override
        public void run() {
            if (remaining == 0) {
                stop();
                onFinished.run();
                return;
            }

            Audience everyone = Audience.audience(Bukkit.getOnlinePlayers());
            everyone.showTitle(Title.title(Component.text(remaining, NamedTextColor.GOLD, TextDecoration.BOLD), Component.empty(), ONE_SECOND));
            everyone.playSound(TICK);
            remaining--;
        }
    }
}
