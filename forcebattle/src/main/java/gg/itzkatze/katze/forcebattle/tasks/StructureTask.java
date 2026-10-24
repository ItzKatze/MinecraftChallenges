package gg.itzkatze.katze.forcebattle.tasks;

import gg.itzkatze.katze.KatzeGames;
import gg.itzkatze.katze.configuration.GlobalSetting;
import gg.itzkatze.katze.configuration.SettingsManager;
import gg.itzkatze.katze.configuration.states.PlayerTeamBothState;
import gg.itzkatze.katze.data.KatzeStructure;
import gg.itzkatze.katze.data.StructureSimplified;
import gg.itzkatze.katze.forcebattle.ForceBattleObjective;
import gg.itzkatze.katze.forcebattle.ForceBattlePlayer;
import gg.itzkatze.katze.forcebattle.ForceBattleType;
import gg.itzkatze.katze.player.KatzePlayer;
import gg.itzkatze.katze.util.KatzeColor;
import gg.itzkatze.katze.util.StringUtility;
import gg.itzkatze.katze.util.Toast;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.generator.structure.GeneratedStructure;
import org.bukkit.generator.structure.Structure;
import org.bukkit.generator.structure.StructurePiece;
import org.bukkit.util.BoundingBox;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class StructureTask implements ForceTask {

    private static final int MAX_CACHE_ENTRIES = 512;
    private static final int SCAN_RADIUS = 4;
    private static final int MAX_IN_FLIGHT_SCANS = 8;

    private static final Map<String, Set<BoundingBox>> STRUCTURE_CACHE =
            Collections.synchronizedMap(new LinkedHashMap<>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, Set<BoundingBox>> eldest) {
                    return size() > MAX_CACHE_ENTRIES;
                }
            });

    private static final Set<String> IN_FLIGHT_SCANS = new HashSet<>();
    private static long cacheGeneration;

    public static void clearCache() {
        synchronized (STRUCTURE_CACHE) {
            STRUCTURE_CACHE.clear();
            IN_FLIGHT_SCANS.clear();
            cacheGeneration++;
        }
    }

    @Override
    public void runTick() {
        if (ForceBattleType.FORCE_STRUCTURE.getSettingState() == PlayerTeamBothState.OFF) return;
        if (!KatzeGames.getTimer().isRunning()) return;

        Registry<Structure> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.STRUCTURE);

        for (KatzePlayer player : KatzePlayer.getOnlinePlayers()) {
            ForceBattlePlayer katzePlayer = (ForceBattlePlayer) player;
            if (!katzePlayer.isParticipating()) continue;

            checkPlayerObjective(katzePlayer, registry);
            checkTeamObjective(katzePlayer, registry);
        }
    }

    private void checkPlayerObjective(ForceBattlePlayer player, Registry<Structure> registry) {
        ForceBattleObjective objective = player.getObjective();

        if (objective == null) return;
        if (objective.getBattleType() != ForceBattleType.FORCE_STRUCTURE) return;

        if (isInsideStructure(player, objective.getObjectiveValue(), registry)) {
            completeObjective(player, objective.getObjectiveString());
            player.updateObjective(true, false);
        }
    }

    private void checkTeamObjective(ForceBattlePlayer player, Registry<Structure> registry) {
        if (!player.isInTeam()) return;
        ForceBattleObjective teamObjective = (ForceBattleObjective) player.getTeam().getObjective();

        if (teamObjective == null) return;
        if (teamObjective.getBattleType() != ForceBattleType.FORCE_STRUCTURE) return;

        if (isInsideStructure(player, teamObjective.getObjectiveValue(), registry)) {
            completeObjective(player, teamObjective.getObjectiveString());
            player.getTeam().updateObjective(player, true, false);
        }
    }

    private boolean isInsideStructure(ForceBattlePlayer player, Object objectiveValue, Registry<Structure> registry) {
        if (objectiveValue instanceof StructureSimplified simplified) {
            for (KatzeStructure structure : simplified.getStructures()) {
                Structure bukkitStructure = registry.get(structure.getKey());

                if (bukkitStructure != null && checkPlayerLocationInStructure(player, structure.getKey(), bukkitStructure)) {
                    return true;
                }
            }

            return false;
        }

        if (objectiveValue instanceof KatzeStructure structure) {
            Structure bukkitStructure = registry.get(structure.getKey());
            return bukkitStructure != null && checkPlayerLocationInStructure(player, structure.getKey(), bukkitStructure);
        }

        return false;
    }

    private boolean checkPlayerLocationInStructure(ForceBattlePlayer fbPlayer, NamespacedKey structureKey, Structure bukkitStructure) {
        Player player = fbPlayer.getPlayer();
        if (player == null) return false;

        Location playerLocation = player.getLocation();
        World world = player.getWorld();

        int chunkX = playerLocation.getBlockX() >> 4;
        int chunkZ = playerLocation.getBlockZ() >> 4;

        String cacheKey = createCacheKey(world, structureKey, chunkX, chunkZ);

        Set<BoundingBox> cachedBoxes;

        synchronized (STRUCTURE_CACHE) {
            Set<BoundingBox> boxes = STRUCTURE_CACHE.get(cacheKey);
            cachedBoxes = boxes == null ? null : Set.copyOf(boxes);
        }

        if (cachedBoxes != null) {
            for (BoundingBox box : cachedBoxes) {
                if (isInsideBoundingBox(playerLocation, box)) {
                    return true;
                }
            }

            return false;
        }

        scheduleScan(world, structureKey, bukkitStructure, chunkX, chunkZ, cacheKey);

        return false;
    }

    private void scheduleScan(World world, NamespacedKey structureKey, Structure bukkitStructure, int centerChunkX, int centerChunkZ, String cacheKey) {
        final long generation;
        final String scanKey;

        synchronized (STRUCTURE_CACHE) {
            generation = cacheGeneration;

            scanKey = cacheKey + ":" + generation;

            if (IN_FLIGHT_SCANS.size() >= MAX_IN_FLIGHT_SCANS) {
                return;
            }

            if (!IN_FLIGHT_SCANS.add(scanKey)) {
                return;
            }
        }

        Bukkit.getScheduler().runTask(KatzeGames.get(), () -> {
            try {
                World currentWorld = Bukkit.getWorld(world.getName());

                if (currentWorld == null) {
                    return;
                }

                Set<BoundingBox> boxes = scanStructure(
                        currentWorld,
                        bukkitStructure,
                        centerChunkX,
                        centerChunkZ
                );

                synchronized (STRUCTURE_CACHE) {
                    if (generation != cacheGeneration) {
                        return;
                    }

                    STRUCTURE_CACHE.put(cacheKey, boxes);
                }
            } catch (Exception e) {
                KatzeGames.get().getLogger().log(Level.WARNING, "Failed to scan structure " + structureKey + " around chunk " + centerChunkX + ", " + centerChunkZ, e);
            } finally {
                synchronized (STRUCTURE_CACHE) {
                    IN_FLIGHT_SCANS.remove(scanKey);
                }
            }
        });
    }

    private Set<BoundingBox> scanStructure(World world, Structure bukkitStructure, int centerChunkX, int centerChunkZ) {
        Set<BoundingBox> boxes = new HashSet<>();

        for (int dx = -SCAN_RADIUS; dx <= SCAN_RADIUS; dx++) {
            for (int dz = -SCAN_RADIUS; dz <= SCAN_RADIUS; dz++) {
                Chunk chunk = world.getChunkAt(
                        centerChunkX + dx,
                        centerChunkZ + dz
                );

                for (GeneratedStructure generated : chunk.getStructures(bukkitStructure)) {
                    for (StructurePiece piece : generated.getPieces()) {
                        BoundingBox box = piece.getBoundingBox();

                        boxes.add(box);
                    }
                }
            }
        }

        return boxes;
    }

    private String createCacheKey(World world, NamespacedKey structureKey, int chunkX, int chunkZ) {
        return world.getUID()
                + ":" + structureKey
                + ":" + chunkX
                + ":" + chunkZ;
    }

    private boolean isInsideBoundingBox(Location location, BoundingBox box) {
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();

        return x >= box.getMinX()
                && x <= box.getMaxX()
                && y >= box.getMinY()
                && y <= box.getMaxY()
                && z >= box.getMinZ()
                && z <= box.getMaxZ();
    }

    private void completeObjective(KatzePlayer player, String objective) {
        String formattedObjective = StringUtility.formatName(objective);
        Material toastIcon = Material.STRUCTURE_BLOCK;
        KatzeGames plugin = KatzeGames.get();

        if (player.isInTeam() && SettingsManager.isEnabled(GlobalSetting.EXTRA_TEAM_OBJECTIVE)) {
            player.getTeam().getPlayers().stream()
                    .filter(teammate -> teammate != player)
                    .filter(teammate -> !teammate.isOffline())
                    .forEach(teammate -> {
                        teammate.sendMessage(Component.text(player.getName() + " has finished " + formattedObjective + "."));
                        Toast.display(teammate.getPlayer(), toastIcon, Component.text(formattedObjective).color(KatzeColor.BLUE), Toast.Style.GOAL, plugin);
                    });
        }

        player.sendMessage(Component.text("You finished " + formattedObjective + "."));
        Toast.display(player.getPlayer(), toastIcon, Component.text(formattedObjective).color(KatzeColor.BLUE), Toast.Style.GOAL, plugin);
    }
}
