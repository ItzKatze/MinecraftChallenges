package gg.itzkatze.katze.lobby.cosmetics;

import gg.itzkatze.katze.level.LevelProgress;
import gg.itzkatze.katze.level.LevelStore;
import gg.itzkatze.katze.level.LevelTag;
import gg.itzkatze.katze.lobby.Lobby;
import gg.itzkatze.katze.lobby.LobbyManager;
import gg.itzkatze.katze.lobby.jumpandrun.JumpAndRun;
import gg.itzkatze.katze.player.KatzePlayer;
import gg.itzkatze.katze.scoreboard.ScoreboardContent;
import gg.itzkatze.katze.scoreboard.ScoreboardManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

import java.util.OptionalInt;
import java.util.UUID;

public final class PointsSidebar implements ScoreboardContent {

    public static void install(@NotNull CosmeticStore store, @NotNull LevelStore levels) {
        store.addBalanceListener(PointsSidebar::refresh);
        levels.addChangeListener(PointsSidebar::refresh);
    }

    public static void refresh(@NotNull UUID player) {
        KatzePlayer.adapt(player).ifPresent(ScoreboardManager::refreshLobby);
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.text("Jump & Run");
    }

    @Override
    public @NotNull Component[] getLines(@NotNull KatzePlayer player) {
        CosmeticStore store = CosmeticStore.get();
        UUID id = player.getUuid();
        OptionalInt score = currentRun(id);
        LevelProgress level = LevelStore.get().progress(id);
        return new Component[]{
                Component.text(" "),
                stat("Level: ", Component.text(level.level(), LevelTag.colorOf(level.level()))),
                stat("XP: ", Component.text(level.cost() == 0 ? "max" : level.into() + "/" + level.cost(), NamedTextColor.WHITE)),
                Component.text(" "),
                stat("Current Run: ", score.isPresent() ? Component.text(score.getAsInt(), NamedTextColor.WHITE) : Component.text("-", NamedTextColor.DARK_GRAY)),
                ranked("Best Run: ", Component.text(store.best(id), NamedTextColor.WHITE), store.rank(LeaderboardMetric.BEST_RUN, id)),
                Component.text(" "),
                ranked("Points: ", Component.text(store.points(id), NamedTextColor.GOLD), store.rank(LeaderboardMetric.CURRENT_POINTS, id)),
                ranked("Lifetime Points: ", Component.text(store.earned(id), NamedTextColor.GOLD), store.rank(LeaderboardMetric.LIFETIME_POINTS, id)),
                ranked("Cases Opened: ", Component.text(store.casesOpened(id), NamedTextColor.WHITE), store.rank(LeaderboardMetric.CASES_OPENED, id))
        };
    }

    private static @NotNull OptionalInt currentRun(@NotNull UUID player) {
        Lobby lobby = LobbyManager.getActiveLobby();
        if (lobby == null) return OptionalInt.empty();

        return lobby.getFeatures().stream()
                .filter(JumpAndRun.class::isInstance)
                .map(JumpAndRun.class::cast)
                .flatMapToInt(jumpAndRun -> jumpAndRun.score(player).stream())
                .findFirst();
    }

    private static @NotNull Component stat(@NotNull String label, @NotNull Component value) {
        return Component.text(label, NamedTextColor.GRAY).append(value);
    }

    private static @NotNull Component ranked(@NotNull String label, @NotNull Component value, @NotNull OptionalInt position) {
        Component line = stat(label, value);
        return position.isEmpty() ? line : line.append(Component.text(" (#" + position.getAsInt() + ")", Leaderboard.rankColor(position.getAsInt() - 1)));
    }
}
