package gg.itzkatze.mcc.lobby.cosmetics;

import gg.itzkatze.mcc.player.data.PlayerDataStore;
import gg.itzkatze.mcc.player.data.PlayerProfile;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.function.Consumer;

public final class CosmeticStore {

    private static final Logger logger = LoggerFactory.getLogger("MinecraftChallenges/" + CosmeticStore.class.getSimpleName());
    private static final int SHORT_ID_LENGTH = 8;

    private static @Nullable CosmeticStore installed;

    private final PlayerDataStore data;
    private final CosmeticCatalog catalog;
    private final Clock clock;
    private final List<Consumer<UUID>> balanceListeners = new ArrayList<>();

    public CosmeticStore(@NotNull PlayerDataStore data, @NotNull CosmeticCatalog catalog) {
        this(data, catalog, Clock.systemDefaultZone());
    }

    public CosmeticStore(@NotNull PlayerDataStore data, @NotNull CosmeticCatalog catalog, @NotNull Clock clock) {
        this.data = data;
        this.catalog = catalog;
        this.clock = clock;
        data.profiles().values().forEach(profile -> profile.cosmetics().normalize(catalog));
    }

    public static @NotNull CosmeticStore get() {
        if (installed == null) {
            throw new IllegalStateException("The cosmetic store has not been loaded");
        }
        return installed;
    }

    public static void install(@Nullable CosmeticStore store) {
        installed = store;
    }

    public static void load() {
        install(new CosmeticStore(PlayerDataStore.get(), CosmeticCatalog.standard()));
    }

    public @NotNull CosmeticCatalog catalog() {
        return catalog;
    }

    public @NotNull LocalDate today() {
        return LocalDate.now(clock);
    }

    public int points(@NotNull UUID player) {
        CosmeticProfile profile = cosmetics(player);
        return profile == null ? 0 : profile.points();
    }

    public int earned(@NotNull UUID player) {
        CosmeticProfile profile = cosmetics(player);
        return profile == null ? 0 : profile.earned();
    }

    public int best(@NotNull UUID player) {
        CosmeticProfile profile = cosmetics(player);
        return profile == null ? 0 : profile.best();
    }

    public int casesOpened(@NotNull UUID player) {
        CosmeticProfile profile = cosmetics(player);
        return profile == null ? 0 : profile.casesOpened();
    }

    public boolean owns(@NotNull UUID player, @NotNull Cosmetic cosmetic) {
        CosmeticProfile profile = cosmetics(player);
        return profile != null && profile.owns(cosmetic.id());
    }

    public @NotNull Optional<Cosmetic> equipped(@NotNull UUID player, @NotNull CosmeticCategory category) {
        CosmeticProfile profile = cosmetics(player);
        return profile == null ? Optional.empty() : profile.equipped(category, catalog);
    }

    public @NotNull Optional<BlockCosmetic> equippedBlock(@NotNull UUID player) {
        return equipped(player, CosmeticCategory.BLOCK).filter(BlockCosmetic.class::isInstance).map(BlockCosmetic.class::cast);
    }

    public @NotNull Optional<SoundCosmetic> equippedSound(@NotNull UUID player) {
        return equipped(player, CosmeticCategory.SOUND).filter(SoundCosmetic.class::isInstance).map(SoundCosmetic.class::cast);
    }

    public @NotNull Optional<MusicCosmetic> equippedMusic(@NotNull UUID player) {
        return equipped(player, CosmeticCategory.MUSIC).filter(MusicCosmetic.class::isInstance).map(MusicCosmetic.class::cast);
    }

    public @NotNull List<Standing> top(@NotNull LeaderboardMetric metric, int limit) {
        return data.profiles().entrySet().stream()
                .filter(entry -> metric.read(entry.getValue().cosmetics()) > 0)
                .sorted(boardOrder(metric))
                .limit(Math.max(limit, 0))
                .map(entry -> new Standing(displayName(entry), metric.read(entry.getValue().cosmetics())))
                .toList();
    }

    public @NotNull OptionalInt rank(@NotNull LeaderboardMetric metric, @NotNull UUID player) {
        PlayerProfile profile = data.find(player);
        if (profile == null || metric.read(profile.cosmetics()) <= 0) return OptionalInt.empty();

        Map.Entry<UUID, PlayerProfile> own = Map.entry(player, profile);
        Comparator<Map.Entry<UUID, PlayerProfile>> order = boardOrder(metric);
        long ahead = data.profiles().entrySet().stream()
                .filter(entry -> metric.read(entry.getValue().cosmetics()) > 0 && order.compare(entry, own) < 0)
                .count();
        return OptionalInt.of((int) ahead + 1);
    }

    public void recordLanding(@NotNull Player player, int runScore) {
        recordLanding(player, runScore, 1);
    }

    public void recordLanding(@NotNull Player player, int runScore, int pointsToGain) {
        CosmeticProfile profile = profile(player);
        int before = profile.points();
        profile.recordLanding(runScore, pointsToGain);
        data.markDirty();

        if (profile.points() != before) {
            notifyBalance(player.getUniqueId());
        }
    }

    public @NotNull PurchaseResult buy(@NotNull Player player, @NotNull Cosmetic cosmetic) {
        CosmeticProfile profile = cosmetics(player.getUniqueId());
        if (profile == null) return cosmetic.isAvailable(today()) ? PurchaseResult.NOT_ENOUGH_POINTS : PurchaseResult.OUT_OF_SEASON;

        PurchaseResult result = profile.buy(cosmetic, catalog, today());
        if (result == PurchaseResult.BOUGHT) {
            data.profile(player.getUniqueId(), player.getName());
            data.markDirty();
            notifyBalance(player.getUniqueId());
        }
        return result;
    }

    public boolean openCase(@NotNull Player player, int price) {
        CosmeticProfile profile = cosmetics(player.getUniqueId());
        if (profile == null) return false;

        int before = profile.points();
        if (!profile.openCase(price)) return false;

        changed(player, profile, before);
        return true;
    }

    public void addPoints(@NotNull Player player, int amount) {
        if (amount <= 0) return;

        CosmeticProfile profile = profile(player);
        int before = profile.points();
        profile.addPoints(amount);
        changed(player, profile, before);
    }

    public boolean grant(@NotNull Player player, @NotNull Cosmetic cosmetic) {
        CosmeticProfile profile = profile(player);
        if (!profile.grant(cosmetic)) return false;

        changed(player, profile, profile.points());
        return true;
    }

    public int addEventPoints(@NotNull Player player, int amount) {
        CosmeticProfile profile = profile(player);
        int before = profile.points();
        int applied = profile.addEventPoints(amount);
        changed(player, profile, before);
        return applied;
    }

    public boolean equip(@NotNull Player player, @NotNull Cosmetic cosmetic) {
        CosmeticProfile profile = cosmetics(player.getUniqueId());
        if (profile == null || !profile.equip(cosmetic, catalog)) return false;

        data.profile(player.getUniqueId(), player.getName());
        data.markDirty();
        return true;
    }

    public void unequip(@NotNull Player player, @NotNull CosmeticCategory category) {
        CosmeticProfile profile = cosmetics(player.getUniqueId());
        if (profile == null || profile.equipped(category, catalog).isEmpty()) return;

        profile.unequip(category, catalog);
        data.profile(player.getUniqueId(), player.getName());
        data.markDirty();
    }

    public void addBalanceListener(@NotNull Consumer<UUID> listener) {
        balanceListeners.add(listener);
    }

    private @Nullable CosmeticProfile cosmetics(@NotNull UUID player) {
        PlayerProfile profile = data.find(player);
        return profile == null ? null : profile.cosmetics();
    }

    private @NotNull CosmeticProfile profile(@NotNull Player player) {
        return data.profile(player.getUniqueId(), player.getName()).cosmetics();
    }

    private static @NotNull Comparator<Map.Entry<UUID, PlayerProfile>> boardOrder(@NotNull LeaderboardMetric metric) {
        return Comparator.<Map.Entry<UUID, PlayerProfile>>comparingInt(entry -> metric.read(entry.getValue().cosmetics())).reversed()
                .thenComparing(CosmeticStore::displayName, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(Map.Entry::getKey);
    }

    private static @NotNull String displayName(@NotNull Map.Entry<UUID, PlayerProfile> entry) {
        String name = entry.getValue().name();
        return name == null || name.isBlank() ? entry.getKey().toString().substring(0, SHORT_ID_LENGTH) : name;
    }

    private void changed(@NotNull Player player, @NotNull CosmeticProfile profile, int pointsBefore) {
        data.profile(player.getUniqueId(), player.getName());
        data.markDirty();
        if (profile.points() != pointsBefore) {
            notifyBalance(player.getUniqueId());
        }
    }

    private void notifyBalance(@NotNull UUID player) {
        for (Consumer<UUID> listener : List.copyOf(balanceListeners)) {
            try {
                listener.accept(player);
            } catch (RuntimeException e) {
                logger.error("A cosmetic balance listener failed", e);
            }
        }
    }
}
