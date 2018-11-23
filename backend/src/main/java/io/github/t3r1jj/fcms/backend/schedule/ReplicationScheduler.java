package io.github.t3r1jj.fcms.backend.schedule;

import io.github.t3r1jj.fcms.backend.model.Event;
import io.github.t3r1jj.fcms.backend.model.Payload;
import io.github.t3r1jj.fcms.backend.model.StoredRecord;
import io.github.t3r1jj.fcms.backend.service.HistoryService;
import io.github.t3r1jj.fcms.backend.service.ReplicationService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
@ConditionalOnProperty("io.github.t3r1jj.fcms.backend.schedule.ReplicationScheduler")
public class ReplicationScheduler {

    private final ReplicationService replicationService;
    private final HistoryService historyService;

    @Autowired
    public ReplicationScheduler(ReplicationService replicationService, HistoryService historyService) {
        this.replicationService = replicationService;
        this.historyService = historyService;
    }

    @Scheduled(fixedDelay = 1000 * 60 * 60 * 2)
    void startReplication() {
        replicationService.safelyReplicateAll();
    }

    @Scheduled(fixedDelay = 1000 * 30)
    void sendSampleNotification() {
        historyService.addAndNotify(new Event.Builder()
                .formatTitle("HELLO")
                .formatDescription("DESCRIPTION")
                .setType(Event.Type.values()[new Random().nextInt(Event.Type.values().length)])
                .setPayload(new Payload(new StoredRecord(ObjectId.get()), Payload.Type.values()[new Random().nextInt(Payload.Type.values().length)]))
                .build());
    }
}
