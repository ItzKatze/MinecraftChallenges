package gg.itzkatze.katze.scoreboard;

import gg.itzkatze.katze.KatzeGames;
import gg.itzkatze.katze.challenges.Challenge;
import gg.itzkatze.katze.challenges.ChallengeManager;
import gg.itzkatze.katze.level.LevelStore;
import gg.itzkatze.katze.level.LevelTag;
import gg.itzkatze.katze.player.KatzePlayer;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * The name above every player's head and in the tab list: a team per online player on each viewer's own scoreboard, with the level as prefix and the
 * active challenge's suffix.
 */
public final class NameTags {

    private static final long PERIOD_TICKS = 3;

    private NameTags() {
    }

    public static void start() {
        Bukkit.getScheduler().runTaskTimer(KatzeGames.get(), NameTags::update, 1, PERIOD_TICKS);
    }

    public static void update() {
        List<KatzePlayer> online = KatzePlayer.getOnlinePlayers();
        Set<String> onlineTeams = new HashSet<>();
        online.forEach(target -> onlineTeams.add(target.getUuid().toString()));

        for (KatzePlayer viewer : online) {
            Player player = viewer.getPlayer();
            if (player == null) continue;

            Scoreboard board = ScoreboardManager.getOrCreateScoreboard(viewer);
            for (KatzePlayer target : online) {
                decorate(board, target);
            }
            for (Team team : List.copyOf(board.getTeams())) {
                if (isUuid(team.getName()) && !onlineTeams.contains(team.getName())) {
                    team.unregister();
                }
            }
            if (player.getScoreboard() != board) {
                player.setScoreboard(board);
            }
        }
    }

    private static void decorate(@NotNull Scoreboard board, @NotNull KatzePlayer target) {
        String name = target.getUuid().toString();
        Team team = board.getTeam(name);
        if (team == null) {
            team = board.registerNewTeam(name);
        }
        if (!team.hasEntry(target.getName())) {
            team.addEntry(target.getName());
        }

        Component prefix = LevelTag.of(LevelStore.get().level(target.getUuid())).append(Component.space());
        if (!prefix.equals(team.prefix())) {
            team.prefix(prefix);
        }
        Component suffix = suffix(target);
        if (!suffix.equals(team.suffix())) {
            team.suffix(suffix);
        }
    }

    private static @NotNull Component suffix(@NotNull KatzePlayer target) {
        return ChallengeManager.getActiveChallenges().stream()
                .sorted(Comparator.comparing(Challenge::getType))
                .map(challenge -> challenge.getNametagSuffix(target))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(Component.empty());
    }

    private static boolean isUuid(@NotNull String name) {
        try {
            UUID.fromString(name);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
