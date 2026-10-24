package gg.itzkatze.katze.lobby.cosmetics.cases;

import org.jetbrains.annotations.NotNull;

final class CaseSpin {

    static final int REVEAL_DELAY = 40;

    enum Tick {
        NONE,
        MOVED,
        DUE
    }

    private final CaseReel reel;
    private final boolean pity;
    private int position;
    private int wait;
    private int revealWait = REVEAL_DELAY;
    private boolean paid;

    CaseSpin(@NotNull CaseReel reel, boolean pity) {
        this.reel = reel;
        this.pity = pity;
        this.wait = reel.delay(0);
    }

    @NotNull CaseReel reel() {
        return reel;
    }

    @NotNull CasePrize prize() {
        return reel.prize();
    }

    boolean pity() {
        return pity;
    }

    int position() {
        return position;
    }

    boolean paid() {
        return paid;
    }

    @NotNull Tick tick() {
        if (paid) return Tick.NONE;
        if (stopped()) return --revealWait <= 0 ? Tick.DUE : Tick.NONE;
        if (--wait > 0) return Tick.NONE;

        position++;
        if (!stopped()) {
            wait = reel.delay(position);
        }
        return Tick.MOVED;
    }

    boolean settle() {
        if (paid) return false;

        paid = true;
        return true;
    }

    private boolean stopped() {
        return position == reel.steps();
    }
}
