package gg.itzkatze.katze.lobby.jumpandrun;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

final class HopSteering {

    private static final double ALIGNMENT = 2.5;
    private static final double CLIMB = 1.0;
    private static final double CLIMB_SPAN = 6.0;
    private static final int MIN_TRIP = 16;
    private static final int ARRIVAL = 6;
    private static final int MAX_LEG_HOPS = 30;
    private static final int STALL_HOPS = 10;
    private static final double PROGRESS = 1.0;
    private static final int WALL_MARGIN = 3;
    private static final int HEIGHT_RANGE = 10;
    private static final int DESTINATION_ATTEMPTS = 20;
    private static final int COLUMN = 5;
    private static final double TOO_CLOSE = 1000;

    private HopSteering() {
    }

    // The milestone is the distance at the last block of progress; hops without a new block of progress count as stalled.
    record Leg(@NotNull Cell destination, int hops, double milestone, int stalled) {
    }

    private record Ranked(@NotNull Cell offset, double key) {
    }

    static void visit(@NotNull BitSet visited, @NotNull Cell cell, @NotNull CellBox playArea) {
        if (playArea.contains(cell)) {
            visited.set(column(cell, playArea));
        }
    }

    static @NotNull Leg advance(@Nullable Leg leg, @NotNull Cell from, @NotNull CellBox playArea, @NotNull BitSet visited, @NotNull Random random) {
        if (leg == null) return begin(from, playArea, visited, random);

        double distance = distance(from, leg.destination());
        if (distance <= ARRIVAL || leg.hops() >= MAX_LEG_HOPS) return begin(from, playArea, visited, random);

        boolean progress = distance <= leg.milestone() - PROGRESS;
        int stalled = progress ? 0 : leg.stalled() + 1;
        if (stalled >= STALL_HOPS) return begin(from, playArea, visited, random);

        return new Leg(leg.destination(), leg.hops() + 1, progress ? distance : leg.milestone(), stalled);
    }

    // Every offset of the tier in the order to try them: a weighted random order. A group keeps its weight; inside it the offsets lean toward the destination,
    // and the groups lean toward its height, so a destination changes which hop comes first but never which hops exist.
    static @NotNull List<Cell> order(@NotNull List<HopReach.Group> groups, @NotNull Cell from, @Nullable Cell destination, @NotNull Random random) {
        double climb = destination == null ? 0 : Math.clamp((destination.y() - from.y()) / CLIMB_SPAN, -1.0, 1.0);
        List<Ranked> ranked = new ArrayList<>();
        for (HopReach.Group group : groups) {
            List<Cell> offsets = group.offsets();
            double[] lean = new double[offsets.size()];
            double total = 0;
            for (int i = 0; i < lean.length; i++) {
                lean[i] = destination == null ? 1 : Math.exp(ALIGNMENT * cosine(from, destination, offsets.get(i)));
                total += lean[i];
            }
            for (int i = 0; i < lean.length; i++) {
                Cell offset = offsets.get(i);
                double weight = group.weight() * lean[i] / total * Math.exp(CLIMB * climb * offset.y());
                ranked.add(new Ranked(offset, -Math.log(1.0 - random.nextDouble()) / weight));
            }
        }
        ranked.sort(Comparator.comparingDouble(Ranked::key));
        return ranked.stream().map(Ranked::offset).toList();
    }

    // The new destination is the candidate farthest from every column the run has been over, among those at least MIN_TRIP away; none that far: the best of the near ones.
    private static @NotNull Leg begin(@NotNull Cell from, @NotNull CellBox playArea, @NotNull BitSet visited, @NotNull Random random) {
        Cell best = from;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (int attempt = 0; attempt < DESTINATION_ATTEMPTS; attempt++) {
            Cell candidate = new Cell(
                    between(playArea.minX() + WALL_MARGIN, playArea.maxX() - WALL_MARGIN, playArea.minX(), playArea.maxX(), random),
                    between(Math.max(playArea.minY(), from.y() - HEIGHT_RANGE), Math.min(playArea.maxY() - HopPlanner.HEADROOM, from.y() + HEIGHT_RANGE), playArea.minY(), playArea.maxY(), random),
                    between(playArea.minZ() + WALL_MARGIN, playArea.maxZ() - WALL_MARGIN, playArea.minZ(), playArea.maxZ(), random));
            double trip = distance(from, candidate);
            double score = distanceToVisited(candidate, playArea, visited) + trip / 1000 - (trip >= MIN_TRIP ? 0 : TOO_CLOSE);
            if (score > bestScore) {
                bestScore = score;
                best = candidate;
            }
        }
        return new Leg(best, 0, distance(from, best), 0);
    }

    private static double distanceToVisited(@NotNull Cell cell, @NotNull CellBox playArea, @NotNull BitSet visited) {
        int across = columnsAcross(playArea);
        double nearest = Double.MAX_VALUE;
        for (int column = visited.nextSetBit(0); column >= 0; column = visited.nextSetBit(column + 1)) {
            double centerX = playArea.minX() + (column / across) * COLUMN + COLUMN / 2.0;
            double centerZ = playArea.minZ() + (column % across) * COLUMN + COLUMN / 2.0;
            nearest = Math.min(nearest, Math.hypot(cell.x() + 0.5 - centerX, cell.z() + 0.5 - centerZ));
        }
        return nearest == Double.MAX_VALUE ? 0 : nearest;
    }

    private static int column(@NotNull Cell cell, @NotNull CellBox playArea) {
        return (cell.x() - playArea.minX()) / COLUMN * columnsAcross(playArea) + (cell.z() - playArea.minZ()) / COLUMN;
    }

    private static int columnsAcross(@NotNull CellBox playArea) {
        return (playArea.maxZ() - playArea.minZ()) / COLUMN + 1;
    }

    private static int between(int low, int high, int min, int max, @NotNull Random random) {
        return low <= high ? low + random.nextInt(high - low + 1) : (min + max) / 2;
    }

    private static double distance(@NotNull Cell a, @NotNull Cell b) {
        return Math.hypot(a.x() - b.x(), a.z() - b.z());
    }

    private static double cosine(@NotNull Cell from, @NotNull Cell destination, @NotNull Cell offset) {
        double toX = destination.x() - from.x();
        double toZ = destination.z() - from.z();
        double length = Math.hypot(toX, toZ);
        return length == 0 ? 0 : (offset.x() * toX + offset.z() * toZ) / (Math.hypot(offset.x(), offset.z()) * length);
    }
}
