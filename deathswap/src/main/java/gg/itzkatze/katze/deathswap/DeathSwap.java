package gg.itzkatze.katze.deathswap;

import gg.itzkatze.katze.challenges.Challenge;
import gg.itzkatze.katze.challenges.ChallengeType;
import gg.itzkatze.katze.commands.framework.KatzeCommand;
import gg.itzkatze.katze.configuration.ChallengeSettingsManager;
import gg.itzkatze.katze.deathswap.gui.DeathSwapSettingsGUI;
import gg.itzkatze.katze.gui.KatzeGUI;
import gg.itzkatze.katze.player.KatzePlayer;
import net.kyori.adventure.text.Component;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class DeathSwap implements Challenge {

    private final DeathSwapGameListener gameListener = new DeathSwapGameListener();
    private final ChallengeSettingsManager settingsManager =
            new ChallengeSettingsManager(this, DeathSwapSettings.DeathSwapSetting.class);

    @Override
    public void onEnable() {
        gameListener.initialize();
        loadData();
    }

    @Override
    public void onDisable() {
        gameListener.cleanup();
    }

    @Override
    public void saveData() {
        gameListener.saveToFile();
    }

    @Override
    public void loadData() {
        gameListener.loadFromFile();
    }

    @Override
    public void onGameStart() {
        gameListener.startGame();
    }

    @Override
    public void onGameReset() {
        gameListener.deleteFile();
        gameListener.cleanup();
        gameListener.initialize();
    }

    @Override
    public @NotNull ChallengeType getType() {
        return ChallengeType.DEATH_SWAP;
    }

    @Override
    public @NotNull List<KatzeCommand> getCommands() {
        return Collections.emptyList();
    }

    @Override
    public @NotNull List<Listener> getListeners() {
        return List.of(gameListener);
    }

    @Override
    public @NotNull List<Runnable> getTasks() {
        return List.of(gameListener::runTask);
    }

    @Override
    public @Nullable Component getActionbar(@NotNull KatzePlayer player, boolean timerRunning, int timerSeconds) {
        return gameListener.getActionbar(player, timerRunning);
    }

    @Override
    public int getActionbarPriority() {
        return 100;
    }

    @Override
    public KatzeGUI getSettingsGUI() {
        return new DeathSwapSettingsGUI();
    }

    @Override
    @Nullable public ChallengeSettingsManager getSettingsManager() {
        return settingsManager;
    }
}
