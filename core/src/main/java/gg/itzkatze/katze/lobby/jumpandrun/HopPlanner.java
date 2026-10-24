package gg.itzkatze.katze.lobby.jumpandrun;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

final class HopPlanner {

    private static final double CORRIDOR_RADIUS = 1.0;
    private static final double WIDE_CORRIDOR_RADIUS = 1.5;
    static final int HEADROOM = 4;
    private static final int START_ATTEMPTS = 200;
    private static final int START_CLEARANCE = 3;

    private HopPlanner() {
    }

    interface Space {

        boolean canPlace(@NotNull Cell cell);

        boolean isFree(@NotNull Cell cell);

        // A flat block, at most 3/16 high, like a carpet or a closed trapdoor: a jump lifts the player clear of it before moving sideways.
        boolean isLow(@NotNull Cell cell);

        boolean isReserved(@NotNull Cell cell);
    }

    record Hop(@NotNull Cell block, @NotNull Set<Cell> reserved, @NotNull HopDifficulty difficulty) {
    }

    record Start(@NotNull Cell block, @NotNull Hop first) {
    }

    // A hard or extreme request falls back to a normal hop when none of its kind fits.
    static @Nullable Hop next(@NotNull Cell from, @NotNull Space space, @NotNull CellBox playArea, @NotNull Set<Cell> exempt, @NotNull Random random,
                              @NotNull HopDifficulty difficulty, @Nullable Cell destination) {
        if (difficulty != HopDifficulty.NORMAL) {
            Hop special = pick(from, space, playArea, exempt, random, difficulty, destination);
            if (special != null) return special;
        }
        return pick(from, space, playArea, exempt, random, HopDifficulty.NORMAL, destination);
    }

    // A start block floats in the upper half of the play area, in open sky above the island, with a first hop to go on with.
    static @Nullable Start start(@NotNull Space space, @NotNull CellBox playArea, @NotNull Random random) {
        int lowest = playArea.minY() + (playArea.maxY() - playArea.minY()) / 2;
        for (int attempt = 0; attempt < START_ATTEMPTS; attempt++) {
            Cell block = new Cell(between(playArea.minX(), playArea.maxX(), random), between(lowest, playArea.maxY(), random), between(playArea.minZ(), playArea.maxZ(), random));
            if (space.isReserved(block) || !hasLandingZone(block, space, playArea) || !isOverIsland(block, space, playArea) || !space.canPlace(block)) continue;

            Hop first = pick(block, space, playArea, Set.of(), random, HopDifficulty.NORMAL, null);
            if (first != null) return new Start(block, first);
        }
        return null;
    }

    static @NotNull Set<Cell> reservation(@NotNull Cell standing, @NotNull Hop next) {
        Set<Cell> reserved = new HashSet<>(landingZone(standing));
        reserved.add(standing);
        reserved.addAll(next.reserved());
        return reserved;
    }

    // Hard and extreme hops leave a jump only a narrow band of takeoffs, and those can swing wider than a normal corridor reaches.
    static @NotNull Set<Cell> corridor(@NotNull Cell a, @NotNull Cell b, @NotNull HopDifficulty difficulty) {
        double radius = difficulty == HopDifficulty.NORMAL ? CORRIDOR_RADIUS : WIDE_CORRIDOR_RADIUS;
        int reach = (int) Math.ceil(radius);
        int lowest = Math.min(a.y(), b.y()) + 1;
        int highest = Math.max(a.y(), b.y()) + HEADROOM;
        Set<Cell> cells = new HashSet<>();
        for (int x = Math.min(a.x(), b.x()) - reach; x <= Math.max(a.x(), b.x()) + reach; x++) {
            for (int z = Math.min(a.z(), b.z()) - reach; z <= Math.max(a.z(), b.z()) + reach; z++) {
                if (distanceToSegment(x, z, a, b) > radius + 1.0E-9) continue;

                for (int y = lowest; y <= highest; y++) {
                    cells.add(new Cell(x, y, z));
                }
            }
        }
        cells.remove(a);
        cells.remove(b);
        return cells;
    }

    static @NotNull Set<Cell> landingZone(@NotNull Cell block) {
        Set<Cell> cells = new HashSet<>();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int y = block.y(); y <= block.y() + HEADROOM; y++) {
                    cells.add(new Cell(block.x() + dx, y, block.z() + dz));
                }
            }
        }
        cells.remove(block);
        return cells;
    }

    private static @Nullable Hop pick(@NotNull Cell from, @NotNull Space space, @NotNull CellBox playArea, @NotNull Set<Cell> exempt, @NotNull Random random,
                                      @NotNull HopDifficulty difficulty, @Nullable Cell destination) {
        Hop fallback = null;
        for (Cell offset : HopSteering.order(HopReach.groups(difficulty), from, destination, random)) {
            Hop hop = tryHop(from, from.add(offset.x(), offset.y(), offset.z()), space, playArea, exempt, difficulty);
            if (hop == null) continue;
            if (hasWayOn(hop.block(), from, exempt, space, playArea)) return hop;
            if (fallback == null) {
                fallback = hop;
            }
        }
        return fallback;
    }

    // A block the run can't leave again ends it: prefer a hop whose block has a normal hop of its own, once the block we leave is gone.
    private static boolean hasWayOn(@NotNull Cell block, @NotNull Cell behind, @NotNull Set<Cell> exempt, @NotNull Space space, @NotNull CellBox playArea) {
        Set<Cell> gone = new HashSet<>(exempt);
        gone.add(behind);
        for (HopReach.Group group : HopReach.groups(HopDifficulty.NORMAL)) {
            for (Cell offset : group.offsets()) {
                if (tryHop(block, block.add(offset.x(), offset.y(), offset.z()), space, playArea, gone, HopDifficulty.NORMAL) != null) return true;
            }
        }
        return false;
    }

    private static boolean hasLandingZone(@NotNull Cell block, @NotNull Space space, @NotNull CellBox playArea) {
        for (Cell cell : landingZone(block)) {
            if (!playArea.contains(cell) || !space.isFree(cell)) return false;
        }
        return true;
    }

    private static boolean isOverIsland(@NotNull Cell block, @NotNull Space space, @NotNull CellBox playArea) {
        for (int y = block.y() + HEADROOM + 1; y <= playArea.maxY(); y++) {
            if (!space.isFree(new Cell(block.x(), y, block.z()))) return false;
        }
        for (int y = block.y() - 1; y >= playArea.minY(); y--) {
            if (!space.isFree(new Cell(block.x(), y, block.z()))) return block.y() - y >= START_CLEARANCE;
        }
        return false;
    }

    private static int between(int min, int max, @NotNull Random random) {
        return min + random.nextInt(max - min + 1);
    }

    private static @Nullable Hop tryHop(@NotNull Cell from, @NotNull Cell block, @NotNull Space space, @NotNull CellBox playArea, @NotNull Set<Cell> exempt,
                                        @NotNull HopDifficulty difficulty) {
        if (!playArea.contains(block) || space.isReserved(block) || !space.canPlace(block)) return null;

        Set<Cell> corridor = corridor(from, block, difficulty);
        Set<Cell> zone = landingZone(block);
        for (Cell cell : corridor) {
            if (!playArea.contains(cell)) return null;
        }
        for (Cell cell : zone) {
            if (!playArea.contains(cell)) return null;
        }

        for (Cell cell : zone) {
            if (!exempt.contains(cell) && !space.isFree(cell)) return null;
        }
        int lowest = Math.min(from.y(), block.y()) + 1;
        for (Cell cell : corridor) {
            if (zone.contains(cell) || exempt.contains(cell) || space.isFree(cell)) continue;
            if (cell.y() == lowest && space.isLow(cell)) continue;

            return null;
        }

        Set<Cell> reserved = new HashSet<>(corridor);
        reserved.addAll(zone);
        reserved.add(block);
        return new Hop(block, reserved, difficulty);
    }

    private static double distanceToSegment(int x, int z, @NotNull Cell a, @NotNull Cell b) {
        double dx = b.x() - a.x();
        double dz = b.z() - a.z();
        double lengthSquared = dx * dx + dz * dz;
        double t = lengthSquared == 0 ? 0 : Math.clamp(((x - a.x()) * dx + (z - a.z()) * dz) / lengthSquared, 0, 1);
        return Math.hypot(x - (a.x() + t * dx), z - (a.z() + t * dz));
    }
}
