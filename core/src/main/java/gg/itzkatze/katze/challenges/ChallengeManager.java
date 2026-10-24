package gg.itzkatze.katze.challenges;

import gg.itzkatze.katze.KatzeGames;
import gg.itzkatze.katze.commands.framework.CommandHandler;
import gg.itzkatze.katze.commands.framework.KatzeCommand;
import gg.itzkatze.katze.game.GameTimer;
import gg.itzkatze.katze.game.TaskManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ChallengeManager {

    @Getter
    private static final Set<Challenge> activeChallenges = new HashSet<>();
    @Getter
    private static boolean gameStarted = false;
    @Getter
    private static String lastStartError;

    /** Commands registered per challenge, so we can unregister them when the challenge is disabled. */
    private static final Map<Challenge, List<KatzeCommand>> commandsByChallenge = new HashMap<>();
    /** Listeners registered per challenge, so we can unregister them when the challenge is disabled. */
    private static final Map<Challenge, List<Listener>> listenersByChallenge = new HashMap<>();

    /**
     * Loads a challenge by activating it.
     * @param challenge The challenge to load
     */
    public static void loadChallenge(@NotNull Challenge challenge) {
        if (activeChallenges.contains(challenge)) {
            return; // Already loaded
        }

        activeChallenges.add(challenge);

        // Initialize the challenge
        challenge.onEnable();

        // Register commands and track for unregister on disable
        List<KatzeCommand> commands = new ArrayList<>(challenge.getCommands());
        commandsByChallenge.put(challenge, commands);
        for (KatzeCommand command : commands) {
            CommandHandler.register(command);
        }
        refreshCommandListForAllPlayers();

        // Register listeners
        List<Listener> listeners = new ArrayList<>(challenge.getListeners());
        listenersByChallenge.put(challenge, listeners);
        for (Listener listener : listeners) {
            Plugin plugin = KatzeGames.get();
            KatzeGames.get().getServer().getPluginManager().registerEvents(listener, plugin);
        }

        // Register tasks
        for (Runnable task : challenge.getTasks()) {
            TaskManager.register(challenge, task);
        }

        TaskManager.startAll();
    }

    /**
     * Unloads a specific challenge.
     * @param challenge The challenge to unload
     */
    public static void unloadChallenge(@NotNull Challenge challenge) {
        if (!activeChallenges.contains(challenge)) {
            return; // Not loaded
        }

        activeChallenges.remove(challenge);

        challenge.onDisable();

        List<KatzeCommand> commands = commandsByChallenge.remove(challenge);
        if (commands != null) {
            for (KatzeCommand command : commands) {
                KatzeCommand.unregister(command);
            }
        }
        refreshCommandListForAllPlayers();

        List<Listener> listeners = listenersByChallenge.remove(challenge);
        if (listeners != null) {
            for (Listener listener : listeners) {
                HandlerList.unregisterAll(listener);
            }
        }

        TaskManager.unregister(challenge);

        if (activeChallenges.isEmpty()) {
            gameStarted = false;
        }
    }

    /**
     * Unloads all challenges.
     */
    public static void unloadAllChallenges() {
        for (Challenge challenge : activeChallenges) {
            challenge.onDisable();
        }
        activeChallenges.clear();
        gameStarted = false;

        for (List<KatzeCommand> commands : commandsByChallenge.values()) {
            for (KatzeCommand command : commands) {
                KatzeCommand.unregister(command);
            }
        }
        commandsByChallenge.clear();

        for (List<Listener> listeners : listenersByChallenge.values()) {
            for (Listener listener : listeners) {
                HandlerList.unregisterAll(listener);
            }
        }
        listenersByChallenge.clear();

        refreshCommandListForAllPlayers();

        TaskManager.stopAll();
    }

    public static boolean startActiveChallenges() {
        if (activeChallenges.isEmpty() || gameStarted) {
            return false;
        }

        lastStartError = null;

        if (!applyTimerSpec()) {
            return false;
        }

        for (Challenge challenge : activeChallenges) {
            challenge.onGameStart();
        }

        gameStarted = true;
        return true;
    }

    /**
     * Marks a restored game as started without calling {@link Challenge#onGameStart()} again.
     */
    public static void markGameStarted() {
        gameStarted = true;
    }

    public static void resetGameState() {
        gameStarted = false;
        for (Challenge challenge : activeChallenges) {
            challenge.onGameReset();
        }
    }

    public static void onTimerElapsed() {
        if (!gameStarted) {
            return;
        }

        for (Challenge challenge : activeChallenges) {
            TimerSpec spec = challenge.getTimerSpec();
            if (spec != null && spec.mode() == GameTimer.Mode.COUNT_DOWN) {
                challenge.onTimerElapsed();
            }
        }
    }

    /**
     * Resends the command list to all online players so the client shows the current
     * set of registered commands (e.g. after loading or disabling a challenge).
     */
    private static void refreshCommandListForAllPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.updateCommands();
        }
    }

    /**
     * Checks if a challenge type is currently active.
     * @param challengeType The challenge type to check
     * @return True if a challenge of this type is active
     */
    public static boolean isChallengeActive(@NotNull ChallengeType challengeType) {
        return activeChallenges.stream()
                .anyMatch(challenge -> challenge.getType() == challengeType);
    }

    private static boolean applyTimerSpec() {
        List<TimerSpec> specs = activeChallenges.stream()
                .map(Challenge::getTimerSpec)
                .filter(java.util.Objects::nonNull)
                .toList();

        GameTimer timer = KatzeGames.getTimer();
        timer.pause();

        if (specs.isEmpty()) {
            timer.setMode(GameTimer.Mode.DISABLED);
            timer.setRemainingSeconds(0);
            return true;
        }

        List<TimerSpec> countdownSpecs = specs.stream()
                .filter(spec -> spec.mode() == GameTimer.Mode.COUNT_DOWN)
                .toList();
        if (!countdownSpecs.isEmpty()) {
            int longestSeconds = countdownSpecs.stream()
                    .mapToInt(TimerSpec::startSeconds)
                    .max()
                    .orElse(0);
            timer.setMode(GameTimer.Mode.COUNT_DOWN);
            timer.setDuration(longestSeconds);
            timer.setRemainingSeconds(longestSeconds);
            return true;
        }

        boolean hasCountUp = specs.stream().anyMatch(spec -> spec.mode() == GameTimer.Mode.COUNT_UP);
        if (hasCountUp) {
            timer.setMode(GameTimer.Mode.COUNT_UP);
            timer.setRemainingSeconds(0);
            return true;
        }

        timer.setMode(GameTimer.Mode.DISABLED);
        timer.setRemainingSeconds(0);
        return true;
    }
}
