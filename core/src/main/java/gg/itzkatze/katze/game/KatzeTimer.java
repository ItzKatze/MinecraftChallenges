package gg.itzkatze.katze.game;

import gg.itzkatze.katze.KatzeGames;
import gg.itzkatze.katze.challenges.Challenge;
import gg.itzkatze.katze.challenges.ChallengeManager;
import gg.itzkatze.katze.event.TimerEndEvent;
import gg.itzkatze.katze.event.TimerPauseEvent;
import gg.itzkatze.katze.event.TimerSetTimeEvent;
import gg.itzkatze.katze.event.TimerStartEvent;
import gg.itzkatze.katze.event.TimerStartTimeChangeEvent;
import gg.itzkatze.katze.player.KatzePlayer;
import gg.itzkatze.katze.util.StringUtility;
import lombok.Getter;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;

public class KatzeTimer implements GameTimer {

    private static final Logger logger = LoggerFactory.getLogger("KatzeGames/" + KatzeTimer.class.getSimpleName());

    @Getter private int duration;
    @Getter private Mode mode;
    @Getter private boolean running;

    private int elapsedSeconds;
    private int remainingSeconds;

    private boolean hitZero;

    public KatzeTimer(int duration, boolean running) {
        this.duration = duration;
        this.running = running;
        this.mode = Mode.DISABLED;
        this.elapsedSeconds = 0;
        this.remainingSeconds = 0;
        this.hitZero = false;
        runTimerTask();
        runActionbarTask();
    }

    private void runActionbarTask() {
        Bukkit.getScheduler().runTaskTimer(KatzeGames.get(), () ->
                KatzePlayer.getOnlinePlayers().forEach(player -> player.sendActionbar(getActionbarMessage(player))),
                1L,
                3L
        );
    }

    private void runTimerTask() {
        Bukkit.getScheduler().runTaskTimer(KatzeGames.get(), () -> {
            if (!running) return;

            elapsedSeconds++;

            if (mode == Mode.COUNT_DOWN) {
                remainingSeconds--;
                if (remainingSeconds < 11 && remainingSeconds > 0) {
                    KatzePlayer.PLAYERS.forEach(p ->
                            p.playSound(Sound.sound(Key.key("block.note_block.bass"), Sound.Source.MASTER, 20, 20)));
                }
                if (remainingSeconds <= 0) {
                    remainingSeconds = 0;
                    stop();
                }
            }
        }, 20L, 20L);
    }

    private Component getActionbarMessage(KatzePlayer player) {
        Challenge actionbarChallenge = ChallengeManager.getActiveChallenges().stream()
                .filter(challenge -> challenge.getActionbar(player, running, elapsedSeconds) != null)
                .max(Comparator.comparingInt(Challenge::getActionbarPriority))
                .orElse(null);
        if (actionbarChallenge != null) {
            return actionbarChallenge.getActionbar(player, running, elapsedSeconds);
        }

        if (mode == Mode.DISABLED) {
            return Component.empty();
        }

        if (!running && (hitZero || !ChallengeManager.isGameStarted())) {
            return Component.empty();
        }

        if (running) {
            String timeStr = mode == Mode.COUNT_DOWN
                    ? StringUtility.formatTime(remainingSeconds)
                    : StringUtility.formatTime(elapsedSeconds);
            return Component.text(timeStr).color(NamedTextColor.BLUE).style(Style.style(TextDecoration.ITALIC));
        }

        return Component.text("Timer paused.").color(NamedTextColor.BLUE).style(Style.style(TextDecoration.ITALIC));
    }

    @Override
    public int getElapsedSeconds() {
        return elapsedSeconds;
    }

    @Override
    public int getRemainingSeconds() {
        if (mode != Mode.COUNT_DOWN) return -1;
        return remainingSeconds;
    }

    @Override
    public void setRemainingSeconds(int seconds) {
        TimerSetTimeEvent setTimeEvent = new TimerSetTimeEvent(seconds);
        Bukkit.getPluginManager().callEvent(setTimeEvent);
        if (setTimeEvent.isCancelled()) {
            logger.info("TimerSetTimeEvent has been denied by an external plugin.");
            return;
        }
        this.remainingSeconds = setTimeEvent.getNewTime();
    }

    @Override
    public void setDuration(int seconds) {
        TimerStartTimeChangeEvent event = new TimerStartTimeChangeEvent(seconds);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            logger.info("TimerStartTimeChangeEvent has been denied by an external plugin.");
            return;
        }
        this.duration = event.getNewStartTime();
    }

    @Override
    public void setMode(Mode mode) {
        this.mode = mode;
        this.hitZero = false;
    }

    @Override
    public boolean hasHitZero() {
        return hitZero;
    }

    @Override
    public void start() {
        TimerStartEvent startEvent = new TimerStartEvent(this.remainingSeconds);
        Bukkit.getPluginManager().callEvent(startEvent);
        if (startEvent.isCancelled()) {
            logger.info("TimerStartEvent has been denied by an external plugin.");
            return;
        }
        this.remainingSeconds = startEvent.getStartTime();
        this.elapsedSeconds = 0;
        this.hitZero = false;
        this.running = true;
    }

    @Override
    public void pause() {
        TimerPauseEvent pauseEvent = new TimerPauseEvent(this.remainingSeconds);
        Bukkit.getPluginManager().callEvent(pauseEvent);
        if (pauseEvent.isCancelled()) {
            logger.info("TimerPauseEvent has been denied by an external plugin.");
            return;
        }
        this.remainingSeconds = pauseEvent.getPauseTime();
        this.running = false;
    }

    @Override
    public void stop() {
        TimerEndEvent endEvent = new TimerEndEvent();
        Bukkit.getPluginManager().callEvent(endEvent);
        if (endEvent.isCancelled()) {
            logger.info("TimerEndEvent has been denied by an external plugin.");
            return;
        }

        ChallengeManager.onTimerElapsed();
        this.running = false;
        hitZero = true;
    }

    @Override
    public void restore(Mode mode, int durationSeconds, int remainingSeconds, int elapsedSeconds, boolean hitZero) {
        this.mode = mode;
        this.duration = durationSeconds;
        this.remainingSeconds = remainingSeconds;
        this.elapsedSeconds = elapsedSeconds;
        this.hitZero = hitZero;
        this.running = false;
    }
}
