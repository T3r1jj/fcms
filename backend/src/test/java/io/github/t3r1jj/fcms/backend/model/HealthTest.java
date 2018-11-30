package io.github.t3r1jj.fcms.backend.model;

import org.testng.annotations.Test;

import java.math.BigInteger;
import java.time.Instant;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

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

}