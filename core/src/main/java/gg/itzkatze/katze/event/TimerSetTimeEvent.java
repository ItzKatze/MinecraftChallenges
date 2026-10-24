package gg.itzkatze.katze.event;

import lombok.Getter;
import gg.itzkatze.katze.util.EventLogger;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class TimerSetTimeEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    @Getter
    @Setter
    private boolean cancelled;

    @Getter
    private int newTime;

    public TimerSetTimeEvent(int newTime) {
        this.newTime = newTime;
    }

    public void setNewTime(int newTime) {
        if (newTime != this.newTime) {
            EventLogger.LOGGER.info("TimerSetTimeEvent: newTime changed to {}.", newTime);
            this.newTime = newTime;
        }
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
