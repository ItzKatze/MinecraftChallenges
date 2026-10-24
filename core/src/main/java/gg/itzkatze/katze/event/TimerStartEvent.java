package gg.itzkatze.katze.event;

import lombok.Getter;
import gg.itzkatze.katze.util.EventLogger;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class TimerStartEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    @Getter
    @Setter
    private boolean cancelled;
    @Getter
    private int startTime;

    public TimerStartEvent(int startTime) {
        this.startTime = startTime;
    }

    public void setStartTime(int startTime) {
        if (startTime != this.startTime) {
            EventLogger.LOGGER.info("TimerStartEvent: value startTime has been changed to {}.", startTime);
            this.startTime = startTime;
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
