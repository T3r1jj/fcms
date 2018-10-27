package io.github.t3r1jj.fcms.backend.repository;

import io.github.t3r1jj.fcms.backend.model.Configuration;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfigurationRepository {
    Configuration get();

    void update(Configuration configuration);
}