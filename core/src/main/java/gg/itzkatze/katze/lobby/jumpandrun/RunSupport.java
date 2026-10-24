package gg.itzkatze.katze.lobby.jumpandrun;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

final class RunSupport {

    private static final double HALF_WIDTH = 0.3;
    private static final double TOLERANCE = 0.001;

    private RunSupport() {
    }

    static @NotNull List<Cell> standingOn(@NotNull Location feet) {
        World world = feet.getWorld();
        if (world == null) return List.of();

        double x = feet.getX();
        double y = feet.getY();
        double z = feet.getZ();
        int cellY = (int) Math.floor(y - TOLERANCE);
        List<Cell> cells = new ArrayList<>(4);
        for (int cellX = (int) Math.floor(x - HALF_WIDTH); cellX <= (int) Math.floor(x + HALF_WIDTH); cellX++) {
            for (int cellZ = (int) Math.floor(z - HALF_WIDTH); cellZ <= (int) Math.floor(z + HALF_WIDTH); cellZ++) {
                for (int level = cellY; level >= cellY - 1; level--) {
                    Collection<BoundingBox> boxes = world.getBlockAt(cellX, level, cellZ).getCollisionShape().getBoundingBoxes();
                    if (supports(boxes, cellX, level, cellZ, x, y, z)) {
                        cells.add(new Cell(cellX, level, cellZ));
                        break;
                    }
                }
            }
        }
        return cells;
    }

    private static boolean supports(@NotNull Collection<BoundingBox> boxes, int cellX, int cellY, int cellZ, double x, double y, double z) {
        for (BoundingBox box : boxes) {
            if (Math.abs(cellY + box.getMaxY() - y) <= TOLERANCE
                    && x + HALF_WIDTH > cellX + box.getMinX() && x - HALF_WIDTH < cellX + box.getMaxX()
                    && z + HALF_WIDTH > cellZ + box.getMinZ() && z - HALF_WIDTH < cellZ + box.getMaxZ()) {
                return true;
            }
        }
        return false;
    }
}
