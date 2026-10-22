package gg.itzkatze.mcc.level;

import gg.itzkatze.mcc.MinecraftChallenges;
import gg.itzkatze.mcc.challenges.ChallengeManager;
import gg.itzkatze.mcc.player.AfkTracker;
import gg.itzkatze.mcc.player.MccPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class PlaytimeXp {

    public static final int GAME_XP_PER_MINUTE = 2;
    public static final int LOBBY_SECONDS_PER_XP = 120;
    private static final long PERIOD_TICKS = 20;

    private final AfkTracker afk;
    private final Map<UUID, Integer> lobbySeconds = new HashMap<>();

    public PlaytimeXp(@NotNull AfkTracker afk) {
        this.afk = afk;
    }

    public void start() {
        Bukkit.getScheduler().runTaskTimer(MinecraftChallenges.get(), this::tick, PERIOD_TICKS, PERIOD_TICKS);
    }

    public void tick() {
        afk.tick();
        boolean inLobby = !ChallengeManager.isGameStarted();
        boolean gameRunning = GameXp.isGameRunning();
        Set<UUID> online = new HashSet<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID id = player.getUniqueId();
            online.add(id);
            if (afk.isAfk(id)) continue;

            if (inLobby) {
                if (lobbySeconds.merge(id, 1, Integer::sum) >= LOBBY_SECONDS_PER_XP) {
                    lobbySeconds.remove(id);
                    LevelStore.get().grant(player, 1);
                }
            } else if (gameRunning) {
                Optional<MccPlayer> participant = MccPlayer.adapt(id).filter(MccPlayer::isParticipating);
                if (participant.isPresent() && GameXp.creditActiveSecond(participant.get()) % 60 == 0) {
                    LevelStore.get().grant(participant.get(), GAME_XP_PER_MINUTE);
                }
            }
        }
        lobbySeconds.keySet().retainAll(online);
    }
}
