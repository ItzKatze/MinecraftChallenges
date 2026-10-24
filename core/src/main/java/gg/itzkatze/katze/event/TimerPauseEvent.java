package gg.itzkatze.katze.event;

import lombok.Getter;
import gg.itzkatze.katze.util.EventLogger;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class TimerPauseEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    @Getter
    @Setter
    private boolean cancelled;
    @Getter
    private int pauseTime;

    public TimerPauseEvent(int pauseTime) {
        this.pauseTime = pauseTime;
    }

    public void setPauseTime(int pauseTime) {
        if (pauseTime != this.pauseTime) {
            EventLogger.LOGGER.info("TimerPauseEvent: value pauseTime has been changed to {}.", pauseTime);
            this.pauseTime = pauseTime;
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
