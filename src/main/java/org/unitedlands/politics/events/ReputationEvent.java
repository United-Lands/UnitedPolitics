package org.unitedlands.politics.events;

import java.util.UUID;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ReputationEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private String eventKey;
    private UUID subjectId;
    private UUID targetId;

    private boolean cancelled;

    public ReputationEvent(String eventKey, UUID subjectId) {
        this.eventKey = eventKey;
        this.subjectId = subjectId;
    }

    public String getEventKey() {
        return eventKey;
    }

    public UUID getSubjectId() {
        return subjectId;
    }

    public UUID getTargetId() {
        return targetId;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

}
