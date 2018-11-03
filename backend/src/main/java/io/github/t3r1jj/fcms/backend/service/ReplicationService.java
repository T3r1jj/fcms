package io.github.t3r1jj.fcms.backend.service;

import io.github.t3r1jj.fcms.backend.model.StoredRecord;
import org.springframework.stereotype.Service;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

@Service
public class ReplicationService {
    public void replicateToPrimary(StoredRecord recordToStore) {
        throw new NotImplementedException();
    }

    public void removeAllReplicas(StoredRecord storedRecord) {
        throw new NotImplementedException();
    }
}
