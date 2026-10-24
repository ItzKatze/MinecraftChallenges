package gg.itzkatze.katze.event;

import lombok.Getter;
import lombok.Setter;
import gg.itzkatze.katze.util.EventLogger;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class TimerStartTimeChangeEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    @Getter
    @Setter
    private boolean cancelled;
    @Getter
    private int newStartTime;

    public TimerStartTimeChangeEvent(int newStartTime) {
        this.newStartTime = newStartTime;
    }

    public void setNewStartTime(int newStartTime) {
        if (newStartTime != this.newStartTime) {
            EventLogger.LOGGER.info("TimerStartTimeChangeEvent: newStartTime changed to {}.", newStartTime);
            this.newStartTime = newStartTime;
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
