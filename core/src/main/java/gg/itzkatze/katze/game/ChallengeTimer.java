package gg.itzkatze.katze.game;

import lombok.Getter;
import lombok.Setter;

public class ChallengeTimer {

    private final long intervalMillis;
    @Getter
    @Setter
    private long remainingMillis;
    private long lastTickMillis = 0L;

    public ChallengeTimer(long intervalMillis) {
        this.intervalMillis = intervalMillis;
        this.remainingMillis = intervalMillis;
    }

    public void tick() {
        long now = System.currentTimeMillis();
        if (lastTickMillis == 0L) {
            lastTickMillis = now;
            return;
        }
        long delta = Math.max(0L, now - lastTickMillis);
        lastTickMillis = now;
        remainingMillis -= delta;
    }

    public boolean hasElapsed() {
        return remainingMillis <= 0L;
    }

    public void reset() {
        remainingMillis = intervalMillis;
    }

    public void consumeElapsed() {
        remainingMillis += intervalMillis;
    }

    public int getSecondsRemaining() {
        return (int) Math.max(0L, (long) Math.ceil(remainingMillis / 1000.0));
    }

    public void resetBaseline() {
        lastTickMillis = 0L;
    }
}
