package gg.itzkatze.mcc.level;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public final class LevelCurve {

    public static final int MAX_LEVEL = 100;
    private static final double TOTAL_XP = 500_000;
    private static final double STEEPNESS = 0.05;
    private static final double MIDPOINT = 105;
    private static final long[] TOTALS = totals();

    private LevelCurve() {
    }

    public static int costOf(int level) {
        if (level < 1 || level >= MAX_LEVEL) return 0;
        return (int) (TOTALS[level + 1] - TOTALS[level]);
    }

    public static long totalFor(int level) {
        return TOTALS[Math.clamp(level, 1, MAX_LEVEL)];
    }

    public static int levelFor(long xp) {
        int found = Arrays.binarySearch(TOTALS, 1, TOTALS.length, xp);
        return Math.max(1, found >= 0 ? found : -found - 2);
    }

    public static @NotNull LevelProgress progress(long xp) {
        int level = levelFor(xp);
        return new LevelProgress(level, (int) Math.min(Integer.MAX_VALUE, Math.max(xp, 0) - totalFor(level)), costOf(level));
    }

    private static long[] totals() {
        long start = Math.round(logistic(1));
        long[] totals = new long[MAX_LEVEL + 1];
        for (int level = 2; level <= MAX_LEVEL; level++) {
            totals[level] = Math.round(logistic(level)) - start;
        }
        return totals;
    }

    private static double logistic(int level) {
        return TOTAL_XP / (1 + Math.exp(-STEEPNESS * (level - MIDPOINT)));
    }
}
