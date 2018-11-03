package io.github.t3r1jj.fcms.backend.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document
public class StoredRecord {
    @Id
    private ObjectId id;
    private final String name;
    private final String tag;
    transient private byte[] data;

    private List<StoredRecord> versions = new ArrayList<>();
    private List<StoredBackup> backups = new ArrayList<>();

    public StoredRecord(String name, String tag, byte[] data) {
        this.name = name;
        this.tag = tag;
        this.data = data;
    }

    public StoredRecord(String name, String tag) {
        this.name = name;
        this.tag = tag;
    }

    public List<StoredRecord> getVersions() {
        return versions;
    }

    public void setVersions(List<StoredRecord> versions) {
        this.versions = versions;
    }

    public List<StoredBackup> getBackups() {
        return backups;
    }

    public void setBackups(List<StoredBackup> backups) {
        this.backups = backups;
    }

    public ObjectId getId() {
        return id;
    }
}
