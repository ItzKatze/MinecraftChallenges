package gg.itzkatze.katze.challenges;

import gg.itzkatze.katze.game.GameTimer;

public record TimerSpec(GameTimer.Mode mode, int startSeconds) {

    public TimerSpec {
        if (startSeconds < 0) {
            throw new IllegalArgumentException("startSeconds cannot be negative");
        }
    }
}
