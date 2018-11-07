package io.github.t3r1jj.fcms.backend.service;

import io.github.t3r1jj.fcms.backend.model.Configuration;
import io.github.t3r1jj.fcms.backend.repository.ConfigurationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConfigurationService {

    private final ConfigurationRepository configurationRepository;

    @Autowired
    public ConfigurationService(ConfigurationRepository configurationRepository) {
        this.configurationRepository = configurationRepository;
    }

    public Configuration getFixedConfiguration() {
        Configuration configuration = getConfiguration();
        Configuration validConfiguration = new StorageFactory().getConfiguration();
        validConfiguration.merge(configuration);
        return validConfiguration;
    }

    private Configuration getConfiguration() {
        return configurationRepository.findById(Configuration.getDefaultId())
                .orElse(new StorageFactory().getConfiguration());
    }

    public void update(Configuration configuration) {
        configurationRepository.save(configuration);
    }

    StorageFactory createStorageFactory() {
        return new StorageFactory(getFixedConfiguration());
    }

    StorageFactory createStorageFactory(Configuration configuration) {
        return new StorageFactory(configuration);
    }
}