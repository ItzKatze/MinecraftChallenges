package gg.itzkatze.katze.lobby;

import gg.itzkatze.katze.KatzeGames;
import gg.itzkatze.katze.player.KatzePlayer;
import gg.itzkatze.katze.util.StructureUtil;
import gg.itzkatze.katze.util.WorldStorage;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.GameRules;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.structure.Mirror;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.structure.Structure;
import org.bukkit.util.BlockVector;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class LobbyManager implements Listener {

    private static final Logger logger = LoggerFactory.getLogger("KatzeGames/" + LobbyManager.class.getSimpleName());
    private static final String ISLAND_PACKAGE = "gg.itzkatze.katze.lobby.islands";
    private static final String WORLD_NAME = "world";
    private static final String RECORD_FILE = "lobby.json";
    private static final int BASE_Y = 190;

    private static final List<LobbyIsland> islands = new ArrayList<>();
    private static @Nullable Lobby activeLobby;
    private static @Nullable String lastIslandId;

    public static void loadIslands() {
        islands.clear();
        KatzeGames.loopThroughPackage(ISLAND_PACKAGE, LobbyIsland.class)
                .sorted(Comparator.comparing(LobbyIsland::getId))
                .forEach(islands::add);
        if (islands.isEmpty()) {
            logger.error("No lobby islands found in {}, players will stay where they are.", ISLAND_PACKAGE);
        }
    }

    public static @NotNull List<LobbyIsland> getIslands() {
        return List.copyOf(islands);
    }

    public static boolean isActive() {
        return activeLobby != null;
    }

    public static @Nullable Lobby getActiveLobby() {
        return activeLobby;
    }

    public static @Nullable Location getSpawn() {
        return activeLobby == null ? null : activeLobby.getSpawn().clone();
    }

    public static void createLobbyIfNeeded() {
        if (activeLobby != null) return;
        World world = Bukkit.getWorld(WORLD_NAME);
        if (world == null) return;

        LobbyRecord record = WorldStorage.load(RECORD_FILE, LobbyRecord.class);
        if (record != null) {
            LobbyIsland recorded = findIsland(record.island);
            if (recorded != null && tryCreate(recorded, world)) return;
            clear(record);
            WorldStorage.delete(RECORD_FILE);
        }

        for (LobbyIsland island : selectionOrder(islands, lastIslandId, ThreadLocalRandom.current())) {
            if (tryCreate(island, world)) return;
        }
        logger.error("No lobby island could be pasted.");
    }

    public static void destroyLobby() {
        Lobby lobby = activeLobby;
        activeLobby = null;
        if (lobby != null) {
            for (LobbyFeature feature : lobby.getFeatures()) {
                if (feature instanceof Listener listener) {
                    HandlerList.unregisterAll(listener);
                }
                feature.onDestroy(lobby);
            }
            if (lobby.getWorld() != null) {
                clear(lobby.getWorld(), lobby.getBounds());
            }
        } else {
            LobbyRecord record = WorldStorage.load(RECORD_FILE, LobbyRecord.class);
            if (record != null) {
                clear(record);
            }
        }
        WorldStorage.delete(RECORD_FILE);

        World world = Bukkit.getWorld(WORLD_NAME);
        if (world != null) {
            world.setGameRule(GameRules.RANDOM_TICK_SPEED, 20);
            world.setGameRule(GameRules.ADVANCE_TIME, true);
            world.setGameRule(GameRules.ADVANCE_WEATHER, true);
            world.setGameRule(GameRules.SPECTATORS_GENERATE_CHUNKS, false);
        }
    }

    public static @Nullable LobbyIsland pickOtherIsland(@NotNull Random random) {
        Lobby lobby = activeLobby;
        if (lobby == null) return null;

        String currentId = lobby.getIsland().getId();
        List<LobbyIsland> order = selectionOrder(islands, currentId, random);
        return order.isEmpty() || order.getFirst().getId().equals(currentId) ? null : order.getFirst();
    }

    public static boolean switchIsland(@NotNull LobbyIsland island) {
        Lobby previous = activeLobby;
        World world = Bukkit.getWorld(WORLD_NAME);
        if (previous == null || world == null) return false;
        if (previous.getIsland().getId().equals(island.getId())) return true;

        Structure structure = StructureUtil.loadStructure(island.getStructureName());
        if (structure == null) return false;

        destroyLobby();
        Lobby lobby = create(island, structure, world);
        for (KatzePlayer player : KatzePlayer.getOnlinePlayers()) {
            player.teleport(lobby.getSpawn());
            onPlayerJoin(player);
        }
        return true;
    }

    public static void onPlayerJoin(@NotNull KatzePlayer player) {
        Lobby lobby = activeLobby;
        if (lobby == null) return;
        lobby.getFeatures().forEach(feature -> feature.onPlayerJoin(player, lobby));
    }

    static @NotNull List<LobbyIsland> selectionOrder(@NotNull List<LobbyIsland> candidates, @Nullable String lastId, @NotNull Random random) {
        List<LobbyIsland> order = new ArrayList<>();
        LobbyIsland previous = null;
        for (LobbyIsland island : candidates) {
            if (candidates.size() > 1 && island.getId().equals(lastId)) {
                previous = island;
            } else {
                order.add(island);
            }
        }
        Collections.shuffle(order, random);
        if (previous != null) {
            order.add(previous);
        }
        return order;
    }

    static boolean isFallingOut(@NotNull BoundingBox bounds, double x, double y, double z) {
        return x >= bounds.getMinX() && x < bounds.getMaxX()
                && z >= bounds.getMinZ() && z < bounds.getMaxZ()
                && y <= bounds.getMinY() + 1;
    }

    private static @Nullable LobbyIsland findIsland(@Nullable String id) {
        return islands.stream().filter(island -> island.getId().equals(id)).findFirst().orElse(null);
    }

    private static boolean tryCreate(@NotNull LobbyIsland island, @NotNull World world) {
        Structure structure = StructureUtil.loadStructure(island.getStructureName());
        if (structure == null) return false;

        create(island, structure, world);
        return true;
    }

    private static @NotNull Lobby create(@NotNull LobbyIsland island, @NotNull Structure structure, @NotNull World world) {
        Lobby lobby = new Lobby(island, world, structure.getSize(), BASE_Y, StructureUtil.barrierBounds(island.getStructureName(), structure));
        emptyContainers(world, lobby.getBounds());
        removeEntities(world, lobby.getBounds());
        // Blocks with block-entity data are pasted last, so anything resting on them (carpets) would break first. The paste replaces each stand-in.
        for (BlockVector position : StructureUtil.blockEntityPositions(island.getStructureName(), structure)) {
            lobby.toWorld(position).getBlock().setType(Material.STONE, false);
        }
        structure.place(lobby.toWorld(new Vector()), true, island.getRotation(), Mirror.NONE, 0, 1.0f, new Random());

        world.setTime(6000);
        world.setStorm(false);
        world.setGameRule(GameRules.RANDOM_TICK_SPEED, 0);
        world.setGameRule(GameRules.ADVANCE_TIME, false);
        world.setGameRule(GameRules.ADVANCE_WEATHER, false);

        WorldStorage.save(RECORD_FILE, LobbyRecord.of(lobby, world));
        activeLobby = lobby;
        lastIslandId = island.getId();

        for (LobbyFeature feature : lobby.getFeatures()) {
            feature.onCreate(lobby);
            if (feature instanceof Listener listener) {
                Bukkit.getPluginManager().registerEvents(listener, KatzeGames.get());
            }
        }
        return lobby;
    }

    private static void clear(@NotNull LobbyRecord record) {
        World world = record.world == null ? null : Bukkit.getWorld(record.world);
        if (world == null || record.min == null || record.max == null || record.min.length != 3 || record.max.length != 3) return;
        clear(world, BoundingBox.of(new Vector(record.min[0], record.min[1], record.min[2]), new Vector(record.max[0], record.max[1], record.max[2])));
    }

    private static void forEachChunk(@NotNull World world, @NotNull BoundingBox bounds, @NotNull Consumer<Chunk> action) {
        int lastX = (int) bounds.getMaxX() - 1;
        int lastZ = (int) bounds.getMaxZ() - 1;
        for (int chunkX = (int) bounds.getMinX() >> 4; chunkX <= lastX >> 4; chunkX++) {
            for (int chunkZ = (int) bounds.getMinZ() >> 4; chunkZ <= lastZ >> 4; chunkZ++) {
                action.accept(world.getChunkAt(chunkX, chunkZ));
            }
        }
    }

    // The game drops the items of a container whenever its block is replaced, so shelves left by an earlier paste are emptied before anything is set over them.
    private static void emptyContainers(@NotNull World world, @NotNull BoundingBox bounds) {
        Predicate<Block> inside = block -> bounds.contains(block.getX(), block.getY(), block.getZ());
        forEachChunk(world, bounds, chunk -> {
            for (BlockState tileEntity : chunk.getTileEntities(inside, false)) {
                if (tileEntity instanceof BlockInventoryHolder holder) {
                    holder.getInventory().clear();
                }
            }
        });
    }

    // The entities a structure brings along (armor stands, cushions) outlive its blocks, and a restart pastes another set over the old one.
    private static void removeEntities(@NotNull World world, @NotNull BoundingBox bounds) {
        forEachChunk(world, bounds, chunk -> {
            for (Entity entity : chunk.getEntities()) {
                if (!(entity instanceof Player) && bounds.contains(entity.getLocation().toVector())) {
                    entity.remove();
                }
            }
        });
    }

    private static void clear(@NotNull World world, @NotNull BoundingBox bounds) {
        emptyContainers(world, bounds);
        for (int x = (int) bounds.getMinX(); x < (int) bounds.getMaxX(); x++) {
            for (int y = (int) bounds.getMinY(); y < (int) bounds.getMaxY(); y++) {
                for (int z = (int) bounds.getMinZ(); z < (int) bounds.getMaxZ(); z++) {
                    world.getBlockAt(x, y, z).setType(Material.AIR, false);
                }
            }
        }
        removeEntities(world, bounds);
    }

    private static boolean isInLobby(@NotNull Block block) {
        Lobby lobby = activeLobby;
        return lobby != null && block.getWorld().equals(lobby.getWorld())
                && lobby.getBounds().contains(block.getX() + 0.5, block.getY() + 0.5, block.getZ() + 0.5);
    }

    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent event) {
        if (isInLobby(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock != null && isInLobby(clickedBlock)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Lobby lobby = activeLobby;
        if (lobby == null) return;

        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.ADVENTURE && player.getGameMode() != GameMode.SURVIVAL) return;

        Location to = event.getTo();
        if (to.getWorld() != null && to.getWorld().equals(lobby.getWorld()) && isFallingOut(lobby.getBounds(), to.getX(), to.getY(), to.getZ())) {
            player.teleport(lobby.getSpawn());
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (activeLobby != null && event.getEntity().getType() == EntityType.PLAYER) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onHungerChange(FoodLevelChangeEvent event) {
        if (activeLobby != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        if (activeLobby != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityAtInteract(PlayerInteractAtEntityEvent event) {
        if (activeLobby != null) {
            event.setCancelled(true);
        }
    }

    private static final class LobbyRecord {
        private String island;
        private String world;
        private int[] min;
        private int[] max;

        private static @NotNull LobbyRecord of(@NotNull Lobby lobby, @NotNull World world) {
            LobbyRecord record = new LobbyRecord();
            record.island = lobby.getIsland().getId();
            record.world = world.getName();
            BoundingBox bounds = lobby.getBounds();
            record.min = new int[]{(int) bounds.getMinX(), (int) bounds.getMinY(), (int) bounds.getMinZ()};
            record.max = new int[]{(int) bounds.getMaxX(), (int) bounds.getMaxY(), (int) bounds.getMaxZ()};
            return record;
        }
    }
}
