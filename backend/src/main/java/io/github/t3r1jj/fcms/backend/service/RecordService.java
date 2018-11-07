package io.github.t3r1jj.fcms.backend.service;

import io.github.t3r1jj.fcms.backend.controller.RecordController;
import io.github.t3r1jj.fcms.backend.model.StoredRecord;
import io.github.t3r1jj.fcms.backend.repository.StoredRecordRepository;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.ListIterator;

import static io.github.t3r1jj.fcms.backend.model.StoredRecord.stringToObjectId;

@Service
public class RecordService {
    private final StoredRecordRepository recordRepository;
    private final ReplicationService replicationService;

    @Autowired
    public RecordService(StoredRecordRepository recordRepository, @Lazy ReplicationService replicationService) {
        this.recordRepository = recordRepository;
        this.replicationService = replicationService;
    }

    public void store(StoredRecord recordToStore) {
        StoredRecord rootRecord = recordToStore.getRootId().map(parentId -> {
            StoredRecord parent = getOne(parentId);
            parent.getVersions().add(recordToStore);
            parent.getRootId().ifPresent(recordToStore::setRootId);
            return parent;
        }).orElse(recordToStore);
        replicationService.uploadToPrimary(recordToStore);
        recordRepository.save(rootRecord);
    }

    public Collection<StoredRecord> findAll() {
        return recordRepository.findAll();
    }

    public void delete(String id) {
        StoredRecord storedRecord = getOne(id);
        replicationService.deleteCascading(storedRecord, false, getRoot(storedRecord));
        recordRepository.deleteById(storedRecord.getId());
    }

    public void forceDelete(String id) {
        StoredRecord storedRecord = getOne(id);
        replicationService.deleteCascading(storedRecord, true, getRoot(storedRecord));
        recordRepository.deleteById(storedRecord.getId());
    }

    @NotNull
    private StoredRecord getOne(String id) {
        return getOne(stringToObjectId(id));
    }

    @NotNull
    private StoredRecord getOne(ObjectId id) {
        return recordRepository.findById(id)
                .orElseThrow(() -> new RecordController.ResourceNotFoundException(String.format("Record with %s id not found", id)));
    }

    /**
     * @param storedRecord to update. If root - updates immediately, if not - searches through root tree and swaps on correct id before update.
     */
    void update(StoredRecord storedRecord) {
        if (storedRecord.getRootId().isPresent()) {
            StoredRecord root = getOne(storedRecord.getRootId().get());
            swapVersions(root, storedRecord);
            recordRepository.save(root);
        } else {
            recordRepository.save(storedRecord);
        }
    }

    private void swapVersions(StoredRecord parent, StoredRecord recordToSwap) {
        ListIterator<StoredRecord> versionIterator = parent.getVersions().listIterator();
        while (versionIterator.hasNext()) {
            StoredRecord version = versionIterator.next();
            if (version.getId().equals(recordToSwap.getId())) {
                versionIterator.remove();
                versionIterator.add(recordToSwap);
                break;
            }
            swapVersions(version, recordToSwap);
        }
    }

    private StoredRecord getRoot(StoredRecord storedRecord) {
        return storedRecord.getRootId().map(this::getOne).orElse(storedRecord);
    }

    public void updateDescription(String id, String description) {
        StoredRecord record = getOne(id);
        record.setDescription(description);
        update(record);
    }
}
