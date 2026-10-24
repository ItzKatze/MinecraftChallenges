package gg.itzkatze.katze.lobby;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Getter
public final class Lobby {

    private final LobbyIsland island;
    private final @Nullable World world;
    private final Vector origin;
    private final BoundingBox bounds;
    private final BoundingBox playArea;
    private final Location spawn;
    private final List<LobbyFeature> features;

    public Lobby(@NotNull LobbyIsland island, @Nullable World world, @NotNull Vector structureSize, int baseY) {
        this(island, world, structureSize, baseY, null);
    }

    public Lobby(@NotNull LobbyIsland island, @Nullable World world, @NotNull Vector structureSize, int baseY, @Nullable BoundingBox barrierBounds) {
        this.island = island;
        this.world = world;

        Vector rotatedSpawn = rotate(island.getSpawnPosition(), island.getRotation());
        this.origin = new Vector(-rotatedSpawn.getBlockX(), baseY, -rotatedSpawn.getBlockZ());

        Vector lastBlock = new Vector(structureSize.getBlockX() - 1, structureSize.getBlockY() - 1, structureSize.getBlockZ() - 1);
        Vector rotatedLastBlock = rotate(lastBlock, island.getRotation());
        Vector min = Vector.getMinimum(new Vector(), rotatedLastBlock).add(origin);
        Vector max = Vector.getMaximum(new Vector(), rotatedLastBlock).add(origin).add(new Vector(1, 1, 1));
        this.bounds = BoundingBox.of(min, max);

        BoundingBox shell = barrierBounds != null ? barrierBounds : BoundingBox.of(new Vector(), structureSize);
        Vector firstInner = rotate(shell.getMin().add(new Vector(1, 1, 1)), island.getRotation()).add(origin);
        Vector lastInner = rotate(shell.getMax().subtract(new Vector(2, 2, 2)), island.getRotation()).add(origin);
        this.playArea = BoundingBox.of(Vector.getMinimum(firstInner, lastInner), Vector.getMaximum(firstInner, lastInner).add(new Vector(1, 1, 1)));

        this.spawn = new Location(world, 0.5, baseY + island.getSpawnPosition().getBlockY(), 0.5, island.getSpawnYaw(), 0);
        this.features = List.copyOf(island.createFeatures());
    }

    public @NotNull Location toWorld(@NotNull Vector structureBlock) {
        Vector block = new Vector(structureBlock.getBlockX(), structureBlock.getBlockY(), structureBlock.getBlockZ());
        Vector world = rotate(block, island.getRotation()).add(origin);
        return new Location(this.world, world.getX(), world.getY(), world.getZ());
    }

    public @NotNull Vector toStructure(@NotNull Location worldLocation) {
        Vector offset = new Vector(worldLocation.getBlockX(), worldLocation.getBlockY(), worldLocation.getBlockZ()).subtract(origin);
        return rotate(offset, inverse(island.getRotation()));
    }

    public float toWorldYaw(float structureYaw) {
        return Location.normalizeYaw(structureYaw + degrees(island.getRotation()));
    }

    public float toStructureYaw(float worldYaw) {
        return Location.normalizeYaw(worldYaw - degrees(island.getRotation()));
    }

    private static float degrees(@NotNull StructureRotation rotation) {
        return switch (rotation) {
            case NONE -> 0;
            case CLOCKWISE_90 -> 90;
            case CLOCKWISE_180 -> 180;
            case COUNTERCLOCKWISE_90 -> -90;
        };
    }

    private static @NotNull StructureRotation inverse(@NotNull StructureRotation rotation) {
        return switch (rotation) {
            case CLOCKWISE_90 -> StructureRotation.COUNTERCLOCKWISE_90;
            case COUNTERCLOCKWISE_90 -> StructureRotation.CLOCKWISE_90;
            case NONE, CLOCKWISE_180 -> rotation;
        };
    }

    public static @NotNull Vector rotate(@NotNull Vector structurePosition, @NotNull StructureRotation rotation) {
        double x = structurePosition.getX();
        double y = structurePosition.getY();
        double z = structurePosition.getZ();
        return switch (rotation) {
            case NONE -> new Vector(x, y, z);
            case CLOCKWISE_90 -> new Vector(-z, y, x);
            case CLOCKWISE_180 -> new Vector(-x, y, -z);
            case COUNTERCLOCKWISE_90 -> new Vector(z, y, -x);
        };
    }
}
