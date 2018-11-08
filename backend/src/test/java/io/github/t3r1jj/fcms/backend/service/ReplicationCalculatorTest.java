package io.github.t3r1jj.fcms.backend.service;

import io.github.t3r1jj.fcms.backend.model.Configuration;
import io.github.t3r1jj.fcms.backend.model.ExternalService;
import io.github.t3r1jj.fcms.backend.model.StoredRecord;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class ReplicationCalculatorTest {

    private final Configuration configuration = new Configuration(new ExternalService[]{
            new ExternalService("1 primary", true, true),
            new ExternalService("2 primary", true, true),
            new ExternalService("1 primary disabled", true, false),
            new ExternalService("2 primary disabled", true, false),
            new ExternalService("3 primary disabled", true, false),

            new ExternalService("1 secondary", false, true),
            new ExternalService("2 secondary", false, true),
            new ExternalService("3 secondary", false, true),
            new ExternalService("4 secondary", false, true),
            new ExternalService("1 secondary disabled", false, false),
            new ExternalService("2 secondary disabled", false, false),
            new ExternalService("3 secondary disabled", false, false),
            new ExternalService("4 secondary disabled", false, false),
            new ExternalService("5 secondary disabled", false, false),
    });

    @Test
    public void testDefaultRangePrimaryLimited() {
        StoredRecord record = new StoredRecord("", "");
        configuration.setPrimaryBackupLimit(0);
        configuration.setSecondaryBackupLimit(0);
        ReplicationCalculator replicationCalculator = new ReplicationCalculator(record, configuration)
                .calculateForPrimary(true);
        assertEquals(replicationCalculator.getBackupCount(), 0);
        assertEquals(replicationCalculator.getBackupLimit(), 0);
    }

    @Test
    public void testDefaultRangePrimary() {
        StoredRecord record = new StoredRecord("", "");
        configuration.setPrimaryBackupLimit(999);
        configuration.setSecondaryBackupLimit(999);
        ReplicationCalculator replicationCalculator = new ReplicationCalculator(record, configuration)
                .calculateForPrimary(true);
        assertEquals(replicationCalculator.getBackupCount(), 0);
        assertEquals(replicationCalculator.getBackupLimit(), 2);
    }

    @Test
    public void testDefaultRangeSecondaryLimited() {
        StoredRecord record = new StoredRecord("", "");
        configuration.setPrimaryBackupLimit(0);
        configuration.setSecondaryBackupLimit(0);
        ReplicationCalculator replicationCalculator = new ReplicationCalculator(record, configuration)
                .calculateForPrimary(false);
        assertEquals(replicationCalculator.getBackupCount(), 0);
        assertEquals(replicationCalculator.getBackupLimit(), 0);
    }

    @Test
    public void testDefaultRangeSecondary() {
        StoredRecord record = new StoredRecord("", "");
        configuration.setPrimaryBackupLimit(999);
        configuration.setSecondaryBackupLimit(999);
        ReplicationCalculator replicationCalculator = new ReplicationCalculator(record, configuration)
                .calculateForPrimary(false);
        assertEquals(replicationCalculator.getBackupCount(), 0);
        assertEquals(replicationCalculator.getBackupLimit(), 4);
    }

    @Test
    public void testRangePrimaryUnlimitedWithBackups() {
        StoredRecord record = new StoredRecord("", "");
        record.getBackups().put("1 primary", null);
        configuration.setPrimaryBackupLimit(999);
        configuration.setSecondaryBackupLimit(999);
        ReplicationCalculator replicationCalculator = new ReplicationCalculator(record, configuration)
                .calculateForPrimary(true);
        assertEquals(replicationCalculator.getBackupCount(), 1);
        assertEquals(replicationCalculator.getBackupLimit(), 2);
    }

    @Test
    public void testRangeSecondaryUnlimitedWithBackups() {
        StoredRecord record = new StoredRecord("", "");
        record.getBackups().put("1 secondary", null);
        configuration.setPrimaryBackupLimit(999);
        configuration.setSecondaryBackupLimit(999);
        ReplicationCalculator replicationCalculator = new ReplicationCalculator(record, configuration)
                .calculateForPrimary(false);
        assertEquals(replicationCalculator.getBackupCount(), 1);
        assertEquals(replicationCalculator.getBackupLimit(), 4);
    }

    @Test
    public void testRangeSecondaryUnlimitedWithBackupsIndependentFromPrimary() {
        StoredRecord record = new StoredRecord("", "");
        record.getBackups().put("1 secondary", null);
        record.getBackups().put("1 primary", null);
        configuration.setPrimaryBackupLimit(999);
        configuration.setSecondaryBackupLimit(999);
        ReplicationCalculator replicationCalculator = new ReplicationCalculator(record, configuration)
                .calculateForPrimary(false);
        assertEquals(replicationCalculator.getBackupCount(), 1);
        assertEquals(replicationCalculator.getBackupLimit(), 4);
    }

    @Test
    public void testRangePrimaryUnlimitedWithBackupsIndependentFromSecondary() {
        StoredRecord record = new StoredRecord("", "");
        record.getBackups().put("1 secondary", null);
        record.getBackups().put("1 primary", null);
        configuration.setPrimaryBackupLimit(999);
        configuration.setSecondaryBackupLimit(999);
        ReplicationCalculator replicationCalculator = new ReplicationCalculator(record, configuration)
                .calculateForPrimary(true);
        assertEquals(replicationCalculator.getBackupCount(), 1);
        assertEquals(replicationCalculator.getBackupLimit(), 2);
    }

    @Test
    public void testRangePrimaryAndSecondaryAndSomeLimits() {
        StoredRecord record = new StoredRecord("", "");
        record.getBackups().put("1 secondary", null);
        record.getBackups().put("1 primary", null);
        configuration.setPrimaryBackupLimit(1);
        configuration.setSecondaryBackupLimit(2);
        ReplicationCalculator replicationCalculator = new ReplicationCalculator(record, configuration)
                .calculateForPrimary(true);
        assertEquals(replicationCalculator.getBackupCount(), 1);
        assertEquals(replicationCalculator.getBackupLimit(), 1);
        replicationCalculator.calculateForPrimary(false);
        assertEquals(replicationCalculator.getBackupCount(), 1);
        assertEquals(replicationCalculator.getBackupLimit(), 2);
    }

    @Test
    public void testRangeUnknownPrimaryService() {
        StoredRecord record = new StoredRecord("", "");
        record.getBackups().put("UNKNOWN", null);
        configuration.setPrimaryBackupLimit(1);
        configuration.setSecondaryBackupLimit(0);
        ReplicationCalculator replicationCalculator = new ReplicationCalculator(record, configuration)
                .calculateForPrimary(true);
        assertEquals(replicationCalculator.getBackupCount(), 0);
        assertEquals(replicationCalculator.getBackupLimit(), 1);
    }
}