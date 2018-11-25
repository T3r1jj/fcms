package io.github.t3r1jj.fcms.backend.model;

public class Payload {
    private StoredRecord record;
    private Progress progress;
    private final Type type;

    public Payload(StoredRecord record, Type type) {
        this.record = record;
        this.type = type;
    }

    public Payload(Progress progress) {
        this.progress = progress;
        this.type = Type.PROGRESS;
    }

    public StoredRecord getRecord() {
        return record;
    }

    public Progress getProgress() {
        return progress;
    }

    public Type getType() {
        return type;
    }

    public enum Type {
        SAVE, DELETE, PROGRESS
    }
}
