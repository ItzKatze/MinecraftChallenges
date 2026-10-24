package gg.itzkatze.katze.lobby.jumpandrun;

import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

record CellBox(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {

    static @NotNull CellBox of(@NotNull BoundingBox cellAligned) {
        return new CellBox(
                (int) Math.floor(cellAligned.getMinX()), (int) Math.floor(cellAligned.getMinY()), (int) Math.floor(cellAligned.getMinZ()),
                (int) Math.ceil(cellAligned.getMaxX()) - 1, (int) Math.ceil(cellAligned.getMaxY()) - 1, (int) Math.ceil(cellAligned.getMaxZ()) - 1);
    }

    boolean contains(@NotNull Cell cell) {
        return cell.x() >= minX && cell.x() <= maxX
                && cell.y() >= minY && cell.y() <= maxY
                && cell.z() >= minZ && cell.z() <= maxZ;
    }
}
