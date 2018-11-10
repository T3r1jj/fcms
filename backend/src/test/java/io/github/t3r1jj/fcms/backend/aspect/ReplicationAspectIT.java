package io.github.t3r1jj.fcms.backend.aspect;

import io.github.t3r1jj.fcms.backend.model.StoredRecord;
import io.github.t3r1jj.fcms.backend.service.ReplicationService;
import org.mockito.exceptions.verification.NoInteractionsWanted;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@SpringBootTest
public class ReplicationAspectIT extends AbstractTestNGSpringContextTests {

    @SpyBean
    @Autowired
    private ReplicationAspect replicationAspect;

    @SpyBean
    @Autowired
    private ReplicationService replicationService;

    @Test
    public void testAfterReplicationCallback() {
        replicationService.safelyReplicateAll();
        verify(replicationAspect).afterReplication();
    }

    @Test(expectedExceptions = {NoInteractionsWanted.class}) //workaround null pointer on verifying replication aspect method
    public void testOnReplicationCallback() throws Throwable {
        StoredRecord storedRecord = new StoredRecord("a", "b", "c".getBytes(), null);
        Method methodUnderTest = ReplicationService.class.getDeclaredMethod("replicateRecordTo", StoredRecord.class, boolean.class);
        methodUnderTest.setAccessible(true);
        methodUnderTest.invoke(replicationService, storedRecord, true);
        verifyNoMoreInteractions(replicationAspect);
    }
}