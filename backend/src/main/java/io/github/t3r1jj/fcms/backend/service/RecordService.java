package io.github.t3r1jj.fcms.backend.service;

import io.github.t3r1jj.fcms.backend.controller.exception.ResourceNotFoundException;
import io.github.t3r1jj.fcms.backend.model.StoredRecord;
import io.github.t3r1jj.fcms.backend.model.StoredRecordMeta;
import io.github.t3r1jj.fcms.backend.repository.StoredRecordMetaRepository;
import io.github.t3r1jj.fcms.backend.repository.StoredRecordRepository;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.ListIterator;
import java.util.Objects;

@Service
public class RecordService {
    private final StoredRecordRepository recordRepository;
    private final StoredRecordMetaRepository metaRepository;
    private final ReplicationService replicationService;

    @Autowired
    public RecordService(StoredRecordRepository recordRepository, StoredRecordMetaRepository metaRepository, @Lazy ReplicationService replicationService) {
        this.recordRepository = recordRepository;
        this.metaRepository = metaRepository;
        this.replicationService = replicationService;
    }

    /**
     * @param recordToStore with rootId pointing to the parent (after store it will be associated with root)
     */
    public void store(StoredRecord recordToStore) {
        StoredRecord parentRecord = recordToStore.getRootId().map(parentId -> {
            StoredRecord parent = getRootOrNode(parentId);
            parent.getVersions().add(recordToStore);
            parent.getRootId().ifPresent(recordToStore::setRootId);
            return parent;
        }).orElse(recordToStore);
        replicationService.uploadToPrimary(recordToStore);
        metaRepository.save(recordToStore.getMeta());
        if (!recordToStore.getRootId().isPresent() || parentRecord.getId().equals(recordToStore.getRootId().get())) {
            recordRepository.save(parentRecord);
        } else {
            update(parentRecord);
        }
    }

    @NotNull
    private StoredRecord getRootOrNode(ObjectId parentId) {
        try {
            return getNode(parentId.toString());
        } catch (ResourceNotFoundException notFoundEx) {
            return getRoot(parentId);
        }
    }

    public Collection<StoredRecord> findAll() {
        return recordRepository.findAll();
    }

    public void delete(String id, boolean force) {
        StoredRecord storedRecord = getNode(id);
        replicationService.deleteCascading(storedRecord, force, getRoot(storedRecord));
        recordRepository.deleteById(storedRecord.getId());
        metaRepository.deleteById(storedRecord.getMeta().getId());
    }

    @NotNull
    private StoredRecord getNode(String id) {
        return recordRepository.findAll()
                .stream()
                .map(r -> r.findInTree(StoredRecord.stringToObjectId(id)))
                .filter(Objects::nonNull)
                .findAny()
                .orElseThrow(() -> new ResourceNotFoundException(String.format("RecordMeta with %s id not found", id)));
    }

    @NotNull
    private StoredRecord getRoot(ObjectId id) {
        return recordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("RecordMeta with %s id not found", id)));
    }

    @NotNull
    private StoredRecordMeta getOneRecordMeta(ObjectId id) {
        return metaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("RecordMeta with %s id not found", id)));
    }

    /**
     * @param storedRecord to update. If root - updates immediately, if not - searches through root tree and swaps on correct id before update.
     */
    void update(StoredRecord storedRecord) {
        if (storedRecord.getRootId().isPresent()) {
            StoredRecord root = getRoot(storedRecord.getRootId().get());
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
        return storedRecord.getRootId().map(this::getRoot).orElse(storedRecord);
    }

    public void updateMeta(StoredRecordMeta recordMeta) {
        getOneRecordMeta(recordMeta.getId());
        metaRepository.save(recordMeta);
    }
}
