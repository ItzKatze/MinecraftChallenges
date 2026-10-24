package gg.itzkatze.katze.player;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;

public final class AfkTracker implements Listener {

    public static final long IDLE_MILLIS = 300_000;

    private final LongSupplier clock;
    private final Map<UUID, Long> lastInput = new ConcurrentHashMap<>();
    private final Set<UUID> afk = ConcurrentHashMap.newKeySet();

    public AfkTracker() {
        this(System::currentTimeMillis);
    }

    public AfkTracker(@NotNull LongSupplier clockMillis) {
        this.clock = clockMillis;
    }

    public boolean isAfk(@NotNull UUID player) {
        return afk.contains(player);
    }

    public void tick() {
        long now = clock.getAsLong();
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID id = player.getUniqueId();
            long idle = now - lastInput.computeIfAbsent(id, key -> now);
            if (idle >= IDLE_MILLIS) {
                if (afk.add(id)) {
                    player.sendMessage(Component.text("You are now AFK.", NamedTextColor.GRAY));
                }
            } else if (afk.remove(id)) {
                player.sendMessage(Component.text("You are no longer AFK.", NamedTextColor.GRAY));
            }
        }
    }

    // Cancelled moves count too: the pause sets a player back, but trying to move is still input.
    @EventHandler(priority = EventPriority.MONITOR)
    public void onMove(@NotNull PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        if (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ() || from.getYaw() != to.getYaw() || from.getPitch() != to.getPitch()) {
            input(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChat(@NotNull AsyncChatEvent event) {
        input(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCommand(@NotNull PlayerCommandPreprocessEvent event) {
        input(event.getPlayer());
    }

    // Standing on a pressure plate or tripwire fires PHYSICAL interactions over and over without any input.
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInteract(@NotNull PlayerInteractEvent event) {
        if (event.getAction() != Action.PHYSICAL) {
            input(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClick(@NotNull InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            input(player);
        }
    }

    @EventHandler
    public void onJoin(@NotNull PlayerJoinEvent event) {
        input(event.getPlayer());
    }

    @EventHandler
    public void onQuit(@NotNull PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        lastInput.remove(id);
        afk.remove(id);
    }

    private void input(@NotNull Player player) {
        lastInput.put(player.getUniqueId(), clock.getAsLong());
    }
}
