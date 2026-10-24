package gg.itzkatze.katze.itemrain;

import gg.itzkatze.katze.challenges.Challenge;
import gg.itzkatze.katze.challenges.ChallengeType;
import gg.itzkatze.katze.commands.framework.KatzeCommand;
import gg.itzkatze.katze.configuration.ChallengeSettingsManager;
import gg.itzkatze.katze.gui.KatzeGUI;
import gg.itzkatze.katze.itemrain.gui.ItemRainSettingsGUI;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class ItemRain implements Challenge {

    private final ItemRainGameListener gameListener = new ItemRainGameListener();
    private final ChallengeSettingsManager settingsManager =
            new ChallengeSettingsManager(this, ItemRainSettings.ItemRainSetting.class);

    @Override
    public void onEnable() {
        gameListener.reset();
    }

    @Override
    public void onDisable() {
        gameListener.reset();
    }

    @Override
    public void onGameReset() {
        gameListener.reset();
    }

    @Override
    public @NotNull ChallengeType getType() {
        return ChallengeType.ITEM_RAIN;
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
    public KatzeGUI getSettingsGUI() {
        return new ItemRainSettingsGUI();
    }

    @Override
    @Nullable public ChallengeSettingsManager getSettingsManager() {
        return settingsManager;
    }
}
