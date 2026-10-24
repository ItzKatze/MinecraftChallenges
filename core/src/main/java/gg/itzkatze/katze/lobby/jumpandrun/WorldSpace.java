package gg.itzkatze.katze.lobby.jumpandrun;

import gg.itzkatze.katze.lobby.cosmetics.MusicManager;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

final class WorldSpace implements HopPlanner.Space {

    private static final Set<Material> AIR = EnumSet.of(Material.AIR, Material.CAVE_AIR, Material.VOID_AIR);
    private static final Set<Material> FREE = EnumSet.of(Material.AIR, Material.CAVE_AIR, Material.VOID_AIR, Material.LIGHT,
            Material.SHORT_GRASS, Material.TALL_GRASS, Material.FERN, Material.LARGE_FERN, Material.DEAD_BUSH, Material.HANGING_ROOTS, Material.BUSH,
            Material.FIREFLY_BUSH, Material.DANDELION, Material.POPPY, Material.BLUE_ORCHID, Material.ALLIUM, Material.AZURE_BLUET, Material.RED_TULIP,
            Material.ORANGE_TULIP, Material.WHITE_TULIP, Material.PINK_TULIP, Material.OXEYE_DAISY, Material.CORNFLOWER, Material.LILY_OF_THE_VALLEY,
            Material.TORCHFLOWER, Material.CLOSED_EYEBLOSSOM, Material.OPEN_EYEBLOSSOM, Material.SUNFLOWER, Material.LILAC, Material.ROSE_BUSH, Material.PEONY);

    private static final double FLAT_HEIGHT = 3.0 / 16.0;

    private final World world;
    private final Reservations reservations;
    private final UUID self;

    WorldSpace(@NotNull World world, @NotNull Reservations reservations, @NotNull UUID self) {
        this.world = world;
        this.reservations = reservations;
        this.self = self;
    }

    static boolean isFreeMaterial(@NotNull Material material) {
        return FREE.contains(material);
    }

    @Override
    public boolean canPlace(@NotNull Cell cell) {
        if (!isLoaded(cell)) return false;

        Block block = world.getBlockAt(cell.x(), cell.y(), cell.z());
        return AIR.contains(block.getType()) && world.getNearbyEntities(BoundingBox.of(block), entity -> !MusicManager.isEmitter(entity)).isEmpty();
    }

    @Override
    public boolean isFree(@NotNull Cell cell) {
        return isLoaded(cell) && FREE.contains(world.getBlockAt(cell.x(), cell.y(), cell.z()).getType());
    }

    @Override
    public boolean isLow(@NotNull Cell cell) {
        if (!isLoaded(cell)) return false;

        Collection<BoundingBox> boxes = world.getBlockAt(cell.x(), cell.y(), cell.z()).getCollisionShape().getBoundingBoxes();
        return !boxes.isEmpty() && boxes.stream().allMatch(box -> box.getMaxY() <= FLAT_HEIGHT + 1.0E-6);
    }

    @Override
    public boolean isReserved(@NotNull Cell cell) {
        return reservations.isReservedByOther(cell, self);
    }

    private boolean isLoaded(@NotNull Cell cell) {
        return world.isChunkLoaded(cell.x() >> 4, cell.z() >> 4);
    }
}
