package gg.itzkatze.katze.lobby.cosmetics;

import gg.itzkatze.katze.KatzeGames;
import gg.itzkatze.katze.lobby.Lobby;
import gg.itzkatze.katze.lobby.LobbyFeature;
import gg.itzkatze.katze.player.KatzePlayer;
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Leaderboard implements LobbyFeature, Listener {

    private static final NamespacedKey TAG = new NamespacedKey("katze", "lobby_leaderboard");
    private static final double FLOOR_OFFSET = 1.0;
    private static final float SCALE = 0.75f;
    private static final float HITBOX_WIDTH = 2.6f;
    private static final float HITBOX_HEIGHT = 2.6f;
    private static final int ENTRIES = 10;
    private static final long REFRESH_TICKS = 20;
    private static final TextColor SILVER = TextColor.color(0xC0C0C0);
    private static final TextColor BRONZE = TextColor.color(0xCD7F32);

    private record Spot(World world, Location location) {
    }

    private static final class Board {
        final TextDisplay display;
        final Interaction hitbox;
        LeaderboardMetric metric = LeaderboardMetric.LIFETIME_POINTS;
        Component shown;

        Board(TextDisplay display, Interaction hitbox, Component shown) {
            this.display = display;
            this.hitbox = hitbox;
            this.shown = shown;
        }

        void show(Component text) {
            if (!text.equals(shown)) {
                display.text(text);
                shown = text;
            }
        }

        boolean isValid() {
            return display.isValid() && hitbox.isValid();
        }

        void remove() {
            display.remove();
            hitbox.remove();
        }
    }

    private final Vector floorCell;
    private final Map<UUID, Board> boards = new HashMap<>();
    private @Nullable Spot spot;
    private @Nullable BukkitTask task;

    public Leaderboard(@NotNull Vector floorCell) {
        this.floorCell = floorCell.clone();
    }

    @Override
    public void onCreate(@NotNull Lobby lobby) {
        World world = lobby.getWorld();
        if (world == null) return;

        Location location = lobby.toWorld(floorCell).add(0.5, FLOOR_OFFSET, 0.5);
        world.addPluginChunkTicket(location.getBlockX() >> 4, location.getBlockZ() >> 4, KatzeGames.get());
        removeLeftovers(world, lobby.getBounds());
        spot = new Spot(world, location);
        task = Bukkit.getScheduler().runTaskTimer(KatzeGames.get(), this::refresh, REFRESH_TICKS, REFRESH_TICKS);
    }

    @Override
    public void onPlayerJoin(@NotNull KatzePlayer player, @NotNull Lobby lobby) {
        Spot spot = this.spot;
        Player bukkitPlayer = player.getPlayer();
        if (spot == null || bukkitPlayer == null) return;

        Board existing = boards.get(bukkitPlayer.getUniqueId());
        if (existing != null) {
            if (existing.isValid()) return;
            existing.remove();
        }
        boards.put(bukkitPlayer.getUniqueId(), spawn(spot, bukkitPlayer));
    }

    @Override
    public void onDestroy(@NotNull Lobby lobby) {
        if (task != null) {
            task.cancel();
            task = null;
        }
        boards.values().forEach(Board::remove);
        boards.clear();

        Spot spot = this.spot;
        this.spot = null;
        if (spot != null) {
            spot.world().removePluginChunkTicket(spot.location().getBlockX() >> 4, spot.location().getBlockZ() >> 4, KatzeGames.get());
        }
    }

    @EventHandler
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        Board board = boards.remove(event.getPlayer().getUniqueId());
        if (board != null) {
            board.remove();
        }
    }

    @EventHandler
    public void onInteractEntity(@NotNull PlayerInteractEntityEvent event) {
        if (!isHitbox(event.getRightClicked())) return;

        event.setCancelled(true);
        if (event.getHand() == EquipmentSlot.HAND && isOwnHitbox(event.getPlayer(), event.getRightClicked())) {
            cycle(event.getPlayer());
        }
    }

    @EventHandler
    public void onAttackEntity(@NotNull PrePlayerAttackEntityEvent event) {
        if (!isHitbox(event.getAttacked())) return;

        event.setCancelled(true);
        if (isOwnHitbox(event.getPlayer(), event.getAttacked())) {
            cycle(event.getPlayer());
        }
    }

    static @NotNull Component render(@NotNull LeaderboardMetric metric, @NotNull List<Standing> standings) {
        TextComponent.Builder text = Component.text()
                .append(Component.text(metric.title(), NamedTextColor.AQUA, TextDecoration.BOLD, TextDecoration.UNDERLINED)).appendNewline()
                .appendNewline()
                ;
        if (standings.isEmpty()) {
            text.append(Component.text("No entries yet", NamedTextColor.GRAY)).appendNewline();
        }
        for (int i = 0; i < standings.size(); i++) {
            Standing standing = standings.get(i);
            text.append(Component.text((i + 1) + ". " + standing.name() + "  " + standing.value(), rankColor(i))).appendNewline();
        }
        return text.appendNewline().append(Component.text("Click to switch", NamedTextColor.GRAY)).build();
    }

    static @NotNull TextColor rankColor(int index) {
        return switch (index) {
            case 0 -> NamedTextColor.GOLD;
            case 1 -> SILVER;
            case 2 -> BRONZE;
            default -> NamedTextColor.DARK_GRAY;
        };
    }

    private static void removeLeftovers(@NotNull World world, @NotNull BoundingBox bounds) {
        world.getNearbyEntities(bounds, entity -> entity.getPersistentDataContainer().has(TAG)).forEach(Entity::remove);
    }

    private @NotNull Board spawn(@NotNull Spot spot, @NotNull Player owner) {
        Component text = render(LeaderboardMetric.LIFETIME_POINTS, CosmeticStore.get().top(LeaderboardMetric.LIFETIME_POINTS, ENTRIES));
        TextDisplay display = spot.world().spawn(spot.location(), TextDisplay.class, entity -> {
            entity.setVisibleByDefault(false);
            entity.setPersistent(false);
            entity.setBillboard(Display.Billboard.CENTER);
            entity.setTransformation(new Transformation(new Vector3f(), new AxisAngle4f(), new Vector3f(SCALE, SCALE, SCALE), new AxisAngle4f()));
            entity.text(text);
            entity.getPersistentDataContainer().set(TAG, PersistentDataType.STRING, "board");
        });
        Interaction hitbox = spot.world().spawn(spot.location(), Interaction.class, entity -> {
            entity.setVisibleByDefault(false);
            entity.setPersistent(false);
            entity.setInteractionWidth(HITBOX_WIDTH);
            entity.setInteractionHeight(HITBOX_HEIGHT);
            entity.getPersistentDataContainer().set(TAG, PersistentDataType.STRING, "board");
        });
        owner.showEntity(KatzeGames.get(), display);
        owner.showEntity(KatzeGames.get(), hitbox);
        return new Board(display, hitbox, text);
    }

    private boolean isHitbox(@NotNull Entity entity) {
        return boards.values().stream().anyMatch(board -> board.hitbox.equals(entity));
    }

    private boolean isOwnHitbox(@NotNull Player player, @NotNull Entity entity) {
        Board board = boards.get(player.getUniqueId());
        return board != null && board.hitbox.equals(entity);
    }

    private void cycle(@NotNull Player player) {
        Board board = boards.get(player.getUniqueId());
        board.metric = board.metric.next();
        board.show(render(board.metric, CosmeticStore.get().top(board.metric, ENTRIES)));
        player.playSound(ShopSounds.CLICK);
    }

    private void refresh() {
        CosmeticStore store = CosmeticStore.get();
        Map<LeaderboardMetric, Component> rendered = new EnumMap<>(LeaderboardMetric.class);
        for (Board board : boards.values()) {
            board.show(rendered.computeIfAbsent(board.metric, metric -> render(metric, store.top(metric, ENTRIES))));
        }
    }
}
