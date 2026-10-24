package gg.itzkatze.katze.spectator;

import gg.itzkatze.katze.KatzeGames;
import gg.itzkatze.katze.challenges.ChallengeManager;
import gg.itzkatze.katze.util.KatzeColor;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SpectatorManager {

    // Keyed by UUID so wrapper upgrades keep it, concurrent because the async chat thread reads it.
    private static final Map<UUID, SpectatorReason> REASONS = new ConcurrentHashMap<>();
    private static final NamespacedKey REACH_KEY = new NamespacedKey("katzegames", "spectator_reach");
    private static final double REACH_BONUS = 7;
    private static final Component ELIMINATED = Component.text(
            "You are out, so you are spectating now. Right-click a player to see their inventory. Your chat only reaches other spectators.", KatzeColor.YELLOW);
    private static final Component STILL_SPECTATING = Component.text("You are still spectating. Type /spectate to play the next battle.", KatzeColor.YELLOW);

    private SpectatorManager() {
    }

    public static boolean isSpectator(@NotNull UUID uuid) {
        return REASONS.containsKey(uuid);
    }

    public static @Nullable SpectatorReason getReason(@NotNull UUID uuid) {
        return REASONS.get(uuid);
    }

    public static void choose(@NotNull UUID uuid) {
        REASONS.put(uuid, SpectatorReason.CHOSEN);
    }

    public static void markLateJoin(@NotNull UUID uuid) {
        REASONS.putIfAbsent(uuid, SpectatorReason.LATE_JOIN);
    }

    public static void eliminate(@NotNull UUID uuid) {
        if (REASONS.putIfAbsent(uuid, SpectatorReason.ELIMINATED) != null) return;

        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;
        player.sendMessage(ELIMINATED);
        if (!player.isDead()) {
            enterSpectatorMode(player);
        }
    }

    public static void remove(@NotNull UUID uuid) {
        REASONS.remove(uuid);
    }

    public static boolean isBattleRunning() {
        return ChallengeManager.isGameStarted() && !KatzeGames.getTimer().hasHitZero();
    }

    public static void restore(@NotNull UUID uuid, @Nullable String id) {
        SpectatorReason reason = SpectatorReason.fromId(id);
        if (reason != null) {
            restore(uuid, reason);
        }
    }

    public static void restore(@NotNull UUID uuid, @NotNull SpectatorReason reason) {
        REASONS.putIfAbsent(uuid, reason);
    }

    public static void onReset() {
        REASONS.values().removeIf(reason -> reason != SpectatorReason.CHOSEN);
        for (UUID uuid : REASONS.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.sendMessage(STILL_SPECTATING);
            }
        }
    }

    public static void enterSpectatorMode(@NotNull Player player) {
        player.setGameMode(GameMode.SPECTATOR);
        AttributeInstance reach = player.getAttribute(Attribute.ENTITY_INTERACTION_RANGE);
        // Transient, so a crash never saves it, and synced to the client, which picks the entities in reach.
        if (reach != null && reach.getModifier(REACH_KEY) == null) {
            reach.addTransientModifier(new AttributeModifier(REACH_KEY, REACH_BONUS, AttributeModifier.Operation.ADD_NUMBER));
        }
    }

    public static void leaveSpectatorMode(@NotNull Player player) {
        AttributeInstance reach = player.getAttribute(Attribute.ENTITY_INTERACTION_RANGE);
        if (reach != null) {
            reach.removeModifier(REACH_KEY);
        }
    }
}
