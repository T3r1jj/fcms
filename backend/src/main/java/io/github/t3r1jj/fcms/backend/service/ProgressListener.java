package io.github.t3r1jj.fcms.backend.service;

import io.github.t3r1jj.fcms.backend.model.Event;
import io.github.t3r1jj.fcms.backend.model.Payload;
import io.github.t3r1jj.fcms.backend.model.Progress;
import io.github.t3r1jj.fcms.backend.model.StoredRecord;

import java.util.function.Consumer;

public class ProgressListener implements Consumer<Long> {
    private final Progress progress;
    Event progressEvent;
    NotificationService notificationService;

    public ProgressListener(Progress progress, NotificationService notificationService, boolean upload) {
        this.progress = progress;
        this.notificationService = notificationService;
        this.progressEvent = new Event.Builder()
                .formatTitle(upload ? "UPLOADING" : "DOWNLOADING")
                .formatDescription("PROGRESS")
                .setPayload(new Payload(progress))
                .setType(Event.Type.PAYLOAD).build();
    }

    @Override
    public void accept(Long bytesWritten) {
        progress.setBytesWritten(bytesWritten);
        notificationService.broadcast(progressEvent);
    }
}
