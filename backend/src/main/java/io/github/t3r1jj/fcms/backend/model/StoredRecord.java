package io.github.t3r1jj.fcms.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.github.t3r1jj.storapi.data.Record;
import io.github.t3r1jj.storapi.data.RecordMeta;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.ByteArrayInputStream;
import java.util.*;

@Document
public class StoredRecord {
    public static ObjectId stringToObjectId(String id) {
        return new ObjectId(id);
    }

    @Id
    @JsonSerialize(using = ToStringSerializer.class)
    private ObjectId id = ObjectId.get();
    @JsonIgnore
    private ObjectId rootId;
    @DBRef
    private StoredRecordMeta meta;
    @Transient
    private byte[] data;

    private List<StoredRecord> versions = new ArrayList<>();
    private Map<String, RecordMeta> backups = new HashMap<>();

    public StoredRecord(ObjectId id) {
        this.id = id;
    }

    public StoredRecord(String name, String tag, byte[] data, String rootId) {
        this.meta = new StoredRecordMeta(name, tag);
        this.data = data;
        if (rootId != null) {
            this.rootId = stringToObjectId(rootId);
        }
    }

    public StoredRecord(String name, String tag) {
        this(name, tag, null, null);
    }

    @PersistenceConstructor
    public StoredRecord(ObjectId id, StoredRecordMeta meta) {
        this.meta = meta;
        this.id = id;
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

    public StoredRecordMeta getMeta() {
        return meta;
    }

    public byte[] getData() {
        return data;
    }

    public Optional<ObjectId> getRootId() {
        return Optional.ofNullable(rootId);
    }

    public void setRootId(ObjectId rootId) {
        this.rootId = rootId;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public Optional<StoredRecord> findParent(ObjectId id) {
        if (getVersions().stream().anyMatch(c -> c.getId().equals(id))) {
            return Optional.of(this);
        } else {
            return getVersions().stream()
                    .map(c -> c.findParent(id))
                    .map(Optional::get)
                    .findAny();
        }
    }

    /**
     * @return Record with data to upload
     * @throws RuntimeException if there is no data
     */
    @NotNull
    public Record prepareRecord() {
        if (this.getData() == null || this.getData().length == 0) {
            throw new RuntimeException("Dude... there is no data to store!");
        }
        return new Record(
                this.meta.getName(),
                this.getId().toString(),
                new ByteArrayInputStream(this.getData())
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StoredRecord that = (StoredRecord) o;
        return id.equals(that.id) &&
                Objects.equals(rootId, that.rootId) &&
                Objects.equals(meta, that.meta) &&
                Objects.equals(versions, that.versions) &&
                Objects.equals(backups, that.backups);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "StoredRecord{" +
                "id=" + id +
                ", rootId=" + rootId +
                ", meta=" + meta +
                ", data=" + Arrays.toString(data) +
                ", versions=" + versions +
                ", backups=" + backups +
                '}';
    }
}
