package io.github.t3r1jj.fcms.backend.service;

import io.github.t3r1jj.fcms.backend.controller.RecordController;
import io.github.t3r1jj.fcms.backend.model.*;
import io.github.t3r1jj.fcms.external.authorized.Storage;
import io.github.t3r1jj.fcms.external.data.Record;
import io.github.t3r1jj.fcms.external.data.RecordMeta;
import io.github.t3r1jj.fcms.external.data.StorageException;
import io.github.t3r1jj.fcms.external.upstream.CleanableStorage;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

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

    public void replicateToPrimary(StoredRecord recordToStore) {
        Configuration configuration = configurationService.getConfiguration();
        ExternalService[] services = configuration.getApiKeys();
        ExternalService primaryService = Stream.of(services)
                .filter(ExternalService::isEnabled)
                .filter(ExternalService::isPrimary)
                .findAny()
                .orElseThrow(() -> new RecordController.ResourceNotFoundException("No enabled primary service found for replication. Update your config."));
        Storage storage = configurationService.createStorageFactory(configuration).create(primaryService.getName());
        storage.login();
        Record recordToUpload = new Record(
                recordToStore.getName(),
                recordToStore.getId().toString(),
                new ByteArrayInputStream(recordToStore.getData()));
        RecordMeta meta = storage.upload(recordToUpload);
        recordToStore.getBackups().put(storage.toString(), meta);
        storage.logout();
    }

    public void deleteCascading(StoredRecord storedRecord, boolean force, StoredRecord root) {
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
            historyService.addAndNotify(new EventBuilder()
                    .formatTitle("UNSUPPORTED DELETE [$s]", externalService)
                    .formatDescription("[$s] Backup of file $s with $s path and id of $s has been removed from " +
                                    "tracking but not storage. Though, the storage might be ephemeral.", externalService,
                            meta.getName(), meta.getPath(), meta.getId())
                    .setType(Event.EventType.DEBUG)
                    .createEvent());
        }
    }

    /**
     * @param externalService name of external service where record has been backed up
     * @param backup          meta
     * @return true if deleted (some services don't support removal)
     */
    private boolean deleteBackup(String externalService, RecordMeta backup, boolean force) {
        Optional<CleanableStorage> cleanableStorage = configurationService.createStorageFactory().createCleanable(externalService);
        return cleanableStorage.map(it -> {
            if (force) {
                forceDeleteBackup(it, backup);
            } else {
                it.delete(backup);
            }
            return true;
        }).orElse(false);
    }

    private void forceDeleteBackup(CleanableStorage storage, RecordMeta meta) {
        try {
            storage.delete(meta);
        } catch (StorageException se) {
            historyService.addAndNotify(new EventBuilder()
                    .formatTitle("DELETE [$s]", storage.toString())
                    .formatDescription("[$s] Backup of file $s with $s path and id of $s has been removed from " +
                                    "tracking but not storage due to an exception:\n", storage.toString(),
                            meta.getName(), meta.getPath(), meta.getId(), se.getMessage())
                    .setType(Event.EventType.WARNING)
                    .createEvent());
        } catch (RuntimeException e) {
            historyService.addAndNotify(new EventBuilder()
                    .formatTitle("DELETE [$s] UNKNOWN ERROR", storage.toString())
                    .formatDescription("[$s] Backup of file $s with $s path and id of $s has been removed from " +
                                    "tracking but not storage due to an unknown exception:\n", storage.toString(),
                            meta.getName(), meta.getPath(), meta.getId(), e.getMessage())
                    .setType(Event.EventType.WARNING)
                    .createEvent());
        }
    }
}
