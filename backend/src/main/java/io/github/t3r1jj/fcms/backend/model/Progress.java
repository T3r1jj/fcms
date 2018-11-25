package io.github.t3r1jj.fcms.backend.model;

public class Progress {
    private long done;
    private final long total;
    private String recordName;
    private String serviceName;

    public Progress(long total, long done) {
        this.total = total;
        this.done = done;
    }

    public Progress(long total, String recordName, String serviceName) {
        this.total = total;
        this.recordName = recordName;
        this.serviceName = serviceName;
    }

    public void setDone(long done) {
        this.done = done;
    }

    public long getDone() {
        return done;
    }

    public long getTotal() {
        return total;
    }

    public String getRecordName() {
        return recordName;
    }

    public String getServiceName() {
        return serviceName;
    }
}
