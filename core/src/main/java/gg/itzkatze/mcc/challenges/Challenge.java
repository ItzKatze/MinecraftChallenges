package gg.itzkatze.mcc.challenges;

import gg.itzkatze.mcc.bossbar.BossbarContent;
import gg.itzkatze.mcc.commands.framework.MccCommand;
import gg.itzkatze.mcc.player.MccPlayer;
import gg.itzkatze.mcc.scoreboard.ScoreboardContent;
import net.kyori.adventure.text.Component;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public interface Challenge extends ChallengeSettings {

    default void onEnable() {}

    default void onDisable() {}

    default void onGameStart() {}

    default void onGameReset() {}

    default void onLobbyJoin(@NotNull MccPlayer player) {}

    default void saveData() {}

    default void loadData() {}

    @Nullable default TimerSpec getTimerSpec() {
        return null;
    }

    default void onTimerElapsed() {}

    @NotNull ChallengeType getType();

    @NotNull List<MccCommand> getCommands();

    @NotNull List<Listener> getListeners();

    @NotNull List<Runnable> getTasks();

    @Nullable default Component getActionbar(@NotNull MccPlayer player, boolean timerRunning, int timerSeconds) {
        return null;
    }

    default int getActionbarPriority() {
        return 0;
    }

    @NotNull default List<Component> getSpectatorInfo(@NotNull MccPlayer target) {
        return Collections.emptyList();
    }

    @Nullable default Component getNametagSuffix(@NotNull MccPlayer target) {
        return null;
    }

    @Nullable default ScoreboardContent getScoreboardContent() {
        return null; // Default implementation returns null
    }

    @NotNull default List<BossbarContent> getBossbarContents() {
        return Collections.emptyList();
    }
}
