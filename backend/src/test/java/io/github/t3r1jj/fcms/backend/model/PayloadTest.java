package io.github.t3r1jj.fcms.backend.model;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class PayloadTest {

    @Test
    public void testGetType_NoRecord_ReplicationProgress() {
        assertEquals(new Payload(new Progress(0, 0)).getType(), Payload.Type.REPLICATION_PROGRESS);
    }

    @Test
    public void testGetType_NoRecordName_ReplicationProgress() {
        assertEquals(new Payload(new Progress(0, null, "")).getType(), Payload.Type.REPLICATION_PROGRESS);
    }

    @Test
    public void testGetType_EmptyRecordName_ReplicationProgress() {
        assertEquals(new Payload(new Progress(0, "", "")).getType(), Payload.Type.REPLICATION_PROGRESS);
    }

    @Test
    public void testGetType_Progress() {
        assertEquals(new Payload(new Progress(0, "sad", "")).getType(), Payload.Type.PROGRESS);
    }
}