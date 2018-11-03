package io.github.t3r1jj.fcms.backend.service;

import io.github.t3r1jj.fcms.backend.controller.RecordController;
import io.github.t3r1jj.fcms.backend.model.StoredRecord;
import io.github.t3r1jj.fcms.backend.repository.StoredRecordRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class RecordService {
    private final StoredRecordRepository recordRepository;
    private final ReplicationService replicationService;

    @Autowired
    public RecordService(StoredRecordRepository recordRepository, ReplicationService replicationService) {
        this.recordRepository = recordRepository;
        this.replicationService = replicationService;
    }

    public void store(StoredRecord recordToStore, String parentId) {
        StoredRecord parent = recordRepository.findById(stringToObjectId(parentId))
                .orElseThrow(() -> new RecordController.ResourceNotFoundException(String.format("Parent with %s id not found", parentId)));
        store(recordToStore);
        parent.getVersions().add(recordToStore);
        recordRepository.save(parent);
    }

    ObjectId stringToObjectId(String parent) {
        return new ObjectId(parent);
    }

    public void store(StoredRecord recordToStore) {
        replicationService.replicateToPrimary(recordToStore);
    }

    public Collection<StoredRecord> findAll() {
        return recordRepository.findAll();
    }

    public void delete(String id) {
        StoredRecord storedRecord = recordRepository.findById(stringToObjectId(id))
                .orElseThrow(() -> new RecordController.ResourceNotFoundException(String.format("Record with %s id not found", id)));
        replicationService.removeAllReplicas(storedRecord);
        recordRepository.deleteById(storedRecord.getId());
    }
}
