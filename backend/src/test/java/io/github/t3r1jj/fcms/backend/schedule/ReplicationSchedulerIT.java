package io.github.t3r1jj.fcms.backend.schedule;

import io.github.t3r1jj.fcms.backend.service.ReplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import static org.mockito.Mockito.verify;

@PropertySource("classpath:application_scheduling.properties")
@SpringBootTest
public class ReplicationSchedulerIT extends AbstractTestNGSpringContextTests {

    @SpyBean
    @Autowired
    private ReplicationService replicationService;

    @SpyBean
    @Autowired
    private ReplicationScheduler replicationScheduler;

    @Test
    public void testStartReplication() {
        verify(replicationScheduler).startReplication();
        verify(replicationService).safelyReplicateAll();
    }
}