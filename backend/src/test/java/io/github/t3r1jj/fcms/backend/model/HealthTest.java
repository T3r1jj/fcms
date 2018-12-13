package io.github.t3r1jj.fcms.backend.model;

import org.testng.annotations.Test;

import java.math.BigInteger;
import java.time.Instant;

import static org.testng.Assert.*;

public class HealthTest {

    @Test
    public void testLoadFromString() {
        Health.BandwidthSize bandwidthSize = new Health.BandwidthSize(BigInteger.ONE, BigInteger.TEN, Instant.now());
        Health.BandwidthSize loadedSize = new Health.BandwidthSize();
        assertTrue(loadedSize.loadFromText(bandwidthSize.toText()));
        assertEquals(loadedSize.download, bandwidthSize.download);
        assertEquals(loadedSize.upload, bandwidthSize.upload);
        assertEquals(loadedSize.getDuration(), bandwidthSize.getDuration());
    }

    @Test
    public void testLoadFromStringSetEnd() {
        Health.BandwidthSize bandwidthSize = new Health.BandwidthSize(BigInteger.ONE, BigInteger.TEN, Instant.now());
        bandwidthSize.setEnd(Instant.now());
        Health.BandwidthSize loadedSize = new Health.BandwidthSize();
        assertTrue(loadedSize.loadFromText(bandwidthSize.toText()));
        loadedSize.setEnd(bandwidthSize.getEnd());
        assertEquals(loadedSize.getStart(), bandwidthSize.getStart());
        assertEquals(loadedSize.getEnd(), bandwidthSize.getEnd());
        assertEquals(loadedSize.getDuration(), bandwidthSize.getDuration());
    }

    @Test
    public void testLoadFromText_NoDuration() {
        Health.BandwidthSize bandwidthSize = new Health.BandwidthSize();
        assertFalse(bandwidthSize.loadFromText("Downloaded: 0 Bytes. Uploaded: 0 Bytes."));
    }

    @Test
    public void testLoadFromText_NoDownload() {
        Health.BandwidthSize bandwidthSize = new Health.BandwidthSize();
        assertFalse(bandwidthSize.loadFromText("Duration: 00:00:00. Uploaded: 0 Bytes."));
    }

    @Test
    public void testLoadFromText_NoUpload() {
        Health.BandwidthSize bandwidthSize = new Health.BandwidthSize();
        assertFalse(bandwidthSize.loadFromText("Duration: 00:00:00. Downloaded: 0 Bytes."));
    }

    @Test
    public void testLoadFromText_DurationWithoutSettingEnd() {
        Health.BandwidthSize bandwidthSize = new Health.BandwidthSize();
        assertTrue(bandwidthSize.loadFromText("Duration: 00:00:00. Downloaded: 0 Bytes. Uploaded: 0 Bytes."));
        assertNull(bandwidthSize.getStart());
        assertNotNull(bandwidthSize.getDuration());
        assertNull(bandwidthSize.getEnd());
    }

    @Test
    public void testLoadFromText() {
        Health.BandwidthSize bandwidthSize = new Health.BandwidthSize();
        assertTrue(bandwidthSize.loadFromText("Duration: 00:00:00. Downloaded: 0 Bytes. Uploaded: 0 Bytes."));
        bandwidthSize.setEnd(Instant.now());
        assertNotNull(bandwidthSize.getStart());
        assertNotNull(bandwidthSize.getDuration());
        assertNotNull(bandwidthSize.getEnd());
    }

}