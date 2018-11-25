package io.github.t3r1jj.fcms.backend.service;

import io.github.t3r1jj.fcms.backend.model.Event;
import io.github.t3r1jj.fcms.backend.model.Payload;
import io.github.t3r1jj.fcms.backend.model.Progress;

import java.util.function.Consumer;

public class ProgressListener implements Consumer<Long> {
    private final Progress progress;
    private final Event progressEvent;
    private final NotificationService notificationService;

    ProgressListener(Progress progress, NotificationService notificationService, boolean upload) {
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
        progress.setDone(bytesWritten);
        notificationService.broadcast(progressEvent);
    }
}
