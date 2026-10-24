package gg.itzkatze.katze.game;

public interface GameTimer {

    enum Mode {
        DISABLED,
        COUNT_UP,
        COUNT_DOWN
    }

    Mode getMode();

    void setMode(Mode mode);

    int getElapsedSeconds();

    int getRemainingSeconds();

    void setRemainingSeconds(int seconds);

    void setDuration(int seconds);

    int getDuration();

    boolean isRunning();

    boolean hasHitZero();

    void start();

    void pause();

    void stop();

    void restore(Mode mode, int durationSeconds, int remainingSeconds, int elapsedSeconds, boolean hitZero);
}
