package gg.itzkatze.katze.lobby.jumpandrun;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

final class HopReach {

    private static final double HARD_CHANCE = 0.25;
    private static final double EXTREME_CHANCE = 0.18;
    private static final double MIN_DISTANCE = 2.5;
    private static final double MAX_FLAT = 3.7;
    private static final double MAX_UP = 3.1;
    private static final double MAX_DOWN = 4.4;
    private static final int SCAN_RADIUS = 5;

    private static final List<Group> NORMAL = List.of(
            new Group(0.5, scan(0, MAX_FLAT)),
            new Group(0.25, scan(1, MAX_UP)),
            new Group(0.25, scan(-1, MAX_DOWN)));

    // The longest hops a standing jump from a 1x1 block still lands, per a tick-by-tick jump simulation, as (|dx|, |dz|) with |dx| >= |dz|.
    // The weights keep the average height change at 0: a run that sinks ends on the barrier floor.
    private static final List<Group> HARD = List.of(
            new Group(0.5, mirrored(0, new int[][]{{4, 0}, {4, 1}, {3, 3}})),
            new Group(0.3, mirrored(1, new int[][]{{3, 1}, {3, 2}})),
            new Group(0.12, mirrored(-1, new int[][]{{4, 2}, {4, 3}})),
            new Group(0.05, mirrored(-2, new int[][]{{4, 2}, {4, 3}, {5, 1}})),
            new Group(0.03, mirrored(-3, new int[][]{{4, 2}, {4, 3}, {4, 4}, {5, 0}, {5, 1}, {5, 2}})));

    // Hops a standing jump can't make but a sprint run-up across the block can, as (|dx|, |dz|) with |dx| >= |dz|: the player may take off one move past the edge,
    // because vanilla keeps them grounded for a tick after they leave it. Per a tick-by-tick jump simulation each lands the hitbox at least 0.15 blocks over the target.
    // The weights keep the average height change at 0.
    private static final List<Group> EXTREME = List.of(
            new Group(0.5, mirrored(0, new int[][]{{5, 0}, {5, 1}, {4, 3}})),
            new Group(0.25, mirrored(1, new int[][]{{4, 0}, {4, 1}, {4, 2}, {3, 3}})),
            new Group(0.25, mirrored(-1, new int[][]{{5, 0}, {5, 1}, {5, 2}, {5, 3}, {4, 4}})));

    private HopReach() {
    }

    record Group(double weight, @NotNull List<Cell> offsets) {
    }

    static @NotNull List<Group> groups(@NotNull HopDifficulty difficulty) {
        return switch (difficulty) {
            case NORMAL -> NORMAL;
            case HARD -> HARD;
            case EXTREME -> EXTREME;
        };
    }

    static @NotNull HopDifficulty roll(@NotNull HopDifficulty previous, @NotNull Random random) {
        if (previous != HopDifficulty.NORMAL) return HopDifficulty.NORMAL;

        double roll = random.nextDouble();
        if (roll < EXTREME_CHANCE) return HopDifficulty.EXTREME;
        return roll < EXTREME_CHANCE + HARD_CHANCE ? HopDifficulty.HARD : HopDifficulty.NORMAL;
    }

    private static @NotNull List<Cell> scan(int dy, double maxDistance) {
        List<Cell> offsets = new ArrayList<>();
        for (int dx = -SCAN_RADIUS; dx <= SCAN_RADIUS; dx++) {
            for (int dz = -SCAN_RADIUS; dz <= SCAN_RADIUS; dz++) {
                double distance = Math.hypot(dx, dz);
                if (distance >= MIN_DISTANCE && distance <= maxDistance) {
                    offsets.add(new Cell(dx, dy, dz));
                }
            }
        }
        return List.copyOf(offsets);
    }

    private static @NotNull List<Cell> mirrored(int dy, int[][] shapes) {
        Set<Cell> offsets = new LinkedHashSet<>();
        for (int[] shape : shapes) {
            for (int signX = -1; signX <= 1; signX += 2) {
                for (int signZ = -1; signZ <= 1; signZ += 2) {
                    offsets.add(new Cell(signX * shape[0], dy, signZ * shape[1]));
                    offsets.add(new Cell(signX * shape[1], dy, signZ * shape[0]));
                }
            }
        }
        return List.copyOf(offsets);
    }
}
