package io.github.t3r1jj.fcms.backend.repository;

import io.github.t3r1jj.fcms.backend.model.StoredRecordMeta;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface StoredRecordMetaRepository extends MongoRepository<StoredRecordMeta, ObjectId> {
}
