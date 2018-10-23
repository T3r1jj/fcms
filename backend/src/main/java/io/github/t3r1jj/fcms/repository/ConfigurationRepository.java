package io.github.t3r1jj.fcms.repository;

import io.github.t3r1jj.fcms.model.Configuration;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfigurationRepository {
    Configuration get();

    void update(Configuration configuration);
}