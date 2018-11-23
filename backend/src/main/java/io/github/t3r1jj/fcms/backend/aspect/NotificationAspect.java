package io.github.t3r1jj.fcms.backend.aspect;


import io.github.t3r1jj.fcms.backend.model.Event;
import io.github.t3r1jj.fcms.backend.model.Payload;
import io.github.t3r1jj.fcms.backend.model.StoredRecord;
import io.github.t3r1jj.fcms.backend.service.NotificationService;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Aspect
@Component
public class NotificationAspect {

    private final NotificationService notificationService;

    @Autowired
    public NotificationAspect(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @AfterReturning(value = "execution(* io.github.t3r1jj.fcms.backend.repository.StoredRecordRepository.save(..))",
            returning = "storedRecord")
    public void afterSave(StoredRecord storedRecord) {
        sendNotificationWithPayload(new Payload(storedRecord, Payload.Type.SAVE));
    }

    @AfterReturning(value = "this(io.github.t3r1jj.fcms.backend.repository.StoredRecordRepository+) && execution(* *.saveAll(..))",
            returning = "storedRecords")
    public void afterSaveAll(List<?> storedRecords) {
        storedRecords.forEach(storedRecord -> sendNotificationWithPayload(new Payload((StoredRecord) storedRecord, Payload.Type.SAVE)));
    }

    @AfterReturning("execution(* io.github.t3r1jj.fcms.backend.repository.StoredRecordRepository.delete(..)) && args(storedRecord)")
    public void afterDelete(StoredRecord storedRecord) {
        sendNotificationWithPayload(new Payload(storedRecord, Payload.Type.DELETE));
    }

    @AfterReturning("execution(* io.github.t3r1jj.fcms.backend.repository.StoredRecordRepository.deleteById(..)) && args(deletedId)")
    public void afterDelete(ObjectId deletedId) {
        sendNotificationWithPayload(new Payload(new StoredRecord(deletedId), Payload.Type.DELETE));
    }

    @AfterReturning("execution(* io.github.t3r1jj.fcms.backend.repository.StoredRecordRepository.deleteAll(..)) && args(storedRecords)")
    public void afterDeleteAll(Iterable<StoredRecord> storedRecords) {
        storedRecords.forEach(storedRecord -> sendNotificationWithPayload(new Payload(storedRecord, Payload.Type.DELETE)));
    }

    private void sendNotificationWithPayload(Payload payload) {
        notificationService.broadcast(new Event.Builder()
                .formatTitle("Record change")
                .formatDescription(payload.getType().toString())
                .setType(Event.Type.PAYLOAD)
                .setPayload(payload)
                .build());
    }
}
