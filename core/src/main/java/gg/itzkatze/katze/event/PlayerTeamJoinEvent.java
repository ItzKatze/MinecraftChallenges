package gg.itzkatze.katze.event;

import lombok.Getter;
import gg.itzkatze.katze.team.KatzeTeam;
import gg.itzkatze.katze.player.KatzePlayer;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerTeamJoinEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    @Getter
    @Setter
    private boolean cancelled;

    @Getter
    private final KatzePlayer whoJoined;
    @Getter
    private final KatzeTeam team;

    public PlayerTeamJoinEvent(KatzeTeam team, KatzePlayer whoJoined) {
        this.team = team;
        this.whoJoined = whoJoined;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
