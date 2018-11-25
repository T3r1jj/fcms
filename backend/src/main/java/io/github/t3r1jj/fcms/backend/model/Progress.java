package io.github.t3r1jj.fcms.backend.model;

public class Progress {
    private long bytesWritten;
    private final long bytesTotal;
    private final String recordName;
    private final String serviceName;

    public Progress(long bytesTotal, String recordName, String serviceName) {
        this.bytesTotal = bytesTotal;
        this.recordName = recordName;
        this.serviceName = serviceName;
    }

    public void setBytesWritten(long bytesWritten) {
        this.bytesWritten = bytesWritten;
    }

    public long getBytesWritten() {
        return bytesWritten;
    }

    public long getBytesTotal() {
        return bytesTotal;
    }

    public String getRecordName() {
        return recordName;
    }

    public String getServiceName() {
        return serviceName;
    }
}
