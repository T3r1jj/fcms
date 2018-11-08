package io.github.t3r1jj.fcms.backend.schedule;

import io.github.t3r1jj.fcms.backend.service.ReplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty("io.github.t3r1jj.fcms.backend.schedule.ReplicationScheduler")
public class ReplicationScheduler {

    private final ReplicationService replicationService;

    @Autowired
    public ReplicationScheduler(ReplicationService replicationService) {
        this.replicationService = replicationService;
    }

    @Scheduled(fixedDelay = 1000 * 60 * 60 * 2)
    void startReplication() {
        replicationService.safelyReplicateAll();
    }
}
