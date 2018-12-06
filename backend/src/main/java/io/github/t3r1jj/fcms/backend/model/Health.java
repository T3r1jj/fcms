package io.github.t3r1jj.fcms.backend.model;

import io.github.t3r1jj.storapi.data.StorageInfo;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.springframework.boot.convert.DurationFormat;
import org.springframework.boot.convert.DurationStyle;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Health {
    public String dbSize;
    public String dbLimit;
    public List<BandwidthSize> bandwidth;
    public List<StorageInfo> storageQuotas;

    public static class BandwidthSize {
        public BigInteger upload;
        public BigInteger download;
        private Instant start;
        private Instant end;
        private Duration duration;

        public BandwidthSize() {
        }

        public BandwidthSize(BigInteger upload, BigInteger download, Instant start) {
            this.upload = upload;
            this.download = download;
            this.start = start;
            this.end = start;
            this.duration = Duration.between(this.start, end);
        }

        public Instant getStart() {
            return start;
        }

        public Instant getEnd() {
            return end;
        }

        public Duration getDuration() {
            return duration;
        }

        public void setEnd(Instant end) {
            this.end = end;
            if (start != null) {
                duration = Duration.between(start, end);
            } else if (duration != null) {
                start = end.minus(duration);
            }
        }

        public String toText() {
            return String.format("Duration: %s. Downloaded: %s Bytes. Uploaded: %s Bytes.",
                    DurationFormatUtils.formatDuration(getDuration().toMillis(), "HH:mm:ss", true),
                    download.toString(), upload.toString());
        }

        public boolean loadFromText(String text) {
            String durationTime = subStringBetween(text, "Duration: ", ". Downloaded");
            String bytesDownloaded = subStringBetween(text, "Downloaded: ", " Bytes.");
            String bytesUploaded = subStringBetween(text, "Uploaded: ", " Bytes.");
            if (durationTime != null && bytesDownloaded != null && bytesUploaded != null) {
                duration = Duration.between(LocalTime.MIN, LocalTime.parse(durationTime));
                download = new BigInteger(bytesDownloaded);
                upload = new BigInteger(bytesUploaded);
                return true;
            } else {
                return false;
            }
        }

        private String subStringBetween(String text, String after, String before) {
            String regex = Pattern.quote(after) + "(.*?)" + Pattern.quote(before);
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                return matcher.group(1);
            }
            return null;
        }
    }

}
