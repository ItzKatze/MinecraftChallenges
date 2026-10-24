package gg.itzkatze.katze.forcebattle;

import gg.itzkatze.katze.KatzeGames;
import gg.itzkatze.katze.bossbar.BossbarManager;
import gg.itzkatze.katze.team.KatzeTeam;
import gg.itzkatze.katze.player.KatzePlayer;
import gg.itzkatze.katze.bossbar.BossbarContent;
import gg.itzkatze.katze.challenges.Challenge;
import gg.itzkatze.katze.challenges.ChallengeManager;
import gg.itzkatze.katze.challenges.ChallengeType;
import gg.itzkatze.katze.challenges.TimerSpec;
import gg.itzkatze.katze.game.GameTimer;
import gg.itzkatze.katze.game.SessionPersistence;
import gg.itzkatze.katze.level.GameXp;
import gg.itzkatze.katze.forcebattle.bossbar.ForceBattlePlayerObjectiveBossbar;
import gg.itzkatze.katze.forcebattle.bossbar.ForceBattleTeamObjectiveBossbar;
import gg.itzkatze.katze.forcebattle.bossbar.ForceBattleWaitingBossbar;
import gg.itzkatze.katze.forcebattle.gui.ForceBattleSettingsGUI;
import gg.itzkatze.katze.forcebattle.tasks.StructureTask;
import gg.itzkatze.katze.commands.framework.KatzeCommand;
import gg.itzkatze.katze.configuration.ChallengeSettingsManager;
import gg.itzkatze.katze.configuration.GlobalSetting;
import gg.itzkatze.katze.configuration.SettingsManager;
import gg.itzkatze.katze.gui.KatzeGUI;
import gg.itzkatze.katze.scoreboard.ScoreboardContent;
import gg.itzkatze.katze.spectator.PlayerViewGui;
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
        KatzePlayer.registerFactory(ChallengeType.FORCE_BATTLE, ForceBattlePlayer::new);
        KatzePlayer.upgrade(ChallengeType.FORCE_BATTLE);
        KatzeTeam.registerFactory(ChallengeType.FORCE_BATTLE, ForceBattleTeam::new);
        KatzeTeam.upgrade(ChallengeType.FORCE_BATTLE);
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
        KatzeTeam.downgrade(ChallengeType.FORCE_BATTLE);
        KatzeTeam.unregisterFactory(ChallengeType.FORCE_BATTLE);
        KatzePlayer.downgrade(ChallengeType.FORCE_BATTLE);
        KatzePlayer.unregisterFactory(ChallengeType.FORCE_BATTLE);
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
        KatzeTeam.teams.stream()
                .filter(ForceBattleTeam.class::isInstance)
                .map(ForceBattleTeam.class::cast)
                .forEach(team -> team.setObjective(null));
        KatzePlayer.PLAYERS.stream()
                .filter(ForceBattlePlayer.class::isInstance)
                .map(ForceBattlePlayer.class::cast)
                .filter(player -> !player.isOffline())
                .forEach(player -> player.reset(false));
    }

    @Override
    public void onLobbyJoin(@NotNull KatzePlayer player) {
        Player bukkitPlayer = player.getPlayer();
        if (bukkitPlayer != null) {
            ForceBattleGuideBook.give(bukkitPlayer);
        }
    }

    @Override
    public @Nullable TimerSpec getTimerSpec() {
        int minutes = Math.max(1, KatzeGames.get().getConfig().getInt(
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
        if (KatzeGames.getTimer().getRemainingSeconds() == 0 && GameXp.claimNaturalEnd(ChallengeType.FORCE_BATTLE)) {
            ForceBattleWinners.reward();
        }
        SessionPersistence.requestSave();
    }

    @Override
    public @NotNull ChallengeType getType() {
        return ChallengeType.FORCE_BATTLE;
    }

    @Override
    @NotNull public List<KatzeCommand> getCommands() {
        return gg.itzkatze.katze.KatzeGames.loopThroughPackage(
                "gg.itzkatze.katze.forcebattle.commands",
                KatzeCommand.class
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
    public @NotNull List<Component> getSpectatorInfo(@NotNull KatzePlayer target) {
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
    public @Nullable Component getNametagSuffix(@NotNull KatzePlayer target) {
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
    public KatzeGUI getSettingsGUI() {
        return new ForceBattleSettingsGUI();
    }

    @Override
    @Nullable public ChallengeSettingsManager getSettingsManager() {
        return settingsManager;
    }
}
