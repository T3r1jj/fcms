package io.github.t3r1jj.fcms.backend.model;

import java.time.Instant;

public class EventBuilder {
    private String title;
    private String description;
    private Event.EventType type;
    private Instant time = Instant.now();

    public EventBuilder formatTitle(String title, Object... args) {
        this.title = String.format(title, args);
        return this;
    }

    public EventBuilder formatDescription(String description, Object... args) {
        this.description = String.format(title, args);
        return this;
    }

    public EventBuilder setType(Event.EventType type) {
        this.type = type;
        return this;
    }

    public Event createEvent() {
        return new Event(title, description, type);
    }
}