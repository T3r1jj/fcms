package io.github.t3r1jj.fcms.backend.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;

@Document
public class Event {
    @Id
    @JsonSerialize(using = ToStringSerializer.class)
    private ObjectId id = ObjectId.get();
    private final String title;
    private final String description;
    private final EventType type;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private final Instant time;
    private boolean read;

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

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public ObjectId getId() {
        return id;
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
        INFO, WARNING, ERROR, DEBUG
    }

    public static class Builder {
        private String title;
        private String description;
        private Event.EventType type;
        private Instant time = Instant.now();

        public Builder formatTitle(String title, Object... args) {
            this.title = String.format(title, args);
            return this;
        }

        public Builder formatDescription(String description, Object... args) {
            this.description = String.format(title, args);
            return this;
        }

        public Builder setType(Event.EventType type) {
            this.type = type;
            return this;
        }

        public Event build() {
            return new Event(title, description, type);
        }
    }
}
