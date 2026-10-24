package gg.itzkatze.katze.bossbar;

import gg.itzkatze.katze.KatzeGames;
import gg.itzkatze.katze.challenges.Challenge;
import gg.itzkatze.katze.challenges.ChallengeManager;
import gg.itzkatze.katze.game.GameTimer;
import gg.itzkatze.katze.player.KatzePlayer;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BossbarManager {

    private static final Map<Challenge, Map<KatzePlayer, Set<BossBar>>> barsByChallenge = new HashMap<>();
    private static BukkitTask runningTask;

    public static void hideBarsForChallenge(Challenge challenge) {
        hideAndClearBarsForChallenge(challenge, barsByChallenge);
    }

    private static void hideAndClearBarsForChallenge(
            Challenge challenge,
            Map<Challenge, Map<KatzePlayer, Set<BossBar>>> byChallenge
    ) {
        Map<KatzePlayer, Set<BossBar>> bars = byChallenge.remove(challenge);
        if (bars == null) return;
        for (Map.Entry<KatzePlayer, Set<BossBar>> entry : bars.entrySet()) {
            KatzePlayer player = entry.getKey();
            for (BossBar bar : entry.getValue()) {
                player.getAudience().hideBossBar(bar);
            }
        }
    }

    public static void runTask() {
        stopTask();
        runningTask = Bukkit.getScheduler().runTaskTimer(KatzeGames.get(), () -> {
            Set<Challenge> activeChallenges = ChallengeManager.getActiveChallenges();
            if (activeChallenges.isEmpty()) return;

            boolean timerRunning = KatzeGames.getTimer().isRunning();
            boolean gameActive = ChallengeManager.isGameStarted()
                    && (KatzeGames.getTimer().getMode() == GameTimer.Mode.DISABLED || timerRunning);
            BossbarKind showKind = timerRunning ? BossbarKind.RUNNING : BossbarKind.NOT_RUNNING;

            for (KatzePlayer player : KatzePlayer.getOnlinePlayers()) {
                if (!player.isParticipating()) {
                    hideAllBarsForPlayer(player);
                    continue;
                }

                for (Challenge challenge : activeChallenges) {
                    clearChallengeBarsForPlayer(player, challenge);
                }

                for (Challenge challenge : activeChallenges) {
                    for (BossbarContent content : challenge.getBossbarContents()) {
                        if (content.getKind() != showKind) continue;
                        BossBar bar = content.getBossbar(player);
                        if (bar != null) {
                            addBossBar(player, bar, challenge);
                        }
                    }
                }
            }
        }, 3L, 3L);
    }

    public static void stopTask() {
        if (runningTask != null) {
            runningTask.cancel();
            runningTask = null;
        }
        for (Challenge challenge : Set.copyOf(barsByChallenge.keySet())) {
            hideAndClearBarsForChallenge(challenge, barsByChallenge);
        }
    }

    private static void hideAllBarsForPlayer(KatzePlayer player) {
        for (Map<KatzePlayer, Set<BossBar>> bars : barsByChallenge.values()) {
            Set<BossBar> set = bars.remove(player);
            if (set != null) {
                for (BossBar bar : set) {
                    player.getAudience().hideBossBar(bar);
                }
            }
        }
    }

    private static void clearChallengeBarsForPlayer(KatzePlayer player, Challenge challenge) {
        Map<KatzePlayer, Set<BossBar>> bars = barsByChallenge.get(challenge);
        if (bars == null) return;
        Set<BossBar> set = bars.remove(player);
        if (set != null) {
            for (BossBar bar : set) {
                player.getAudience().hideBossBar(bar);
            }
        }
    }

    private static void addBossBar(KatzePlayer player, BossBar bar, Challenge challenge) {
        player.getAudience().showBossBar(bar);
        barsByChallenge
                .computeIfAbsent(challenge, k -> new HashMap<>())
                .computeIfAbsent(player, k -> new HashSet<>())
                .add(bar);
    }
}
