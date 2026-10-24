package gg.itzkatze.katze.lobby.npc;

import gg.itzkatze.katze.KatzeGames;
import gg.itzkatze.katze.gui.KatzeGUI;
import gg.itzkatze.katze.lobby.Lobby;
import gg.itzkatze.katze.lobby.LobbyFeature;
import gg.itzkatze.katze.player.KatzePlayer;
import io.papermc.paper.entity.LookAnchor;
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mannequin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class NpcSpot implements LobbyFeature, Listener {

    private static final long TASK_PERIOD_TICKS = 2;
    private static final double FACE_RANGE = 8;
    private static final double PUSH_STRENGTH = 0.4;
    private static final double PUSH_HOP = 0.25;
    private static final double SPOT_HALF_WIDTH = 0.3;
    private static final double SPOT_HEIGHT = 1.8;

    private final LobbyNpc npc;
    private final Vector position;
    private final float yaw;
    private final Map<UUID, Mannequin> copies = new HashMap<>();
    private @Nullable Location location;
    private @Nullable BoundingBox spotBox;
    private @Nullable BukkitTask task;

    public NpcSpot(@NotNull LobbyNpc npc, @NotNull Vector position, float yaw) {
        this.npc = npc;
        this.position = position.clone();
        this.yaw = yaw;
    }

    @Override
    public void onCreate(@NotNull Lobby lobby) {
        World world = lobby.getWorld();
        if (world == null) return;

        Location location = standingLocation(lobby);
        this.location = location;
        this.spotBox = new BoundingBox(
                location.getX() - SPOT_HALF_WIDTH, location.getY(), location.getZ() - SPOT_HALF_WIDTH,
                location.getX() + SPOT_HALF_WIDTH, location.getY() + SPOT_HEIGHT, location.getZ() + SPOT_HALF_WIDTH);
        world.addPluginChunkTicket(location.getBlockX() >> 4, location.getBlockZ() >> 4, KatzeGames.get());
        NpcEntities.removeLeftovers(world, lobby.getBounds());
        task = Bukkit.getScheduler().runTaskTimer(KatzeGames.get(), this::tick, TASK_PERIOD_TICKS, TASK_PERIOD_TICKS);
    }

    @Override
    public void onPlayerJoin(@NotNull KatzePlayer player, @NotNull Lobby lobby) {
        Player bukkitPlayer = player.getPlayer();
        if (location == null || bukkitPlayer == null) return;

        Mannequin copy = copies.get(bukkitPlayer.getUniqueId());
        if (copy != null && copy.isValid()) return;
        copies.put(bukkitPlayer.getUniqueId(), NpcEntities.spawn(npc, location, bukkitPlayer));
    }

    @Override
    public void onDestroy(@NotNull Lobby lobby) {
        if (task != null) {
            task.cancel();
            task = null;
        }
        copies.values().forEach(Entity::remove);
        copies.clear();
        NpcDialogue.stopAll(npc.getId());

        Location location = this.location;
        this.location = null;
        this.spotBox = null;
        if (location != null) {
            location.getWorld().removePluginChunkTicket(location.getBlockX() >> 4, location.getBlockZ() >> 4, KatzeGames.get());
        }
    }

    @NotNull Location standingLocation(@NotNull Lobby lobby) {
        Location location = lobby.toWorld(position).add(0.5, 0, 0.5);
        if (location.getWorld() != null) {
            location.add(0, floorHeight(location.getBlock()), 0);
        }
        location.setYaw(lobby.toWorldYaw(yaw));
        location.setPitch(0);
        return location;
    }

    static @NotNull Vector pushVelocity(@NotNull Location spot, @NotNull Location player) {
        Vector away = new Vector(player.getX() - spot.getX(), 0, player.getZ() - spot.getZ());
        if (away.lengthSquared() < 1.0E-6) {
            away = spot.getDirection().setY(0);
        }
        return away.normalize().multiply(PUSH_STRENGTH).setY(PUSH_HOP);
    }

    private static double floorHeight(@NotNull Block block) {
        return block.getCollisionShape().getBoundingBoxes().stream().mapToDouble(BoundingBox::getMaxY).max().orElse(0);
    }

    private void tick() {
        Location location = this.location;
        BoundingBox spotBox = this.spotBox;
        if (location == null || spotBox == null) return;

        copies.forEach((owner, copy) -> face(copy, Bukkit.getPlayer(owner), location));
        for (Entity entity : location.getWorld().getNearbyEntities(spotBox, entity -> entity instanceof Player)) {
            Player player = (Player) entity;
            if (player.getGameMode() == GameMode.ADVENTURE || player.getGameMode() == GameMode.SURVIVAL) {
                player.setVelocity(pushVelocity(location, player.getLocation()));
            }
        }
    }

    private static void face(@NotNull Mannequin copy, @Nullable Player owner, @NotNull Location location) {
        if (!copy.isValid()) return;

        if (owner != null && owner.getWorld().equals(location.getWorld()) && owner.getLocation().distanceSquared(location) <= FACE_RANGE * FACE_RANGE) {
            Location eyes = owner.getEyeLocation();
            copy.lookAt(eyes.getX(), eyes.getY(), eyes.getZ(), LookAnchor.EYES);
        } else if (copy.getYaw() != location.getYaw() || copy.getPitch() != 0) {
            copy.setRotation(location.getYaw(), 0);
        }
    }

    @EventHandler
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        Mannequin copy = copies.remove(uuid);
        if (copy != null) {
            copy.remove();
        }
        NpcDialogue.stop(uuid);
    }

    @EventHandler
    public void onInteractEntity(@NotNull PlayerInteractEntityEvent event) {
        if (!copies.containsValue(event.getRightClicked())) return;

        event.setCancelled(true);
        if (event.getHand() == EquipmentSlot.HAND && isOwnCopy(event.getPlayer(), event.getRightClicked())) {
            click(event.getPlayer());
        }
    }

    @EventHandler
    public void onAttackEntity(@NotNull PrePlayerAttackEntityEvent event) {
        if (!copies.containsValue(event.getAttacked())) return;

        event.setCancelled(true);
        if (isOwnCopy(event.getPlayer(), event.getAttacked())) {
            click(event.getPlayer());
        }
    }

    private boolean isOwnCopy(@NotNull Player player, @NotNull Entity entity) {
        return entity.equals(copies.get(player.getUniqueId()));
    }

    private void click(@NotNull Player player) {
        UUID uuid = player.getUniqueId();
        if (NpcDialogue.isTalking(uuid, npc.getId())) return;

        List<Component> lines = npc.getDialogue(KatzePlayer.adapt(player));
        if (lines.isEmpty() || (npc.getDialogueMode() == DialogueMode.FIRST_TIME_ONLY && NpcDialogue.hasSeen(uuid, npc.getId()))) {
            openGui(uuid);
            return;
        }
        NpcDialogue.start(player, npc, lines, () -> openGui(uuid));
    }

    private void openGui(@NotNull UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;

        KatzePlayer katzePlayer = KatzePlayer.adapt(player);
        KatzeGUI gui = npc.createGui(katzePlayer);
        if (gui == null) return;
        NpcDialogue.stop(uuid);
        gui.open(katzePlayer);
    }
}
