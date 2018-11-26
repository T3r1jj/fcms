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
    private final Type type;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private final Instant time;
    private boolean read;
    private transient Payload payload;

    public Event(String title, String description, Type type) {
        this(title, description, type, null, false, null);
    }

    @PersistenceConstructor
    public Event(String title, String description, Type type, Instant time, boolean read, Payload payload) {
        this.title = title;
        this.description = description;
        if (time == null) {
            this.time = Instant.now();
        } else {
            this.time = time;
        }
        this.type = type;
        this.read = read;
        this.payload = payload;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Type getType() {
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

    public Payload getPayload() {
        return payload;
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

    public enum Type {
        INFO, WARNING, ERROR, DEBUG, PAYLOAD
    }

    public static class Builder {
        private String title;
        private String description;
        private Payload payload;
        private Type type;
        private boolean read;
        private Instant time;

        public Builder formatTitle(String title, Object... args) {
            this.title = String.format(title, args);
            return this;
        }

        public Builder formatDescription(String description, Object... args) {
            this.description = String.format(description, args);
            return this;
        }

        public Builder setType(Type type) {
            this.type = type;
            return this;
        }

        public Builder setPayload(Payload payload) {
            this.payload = payload;
            return this;
        }

        public Builder setRead(boolean read) {
            this.read = read;
            return this;
        }

        public Builder setTime(Instant time) {
            this.time = time;
            return this;
        }

        public Event build() {
            return new Event(title, description, type, time, read, payload);
        }
    }
}
