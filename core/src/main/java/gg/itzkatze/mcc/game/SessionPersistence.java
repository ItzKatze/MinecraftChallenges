package gg.itzkatze.mcc.game;

import com.google.gson.annotations.SerializedName;
import gg.itzkatze.mcc.MinecraftChallenges;
import gg.itzkatze.mcc.challenges.Challenge;
import gg.itzkatze.mcc.challenges.ChallengeManager;
import gg.itzkatze.mcc.challenges.ChallengeRegistry;
import gg.itzkatze.mcc.challenges.ChallengeType;
import gg.itzkatze.mcc.configuration.ChallengeSettingsManager;
import gg.itzkatze.mcc.configuration.SettingsManager;
import gg.itzkatze.mcc.level.GameXp;
import gg.itzkatze.mcc.player.MccPlayer;
import gg.itzkatze.mcc.spectator.SpectatorManager;
import gg.itzkatze.mcc.spectator.SpectatorReason;
import gg.itzkatze.mcc.team.MccTeam;
import gg.itzkatze.mcc.util.WorldStorage;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Persists the game session (active challenges, timer, participants, teams and settings) next to the main world,
 * so a running game survives a crash or restart. Each challenge persists its own progress in {@link Challenge#saveData()}.
 */
public final class SessionPersistence {

    private static final Logger logger = LoggerFactory.getLogger("MinecraftChallenges/" + SessionPersistence.class.getSimpleName());
    private static final String FILE_NAME = "session.json";
    private static final String GLOBAL_SETTINGS_KEY = "GLOBAL";

    // Saving before the restore ran would replace the saved session with an empty one, e.g. when onEnable fails early.
    private static boolean restoreDone = false;
    private static boolean awaitingResume = false;
    private static @Nullable BukkitTask pendingSave;

    private SessionPersistence() {
    }

    public static void restore() {
        try {
            SessionData session = WorldStorage.load(FILE_NAME, SessionData.class);
            if (session != null) {
                restore(session);
            }
        } catch (RuntimeException e) {
            logger.warn("Failed to restore the saved game session, starting fresh.", e);
        }
        if (ChallengeManager.isGameStarted()) {
            GameXp.restore();
        } else {
            GameXp.delete();
        }
        restoreDone = true;
    }

    public static void requestSave() {
        MinecraftChallenges plugin = MinecraftChallenges.get();
        if (pendingSave != null || plugin == null || !plugin.isEnabled()) {
            return;
        }
        pendingSave = Bukkit.getScheduler().runTask(plugin, SessionPersistence::saveNow);
    }

    public static void saveNow() {
        if (pendingSave != null) {
            pendingSave.cancel();
            pendingSave = null;
        }
        if (!restoreDone) {
            return;
        }

        WorldStorage.save(FILE_NAME, snapshot());
        GameXp.save();
        for (Challenge challenge : ChallengeManager.getActiveChallenges()) {
            try {
                challenge.saveData();
            } catch (RuntimeException e) {
                logger.warn("Failed to save the data of {}.", challenge.getType(), e);
            }
        }
    }

    /**
     * Saves the worlds and then the session, so both agree right after a game starts.
     */
    public static void checkpoint() {
        Bukkit.savePlayers();
        Bukkit.getWorlds().forEach(World::save);
        requestSave();
    }

    public static void delete() {
        WorldStorage.delete(FILE_NAME);
    }

    public static boolean isAwaitingResume() {
        GameTimer timer = MinecraftChallenges.getTimer();
        if (timer.isRunning() || timer.hasHitZero() || !ChallengeManager.isGameStarted()) {
            awaitingResume = false;
        }
        return awaitingResume;
    }

    private static void restore(@NotNull SessionData session) {
        restoreSettings(session.settings);
        restoreParticipants(session);
        boolean challengeRestored = restoreChallenges(session.activeChallenges);
        if (session.gameStarted && challengeRestored) {
            ChallengeManager.markGameStarted();
        }
        restoreTimer(session.timer);

        GameTimer timer = MinecraftChallenges.getTimer();
        awaitingResume = ChallengeManager.isGameStarted() && timer.getMode() != GameTimer.Mode.DISABLED && !timer.hasHitZero();
        logger.info("Restored the saved game session with {} active challenge(s).", ChallengeManager.getActiveChallenges().size());
    }

    private static void restoreSettings(@Nullable Map<String, Map<String, String>> settings) {
        if (settings == null) {
            return;
        }
        importSettings(SettingsManager.getGlobalSettingsManager(), settings.get(GLOBAL_SETTINGS_KEY));
        for (Challenge challenge : ChallengeRegistry.getAllChallenges()) {
            importSettings(challenge.getSettingsManager(), settings.get(challenge.getType().name()));
        }
    }

    private static void importSettings(@Nullable ChallengeSettingsManager settingsManager, @Nullable Map<String, String> states) {
        if (settingsManager != null && states != null) {
            settingsManager.importStates(states);
        }
    }

    private static void restoreParticipants(@NotNull SessionData session) {
        for (PlayerData player : Objects.requireNonNullElse(session.players, List.<PlayerData>of())) {
            UUID uuid = parseUuid(player.uuid);
            if (uuid != null && player.name != null) {
                MccPlayer.restore(uuid, player.name, player.excluded);
                SpectatorManager.restore(uuid, player.spectator);
            }
        }

        for (TeamData team : Objects.requireNonNullElse(session.teams, List.<TeamData>of())) {
            List<MccPlayer> members = Objects.requireNonNullElse(team.members, List.<String>of()).stream()
                    .map(SessionPersistence::parseUuid)
                    .filter(Objects::nonNull)
                    .map(MccPlayer::adapt)
                    .flatMap(Optional::stream)
                    .toList();
            if (!members.isEmpty() && MccTeam.ofId(team.id).isEmpty()) {
                MccTeam.restore(team.id, team.privateTeam, members);
            }
        }
    }

    private static boolean restoreChallenges(@Nullable List<String> typeNames) {
        boolean challengeRestored = false;
        for (String typeName : Objects.requireNonNullElse(typeNames, List.<String>of())) {
            Challenge challenge = findChallenge(typeName);
            if (challenge == null) {
                logger.warn("Skipping challenge {} of the saved session because it is not available.", typeName);
                continue;
            }
            try {
                ChallengeManager.loadChallenge(challenge);
                challengeRestored = true;
            } catch (RuntimeException e) {
                logger.warn("Failed to restore challenge {}.", typeName, e);
            }
        }
        return challengeRestored;
    }

    private static @Nullable Challenge findChallenge(@Nullable String typeName) {
        if (typeName == null) {
            return null;
        }
        try {
            return ChallengeRegistry.getChallenge(ChallengeType.valueOf(typeName));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static void restoreTimer(@Nullable TimerData timer) {
        if (timer == null || timer.mode == null) {
            return;
        }
        GameTimer.Mode mode;
        try {
            mode = GameTimer.Mode.valueOf(timer.mode);
        } catch (IllegalArgumentException e) {
            logger.warn("Unknown timer mode {} in the saved session, keeping the current timer.", timer.mode);
            return;
        }
        MinecraftChallenges.getTimer().restore(mode, timer.durationSeconds, Math.max(0, timer.remainingSeconds), timer.elapsedSeconds, timer.hitZero);
    }

    private static @Nullable UUID parseUuid(@Nullable String raw) {
        if (raw == null) {
            return null;
        }
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static @NotNull SessionData snapshot() {
        SessionData session = new SessionData();
        session.activeChallenges = ChallengeManager.getActiveChallenges().stream()
                .map(Challenge::getType)
                .sorted()
                .map(ChallengeType::name)
                .toList();
        session.gameStarted = ChallengeManager.isGameStarted();
        session.timer = TimerData.of(MinecraftChallenges.getTimer());
        session.players = MccPlayer.PLAYERS.stream().map(PlayerData::of).toList();
        session.teams = MccTeam.teams.stream().map(TeamData::of).toList();
        session.settings = exportSettings();
        return session;
    }

    private static @NotNull Map<String, Map<String, String>> exportSettings() {
        Map<String, Map<String, String>> settings = new LinkedHashMap<>();
        settings.put(GLOBAL_SETTINGS_KEY, SettingsManager.getGlobalSettingsManager().exportStates());
        ChallengeRegistry.getAllChallenges().stream()
                .sorted(Comparator.comparing(Challenge::getType))
                .forEach(challenge -> {
                    ChallengeSettingsManager settingsManager = challenge.getSettingsManager();
                    if (settingsManager != null) {
                        settings.put(challenge.getType().name(), settingsManager.exportStates());
                    }
                });
        return settings;
    }

    private static final class SessionData {
        List<String> activeChallenges = new ArrayList<>();
        boolean gameStarted;
        TimerData timer;
        List<PlayerData> players = new ArrayList<>();
        List<TeamData> teams = new ArrayList<>();
        Map<String, Map<String, String>> settings = new LinkedHashMap<>();
    }

    private static final class TimerData {
        String mode;
        int durationSeconds;
        int remainingSeconds;
        int elapsedSeconds;
        boolean hitZero;

        static TimerData of(@NotNull GameTimer timer) {
            TimerData data = new TimerData();
            data.mode = timer.getMode().name();
            data.durationSeconds = timer.getDuration();
            data.remainingSeconds = timer.getRemainingSeconds();
            data.elapsedSeconds = timer.getElapsedSeconds();
            data.hitZero = timer.hasHitZero();
            return data;
        }
    }

    private static final class PlayerData {
        String uuid;
        String name;
        boolean excluded;
        String spectator;

        static PlayerData of(@NotNull MccPlayer player) {
            PlayerData data = new PlayerData();
            data.uuid = player.getUuid().toString();
            data.name = player.getName();
            data.excluded = player.isExcluded();
            SpectatorReason reason = SpectatorManager.getReason(player.getUuid());
            data.spectator = reason == null ? null : reason.getId();
            return data;
        }
    }

    private static final class TeamData {
        int id;
        @SerializedName("private")
        boolean privateTeam;
        List<String> members = new ArrayList<>();

        static TeamData of(@NotNull MccTeam team) {
            TeamData data = new TeamData();
            data.id = team.getId();
            data.privateTeam = team.isPrivate();
            data.members = team.getPlayers().stream().map(player -> player.getUuid().toString()).toList();
            return data;
        }
    }
}
