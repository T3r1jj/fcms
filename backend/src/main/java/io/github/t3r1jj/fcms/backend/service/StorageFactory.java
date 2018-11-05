package io.github.t3r1jj.fcms.backend.service;

import io.github.t3r1jj.fcms.backend.model.Configuration;
import io.github.t3r1jj.fcms.external.authorized.Storage;
import io.github.t3r1jj.fcms.external.upstream.CleanableStorage;

import java.util.Optional;

class StorageFactory {
    private final Configuration configuration;

    StorageFactory(Configuration configuration) {
        this.configuration = configuration;
    }

    Storage create(String externalService) {
        return null;
    }

    Optional<CleanableStorage> createCleanable(String externalService) {
        return Optional.empty();
    }
}
