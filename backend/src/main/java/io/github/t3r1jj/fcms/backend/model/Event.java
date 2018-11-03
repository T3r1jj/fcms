package io.github.t3r1jj.fcms.backend.model;

import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;

@Document
public class Event {
    private final String title;
    private final String description;
    private final EventType type;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private final Instant time;

    public Event(String title, String description, EventType type) {
        this(title, description, type, Instant.now());
    }

    @PersistenceConstructor
    public Event(String title, String description, EventType type, Instant time) {
        this.title = title;
        this.description = description;
        this.time = time;
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public EventType getType() {
        return type;
    }

    public Instant getTime() {
        return time;
    }

    @Override
    public String toString() {
        return "Event{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", type=" + type +
                ", time=" + time +
                '}';
    }

    public enum EventType {
        INFO, WARNING, ERROR
    }
}
