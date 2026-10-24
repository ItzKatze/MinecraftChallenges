package gg.itzkatze.katze.scoreboard;

import gg.itzkatze.katze.KatzeGames;
import gg.itzkatze.katze.challenges.Challenge;
import gg.itzkatze.katze.challenges.ChallengeManager;
import gg.itzkatze.katze.configuration.SettingsManager;
import gg.itzkatze.katze.configuration.GlobalSetting;
import gg.itzkatze.katze.game.GameTimer;
import gg.itzkatze.katze.lobby.LobbyManager;
import gg.itzkatze.katze.lobby.cosmetics.PointsSidebar;
import gg.itzkatze.katze.player.KatzePlayer;
import gg.itzkatze.katze.util.KatzeColor;
import io.papermc.paper.scoreboard.numbers.NumberFormat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;


public class ScoreboardManager {

    private static final String SIDEBAR_OBJECTIVE = "challenge_info";
    private static final Map<KatzePlayer, Scoreboard> SCOREBOARDS = new HashMap<>();
    private static final Map<KatzePlayer, Map<Integer, Component>> LAST_LINES = new HashMap<>();
    private static final ScoreboardContent LOBBY_CONTENT = new PointsSidebar();

    public static void startUpdater() {
        Bukkit.getScheduler().runTaskTimer(KatzeGames.get(), ScoreboardManager::updateAll, 20L, 20L);
    }

    public static void updateAll() {
        boolean gameActive = ChallengeManager.isGameStarted()
                && (KatzeGames.getTimer().getMode() == GameTimer.Mode.DISABLED
                    || KatzeGames.getTimer().isRunning());

        SCOREBOARDS.keySet().removeIf(wrapper -> !KatzePlayer.PLAYERS.contains(wrapper));
        LAST_LINES.keySet().removeIf(wrapper -> !KatzePlayer.PLAYERS.contains(wrapper));

        boolean lobbyDue = lobbySidebarDue();
        for (KatzePlayer katzePlayer : KatzePlayer.getOnlinePlayers()) {
            boolean shouldHaveScoreboard = katzePlayer.isParticipating() && gameActive;
            boolean showScoreboard = SettingsManager.isEnabled(GlobalSetting.SHOW_SCOREBOARD);

            if (shouldHaveScoreboard && showScoreboard) {
                updateScoreboard(katzePlayer);
            } else if (lobbyDue) {
                render(katzePlayer, LOBBY_CONTENT);
            } else if (SCOREBOARDS.containsKey(katzePlayer)) {
                removeSidebar(katzePlayer);
            }
        }
    }

    public static void refreshLobby(@NotNull KatzePlayer player) {
        if (!lobbySidebarDue() || player.getPlayer() == null) return;

        render(player, LOBBY_CONTENT);
    }

    static boolean lobbySidebarDue() {
        return LobbyManager.isActive() && !ChallengeManager.isGameStarted() && SettingsManager.isEnabled(GlobalSetting.SHOW_SCOREBOARD);
    }

    public static Scoreboard getOrCreateScoreboard(KatzePlayer player) {
        return SCOREBOARDS.computeIfAbsent(player, p -> Bukkit.getScoreboardManager().getNewScoreboard());
    }

    public static @Nullable Scoreboard findScoreboard(KatzePlayer player) {
        return SCOREBOARDS.get(player);
    }

    public static void updateScoreboard(KatzePlayer player) {
        Set<Challenge> activeChallenges = ChallengeManager.getActiveChallenges();
        if (activeChallenges.isEmpty()) return;

        // Find the challenge with the highest priority
        Challenge highestPriorityChallenge = activeChallenges.stream()
                .filter(c -> c.getScoreboardContent() != null)
                .max(Comparator.comparingInt(c -> c.getScoreboardContent().getPriority()))
                .orElse(null);
        if (highestPriorityChallenge == null) return;

        ScoreboardContent content = highestPriorityChallenge.getScoreboardContent();
        if (content == null) return;

        render(player, content);
    }

    private static void render(@NotNull KatzePlayer player, @NotNull ScoreboardContent content) {
        Scoreboard scoreboard = getOrCreateScoreboard(player);

        Component title = styleTitle(content.getTitle());
        Objective obj = scoreboard.getObjective(SIDEBAR_OBJECTIVE);
        if (obj == null) {
            obj = scoreboard.registerNewObjective(SIDEBAR_OBJECTIVE, "dummy", title);
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        } else if (!title.equals(obj.displayName())) {
            obj.displayName(title);
        }

        Map<Integer, Component> last = LAST_LINES.computeIfAbsent(player, p -> new HashMap<>());
        Map<Integer, Component> newLines = new HashMap<>();

        // Get lines from the challenge's scoreboard content
        Component[] lines = content.getLines(player);

        // Add lines from top to bottom (scoreboard shows from bottom to top)
        int lineNumber = 15;
        for (int i = 0; i < lines.length && lineNumber > 0; i++) {
            if (lines[i] != null && !lines[i].equals(Component.empty())) {
                newLines.put(lineNumber--, lines[i]);
            }
        }

        // Remove old lines that are no longer present
        for (Map.Entry<Integer, Component> entry : last.entrySet()) {
            if (!newLines.containsKey(entry.getKey())) {
                scoreboard.resetScores(lineKey(entry.getKey()));
            }
        }

        // Add new lines
        for (Map.Entry<Integer, Component> entry : newLines.entrySet()) {
            if (!Objects.equals(entry.getValue(), last.get(entry.getKey()))) {
                Score score = obj.getScore(lineKey(entry.getKey()));
                score.customName(styleLine(entry.getValue()));
                score.numberFormat(NumberFormat.blank());
                score.setScore(entry.getKey());
            }
        }

        LAST_LINES.put(player, newLines);

        if (player.getPlayer().getScoreboard() != scoreboard) {
            player.getPlayer().setScoreboard(scoreboard);
        }
    }

    public static void removeSidebar(KatzePlayer player) {
        LAST_LINES.remove(player);
        Scoreboard scoreboard = SCOREBOARDS.get(player);
        if (scoreboard == null) return;

        Objective sidebar = scoreboard.getObjective(SIDEBAR_OBJECTIVE);
        if (sidebar != null) {
            sidebar.unregister();
        }
    }

    private static Component styleTitle(Component title) {
        return title.colorIfAbsent(KatzeColor.GOLD)
                .decorate(TextDecoration.BOLD);
    }

    private static Component styleLine(Component line) {
        return line.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }

    private static String lineKey(int lineNumber) {
        return "line_" + lineNumber;
    }
}
