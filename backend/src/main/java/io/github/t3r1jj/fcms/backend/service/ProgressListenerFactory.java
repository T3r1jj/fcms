package io.github.t3r1jj.fcms.backend.service;

import io.github.t3r1jj.fcms.backend.model.Health;
import io.github.t3r1jj.fcms.backend.model.Progress;

import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;

class ProgressListenerFactory {
    private final NotificationService notificationService;
    private final Health.BandwidthSize bandwidthSize = new Health.BandwidthSize(BigInteger.ZERO, BigInteger.ZERO, Instant.now());

    ProgressListenerFactory(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    ProgressListener create(Progress progress, boolean upload) {
        return new ProgressListener(progress, notificationService, upload) {
            private Long prevBytesWritten = 0L;

            @Override
            public void accept(Long bytesWritten) {
                super.accept(bytesWritten);
                if (upload) {
                    bandwidthSize.upload = bandwidthSize.upload.add(BigInteger.valueOf(bytesWritten - prevBytesWritten));
                } else {
                    bandwidthSize.download = bandwidthSize.download.add(BigInteger.valueOf(bytesWritten - prevBytesWritten));
                }
                prevBytesWritten = bytesWritten;
            }
        };
    }

    String getBandwidth() {
        bandwidthSize.setEnd(Instant.now());
        return bandwidthSize.toText();
    }
}
