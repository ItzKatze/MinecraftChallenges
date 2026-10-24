package gg.itzkatze.katze.game;

import lombok.Getter;
import lombok.Setter;

public final class TimerRegistry {

    @Getter
    @Setter
    private static GameTimer timer = new NoOpTimer();

    private TimerRegistry() {
    }

    public static void resetToNoOp() {
        timer = new NoOpTimer();
    }

    private static final class NoOpTimer implements GameTimer {
        private int duration = 5400;
        private boolean running = false;
        private Mode mode = Mode.DISABLED;
        private int elapsedSeconds = 0;
        private int remainingSeconds = 0;

        @Override public Mode getMode() { return mode; }
        @Override public void setMode(Mode mode) { this.mode = mode; }
        @Override public int getElapsedSeconds() { return elapsedSeconds; }
        @Override public int getRemainingSeconds() { return mode == Mode.COUNT_DOWN ? remainingSeconds : -1; }
        @Override public void setRemainingSeconds(int seconds) { this.remainingSeconds = seconds; }
        @Override public void setDuration(int seconds) { this.duration = seconds; }
        @Override public int getDuration() { return duration; }
        @Override public boolean isRunning() { return running; }
        @Override public boolean hasHitZero() { return false; }
        @Override public void start() { this.running = true; }
        @Override public void pause() { this.running = false; }
        @Override public void stop() { this.running = false; }

        @Override
        public void restore(Mode mode, int durationSeconds, int remainingSeconds, int elapsedSeconds, boolean hitZero) {
            this.mode = mode;
            this.duration = durationSeconds;
            this.remainingSeconds = remainingSeconds;
            this.elapsedSeconds = elapsedSeconds;
            this.running = false;
        }
    }
}
