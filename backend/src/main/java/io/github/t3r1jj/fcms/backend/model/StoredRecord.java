package io.github.t3r1jj.fcms.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.t3r1jj.fcms.external.data.RecordMeta;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;

@Document
public class StoredRecord {
    public static ObjectId stringToObjectId(String id) {
        return new ObjectId(id);
    }

    @Id
    private ObjectId id = ObjectId.get();
    @JsonIgnore
    private ObjectId rootId;
    private final String name;
    private final String tag;
    transient private byte[] data;
    private String description;

    private List<StoredRecord> versions = new ArrayList<>();
    private Map<String, RecordMeta> backups = new HashMap<>();

    public StoredRecord(String name, String tag, byte[] data, String rootId) {
        this.name = name;
        this.tag = tag;
        this.data = data;
        if (rootId != null) {
            this.rootId = stringToObjectId(rootId);
        }
    }

    @PersistenceConstructor
    public StoredRecord(String name, String tag) {
        this(name, tag, null, null);
    }

    public List<StoredRecord> getVersions() {
        return versions;
    }

    public Map<String, RecordMeta> getBackups() {
        return backups;
    }

    public ObjectId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public byte[] getData() {
        return data;
    }

    public String getTag() {
        return tag;
    }

    public Optional<ObjectId> getRootId() {
        return Optional.ofNullable(rootId);
    }

    public void setRootId(ObjectId rootId) {
        this.rootId = rootId;
    }

    public StoredRecord findParent(ObjectId id) {
        if (getVersions().stream().anyMatch(c -> c.getId().equals(id))) {
            return this;
        } else {
            return getVersions().stream()
                    .map(c -> c.findParent(id))
                    .findAny()
                    .orElse(null);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StoredRecord that = (StoredRecord) o;
        return id.equals(that.id) &&
                Objects.equals(rootId, that.rootId) &&
                name.equals(that.name) &&
                Objects.equals(tag, that.tag) &&
                Objects.equals(description, that.description) &&
                Objects.equals(versions, that.versions) &&
                Objects.equals(backups, that.backups);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
