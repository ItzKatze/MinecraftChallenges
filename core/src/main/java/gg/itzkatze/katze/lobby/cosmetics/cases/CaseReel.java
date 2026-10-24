package gg.itzkatze.katze.lobby.cosmetics.cases;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

final class CaseReel {

    static final int STEPS = 40;
    static final int WIDTH = 9;
    static final int ROW_STAGGER = 30;

    private final List<CasePrize> strip;
    private final int extraSteps;

    private CaseReel(@NotNull List<CasePrize> strip, int extraSteps) {
        this.strip = strip;
        this.extraSteps = extraSteps;
    }

    static @NotNull CaseReel build(@NotNull CasePrize prize, @NotNull Supplier<CasePrize> filler, int row) {
        int extraSteps = row * ROW_STAGGER;
        int length = STEPS + extraSteps + WIDTH;
        int prizeIndex = STEPS + extraSteps + WIDTH / 2;
        List<CasePrize> strip = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            strip.add(i == prizeIndex ? prize : filler.get());
        }
        return new CaseReel(List.copyOf(strip), extraSteps);
    }

    int steps() {
        return STEPS + extraSteps;
    }

    @NotNull List<CasePrize> window(int position) {
        return strip.subList(position, position + WIDTH);
    }

    @NotNull CasePrize prize() {
        return strip.get(steps() + WIDTH / 2);
    }

    int delay(int step) {
        return step < extraSteps ? 1 : curve(step - extraSteps);
    }

    // The reel slows down dramatically near the end, creating suspense before the final stop.
    private static int curve(int step) {
        double progress = step / (double) (STEPS - 1);
        return 1 + (int) Math.round(18 * Math.pow(progress, 5));
    }
}
