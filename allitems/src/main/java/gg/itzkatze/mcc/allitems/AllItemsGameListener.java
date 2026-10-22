package gg.itzkatze.mcc.allitems;

import gg.itzkatze.mcc.MinecraftChallenges;
import gg.itzkatze.mcc.allitems.data.AllItemsData;
import gg.itzkatze.mcc.data.BiomeSimplified;
import gg.itzkatze.mcc.data.MccAdvancement;
import gg.itzkatze.mcc.data.MccStructure;
import gg.itzkatze.mcc.data.StructureSimplified;
import gg.itzkatze.mcc.level.LevelStore;
import gg.itzkatze.mcc.player.MccPlayer;
import gg.itzkatze.mcc.util.StringUtility;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BoundingBox;
import org.bukkit.generator.structure.GeneratedStructure;
import org.bukkit.generator.structure.Structure;
import org.bukkit.generator.structure.StructurePiece;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class AllItemsGameListener implements Listener {

    private final AllItemsData data;
    private final Map<NamespacedKey, MccAdvancement> advancementByKey = new ConcurrentHashMap<>();
    private BukkitTask structureTask;

    private static final Map<String, Set<BoundingBox>> STRUCTURE_CACHE = new ConcurrentHashMap<>();

    public AllItemsGameListener(AllItemsData data) {
        this.data = data;
        for (MccAdvancement advancement : MccAdvancement.values()) {
            advancementByKey.put(advancement.getKey(), advancement);
        }
    }

    public void initialize() {
        startStructureTask();
    }

    public void cleanup() {
        if (structureTask != null) {
            structureTask.cancel();
            structureTask = null;
        }
        STRUCTURE_CACHE.clear();
    }

    @EventHandler
    public void onPlayerPickupItem(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (!MinecraftChallenges.getTimer().isRunning()) {
            return;
        }
        if (!AllItemsSettings.isItemsEnabled()) {
            return;
        }
        if (!MccPlayer.adapt(player).isParticipating()) {
            return;
        }

        Material material = event.getItem().getItemStack().getType();
        if (!material.isItem() || material.name().contains("LEGACY")) {
            return;
        }

        if (data.recordItemFound(player, material)) {
            discover(player, AllItemsXp.ITEM, "item", material.name());
        }
    }

    @EventHandler
    public void onInventoryPickupItem(InventoryPickupItemEvent event) {
        if (!MinecraftChallenges.getTimer().isRunning()) {
            return;
        }
        if (!AllItemsSettings.isItemsEnabled()) {
            return;
        }

        ItemStack itemStack = event.getItem().getItemStack();
        Material material = itemStack.getType();

        if (!material.isItem() || material.name().contains("LEGACY")) {
            return;
        }

        if (data.isItemFound(material)) {
            return;
        }

        MinecraftChallenges.get().getLogger().info(
                String.format("Item automatically collected: %s", material.name())
        );
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (!MinecraftChallenges.getTimer().isRunning()) {
            return;
        }
        if (!AllItemsSettings.isItemsEnabled()) {
            return;
        }
        if (!MccPlayer.adapt(player).isParticipating()) {
            return;
        }

        ItemStack itemStack = event.getCurrentItem();
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return;
        }

        Material material = itemStack.getType();
        if (!material.isItem() || material.name().contains("LEGACY")) {
            return;
        }

        if (data.recordItemFound(player, material)) {
            discover(player, AllItemsXp.ITEM, "item", material.name());
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!MinecraftChallenges.getTimer().isRunning()) {
            return;
        }
        if (!AllItemsSettings.isBiomesEnabled()) {
            return;
        }
        Player player = event.getPlayer();
        if (!MccPlayer.adapt(player).isParticipating()) {
            return;
        }
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        Biome biome = player.getWorld().getBiome(player.getLocation());
        BiomeSimplified simplified = getSimplifiedBiome(biome);
        if (simplified == null) {
            return;
        }
        if (data.recordBiomeFound(player, simplified)) {
            discover(player, AllItemsXp.BIOME, "biome", simplified.getName());
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!MinecraftChallenges.getTimer().isRunning()) {
            return;
        }
        if (!AllItemsSettings.isMobsEnabled()) {
            return;
        }
        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();
        if (killer == null) {
            return;
        }
        if (!MccPlayer.adapt(killer).isParticipating()) {
            return;
        }

        EntityType type = entity.getType();
        if (!type.isAlive() || !type.isSpawnable() || type == EntityType.PLAYER) {
            return;
        }

        if (data.recordMobFound(killer, type)) {
            discover(killer, AllItemsXp.MOB, "mob", type.name());
        }
    }

    @EventHandler
    public void onAdvancement(PlayerAdvancementDoneEvent event) {
        if (!MinecraftChallenges.getTimer().isRunning()) {
            return;
        }
        if (!AllItemsSettings.isAdvancementsEnabled()) {
            return;
        }
        Player player = event.getPlayer();
        if (!MccPlayer.adapt(player).isParticipating()) {
            return;
        }

        MccAdvancement advancement = advancementByKey.get(event.getAdvancement().getKey());
        if (advancement == null) {
            return;
        }

        if (data.recordAdvancementFound(player, advancement)) {
            discover(player, AllItemsXp.ADVANCEMENT, "advancement", advancement.getName());
        }
    }

    private BiomeSimplified getSimplifiedBiome(Biome biome) {
        for (BiomeSimplified simplified : BiomeSimplified.values()) {
            if (simplified.getBiomes().contains(biome)) {
                return simplified;
            }
        }
        return null;
    }

    private void startStructureTask() {
        structureTask = Bukkit.getScheduler().runTaskTimer(MinecraftChallenges.get(), () -> {
            if (!MinecraftChallenges.getTimer().isRunning()) {
                return;
            }
            if (!AllItemsSettings.isStructuresEnabled()) {
                return;
            }

            Registry<Structure> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.STRUCTURE);
            for (MccPlayer mccPlayer : MccPlayer.getOnlinePlayers()) {
                if (!mccPlayer.isParticipating()) continue;
                Player player = mccPlayer.getPlayer();
                if (player == null) continue;

                checkStructuresForPlayer(player, registry);
            }
        }, 40L, 40L);
    }

    private void checkStructuresForPlayer(Player player, Registry<Structure> registry) {
        for (StructureSimplified simplified : StructureSimplified.values()) {
            if (data.hasPlayerFoundStructure(player.getUniqueId(), simplified)) {
                continue;
            }
            for (MccStructure structure : simplified.getStructures()) {
                Structure bukkitStruct = registry.get(structure.getKey());
                if (bukkitStruct != null && checkPlayerLocationInStructure(player, structure.getKey(), bukkitStruct)) {
                    if (data.recordStructureFound(player, simplified)) {
                        discover(player, AllItemsXp.STRUCTURE, "structure", simplified.getName());
                    }
                    return;
                }
            }
        }
    }

    private boolean checkPlayerLocationInStructure(Player player, NamespacedKey structureKey, Structure bukkitStruct) {
        World world = player.getWorld();
        String cacheKey = world.getName() + ":" + structureKey;
        Set<BoundingBox> cachedBoxes = STRUCTURE_CACHE.get(cacheKey);
        if (cachedBoxes != null) {
            for (BoundingBox box : cachedBoxes) {
                if (isInsideBoundingBox(player.getLocation(), box)) {
                    return true;
                }
            }
        }

        Bukkit.getScheduler().runTaskAsynchronously(MinecraftChallenges.get(), () -> scanStructure(world, player, bukkitStruct, cacheKey));
        return false;
    }

    private void scanStructure(World world, Player player, Structure bukkitStruct, String cacheKey) {
        try {
            int radius = 4;
            Set<BoundingBox> newBoxes = new HashSet<>();
            int centerX = player.getLocation().getChunk().getX();
            int centerZ = player.getLocation().getChunk().getZ();

            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    for (GeneratedStructure generated : world.getChunkAt(centerX + dx, centerZ + dz).getStructures(bukkitStruct)) {
                        for (StructurePiece piece : generated.getPieces()) {
                            newBoxes.add(piece.getBoundingBox());
                        }
                    }
                }
            }

            if (!newBoxes.isEmpty()) {
                STRUCTURE_CACHE.compute(cacheKey, (key, oldSet) -> {
                    if (oldSet == null) return newBoxes;
                    oldSet.addAll(newBoxes);
                    return oldSet;
                });
            }
        } catch (Exception ignored) {
        }
    }

    private boolean isInsideBoundingBox(org.bukkit.Location loc, BoundingBox box) {
        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();
        return x >= box.getMinX() && x <= box.getMaxX()
                && y >= box.getMinY() && y <= box.getMaxY()
                && z >= box.getMinZ() && z <= box.getMaxZ();
    }

    private void discover(Player player, AllItemsXp reward, String type, String name) {
        LevelStore.get().grant(player, reward.xp());
        announceDiscovery(player, type, name);
    }

    private void announceDiscovery(Player player, String type, String name) {
        String formatted = StringUtility.formatName(name);
        if (AllItemsSettings.isTeamMode()) {
            MinecraftChallenges.broadcast(Component.text(player.getName() + " discovered " + formatted + " (" + type + ") for the team!"));
        } else {
            MinecraftChallenges.broadcast(Component.text(player.getName() + " discovered " + formatted + " (" + type + ")!"));
        }
        MccPlayer.adapt(player).sendMessage(Component.text("You discovered " + formatted + "!").color(NamedTextColor.GREEN));
    }
}
