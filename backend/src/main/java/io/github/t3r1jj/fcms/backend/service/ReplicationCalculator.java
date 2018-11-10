package io.github.t3r1jj.fcms.backend.service;

import io.github.t3r1jj.fcms.backend.model.Configuration;
import io.github.t3r1jj.fcms.backend.model.ExternalService;
import io.github.t3r1jj.fcms.backend.model.StoredRecord;

import static io.github.t3r1jj.fcms.backend.Utils.notIf;

class ReplicationCalculator {
    private StoredRecord storedRecord;
    private Configuration configuration;
    private long backupLimit;
    private long backupCount;

    ReplicationCalculator(StoredRecord storedRecord, Configuration configuration) {
        this.storedRecord = storedRecord;
        this.configuration = configuration;
    }

    long getBackupLimit() {
        return backupLimit;
    }

    long getBackupCount() {
        return backupCount;
    }

    ReplicationCalculator calculateForPrimary(boolean primary) {
        backupLimit = Math.min(primary ? configuration.getPrimaryBackupLimit() : configuration.getSecondaryBackupLimit(),
                configuration.getEnabledServicesStream(primary).count());
        backupCount = configuration.stream()
                .filter(notIf(ExternalService::isPrimary, !primary))
                .filter(s -> storedRecord.getBackups().containsKey(s.getName()))
                .count();
        return this;
    }

    boolean isAnyBackupPossible() {
        calculateForPrimary(true);
        boolean primaryBAckupPossible = getBackupCount() < getBackupLimit();
        calculateForPrimary(false);
        boolean secondaryBackupPossible = getBackupCount() < getBackupLimit();
        return primaryBAckupPossible || secondaryBackupPossible;
    }
}
