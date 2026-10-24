package gg.itzkatze.katze.event;

import lombok.Getter;
import gg.itzkatze.katze.challenges.ChallengeObjective;
import gg.itzkatze.katze.team.KatzeTeam;
import gg.itzkatze.katze.player.KatzePlayer;
import gg.itzkatze.katze.util.EventLogger;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ObjectiveUpdateEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    @Getter
    private final KatzePlayer katzePlayer;
    @Getter
    private final KatzeTeam team;
    @Getter
    private ChallengeObjective newObjective;
    @Getter
    @Setter
    private boolean cancelled;

    public ObjectiveUpdateEvent(@NotNull KatzePlayer katzePlayer, @NotNull ChallengeObjective newObjective) {
        this.katzePlayer = katzePlayer;
        this.team = null;
        this.newObjective = newObjective;
    }

    public ObjectiveUpdateEvent(@NotNull KatzeTeam team, @NotNull ChallengeObjective newObjective) {
        this.katzePlayer = null;
        this.team = team;
        this.newObjective = newObjective;
    }

    public void setNewObjective(ChallengeObjective newObjective) {
        if (this.newObjective != newObjective) {
            EventLogger.LOGGER.info("ObjectiveUpdateEvent: newObjective changed to {}.", newObjective.getObjectiveString());
            this.newObjective = newObjective;
        }
    }

    public boolean isTeamEvent() {
        return team != null;
    }

    public boolean isPlayerEvent() {
        return katzePlayer != null;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
