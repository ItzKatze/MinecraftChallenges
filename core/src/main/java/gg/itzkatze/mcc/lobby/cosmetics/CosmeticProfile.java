package gg.itzkatze.mcc.lobby.cosmetics;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class CosmeticProfile {

    private int points;
    private int earned;
    private int best;
    private int casesOpened;
    private int duplicateStreak;
    private int caseCount = 1;
    private List<String> owned = new ArrayList<>();
    private List<String> equipped = new ArrayList<>();

    public CosmeticProfile() {
    }

    static @NotNull CosmeticProfile restored(int points, int earned, int best, @Nullable List<String> owned, @Nullable List<String> equipped) {
        CosmeticProfile profile = new CosmeticProfile();
        profile.points = points;
        profile.earned = earned;
        profile.best = best;
        profile.owned = owned == null ? null : new ArrayList<>(owned);
        profile.equipped = equipped == null ? null : new ArrayList<>(equipped);
        return profile;
    }

    int points() {
        return points;
    }

    int earned() {
        return earned;
    }

    int best() {
        return best;
    }

    int casesOpened() {
        return casesOpened;
    }

    int duplicateStreak() {
        return duplicateStreak;
    }

    int caseCount() {
        return caseCount;
    }

    void rememberCaseCount(int count) {
        caseCount = count;
    }

    @NotNull List<String> owned() {
        return Collections.unmodifiableList(owned);
    }

    @NotNull List<String> equipped() {
        return Collections.unmodifiableList(equipped);
    }

    void recordLanding(int runScore) {
        recordLanding(runScore, 1);}

    void recordLanding(int runScore, int pointsToGain) {
        gain(pointsToGain);
        best = Math.max(best, runScore);
    }

    boolean owns(@NotNull String id) {
        return owned.contains(id);
    }

    boolean spend(int amount) {
        if (amount < 0 || points < amount) return false;

        points -= amount;
        return true;
    }

    boolean openCases(int price, int count) {
        if (count < 1 || !spend(price * count)) return false;

        casesOpened = add(casesOpened, count);
        return true;
    }

    void addPoints(int amount) {
        gain(amount);
    }

    // A new cosmetic ends the run of duplicates; a duplicate pays its refund and extends it.
    boolean winCosmetic(@NotNull Cosmetic cosmetic, int refund) {
        if (owns(cosmetic.id())) {
            gain(refund);
            duplicateStreak = add(duplicateStreak, 1);
            return false;
        }
        owned.add(cosmetic.id());
        duplicateStreak = 0;
        return true;
    }

    int addEventPoints(int amount) {
        int before = points;
        if (amount >= 0) {
            gain(amount);
        } else {
            points = Math.max(0, points + amount);
        }
        return points - before;
    }

    boolean equip(@NotNull Cosmetic cosmetic, @NotNull CosmeticCatalog catalog) {
        if (!owns(cosmetic.id())) return false;

        unequip(cosmetic.category(), catalog);
        equipped.add(cosmetic.id());
        return true;
    }

    void unequip(@NotNull CosmeticCategory category, @NotNull CosmeticCatalog catalog) {
        equipped.removeIf(id -> catalog.find(id).map(Cosmetic::category).orElse(null) == category);
    }

    @NotNull Optional<Cosmetic> equipped(@NotNull CosmeticCategory category, @NotNull CosmeticCatalog catalog) {
        return equipped.reversed().stream()
                .map(catalog::find)
                .flatMap(Optional::stream)
                .filter(cosmetic -> cosmetic.category() == category)
                .findFirst();
    }

    void normalize(@NotNull CosmeticCatalog catalog) {
        points = Math.max(points, 0);
        earned = Math.max(earned, 0);
        best = Math.max(best, 0);
        casesOpened = Math.max(casesOpened, 0);
        duplicateStreak = Math.max(duplicateStreak, 0);
        caseCount = Math.max(caseCount, 1);
        owned = owned == null ? new ArrayList<>() : new ArrayList<>(owned.stream().filter(Objects::nonNull).collect(Collectors.toCollection(LinkedHashSet::new)));

        List<String> known = equipped == null ? List.of() : equipped.stream().filter(id -> id != null && catalog.find(id).isPresent()).toList();
        Set<CosmeticCategory> seen = EnumSet.noneOf(CosmeticCategory.class);
        List<String> kept = new ArrayList<>();
        for (String id : known.reversed()) {
            if (seen.add(catalog.find(id).orElseThrow().category())) {
                kept.add(id);
            }
        }
        Collections.reverse(kept);
        equipped = kept;
    }

    // Every gain also counts toward the lifetime points, and nothing else changes them, so they never go down.
    private void gain(int amount) {
        if (amount <= 0) return;

        points = add(points, amount);
        earned = add(earned, amount);
    }

    private static int add(int value, int amount) {
        return (int) Math.min(Integer.MAX_VALUE, (long) value + amount);
    }
}
