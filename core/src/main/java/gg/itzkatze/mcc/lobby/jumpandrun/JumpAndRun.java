package gg.itzkatze.mcc.lobby.jumpandrun;

import gg.itzkatze.mcc.MinecraftChallenges;
import gg.itzkatze.mcc.lobby.Lobby;
import gg.itzkatze.mcc.lobby.LobbyFeature;
import gg.itzkatze.mcc.lobby.cosmetics.CosmeticStore;
import gg.itzkatze.mcc.lobby.cosmetics.MusicManager;
import gg.itzkatze.mcc.lobby.cosmetics.PointsSidebar;
import gg.itzkatze.mcc.lobby.cosmetics.PointsText;
import gg.itzkatze.mcc.lobby.cosmetics.SoundCosmetic;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class JumpAndRun implements LobbyFeature, Listener {

    private static final Logger logger = LoggerFactory.getLogger("MinecraftChallenges/" + JumpAndRun.class.getSimpleName());
    private static final Sound PLATE_CLICK = Sound.sound(Key.key("block.metal_pressure_plate.click_on"), Sound.Source.BLOCK, 1f, 1f);
    private static final Sound RUN_OVER = Sound.sound(Key.key("block.beacon.deactivate"), Sound.Source.PLAYER, 1f, 1f);
    private static final Component NO_START_ROOM = Component.text("There is no room for a start block right now.", NamedTextColor.RED);

    private enum Ending {
        FAILED,
        NO_ROOM,
        SILENT
    }

    private record Arena(Lobby lobby, World world, Cell plate, CellBox playArea, TextDisplay display) {
    }

    private final Vector platePosition;
    private final Map<UUID, JumpRun> runs = new LinkedHashMap<>();
    private final Reservations reservations = new Reservations();
    private final Map<UUID, RunColor> lastColors = new HashMap<>();
    private @Nullable Arena arena;
    private final RunMultiplier runMultiplier = new RunMultiplier();
    private final MusicManager musicManager;
    private final Plugin plugin;

    public JumpAndRun(@NotNull Vector platePosition, @NotNull Plugin plugin) {
        this.platePosition = platePosition.clone();
        this.plugin = plugin;
        this.musicManager = new MusicManager(MinecraftChallenges.get());
    }

    @Override
    public void onCreate(@NotNull Lobby lobby) {
        World world = lobby.getWorld();
        if (world == null) return;

        Cell plate = cellOf(lobby.toWorld(platePosition));
        Block plateBlock = world.getBlockAt(plate.x(), plate.y(), plate.z());
        if (world.getBlockAt(plate.x(), plate.y() - 1, plate.z()).getCollisionShape().getBoundingBoxes().isEmpty()) {
            logger.warn("The Jump & Run plate of island {} at {} has no solid block below it.", lobby.getIsland().getId(), plate);
        }
        plateBlock.setType(Material.LIGHT_WEIGHTED_PRESSURE_PLATE, false);
        world.addPluginChunkTicket(plate.x() >> 4, plate.z() >> 4, MinecraftChallenges.get());
        PlateDisplay.removeLeftovers(world, lobby.getBounds());
        arena = new Arena(lobby, world, plate, CellBox.of(lobby.getPlayArea()), PlateDisplay.spawn(world, plate));
    }

    @Override
    public void onDestroy(@NotNull Lobby lobby) {
        Arena arena = this.arena;
        if (arena == null) return;

        for (JumpRun run : List.copyOf(runs.values())) {
            end(arena, run, Ending.SILENT);
        }
        musicManager.stopAll();
        arena.display().remove();
        arena.world().removePluginChunkTicket(arena.plate().x() >> 4, arena.plate().z() >> 4, MinecraftChallenges.get());
        reservations.clear();
        lastColors.clear();
        this.arena = null;
    }

    public @NotNull OptionalInt score(@NotNull UUID player) {
        JumpRun run = runs.get(player);
        return run == null ? OptionalInt.empty() : OptionalInt.of(run.getScore());
    }

    // Lowest priority: the lobby's fall catch teleports a player on the barrier floor first otherwise, which would end the run without its message.
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onMove(@NotNull PlayerMoveEvent event) {
        Arena arena = this.arena;
        if (arena == null || !event.hasChangedPosition()) return;

        Player player = event.getPlayer();
        JumpRun run = runs.get(player.getUniqueId());
        if (!takesPart(arena, player)) {
            if (run != null) {
                end(arena, run, Ending.SILENT);
            }
            return;
        }

        if (run == null) {
            if (cellOf(event.getTo()).equals(arena.plate()) && !cellOf(event.getFrom()).equals(arena.plate())) {
                start(arena, player);
            }
            return;
        }
        checkSupport(arena, run, event.getTo());
    }

    @EventHandler
    public void onTeleport(@NotNull PlayerTeleportEvent event) {
        endSilently(event.getPlayer().getUniqueId());
    }

    // A failed run's music may still be fading, and its silent flag has to go before the game saves the player.
    @EventHandler
    public void onQuit(@NotNull PlayerQuitEvent event) {
        endSilently(event.getPlayer().getUniqueId());
        musicManager.stop(event.getPlayer());
    }

    private void endSilently(@NotNull UUID player) {
        Arena arena = this.arena;
        JumpRun run = runs.get(player);
        if (arena != null && run != null) {
            end(arena, run, Ending.SILENT);
        }
    }

    private boolean takesPart(@NotNull Arena arena, @NotNull Player player) {
        GameMode mode = player.getGameMode();
        return (mode == GameMode.ADVENTURE || mode == GameMode.SURVIVAL) && arena.world().equals(player.getWorld());
    }

    // The run is registered only after the teleport: its own teleport handler ends a registered run silently.
    private void start(@NotNull Arena arena, @NotNull Player player) {
        UUID id = player.getUniqueId();
        HopPlanner.Start start = HopPlanner.start(new WorldSpace(arena.world(), reservations, id), arena.playArea(), ThreadLocalRandom.current());
        if (start == null) {
            player.sendMessage(NO_START_ROOM);
            return;
        }

        RunLook look = lookFor(id);
        Cell block = start.block();
        Cell first = start.first().block();
        place(arena, block, look.block());
        place(arena, first, look.block());

        Location arrival = new Location(arena.world(), block.x() + 0.5, block.y() + 1.0, block.z() + 0.5);
        arrival.setDirection(new Vector(first.x() - block.x(), first.y() - block.y() - player.getEyeHeight(), first.z() - block.z()));
        if (!player.teleport(arrival)) {
            removeBlock(arena, block, look.block());
            removeBlock(arena, first, look.block());
            player.sendMessage(NO_START_ROOM);
            return;
        }

        JumpRun run = new JumpRun(player, look, block, first);
        HopSteering.visit(run.getVisited(), block, arena.playArea());
        runs.put(id, run);

        runMultiplier.reset(id);
        if (look.color() != null) {
            lastColors.put(id, look.color());
        }
        reservations.set(id, HopPlanner.reservation(block, start.first()));
        PointsSidebar.refresh(id);
        player.sendMessage(look.startMessage());
        player.playSound(PLATE_CLICK);
        musicManager.start(player);
    }

    private @NotNull RunLook lookFor(@NotNull UUID id) {
        return CosmeticStore.get().equippedBlock(id).map(RunLook::of).orElseGet(() -> {
            List<RunColor> inUse = runs.values().stream().map(run -> run.getLook().color()).filter(Objects::nonNull).toList();
            return RunColor.pick(inUse, lastColors.get(id)).look();
        });
    }

    private void checkSupport(@NotNull Arena arena, @NotNull JumpRun run, @NotNull Location to) {
        List<Cell> support = RunSupport.standingOn(to);
        for (Cell cell : support) {
            if (!cell.equals(run.getCurrent()) && !cell.equals(run.getNext())) {
                end(arena, run, Ending.FAILED);
                return;
            }
        }
        if (run.getNext() != null && support.contains(run.getNext())) {
            land(arena, run);
        }
    }

    private void land(@NotNull Arena arena, @NotNull JumpRun run) {
        HopDifficulty landed = run.getNextDifficulty();
        Cell previous = run.getCurrent();
        boolean onEvent = run.land();
        CosmeticStore store = CosmeticStore.get();
        Random random = ThreadLocalRandom.current();
        int points = runMultiplier.awardLanding(
                run.getPlayer(),
                run.getScore()
        );

        store.recordLanding(run.getPlayer(), run.getScore(), points);
        if (onEvent) {
            payEvent(arena, run, store, random);
        } else {
            run.getPlayer().playSound(store.equippedSound(run.getId()).map(SoundCosmetic::sound).orElse(SoundCosmetic.DEFAULT));
        }
        removeBlock(arena, previous, run.getLook().block());

        Cell standing = run.getCurrent();
        HopSteering.visit(run.getVisited(), standing, arena.playArea());
        HopSteering.Leg leg = HopSteering.advance(run.getLeg(), standing, arena.playArea(), run.getVisited(), random);
        run.setLeg(leg);
        HopPlanner.Hop hop = HopPlanner.next(standing, new WorldSpace(arena.world(), reservations, run.getId()), arena.playArea(), Set.of(), random,
                HopReach.roll(landed, random), leg.destination());
        if (hop == null) {
            end(arena, run, Ending.NO_ROOM);
            return;
        }

        boolean nextEvent = EventBlock.rolls(onEvent, random);
        place(arena, hop.block(), nextEvent ? EventBlock.MATERIAL : run.getLook().block());
        run.setNext(hop.block(), hop.difficulty(), nextEvent);
        reservations.set(run.getId(), HopPlanner.reservation(standing, hop));
    }

    // The landing point is already banked, so a loss never takes the balance below 0; the used Event Block turns into a normal block of the run.
    private void payEvent(@NotNull Arena arena, @NotNull JumpRun run, @NotNull CosmeticStore store, @NotNull Random random) {
        int applied = store.addEventPoints(run.getPlayer(), EventBlock.payout(random));
        run.recordEvent(applied);
        place(arena, run.getCurrent(), run.getLook().block());
        EventBlock.animatePayout(plugin, run.getPlayer(), applied);
    }

    private void end(@NotNull Arena arena, @NotNull JumpRun run, @NotNull Ending ending) {
        runs.remove(run.getId());
        reservations.release(run.getId());
        removeBlock(arena, run.getCurrent(), run.getLook().block());
        if (run.getNext() != null) {
            removeBlock(arena, run.getNext(), run.isNextEvent() ? EventBlock.MATERIAL : run.getLook().block());
        }
        PointsSidebar.refresh(run.getId());
        if (ending == Ending.SILENT) {
            musicManager.stop(run.getPlayer());
            return;
        }

        musicManager.fadeOut(run.getPlayer());
        String reason = ending == Ending.NO_ROOM ? "There is no room for another block. " : "";
        int score = run.getScore();
        Component message = Component.text("Jump & Run over! " + reason + "You reached ", NamedTextColor.RED)
                .append(Component.text(score, NamedTextColor.WHITE))
                .append(Component.text(score == 1 ? " block." : " blocks.", NamedTextColor.RED));
        if (score > 0) {
            message = message.append(Component.text(" +" + PointsText.amount(score), NamedTextColor.GREEN));
            if (run.getEventBlocks() > 0) {
                message = message.append(Component.text(", Event Blocks " + String.format("%+d", run.getEventPoints()), NamedTextColor.GOLD));
            }
            message = message.append(Component.text(" (" + CosmeticStore.get().points(run.getId()) + " total).", NamedTextColor.GRAY));
        }
        runMultiplier.reset(run.getId());
        run.getPlayer().sendMessage(message);
        run.getPlayer().playSound(RUN_OVER);
    }

    private void place(@NotNull Arena arena, @NotNull Cell cell, @NotNull Material material) {
        arena.world().getBlockAt(cell.x(), cell.y(), cell.z()).setType(material, false);
    }

    private void removeBlock(@NotNull Arena arena, @NotNull Cell cell, @NotNull Material material) {
        Block block = arena.world().getBlockAt(cell.x(), cell.y(), cell.z());
        if (block.getType() == material) {
            block.setType(Material.AIR, false);
        }
    }

    private static @NotNull Cell cellOf(@NotNull Location location) {
        return new Cell(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }
}
