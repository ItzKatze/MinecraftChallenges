package gg.itzkatze.katze.randomizer;

import gg.itzkatze.katze.challenges.Challenge;
import gg.itzkatze.katze.challenges.ChallengeType;
import gg.itzkatze.katze.commands.framework.KatzeCommand;
import gg.itzkatze.katze.configuration.ChallengeSettingsManager;
import gg.itzkatze.katze.configuration.Setting;
import gg.itzkatze.katze.configuration.states.SettingStateType;
import gg.itzkatze.katze.gui.KatzeGUI;
import gg.itzkatze.katze.randomizer.gui.RandomizerSettingsGUI;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class Randomizer implements Challenge {

    private final RandomizerGameListener gameListener = new RandomizerGameListener();
    private final ChallengeSettingsManager settingsManager =
            new ChallengeSettingsManager(this, RandomizerSettings.RandomizerSetting.class, this::onSettingChanged);

    @Override
    public void onEnable() {
        gameListener.loadFromFile();
    }

    @Override
    public void onDisable() {
        gameListener.saveToFile();
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
    public void onGameReset() {
        gameListener.resetMappings();
    }

    @Override
    public @NotNull ChallengeType getType() {
        return ChallengeType.RANDOMIZER;
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
        return Collections.emptyList();
    }

    @Override
    public KatzeGUI getSettingsGUI() {
        return new RandomizerSettingsGUI();
    }

    @Override
    @Nullable public ChallengeSettingsManager getSettingsManager() {
        return settingsManager;
    }

    private void onSettingChanged(Challenge challenge, Setting setting, SettingStateType oldState, SettingStateType newState) {
        if (setting == RandomizerSettings.RandomizerSetting.SHARED_MAPPINGS) {
            gameListener.resetMappings();
        }
    }
}
