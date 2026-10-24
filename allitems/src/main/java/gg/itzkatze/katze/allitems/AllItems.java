package gg.itzkatze.katze.allitems;

import gg.itzkatze.katze.KatzeGames;
import gg.itzkatze.katze.bossbar.BossbarManager;
import gg.itzkatze.katze.bossbar.BossbarContent;
import gg.itzkatze.katze.challenges.Challenge;
import gg.itzkatze.katze.challenges.ChallengeType;
import gg.itzkatze.katze.allitems.data.AllItemsData;
import gg.itzkatze.katze.allitems.gui.AllItemsSettingsGUI;
import gg.itzkatze.katze.commands.framework.KatzeCommand;
import gg.itzkatze.katze.configuration.ChallengeSettingsManager;
import gg.itzkatze.katze.gui.KatzeGUI;
import gg.itzkatze.katze.scoreboard.ScoreboardContent;
import gg.itzkatze.katze.util.WorldStorage;
import lombok.Getter;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class AllItems implements Challenge {

    @Getter
    private final static AllItemsData data = new AllItemsData();
    private final AllItemsGameListener gameListener = new AllItemsGameListener(data);
    private final ChallengeSettingsManager settingsManager =
            new ChallengeSettingsManager(this, AllItemsSettings.AllItemsSetting.class);

    @Override
    public void onEnable() {
        data.loadFromFile();

        gameListener.initialize();
    }

    @Override
    public void onDisable() {
        BossbarManager.hideBarsForChallenge(this);
        data.saveToFile();
        gameListener.cleanup();
    }

    @Override
    public void saveData() {
        data.saveToFile();
    }

    @Override
    public void loadData() {
        data.loadFromFile();
    }

    @Override
    public void onGameReset() {
        WorldStorage.delete("all_items_progress.json");
        data.clearAllProgress();
    }

    @Override
    public @NotNull ChallengeType getType() {
        return ChallengeType.ALL_ITEMS;
    }

    @Override
    @NotNull public List<KatzeCommand> getCommands() {
        return KatzeGames.loopThroughPackage(
                "gg.itzkatze.katze.allitems.commands",
                KatzeCommand.class
        ).toList();
    }

    @Override
    @NotNull public List<Listener> getListeners() {
        return List.of(gameListener);
    }

    @Override
    @NotNull public List<Runnable> getTasks() {
        return List.of();
    }

    @Override
    public ScoreboardContent getScoreboardContent() {
        return new AllItemsScoreboardContent(data);
    }

    @Override
    public @NonNull List<BossbarContent> getBossbarContents() {
        return List.of(
                new AllItemsBossbarContent(data),
                new AllItemsChainBossbarContent(data)
        );
    }

    @Override
    public KatzeGUI getSettingsGUI() {
        return new AllItemsSettingsGUI();
    }

    @Override
    @Nullable public ChallengeSettingsManager getSettingsManager() {
        return settingsManager;
    }
}
