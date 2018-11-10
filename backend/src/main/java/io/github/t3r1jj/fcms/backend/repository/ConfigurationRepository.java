package io.github.t3r1jj.fcms.backend.repository;

import io.github.t3r1jj.fcms.backend.model.Configuration;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfigurationRepository extends MongoRepository<Configuration, String> {
}