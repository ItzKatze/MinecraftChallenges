package gg.itzkatze.katze.lobby.jumpandrun;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.BitSet;
import java.util.Objects;
import java.util.UUID;

@Getter
final class JumpRun {

    private final Player player;
    private final UUID id;
    private final RunLook look;
    private final BitSet visited = new BitSet();
    private Cell current;
    private @Nullable Cell next;
    private HopDifficulty nextDifficulty = HopDifficulty.NORMAL;
    private boolean nextEvent;
    private @Nullable HopSteering.Leg leg;
    private int score;
    private int eventBlocks;
    private int eventPoints;

    JumpRun(@NotNull Player player, @NotNull RunLook look, @NotNull Cell start, @NotNull Cell next) {
        this.player = player;
        this.id = player.getUniqueId();
        this.look = look;
        this.current = start;
        this.next = next;
    }

    boolean land() {
        boolean event = nextEvent;
        score++;
        current = Objects.requireNonNull(next);
        next = null;
        nextEvent = false;
        return event;
    }

    void setNext(@NotNull Cell next, @NotNull HopDifficulty difficulty, boolean event) {
        this.next = next;
        this.nextDifficulty = difficulty;
        this.nextEvent = event;
    }

    void recordEvent(int applied) {
        eventBlocks++;
        eventPoints += applied;
    }

    void setLeg(@NotNull HopSteering.Leg leg) {
        this.leg = leg;
    }
}
