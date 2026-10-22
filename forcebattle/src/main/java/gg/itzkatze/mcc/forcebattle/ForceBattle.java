package gg.itzkatze.mcc.forcebattle;

import gg.itzkatze.mcc.MinecraftChallenges;
import gg.itzkatze.mcc.bossbar.BossbarManager;
import gg.itzkatze.mcc.team.MccTeam;
import gg.itzkatze.mcc.player.MccPlayer;
import gg.itzkatze.mcc.bossbar.BossbarContent;
import gg.itzkatze.mcc.challenges.Challenge;
import gg.itzkatze.mcc.challenges.ChallengeManager;
import gg.itzkatze.mcc.challenges.ChallengeType;
import gg.itzkatze.mcc.challenges.TimerSpec;
import gg.itzkatze.mcc.game.GameTimer;
import gg.itzkatze.mcc.game.SessionPersistence;
import gg.itzkatze.mcc.level.GameXp;
import gg.itzkatze.mcc.forcebattle.bossbar.ForceBattlePlayerObjectiveBossbar;
import gg.itzkatze.mcc.forcebattle.bossbar.ForceBattleTeamObjectiveBossbar;
import gg.itzkatze.mcc.forcebattle.bossbar.ForceBattleWaitingBossbar;
import gg.itzkatze.mcc.forcebattle.gui.ForceBattleSettingsGUI;
import gg.itzkatze.mcc.forcebattle.tasks.StructureTask;
import gg.itzkatze.mcc.commands.framework.MccCommand;
import gg.itzkatze.mcc.configuration.ChallengeSettingsManager;
import gg.itzkatze.mcc.configuration.GlobalSetting;
import gg.itzkatze.mcc.configuration.SettingsManager;
import gg.itzkatze.mcc.gui.MccGUI;
import gg.itzkatze.mcc.scoreboard.ScoreboardContent;
import gg.itzkatze.mcc.spectator.PlayerViewGui;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameRules;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class ForceBattle implements Challenge {
    public static final String BATTLE_DURATION_MINUTES_KEY = "forcebattle-duration-minutes";
    public static final int DEFAULT_BATTLE_DURATION_MINUTES = 90;


    private final ForceBattleGameListener gameListener = new ForceBattleGameListener();
    private final ChallengeSettingsManager settingsManager =
            new ChallengeSettingsManager(this, ForceBattleSettings.class);

    @Override
    public void onEnable() {
        MccPlayer.registerFactory(ChallengeType.FORCE_BATTLE, ForceBattlePlayer::new);
        MccPlayer.upgrade(ChallengeType.FORCE_BATTLE);
        MccTeam.registerFactory(ChallengeType.FORCE_BATTLE, ForceBattleTeam::new);
        MccTeam.upgrade(ChallengeType.FORCE_BATTLE);
        loadData();
        if (!ChallengeManager.isGameStarted()) {
            Bukkit.getOnlinePlayers().forEach(ForceBattleGuideBook::give);
        }
    }

    @Override
    public void onDisable() {
        saveData();
        ForceBattleGameListener.startPhase = true;
        gameListener.clearPendingRestores();
        gameListener.resetWalking();
        StructureTask.clearCache();
        ForceBattleObjective.deleteAll();
        BossbarManager.hideBarsForChallenge(this);
        ForceBattleTeam.deleteAll();
        MccTeam.downgrade(ChallengeType.FORCE_BATTLE);
        MccTeam.unregisterFactory(ChallengeType.FORCE_BATTLE);
        MccPlayer.downgrade(ChallengeType.FORCE_BATTLE);
        MccPlayer.unregisterFactory(ChallengeType.FORCE_BATTLE);
    }

    @Override
    public void saveData() {
        ForceBattlePersistence.saveData();
    }

    @Override
    public void loadData() {
        ForceBattlePersistence.loadData();
    }

    @Override
    public void onGameStart() {
        Bukkit.getWorlds().forEach(world ->
                world.setGameRule(GameRules.SHOW_ADVANCEMENT_MESSAGES, false));
        gameListener.startGame();
    }

    @Override
    public void onGameReset() {
        ForceBattlePersistence.deleteData();
        ForceBattleGameListener.startPhase = true;
        gameListener.clearPendingRestores();
        gameListener.resetWalking();
        StructureTask.clearCache();
        ForceBattleObjective.deleteAll();
        ForceBattleObjectiveManager.getInstance().clearChains();
        MccTeam.teams.stream()
                .filter(ForceBattleTeam.class::isInstance)
                .map(ForceBattleTeam.class::cast)
                .forEach(team -> team.setObjective(null));
        MccPlayer.PLAYERS.stream()
                .filter(ForceBattlePlayer.class::isInstance)
                .map(ForceBattlePlayer.class::cast)
                .filter(player -> !player.isOffline())
                .forEach(player -> player.reset(false));
    }

    @Override
    public void onLobbyJoin(@NotNull MccPlayer player) {
        Player bukkitPlayer = player.getPlayer();
        if (bukkitPlayer != null) {
            ForceBattleGuideBook.give(bukkitPlayer);
        }
    }

    @Override
    public @Nullable TimerSpec getTimerSpec() {
        int minutes = Math.max(1, MinecraftChallenges.get().getConfig().getInt(
                BATTLE_DURATION_MINUTES_KEY,
                DEFAULT_BATTLE_DURATION_MINUTES
        ));
        int seconds = minutes * 60;
        return new TimerSpec(GameTimer.Mode.COUNT_DOWN, seconds);
    }

    @Override
    public void onTimerElapsed() {
        gameListener.handleBattleEnd();
        // A /reset stops the timer too, with time left; only a countdown that ran out is the battle's end.
        if (MinecraftChallenges.getTimer().getRemainingSeconds() == 0 && GameXp.claimNaturalEnd(ChallengeType.FORCE_BATTLE)) {
            ForceBattleWinners.reward();
        }
        SessionPersistence.requestSave();
    }

    @Override
    public @NotNull ChallengeType getType() {
        return ChallengeType.FORCE_BATTLE;
    }

    @Override
    @NotNull public List<MccCommand> getCommands() {
        return gg.itzkatze.mcc.MinecraftChallenges.loopThroughPackage(
                "gg.itzkatze.mcc.forcebattle.commands",
                MccCommand.class
        ).toList();
    }

    @Override
    @NotNull public List<Listener> getListeners() {
        return List.of(gameListener);
    }

    @Override
    @NotNull public List<Runnable> getTasks() {
        return List.of(gameListener::runTasks);
    }

    @Override
    public @NotNull List<Component> getSpectatorInfo(@NotNull MccPlayer target) {
        if (!(target instanceof ForceBattlePlayer player)) return List.of();

        List<Component> lines = new ArrayList<>();
        ForceBattleObjective objective = player.getObjective();
        lines.add(PlayerViewGui.infoLine("Objective", objective == null ? Component.text("none") : ForceBattleNametag.objectiveText(objective)));
        if (SettingsManager.isEnabled(GlobalSetting.EXTRA_TEAM_OBJECTIVE) && player.getTeam() instanceof ForceBattleTeam team && team.getObjective() != null) {
            lines.add(PlayerViewGui.infoLine("Team objective", ForceBattleNametag.objectiveText(team.getObjective())));
        }
        lines.add(PlayerViewGui.infoLine("Points", Component.text(player.getPoints())));
        return lines;
    }

    @Override
    public @Nullable Component getNametagSuffix(@NotNull MccPlayer target) {
        return target instanceof ForceBattlePlayer player ? ForceBattleNametag.suffix(player) : null;
    }

    @Override
    public ScoreboardContent getScoreboardContent() {
        return new ForceBattleScoreboardContent();
    }

    @Override
    public @NonNull List<BossbarContent> getBossbarContents() {
        return List.of(
                new ForceBattleWaitingBossbar(),
                new ForceBattlePlayerObjectiveBossbar(),
                new ForceBattleTeamObjectiveBossbar()
        );
    }

    @Override
    public MccGUI getSettingsGUI() {
        return new ForceBattleSettingsGUI();
    }

    @Override
    @Nullable public ChallengeSettingsManager getSettingsManager() {
        return settingsManager;
    }
}
