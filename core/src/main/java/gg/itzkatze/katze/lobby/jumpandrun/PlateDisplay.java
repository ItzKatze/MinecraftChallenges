package gg.itzkatze.katze.lobby.jumpandrun;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TextDisplay;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

final class PlateDisplay {

    private static final NamespacedKey TAG = new NamespacedKey("katze", "lobby_jump_and_run");
    private static final double HEIGHT = 1.6;
    private static final Component TEXT = Component.textOfChildren(
            Component.text("Jump & Run", NamedTextColor.GOLD, TextDecoration.BOLD),
            Component.newline(),
            Component.text("Step on the plate", NamedTextColor.GRAY));

    private PlateDisplay() {
    }

    static @NotNull TextDisplay spawn(@NotNull World world, @NotNull Cell plate) {
        Location location = new Location(world, plate.x() + 0.5, plate.y() + HEIGHT, plate.z() + 0.5);
        return world.spawn(location, TextDisplay.class, display -> {
            display.setPersistent(false);
            display.setBillboard(Display.Billboard.CENTER);
            display.text(TEXT);
            display.getPersistentDataContainer().set(TAG, PersistentDataType.STRING, "plate");
        });
    }

    static void removeLeftovers(@NotNull World world, @NotNull BoundingBox bounds) {
        world.getNearbyEntities(bounds, entity -> entity.getPersistentDataContainer().has(TAG)).forEach(Entity::remove);
    }
}
