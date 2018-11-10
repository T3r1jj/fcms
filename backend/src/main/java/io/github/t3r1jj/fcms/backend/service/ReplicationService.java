package io.github.t3r1jj.fcms.backend.service;

import io.github.t3r1jj.fcms.backend.controller.exception.ResourceNotFoundException;
import io.github.t3r1jj.fcms.backend.model.*;
import io.github.t3r1jj.fcms.external.authenticated.AuthenticatedStorage;
import io.github.t3r1jj.fcms.external.data.Record;
import io.github.t3r1jj.fcms.external.data.RecordMeta;
import io.github.t3r1jj.fcms.external.data.exception.StorageException;
import io.github.t3r1jj.fcms.external.data.exception.StorageUnauthenticatedException;
import io.github.t3r1jj.fcms.external.upstream.CleanableStorage;
import io.github.t3r1jj.fcms.external.upstream.UpstreamStorage;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

@Service
public class ReplicationService {
    private final ConfigurationService configurationService;
    private final RecordService recordService;
    private final HistoryService historyService;

    public ReplicationService(ConfigurationService configurationService, RecordService recordService, HistoryService historyService) {
        this.configurationService = configurationService;
        this.recordService = recordService;
        this.historyService = historyService;
    }

    /**
     * @param recordToStore record with data to do the first primary replication. Ignores config limits.
     * @throws ResourceNotFoundException if no external service found for replication
     */
    void uploadToPrimary(StoredRecord recordToStore) {
        StorageFactory storageFactory = configurationService.createStorageFactory();
        Configuration configuration = storageFactory.getConfiguration();
        ExternalService primaryService = configuration.getEnabledServicesStream(true)
                .findAny()
                .orElseThrow(() -> new ResourceNotFoundException("No enabled primary service found for replication. Update your config."));
        AuthenticatedStorage authenticatedStorage = storageFactory.createAuthenticatedStorage(primaryService.getName());
        upload(recordToStore, authenticatedStorage);
    }

    public void safelyReplicateAll() {
        recordService.findAll()
                .parallelStream()
                .sorted(Collections.reverseOrder())
                .forEach(this::replicateSafely);
    }

    private void replicateSafely(StoredRecord storedRecord) {
        try {
            replicate(storedRecord);
        } catch (Exception e) {
            historyService.addAndNotify(new Event.Builder()
                    .formatTitle("REPLICATE [%s]", storedRecord.getName())
                    .formatTitle("Error during replication of record with %s id:\n %s", storedRecord.getId().toString(), e.getMessage())
                    .build()
            );
        }
    }

    public void replicate(StoredRecord storedRecord) {
        StorageFactory storageFactory = configurationService.createStorageFactory();
        if (storedRecord.getData() == null) {
            populateRecord(storedRecord, storageFactory);
        }
        Configuration configuration = storageFactory.getConfiguration();
        ReplicationCalculator replicationCalculator = new ReplicationCalculator(storedRecord, configuration);
        replicateRecordsTo(storedRecord, replicationCalculator, true);
        replicateRecordsTo(storedRecord, replicationCalculator, false);
    }

    private void replicateRecordsTo(StoredRecord storedRecord, ReplicationCalculator replicationCalculator, boolean primary) {
        replicationCalculator.calculateForPrimary(primary);
        long backupLimit = replicationCalculator.getBackupLimit();
        long backupCount = replicationCalculator.getBackupCount();
        while (backupCount < backupLimit) {
            if (replicateRecordTo(storedRecord, primary)) {
                recordService.update(storedRecord);
            }
            backupCount++;
        }
    }

    /**
     * @param recordToReplicate with data to any left service. Ignores config limits.
     * @param primary           true if replicate to primary service
     * @return true if replicated and db update is required
     */
    boolean replicateRecordTo(StoredRecord recordToReplicate, boolean primary) {
        StorageFactory storageFactory = configurationService.createStorageFactory();
        Configuration configuration = storageFactory.getConfiguration();
        Optional<ExternalService> anyLeftService = configuration.getEnabledServicesStream(primary)
                .filter(s -> !recordToReplicate.getBackups().containsKey(s.getName()))
                .findAny();
        if (anyLeftService.isPresent()) {
            ExternalService primaryService = anyLeftService.get();
            UpstreamStorage storage = storageFactory.createUpstreamStorage(primaryService);
            upload(recordToReplicate, storage);
            return true;
        }
        return false;
    }

    private void populateRecord(StoredRecord storedRecord, StorageFactory storageFactory) {
        Configuration configuration = storageFactory.getConfiguration();
        Optional<ExternalService> downloadService = configuration
                .getEnabledServicesStream(true)
                .filter(s -> storedRecord.getBackups().containsKey(s.getName()))
                .findAny();
        downloadService
                .map(s -> download(storedRecord.getBackups().get(s.getName()), storageFactory.createAuthenticatedStorage(s.getName())))
                .map(r -> toByteArrayOrNull(r.getData()))
                .ifPresent(storedRecord::setData);
    }

    private Record download(RecordMeta meta, AuthenticatedStorage storage) {
        storage.login();
        Record record = storage.download(meta.getPath());
        storage.logout();
        return record;
    }

    private byte[] toByteArrayOrNull(InputStream inputStream) {
        try {
            return IOUtils.toByteArray(inputStream);
        } catch (IOException e) {
            throw new StorageException("Could not convert input stream to byte array");
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    private void upload(StoredRecord recordToStore, UpstreamStorage storage) {
        Record recordToUpload = recordToStore.prepareRecord();
        RecordMeta meta;
        try {
            meta = storage.upload(recordToUpload);
        } catch (StorageUnauthenticatedException sue) {
            AuthenticatedStorage authenticatedStorage = sue.getStorage();
            authenticatedStorage.login();
            meta = authenticatedStorage.upload(recordToUpload);
            authenticatedStorage.logout();
        }
        recordToStore.getBackups().put(storage.toString(), meta);
    }

    void deleteCascading(StoredRecord storedRecord, boolean force, StoredRecord root) {
        deleteVersionsBackups(storedRecord, force, root);
        root.findParent(storedRecord.getId()).getVersions().remove(storedRecord);
    }

    private void deleteVersionsBackups(StoredRecord storedRecord, boolean force, StoredRecord root) {
        deleteVersions(storedRecord, force, root);
        deleteBackups(storedRecord, force, root);
    }

    private void deleteVersions(StoredRecord storedRecord, boolean force, StoredRecord root) {
        Iterator<StoredRecord> versionIterator = storedRecord.getVersions().iterator();
        while (versionIterator.hasNext()) {
            deleteVersionsBackups(versionIterator.next(), force, root);
            versionIterator.remove();
            if (!force) {
                recordService.update(root);
            }
        }
    }

    private void deleteBackups(StoredRecord storedRecord, boolean force, StoredRecord root) {
        Iterator<Map.Entry<String, RecordMeta>> backupIterator = storedRecord.getBackups().entrySet().iterator();
        while (backupIterator.hasNext()) {
            Map.Entry<String, RecordMeta> e = backupIterator.next();
            deleteBackupWithNotification(e.getKey(), e.getValue(), force);
            backupIterator.remove();
            if (!force) {
                recordService.update(root);
            }
        }
    }

    private void deleteBackupWithNotification(String externalService, RecordMeta meta, boolean force) {
        boolean deleted = deleteBackup(externalService, meta, force);
        if (!deleted) {
            historyService.addAndNotify(new Event.Builder()
                    .formatTitle("UNSUPPORTED DELETE [%s]", externalService)
                    .formatDescription("[%s] Backup of file %s with %s path and id of %s has been removed from " +
                                    "tracking but not storage. Though, the storage might be ephemeral.", externalService,
                            meta.getName(), meta.getPath(), meta.getId())
                    .setType(Event.EventType.DEBUG)
                    .build());
        }
    }

    /**
     * @param externalService name of external service where record has been backed up
     * @param backup          meta
     * @param force           if should not fail on any exception
     * @return true if deleted (some services don't support removal)
     */
    private boolean deleteBackup(String externalService, RecordMeta backup, boolean force) {
        Optional<CleanableStorage> cleanableStorage = configurationService.createStorageFactory().createCleanableStorage(externalService);
        return cleanableStorage.map(it -> {
            if (force) {
                forceDeleteBackup(it, backup);
            } else {
                delete(backup, it);
            }
            return true;
        }).orElse(false);
    }

    private void forceDeleteBackup(CleanableStorage storage, RecordMeta meta) {
        try {
            delete(meta, storage);
        } catch (StorageException se) {
            historyService.addAndNotify(new Event.Builder()
                    .formatTitle("DELETE [%s]", storage.toString())
                    .formatDescription("[%s] Backup of file %s with %s path and id of %s has been removed from " +
                                    "tracking but not storage due to an exception:\n", storage.toString(),
                            meta.getName(), meta.getPath(), meta.getId(), se.getMessage())
                    .setType(Event.EventType.WARNING)
                    .build());
        } catch (RuntimeException e) {
            historyService.addAndNotify(new Event.Builder()
                    .formatTitle("DELETE [%s] UNKNOWN ERROR", storage.toString())
                    .formatDescription("[%s] Backup of file %s with %s path and id of %s has been removed from " +
                                    "tracking but not storage due to an unknown exception:\n", storage.toString(),
                            meta.getName(), meta.getPath(), meta.getId(), e.getMessage())
                    .setType(Event.EventType.WARNING)
                    .build());
        }
    }

    private void delete(RecordMeta backup, CleanableStorage cleanableStorage) {
        try {
            cleanableStorage.delete(backup);
        } catch (StorageUnauthenticatedException sue) {
            AuthenticatedStorage storage = sue.getStorage();
            storage.login();
            storage.delete(backup);
            storage.logout();
        }
    }

}
