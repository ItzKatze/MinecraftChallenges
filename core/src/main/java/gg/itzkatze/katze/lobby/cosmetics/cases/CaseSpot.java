package gg.itzkatze.katze.lobby.cosmetics.cases;

import gg.itzkatze.katze.KatzeGames;
import gg.itzkatze.katze.lobby.Lobby;
import gg.itzkatze.katze.lobby.LobbyFeature;
import gg.itzkatze.katze.lobby.cosmetics.PointsText;
import gg.itzkatze.katze.player.KatzePlayer;
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CaseSpot implements LobbyFeature, Listener {

    private static final Logger logger = LoggerFactory.getLogger("KatzeGames/" + CaseSpot.class.getSimpleName());
    private static final double LABEL_HEIGHT = 1.1;
    private static final float HITBOX_SIZE = 1.0f;
    private static final BlockFace[] FACINGS = {BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH, BlockFace.EAST};
    private static final Component LABEL = Component.textOfChildren(
            Component.text("Cosmetic Case", NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD),
            Component.newline(),
            Component.text(PointsText.amount(CaseLoot.PRICE), NamedTextColor.GOLD),
            Component.newline(),
            Component.text("CLICK", NamedTextColor.YELLOW, TextDecoration.BOLD));

    private record Placed(World world, Block chest, TextDisplay label, Interaction hitbox) {
    }

    private final Vector cell;
    private final float yaw;
    private @Nullable Placed placed;

    public CaseSpot(@NotNull Vector cell, float yaw) {
        this.cell = cell.clone();
        this.yaw = yaw;
    }

    static @NotNull BlockFace facing(float worldYaw) {
        return FACINGS[Math.floorMod(Math.round(worldYaw / 90f), FACINGS.length)];
    }

    @Override
    public void onCreate(@NotNull Lobby lobby) {
        World world = lobby.getWorld();
        if (world == null) return;

        Location location = lobby.toWorld(cell);
        Block chest = world.getBlockAt(location);
        if (world.getBlockAt(chest.getX(), chest.getY() - 1, chest.getZ()).getCollisionShape().getBoundingBoxes().isEmpty()) {
            logger.warn("The Cosmetic Case of island {} at {} has no solid block below it.", lobby.getIsland().getId(), location.toVector());
        }
        chest.setType(Material.ENDER_CHEST, false);
        if (chest.getBlockData() instanceof Directional directional) {
            directional.setFacing(facing(lobby.toWorldYaw(yaw)));
            chest.setBlockData(directional, false);
        }
        world.addPluginChunkTicket(chest.getX() >> 4, chest.getZ() >> 4, KatzeGames.get());

        Location center = location.clone().add(0.5, 0, 0.5);
        TextDisplay label = world.spawn(center.clone().add(0, LABEL_HEIGHT, 0), TextDisplay.class, display -> {
            display.setPersistent(false);
            display.setBillboard(Display.Billboard.CENTER);
            display.text(LABEL);
        });
        Interaction hitbox = world.spawn(center, Interaction.class, interaction -> {
            interaction.setPersistent(false);
            interaction.setInteractionWidth(HITBOX_SIZE);
            interaction.setInteractionHeight(HITBOX_SIZE);
        });
        placed = new Placed(world, chest, label, hitbox);
    }

    @Override
    public void onDestroy(@NotNull Lobby lobby) {
        Placed placed = this.placed;
        this.placed = null;
        if (placed == null) return;

        placed.label().remove();
        placed.hitbox().remove();
        placed.world().removePluginChunkTicket(placed.chest().getX() >> 4, placed.chest().getZ() >> 4, KatzeGames.get());
    }

    @EventHandler
    public void onInteractEntity(@NotNull PlayerInteractEntityEvent event) {
        if (!isHitbox(event.getRightClicked())) return;

        event.setCancelled(true);
        if (event.getHand() == EquipmentSlot.HAND) {
            CaseConfirmGui.offer(KatzePlayer.adapt(event.getPlayer()));
        }
    }

    @EventHandler
    public void onAttackEntity(@NotNull PrePlayerAttackEntityEvent event) {
        if (!isHitbox(event.getAttacked())) return;

        event.setCancelled(true);
        CaseConfirmGui.offer(KatzePlayer.adapt(event.getPlayer()));
    }

    private boolean isHitbox(@NotNull Entity entity) {
        Placed placed = this.placed;
        return placed != null && placed.hitbox().equals(entity);
    }

    public @Nullable Location getChestLocation() {
        Placed placed = this.placed;
        return placed == null ? null : placed.chest().getLocation();
    }
}
