package gg.itzkatze.katze.level;

import gg.itzkatze.katze.KatzeGames;
import gg.itzkatze.katze.challenges.ChallengeManager;
import gg.itzkatze.katze.challenges.ChallengeType;
import gg.itzkatze.katze.game.GameTimer;
import gg.itzkatze.katze.game.SessionPersistence;
import gg.itzkatze.katze.player.KatzePlayer;
import gg.itzkatze.katze.util.WorldStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class GameXp {

    public static final String FILE_NAME = "level_game.json";
    public static final int SECONDS_PER_GAME_XP = 108;

    private static @Nullable Ledger ledger;

    private GameXp() {
    }

    public static void begin() {
        ledger = new Ledger();
    }

    public static boolean isGameRunning() {
        GameTimer timer = KatzeGames.getTimer();
        return ChallengeManager.isGameStarted()
                && (ledger == null || !ledger.finished)
                && !timer.hasHitZero()
                && (timer.getMode() == GameTimer.Mode.DISABLED || timer.isRunning());
    }

    public static int creditActiveSecond(@NotNull KatzePlayer player) {
        Entry entry = ledger().players.computeIfAbsent(player.getUuid().toString(), key -> new Entry());
        entry.name = player.getName();
        entry.activeSeconds++;
        return entry.activeSeconds;
    }

    public static boolean claimNaturalEnd(@NotNull ChallengeType type) {
        if (!ledger().ended.add(type.name())) return false;

        finish();
        return true;
    }

    public static void finish() {
        Ledger current = ledger;
        if (current == null || current.finished) return;

        current.finished = true;
        current.players.forEach((key, entry) -> {
            UUID id = parseUuid(key);
            if (id != null && entry != null) {
                LevelStore.get().grant(id, entry.name, entry.activeSeconds / SECONDS_PER_GAME_XP);
            }
        });
        SessionPersistence.requestSave();
    }

    public static void onReset() {
        finish();
        delete();
    }

    public static void save() {
        if (ledger == null) {
            WorldStorage.delete(FILE_NAME);
        } else {
            WorldStorage.save(FILE_NAME, ledger);
        }
    }

    public static void restore() {
        Ledger loaded = WorldStorage.load(FILE_NAME, Ledger.class);
        ledger = loaded == null ? null : loaded.normalized();
    }

    public static void delete() {
        ledger = null;
        WorldStorage.delete(FILE_NAME);
    }

    private static @NotNull Ledger ledger() {
        if (ledger == null) {
            ledger = new Ledger();
        }
        return ledger;
    }

    private static @Nullable UUID parseUuid(@NotNull String raw) {
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static final class Ledger {
        boolean finished;
        Set<String> ended = new LinkedHashSet<>();
        Map<String, Entry> players = new LinkedHashMap<>();

        @NotNull Ledger normalized() {
            if (ended == null) {
                ended = new LinkedHashSet<>();
            }
            if (players == null) {
                players = new LinkedHashMap<>();
            }
            return this;
        }
    }

    private static final class Entry {
        String name;
        int activeSeconds;
    }
}
