package gg.itzkatze.katze.itemrain;

import gg.itzkatze.katze.KatzeGames;
import gg.itzkatze.katze.game.ChallengeTimer;
import gg.itzkatze.katze.game.GameTimer;
import gg.itzkatze.katze.spectator.SpectatorManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ItemRainGameListener implements Listener {

    private static final List<Material> ITEM_MATERIALS = buildItemMaterials();

    private final Random random = new Random();
    private ChallengeTimer dropTimer;
    private int lastIntervalSeconds = -1;

    public void reset() {
        dropTimer = null;
        lastIntervalSeconds = -1;
    }

    public void runTask() {
        if (KatzeGames.getTimer().getMode() != GameTimer.Mode.DISABLED
                && !KatzeGames.getTimer().isRunning()) {
            if (dropTimer != null) {
                dropTimer.resetBaseline();
            }
            return;
        }

        int intervalSeconds = Math.max(1, ItemRainSettings.getIntervalSeconds());
        if (intervalSeconds != lastIntervalSeconds || dropTimer == null) {
            lastIntervalSeconds = intervalSeconds;
            dropTimer = new ChallengeTimer(intervalSeconds * 1000L);
        }

        dropTimer.tick();
        if (dropTimer.hasElapsed()) {
            dropTimer.consumeElapsed();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (SpectatorManager.isSpectator(player.getUniqueId())) continue;
                giveRandomItem(player);
            }
        }
    }

    private void giveRandomItem(Player player) {
        if (ITEM_MATERIALS.isEmpty()) return;
        Material material = ITEM_MATERIALS.get(random.nextInt(ITEM_MATERIALS.size()));
        ItemStack item = new ItemStack(material, 1);

        if (ItemRainSettings.isSkyDropEnabled()) {
            dropItemFromSky(player, item);
            return;
        }

        Map<Integer, ItemStack> leftovers = player.getInventory().addItem(item);
        for (ItemStack leftover : leftovers.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), leftover);
        }
    }

    private void dropItemFromSky(Player player, ItemStack item) {
        int radius = Math.max(0, ItemRainSettings.getSkyDropRadius());
        double offsetX = radius == 0 ? 0 : random.nextDouble(-radius, radius);
        double offsetZ = radius == 0 ? 0 : random.nextDouble(-radius, radius);

        Location baseLocation = player.getLocation().clone().add(offsetX, 0, offsetZ);
        int highestY = player.getWorld().getHighestBlockYAt(baseLocation.getBlockX(), baseLocation.getBlockZ());

        double spawnY = Math.min(
                player.getWorld().getMaxHeight() - 2,
                Math.max(player.getLocation().getY() + 15, highestY + 3)
        );

        Location dropLocation = new Location(
                player.getWorld(),
                baseLocation.getX(),
                spawnY,
                baseLocation.getZ()
        );

        while (spawnY > player.getLocation().getY() + 2 && dropLocation.getBlock().getType().isSolid()) {
            spawnY--;
            dropLocation.setY(spawnY);
        }

        player.getWorld().dropItem(dropLocation, item, droppedItem -> {
            droppedItem.setVelocity(new Vector(0, -0.2, 0));
            droppedItem.setCanMobPickup(false);
            droppedItem.setPickupDelay(10);
        });
    }

    private static List<Material> buildItemMaterials() {
        List<Material> materials = new ArrayList<>();
        for (Material material : Material.values()) {
            if (!material.isItem()) continue;
            if (material == Material.AIR) continue;
            materials.add(material);
        }
        return materials;
    }
}
