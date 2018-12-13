package io.github.t3r1jj.fcms.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.mongodb.annotations.Immutable;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Objects;

@Document
@Immutable
public class StoredRecordMeta {
    @Id
    @JsonSerialize(using = ToStringSerializer.class)
    private ObjectId id = ObjectId.get();
    private String name;
    private String tag;
    private String description;

    @PersistenceConstructor
    public StoredRecordMeta(@JsonProperty("id") ObjectId id,
                            @JsonProperty("name") String name,
                            @JsonProperty("tag") String tag,
                            @JsonProperty("description") String description) {
        this.id = id;
        this.name = name;
        this.tag = tag;
        this.description = description;
    }

    public StoredRecordMeta(String name, String tag) {
        this.name = name;
        this.tag = tag;
    }

    public ObjectId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTag() {
        return tag;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StoredRecordMeta)) return false;
        StoredRecordMeta that = (StoredRecordMeta) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(tag, that.tag) &&
                Objects.equals(description, that.description);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(id);
    }
}
