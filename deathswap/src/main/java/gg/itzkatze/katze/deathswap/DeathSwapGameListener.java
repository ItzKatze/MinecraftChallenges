package gg.itzkatze.katze.deathswap;

import gg.itzkatze.katze.KatzeGames;
import gg.itzkatze.katze.challenges.ChallengeType;
import gg.itzkatze.katze.game.ChallengeTimer;
import gg.itzkatze.katze.game.GameTimer;
import gg.itzkatze.katze.game.SessionPersistence;
import gg.itzkatze.katze.level.GameXp;
import gg.itzkatze.katze.level.LevelStore;
import gg.itzkatze.katze.player.KatzePlayer;
import gg.itzkatze.katze.spectator.SpectatorManager;
import gg.itzkatze.katze.spectator.SpectatorReason;
import gg.itzkatze.katze.util.KatzeColor;
import gg.itzkatze.katze.util.StringUtility;
import gg.itzkatze.katze.util.WorldStorage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class DeathSwapGameListener implements Listener {

    private static final String FILE_NAME = "deathswap_data.json";

    private final Set<UUID> eliminatedPlayers = new HashSet<>();
    private final Random random = new Random();
    private boolean started = false;
    private boolean finished = false;
    private int swapCount = 0;
    private UUID winnerId = null;

    private ChallengeTimer elapsedTimer;
    private ChallengeTimer swapTimer;

    private int lastIntervalMinutes = -1;
    private int elapsedSeconds = 0;
    private int lastPlayedCountdownSecond = -1;

    public void initialize() {
        eliminatedPlayers.clear();
        started = false;
        finished = false;
        swapCount = 0;
        winnerId = null;
        lastIntervalMinutes = -1;
        elapsedSeconds = 0;
        lastPlayedCountdownSecond = -1;
        elapsedTimer = null;
        swapTimer = null;
    }

    public void cleanup() {
        initialize();
    }

    public void startGame() {
        initialize();
        started = true;
        elapsedTimer = new ChallengeTimer(1_000L);
        swapTimer = new ChallengeTimer(getSwapIntervalMillis());
    }

    public void saveToFile() {
        SaveData data = new SaveData();
        data.started = started;
        data.finished = finished;
        data.eliminated = eliminatedPlayers.stream().map(UUID::toString).toList();
        data.swapCount = swapCount;
        data.winner = winnerId == null ? null : winnerId.toString();
        data.elapsedSeconds = elapsedSeconds;
        data.millisUntilSwap = swapTimer == null ? null : swapTimer.getRemainingMillis();
        WorldStorage.save(FILE_NAME, data);
    }

    public void loadFromFile() {
        SaveData data = WorldStorage.load(FILE_NAME, SaveData.class);
        if (data == null) return;

        initialize();
        started = data.started;
        finished = data.finished;
        if (data.eliminated != null) {
            data.eliminated.stream().map(DeathSwapGameListener::parseUuid).filter(Objects::nonNull).forEach(eliminatedPlayers::add);
        }
        // session.json restored the reasons already; this keeps the eliminated players out when the save predates the spectators.
        eliminatedPlayers.forEach(uuid -> SpectatorManager.restore(uuid, SpectatorReason.ELIMINATED));
        swapCount = data.swapCount;
        winnerId = parseUuid(data.winner);
        elapsedSeconds = data.elapsedSeconds;

        if (started) {
            elapsedTimer = new ChallengeTimer(1_000L);
            swapTimer = new ChallengeTimer(getSwapIntervalMillis());
            if (data.millisUntilSwap != null) {
                swapTimer.setRemainingMillis(data.millisUntilSwap);
            }
            lastIntervalMinutes = Math.max(1, DeathSwapSettings.getSwapIntervalMinutes());
        }
    }

    public void deleteFile() {
        WorldStorage.delete(FILE_NAME);
    }

    public void runTask() {
        if (!started || finished) return;

        if (KatzeGames.getTimer().getMode() != GameTimer.Mode.DISABLED
                && !KatzeGames.getTimer().isRunning()) {
            elapsedTimer.resetBaseline();
            swapTimer.resetBaseline();
            return;
        }

        int intervalMinutes = Math.max(1, DeathSwapSettings.getSwapIntervalMinutes());
        if (intervalMinutes != lastIntervalMinutes) {
            lastIntervalMinutes = intervalMinutes;
            swapTimer = new ChallengeTimer(getSwapIntervalMillis());
        }

        // Tick both timers (they share the same wall clock).
        elapsedTimer.tick();
        if (elapsedTimer.hasElapsed()) {
            elapsedSeconds++;
            elapsedTimer.consumeElapsed();
        }

        swapTimer.tick();

        int secondsUntilSwap = swapTimer.getSecondsRemaining();
        if (secondsUntilSwap <= 10 && secondsUntilSwap > 0 && secondsUntilSwap != lastPlayedCountdownSecond) {
            lastPlayedCountdownSecond = secondsUntilSwap;
            net.kyori.adventure.sound.Sound sound = net.kyori.adventure.sound.Sound.sound(
                    net.kyori.adventure.key.Key.key("block.note_block.bass"),
                    net.kyori.adventure.sound.Sound.Source.MASTER,
                    20f,
                    20f
            );
            KatzePlayer.getOnlinePlayers().forEach(p -> p.playSound(sound));
        } else if (secondsUntilSwap > 10) {
            lastPlayedCountdownSecond = -1;
        }

        while (swapTimer.hasElapsed()) {
            lastPlayedCountdownSecond = -1;
            boolean swapped = swapAlivePlayers();
            if (swapped) {
                swapCount++;
                SessionPersistence.requestSave();
            }
            swapTimer.consumeElapsed();
            if (!swapped) break;
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        eliminate(event.getEntity());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        eliminate(event.getPlayer());
    }

    private void eliminate(Player player) {
        if (!started || finished || SpectatorManager.isSpectator(player.getUniqueId())) return;
        if (eliminatedPlayers.add(player.getUniqueId())) {
            SpectatorManager.eliminate(player.getUniqueId());
            checkForWinner();
            SessionPersistence.requestSave();
        }
    }

    public Component getActionbar(KatzePlayer player, boolean timerRunning) {
        if (!started) {
            return null;
        }

        Component message = Component.text()
                .append(Component.text("Elapsed: ").color(KatzeColor.LIGHT_GRAY))
                .append(Component.text(StringUtility.formatTime(elapsedSeconds).trim()).color(KatzeColor.LIGHT_BLUE))
                .append(Component.text(" | ").color(KatzeColor.GRAY))
                .append(Component.text("Swaps: ").color(KatzeColor.LIGHT_GRAY))
                .append(Component.text(swapCount).color(KatzeColor.MAGENTA))
                .build();

        int secondsUntilSwap = swapTimer != null ? swapTimer.getSecondsRemaining() : 0;
        if (secondsUntilSwap <= 10) {
            message = message.append(Component.text(" | ").color(KatzeColor.GRAY))
                    .append(Component.text("Next swap: ").color(KatzeColor.LIGHT_GRAY))
                    .append(Component.text(secondsUntilSwap + "s").color(KatzeColor.RED));
        } else if (KatzeGames.getTimer().getMode() != GameTimer.Mode.DISABLED && !timerRunning) {
            message = message.append(Component.text(" | ").color(KatzeColor.GRAY))
                    .append(Component.text("Paused").color(KatzeColor.BLUE));
        }

        return message;
    }

    private boolean swapAlivePlayers() {
        List<Player> alivePlayers = getAlivePlayers();
        if (alivePlayers.size() < 2) return false;

        Collections.shuffle(alivePlayers, random);

        for (int i = 0; i + 1 < alivePlayers.size(); i += 2) {
            Player first = alivePlayers.get(i);
            Player second = alivePlayers.get(i + 1);

            Location firstLocation = first.getLocation().clone();
            Location secondLocation = second.getLocation().clone();

            first.teleport(secondLocation);
            second.teleport(firstLocation);
        }

        for (Player survivor : alivePlayers) {
            KatzePlayer participant = KatzePlayer.adapt(survivor);
            if (participant.isParticipating()) {
                LevelStore.get().grant(participant, DeathSwapXp.SWAP_SURVIVED.xp());
            }
        }
        return true;
    }

    private List<Player> getAlivePlayers() {
        List<Player> alivePlayers = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (eliminatedPlayers.contains(player.getUniqueId())) continue;
            if (player.getGameMode() == GameMode.SPECTATOR) continue;
            alivePlayers.add(player);
        }
        return alivePlayers;
    }

    private void checkForWinner() {
        if (finished) return;
        List<Player> alivePlayers = getAlivePlayers();
        if (alivePlayers.size() != 1) return;

        finished = true;

        Player winner = alivePlayers.getFirst();
        if (winnerId != null && winnerId.equals(winner.getUniqueId())) {
            return;
        }
        winnerId = winner.getUniqueId();
        KatzePlayer winnerWrapper = KatzePlayer.adapt(winner);
        if (GameXp.claimNaturalEnd(ChallengeType.DEATH_SWAP) && winnerWrapper.isParticipating()) {
            LevelStore.get().grant(winnerWrapper, DeathSwapXp.WIN.xp());
        }
        KatzeGames.broadcast(
                Component.text("Death Swap winner: ").color(KatzeColor.GOLD)
                        .append(Component.text(winner.getName()).color(KatzeColor.LIME))
        );

        Title title = Title.title(
                Component.text("Death Swap").color(KatzeColor.GOLD),
                Component.text(winner.getName() + " won!").color(KatzeColor.LIME),
                Title.Times.times(Duration.ofSeconds(1), Duration.ofSeconds(3), Duration.ofSeconds(1))
        );

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.showTitle(title);
        }
    }

    private long getSwapIntervalMillis() {
        return Math.max(1, DeathSwapSettings.getSwapIntervalMinutes()) * 60_000L;
    }

    private static @Nullable UUID parseUuid(@Nullable String raw) {
        if (raw == null) return null;
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static final class SaveData {
        boolean started;
        boolean finished;
        List<String> eliminated = new ArrayList<>();
        int swapCount;
        String winner;
        int elapsedSeconds;
        Long millisUntilSwap;
    }
}
