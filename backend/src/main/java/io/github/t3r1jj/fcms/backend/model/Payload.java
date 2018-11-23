package io.github.t3r1jj.fcms.backend.model;

public class Payload {
    private StoredRecord record;
    private Type type;

    public Payload(StoredRecord record, Type type) {
        this.record = record;
        this.type = type;
    }

    public StoredRecord getRecord() {
        return record;
    }

    public Type getType() {
        return type;
    }

    public enum Type {
        SAVE, DELETE
    }
}
