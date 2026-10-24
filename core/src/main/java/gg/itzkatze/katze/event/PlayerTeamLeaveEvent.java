package gg.itzkatze.katze.event;

import lombok.Getter;
import gg.itzkatze.katze.team.KatzeTeam;
import gg.itzkatze.katze.player.KatzePlayer;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerTeamLeaveEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    @Getter
    @Setter
    private boolean cancelled;

    @Getter
    private final KatzePlayer whoLeaves;
    @Getter
    private final KatzeTeam team;

    public PlayerTeamLeaveEvent(KatzeTeam team, KatzePlayer whoLeaves) {
        this.team = team;
        this.whoLeaves = whoLeaves;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
