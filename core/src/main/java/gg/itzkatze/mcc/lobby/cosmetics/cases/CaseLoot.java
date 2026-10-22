package gg.itzkatze.mcc.lobby.cosmetics.cases;

import gg.itzkatze.mcc.lobby.cosmetics.Cosmetic;
import gg.itzkatze.mcc.lobby.cosmetics.CosmeticCatalog;
import gg.itzkatze.mcc.lobby.cosmetics.Rarity;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;

public final class CaseLoot {

    public static final int PRICE = 100;
    public static final int PITY_STREAK = 15;

    // A row pays a fixed amount of points or, with points 0, a random available cosmetic of its rarity. Chances are in percent.
    private record Row(@NotNull Rarity rarity, double chance, int points) {
    }

    private static final List<Row> ROWS = List.of(
            new Row(Rarity.COMMON, 25, 25), new Row(Rarity.COMMON, 18, 50), new Row(Rarity.COMMON, 20, 0),
            new Row(Rarity.UNCOMMON, 10, 100), new Row(Rarity.UNCOMMON, 15, 0),
            new Row(Rarity.RARE, 6, 200), new Row(Rarity.RARE, 10, 0),
            new Row(Rarity.EPIC, 1.5, 500), new Row(Rarity.EPIC, 3.5, 0),
            new Row(Rarity.LEGENDARY, 0.4, 1000), new Row(Rarity.LEGENDARY, 1.5, 0),
            new Row(Rarity.MYTHIC, 0.4, 0));

    private CaseLoot() {
    }

    static @NotNull List<Cosmetic> available(@NotNull CosmeticCatalog catalog, @NotNull LocalDate today) {
        return catalog.all().stream().filter(cosmetic -> cosmetic.isAvailable(today)).toList();
    }

    static @NotNull CasePrize drawPrize(@NotNull List<? extends Cosmetic> available, @NotNull Random random) {
        return draw(available, random, false);
    }

    static @NotNull List<Cosmetic> missing(@NotNull List<? extends Cosmetic> available, @NotNull Predicate<? super Cosmetic> owned) {
        return available.stream().filter(cosmetic -> !owned.test(cosmetic)).map(Cosmetic.class::cast).toList();
    }

    static boolean pityDue(int duplicateStreak, @NotNull List<? extends Cosmetic> missing) {
        return duplicateStreak >= PITY_STREAK && !missing.isEmpty();
    }

    // Every missing cosmetic keeps its exact chance from a normal case, so this is the case rolled again and again until it lands on a missing one.
    static @NotNull CasePrize.Item drawPity(@NotNull List<? extends Cosmetic> available, @NotNull List<? extends Cosmetic> missing, @NotNull Random random) {
        Map<Rarity, Integer> sizes = new EnumMap<>(Rarity.class);
        for (Cosmetic cosmetic : available) {
            sizes.merge(cosmetic.rarity(), 1, Integer::sum);
        }
        double[] weights = new double[missing.size()];
        double total = 0;
        for (int i = 0; i < missing.size(); i++) {
            Rarity rarity = missing.get(i).rarity();
            weights[i] = cosmeticChance(rarity) / sizes.getOrDefault(rarity, 1);
            total += weights[i];
        }

        double roll = random.nextDouble() * total;
        for (int i = 0; i < missing.size(); i++) {
            roll -= weights[i];
            if (roll < 0) return new CasePrize.Item(missing.get(i));
        }
        return new CasePrize.Item(missing.getLast());
    }

    private static double cosmeticChance(@NotNull Rarity rarity) {
        return ROWS.stream().filter(row -> row.points() == 0 && row.rarity() == rarity).mapToDouble(Row::chance).sum();
    }

    // The reel shows rare prizes far more often than they drop, so most rolls pass a few of them.
    static @NotNull CasePrize drawShown(@NotNull List<? extends Cosmetic> available, @NotNull Random random) {
        return draw(available, random, true);
    }

    static double boost(@NotNull Rarity rarity) {
        return switch (rarity) {
            case COMMON -> 1;
            case UNCOMMON -> 1.5;
            case RARE -> 2.5;
            case EPIC -> 5;
            case LEGENDARY -> 10;
            case MYTHIC -> 25;
        };
    }

    static int refund(@NotNull Rarity rarity) {
        return switch (rarity) {
            case COMMON -> 10;
            case UNCOMMON -> 25;
            case RARE -> 75;
            case EPIC -> 125;
            case LEGENDARY -> 250;
            case MYTHIC -> 500;
        };
    }

    private static @NotNull CasePrize draw(@NotNull List<? extends Cosmetic> available, @NotNull Random random, boolean boosted) {
        Map<Rarity, List<Cosmetic>> pools = new EnumMap<>(Rarity.class);
        for (Cosmetic cosmetic : available) {
            pools.computeIfAbsent(cosmetic.rarity(), rarity -> new ArrayList<>()).add(cosmetic);
        }

        double[] weights = new double[ROWS.size()];
        double total = 0;
        for (int i = 0; i < ROWS.size(); i++) {
            Row row = ROWS.get(i);
            boolean empty = row.points() == 0 && !pools.containsKey(row.rarity());
            weights[i] = empty ? 0 : row.chance() * (boosted ? boost(row.rarity()) : 1);
            total += weights[i];
        }

        double roll = random.nextDouble() * total;
        int picked = -1;
        for (int i = 0; i < ROWS.size(); i++) {
            if (weights[i] <= 0) continue;
            picked = i;
            roll -= weights[i];
            if (roll < 0) break;
        }
        Row row = ROWS.get(picked);
        if (row.points() > 0) return new CasePrize.Points(row.points(), row.rarity());

        List<Cosmetic> pool = pools.get(row.rarity());
        return new CasePrize.Item(pool.get(random.nextInt(pool.size())));
    }
}
