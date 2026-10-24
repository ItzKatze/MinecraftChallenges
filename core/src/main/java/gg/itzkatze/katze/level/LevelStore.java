package gg.itzkatze.katze.level;

import gg.itzkatze.katze.player.KatzePlayer;
import gg.itzkatze.katze.player.data.PlayerDataStore;
import gg.itzkatze.katze.player.data.PlayerProfile;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class LevelStore {

    public interface LevelUpListener {
        void onLevelUp(@NotNull UUID player, int oldLevel, int newLevel);
    }

    private static final Logger logger = LoggerFactory.getLogger("KatzeGames/" + LevelStore.class.getSimpleName());

    private static @Nullable LevelStore installed;

    private final PlayerDataStore data;
    private final Map<UUID, Integer> levels = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> recentGains = new HashMap<>();
    private final List<LevelUpListener> levelUpListeners = new ArrayList<>();
    private final List<Consumer<UUID>> changeListeners = new ArrayList<>();

    public LevelStore(@NotNull PlayerDataStore data) {
        this.data = data;
        data.profiles().forEach((id, profile) -> levels.put(id, LevelCurve.levelFor(profile.xp())));
    }

    public static @NotNull LevelStore get() {
        if (installed == null) {
            throw new IllegalStateException("The level store has not been loaded");
        }
        return installed;
    }

    public static void install(@Nullable LevelStore store) {
        installed = store;
    }

    public static void load() {
        install(new LevelStore(PlayerDataStore.get()));
    }

    public int xp(@NotNull UUID player) {
        PlayerProfile profile = data.find(player);
        return profile == null ? 0 : profile.xp();
    }

    public int level(@NotNull UUID player) {
        return levels.getOrDefault(player, 1);
    }

    public @NotNull LevelProgress progress(@NotNull UUID player) {
        return LevelCurve.progress(xp(player));
    }

    public void grant(@NotNull UUID player, @Nullable String name, int amount) {
        if (amount <= 0) return;

        PlayerProfile profile = data.profile(player, name);
        int before = profile.xp();
        profile.setXp(add(before, amount));
        recentGains.merge(player, amount, LevelStore::add);
        changed(player, before, profile.xp());
    }

    public void grant(@NotNull KatzePlayer player, int amount) {
        grant(player.getUuid(), player.getName(), amount);
    }

    public void grant(@NotNull Player player, int amount) {
        grant(player.getUniqueId(), player.getName(), amount);
    }

    public int adjust(@NotNull UUID player, @Nullable String name, int delta) {
        PlayerProfile profile = data.profile(player, name);
        int before = profile.xp();
        profile.setXp((int) Math.clamp((long) before + delta, 0, Integer.MAX_VALUE));
        changed(player, before, profile.xp());
        return profile.xp();
    }

    public void addLevelUpListener(@NotNull LevelUpListener listener) {
        levelUpListeners.add(listener);
    }

    public void addChangeListener(@NotNull Consumer<UUID> listener) {
        changeListeners.add(listener);
    }

    public @NotNull Map<UUID, Integer> drainRecentGains() {
        Map<UUID, Integer> drained = Map.copyOf(recentGains);
        recentGains.clear();
        return drained;
    }

    private void changed(@NotNull UUID player, int xpBefore, int xpAfter) {
        int oldLevel = LevelCurve.levelFor(xpBefore);
        int newLevel = LevelCurve.levelFor(xpAfter);
        levels.put(player, newLevel);
        data.markDirty();

        if (newLevel > oldLevel) {
            for (LevelUpListener listener : List.copyOf(levelUpListeners)) {
                try {
                    listener.onLevelUp(player, oldLevel, newLevel);
                } catch (RuntimeException e) {
                    logger.error("A level-up listener failed", e);
                }
            }
        }
        for (Consumer<UUID> listener : List.copyOf(changeListeners)) {
            try {
                listener.accept(player);
            } catch (RuntimeException e) {
                logger.error("A level change listener failed", e);
            }
        }
    }

    private static int add(int value, int amount) {
        return (int) Math.min(Integer.MAX_VALUE, (long) value + amount);
    }
}
