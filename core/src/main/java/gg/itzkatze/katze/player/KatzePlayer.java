package gg.itzkatze.katze.player;

import gg.itzkatze.katze.challenges.Challenge;
import lombok.Getter;
import gg.itzkatze.katze.challenges.ChallengeManager;
import gg.itzkatze.katze.challenges.ChallengeObjective;
import gg.itzkatze.katze.challenges.ChallengeType;
import gg.itzkatze.katze.commands.framework.CallerType;
import gg.itzkatze.katze.commands.framework.CommandCaller;
import gg.itzkatze.katze.event.PlayerExcludeEvent;
import gg.itzkatze.katze.game.GameLifecycleManager;
import gg.itzkatze.katze.spectator.SpectatorManager;
import gg.itzkatze.katze.spectator.SpectatorReason;
import gg.itzkatze.katze.team.KatzeTeam;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.IntPredicate;

public class KatzePlayer implements CommandCaller {

    public static final List<KatzePlayer> PLAYERS = new ArrayList<>();
    private static final Logger logger = LoggerFactory.getLogger("KatzeGames/" + KatzePlayer.class.getSimpleName());
    private static final Map<ChallengeType, PlayerAdapterFactory> factories = new HashMap<>();

    @Getter
    private final UUID uuid;
    protected String name;
    @Getter
    private boolean excluded;

    public KatzePlayer(@NotNull Player player) {
        this(player.getUniqueId(), player.getName());
    }

    public KatzePlayer(@NotNull UUID uuid, @NotNull String name) {
        this.uuid = uuid;
        this.name = name;
        PLAYERS.add(this);
    }

    /**
     * Registers a factory for creating challenge-specific player wrappers.
     * Called by challenge modules in their {@code onEnable}.
     */
    public static void registerFactory(@NotNull ChallengeType type, @NotNull PlayerAdapterFactory factory) {
        factories.put(type, factory);
    }

    /**
     * Unregisters the factory for the given challenge type. Called in {@code onDisable}.
     */
    public static void unregisterFactory(@NotNull ChallengeType type) {
        factories.remove(type);
    }

    /**
     * Upgrades all player wrappers, online and offline, to the challenge-specific type if a factory is registered.
     * Call from the challenge's {@code onEnable}.
     */
    public static void upgrade(@NotNull ChallengeType type) {
        PlayerAdapterFactory factory = factories.get(type);
        if (factory == null) return;

        for (KatzePlayer player : new ArrayList<>(PLAYERS)) {
            replace(player, factory.adapt(player.getUuid(), player.getName()));
        }
    }

    /**
     * Returns the player wrapper for the given Bukkit player.
     * Uses registered factories for active challenges; otherwise returns a base KatzePlayer.
     */
    public static @NotNull KatzePlayer adapt(Player object) {
        return adapt(object.getUniqueId(), object.getName());
    }

    /**
     * Returns the wrapper for the given player, creating one if the player has none yet.
     */
    public static @NotNull KatzePlayer adapt(@NotNull UUID uuid, @NotNull String name) {
        Optional<KatzePlayer> existing = adapt(uuid);
        if (existing.isPresent()) {
            return existing.get();
        }
        for (Challenge challenge : ChallengeManager.getActiveChallenges()) {
            PlayerAdapterFactory factory = factories.get(challenge.getType());
            if (factory != null) {
                return factory.adapt(uuid, name);
            }
        }
        return new KatzePlayer(uuid, name);
    }

    public static @NotNull KatzePlayer restore(@NotNull UUID uuid, @NotNull String name, boolean excluded) {
        KatzePlayer player = adapt(uuid, name);
        player.excluded = excluded;
        return player;
    }

    public static @NotNull Optional<KatzePlayer> adapt(String name) {
        for (KatzePlayer katzePlayer : PLAYERS) {
            if (katzePlayer.getName().equalsIgnoreCase(name)) {
                return Optional.of(katzePlayer);
            }
        }
        return Optional.empty();
    }

    public static @NotNull Optional<KatzePlayer> adapt(UUID uuid) {
        for (KatzePlayer katzePlayer : PLAYERS) {
            if (katzePlayer.getUuid().equals(uuid)) {
                return Optional.of(katzePlayer);
            }
        }
        return Optional.empty();
    }

    public static void remove(UUID uuid) {
        List<KatzePlayer> matches = PLAYERS.stream()
                .filter(player -> player.uuid.equals(uuid))
                .toList();
        for (KatzePlayer player : matches) {
            KatzeTeam team = player.getTeam();
            if (team != null) {
                team.getPlayers().remove(player);
                if (team.getPlayers().isEmpty()) team.delete();
            }
            PLAYERS.remove(player);
        }
        SpectatorManager.remove(uuid);
    }

    public static void removeOfflinePlayers() {
        PLAYERS.stream()
                .filter(KatzePlayer::isOffline)
                .map(KatzePlayer::getUuid)
                .toList()
                .forEach(KatzePlayer::remove);
    }

    /**
     * Replaces all player wrappers for the given challenge type with base KatzePlayer.
     * Call from the challenge's {@code onDisable}.
     */
    public static void downgrade(@NotNull ChallengeType type) {
        for (KatzePlayer player : new ArrayList<>(PLAYERS)) {
            if (player.getChallengeType().orElse(null) == type) {
                replace(player, new KatzePlayer(player.getUuid(), player.getName()));
            }
        }
    }

    private static void replace(@NotNull KatzePlayer old, @NotNull KatzePlayer replacement) {
        PLAYERS.remove(old);
        replacement.excluded = old.excluded;
        KatzeTeam.replacePlayer(old, replacement);
    }

    /**
     * Challenge type for this player wrapper; empty for base KatzePlayer.
     */
    public @NotNull Optional<ChallengeType> getChallengeType() {
        return Optional.empty();
    }

    public static @NotNull List<KatzePlayer> getOnlinePlayers() {
        List<KatzePlayer> onlinePlayers = new ArrayList<>();
        for (KatzePlayer katzePlayer : PLAYERS) {
            if (katzePlayer.isOffline()) continue;
            onlinePlayers.add(katzePlayer);
        }
        return onlinePlayers;
    }

    public static @NotNull List<UUID> getAllUUIDs() {
        List<UUID> uuids = new ArrayList<>();
        for (KatzePlayer katzePlayer : PLAYERS) {
            uuids.add(katzePlayer.uuid);
        }
        return uuids;
    }

    public static Map<Integer, KatzePlayer> getPlaces() {
        Map<Integer, KatzePlayer> places = new HashMap<>();
        Map<KatzePlayer, Integer> pointMap = new HashMap<>();
        for (KatzePlayer player : PLAYERS) {
            if (player.isRanked()) {
                pointMap.put(player, player.getPoints());
            }
        }
        List<Map.Entry<KatzePlayer, Integer>> sorted = pointMap.entrySet().stream()
                .sorted(Map.Entry.<KatzePlayer, Integer>comparingByValue().reversed())
                .toList();
        int place = 1;
        for (Map.Entry<KatzePlayer, Integer> entry : sorted) {
            places.put(place++, entry.getKey());
        }
        return places;
    }

    public static int getPlace(KatzePlayer player) {
        List<KatzePlayer> sorted = new ArrayList<>(PLAYERS.stream().filter(KatzePlayer::isRanked).toList());
        sorted.sort(Comparator.comparingInt(KatzePlayer::getPoints).reversed());
        int place = 1;
        int previousPoints = -1;
        for (int i = 0; i < sorted.size(); i++) {
            KatzePlayer current = sorted.get(i);
            if (current.getPoints() != previousPoints) {
                place = i + 1;
                previousPoints = current.getPoints();
            }
            if (current.equals(player)) return place;
        }
        return -1;
    }

    public int getPoints() {
        return 0;
    }

    public void setPoints(int points) {
    }

    public int getChainProgress() {
        return 0;
    }

    public void updateObjective(boolean finishLast, boolean hasBeenSkipped) {
    }

    public void setCurrentObjective(ChallengeObjective newObjective, boolean finishLast, boolean hasBeenSkipped) {
    }

    public void addSkippedObjectiveToList(ChallengeObjective objective) {
    }

    @Override
    public CallerType callerType() {
        return CallerType.PLAYER;
    }

    @Override
    public String getName() {
        if (this.name == null) {
            if (getPlayer() == null) return "N/A";
            this.name = getPlayer().getName();
        }
        return this.name;
    }

    public Audience getAudience() {
        Player player = getPlayer();
        return player == null ? Audience.empty() : player;
    }

    public KatzeTeam getTeam() {
        for (KatzeTeam team : KatzeTeam.teams) {
            if (team.getPlayers().contains(this)) return team;
        }
        return null;
    }

    public boolean isInTeam() {
        return getTeam() != null;
    }

    public void setExcluded(boolean excluded) {
        PlayerExcludeEvent excludeEvent = new PlayerExcludeEvent(this, excluded);
        Bukkit.getPluginManager().callEvent(excludeEvent);
        if (excludeEvent.isCancelled()) {
            logger.info("PlayerExcludeEvent has been denied by an external plugin.");
            return;
        }
        this.excluded = excludeEvent.isNewExcluded();
    }

    public boolean isSpectator() {
        return SpectatorManager.isSpectator(uuid);
    }

    public boolean isParticipating() {
        return !isExcluded() && !isSpectator();
    }

    public boolean isRanked() {
        SpectatorReason reason = SpectatorManager.getReason(uuid);
        return reason == null || reason.isRanked();
    }

    public void leaveTeam() {
        if (isInTeam()) getTeam().removePlayer(this);
    }

    public void addToTeam(@NotNull KatzeTeam team) {
        leaveTeam();
        if (!isInTeam()) team.addPlayer(this);
    }

    public void sendMessage(Component message) {
        getAudience().sendMessage(message);
    }

    public void sendActionbar(Component message) {
        getAudience().sendActionBar(message);
    }

    public void playSound(Sound sound) {
        getAudience().playSound(sound);
    }

    public @Nullable Player getPlayer() {
        return Bukkit.getPlayer(getUuid());
    }

    @Override
    public boolean hasPermission(@NotNull PermissionDefault permission) {
        Player player = getPlayer();
        if (player == null) return false;
        return permission.getValue(player.isOp());
    }

    public void handleReset() {
        if (getPlayer() == null) return;
        GameLifecycleManager.sendToLobby(this);
    }

    public boolean isOffline() {
        if (getPlayer() == null) return true;
        return !getPlayer().isOnline();
    }

    public void openInventory(Inventory inventory) {
        if (getPlayer() == null) return;
        getPlayer().openInventory(inventory);
    }

    public void setScoreboard(Scoreboard scoreboard) {
        if (getPlayer() == null) return;
        getPlayer().setScoreboard(scoreboard);
    }

    public void teleport(Location location) {
        if (getPlayer() == null) return;
        getPlayer().teleport(location);
    }

    public Inventory getInventory() {
        if (getPlayer() == null) return null;
        return getPlayer().getInventory();
    }

    public World getWorld() {
        if (getPlayer() == null) return null;
        return getPlayer().getWorld();
    }

    public @Nullable Inventory getBackpack() {
        return null;
    }

    public @Nullable List<ItemStack> getBackpackContents() {
        Inventory backpack = getBackpack();
        return backpack == null ? null : copyContents(backpack, slot -> true);
    }

    protected static @NotNull List<ItemStack> copyContents(@NotNull Inventory inventory, @NotNull IntPredicate include) {
        List<ItemStack> contents = new ArrayList<>();
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            if (include.test(slot)) {
                ItemStack stack = inventory.getItem(slot);
                contents.add(stack == null || stack.isEmpty() ? null : stack.clone());
            }
        }
        return contents;
    }
}
