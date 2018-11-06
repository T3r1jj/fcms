package io.github.t3r1jj.fcms.backend.service;

import io.github.t3r1jj.fcms.backend.model.Configuration;
import io.github.t3r1jj.fcms.backend.model.ExternalService;
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

    public Configuration getConfiguration() {
        Configuration configuration = configurationRepository.findById(Configuration.getDefaultId())
                .orElse(new StorageFactory().getConfiguration());
        return fixConfiguration(configuration);
    }

    /**
     * Fixes old configurations (when external module changes) and invalid ones (caused by invalid update from api)
     *
     * @param configuration to fix
     * @return valid configuration
     */
    private Configuration fixConfiguration(Configuration configuration) {
        Configuration validConfiguration = new StorageFactory().getConfiguration();
        ExternalService[] validServices = validConfiguration.getServices();
        ExternalService[] services = configuration.getServices();
        for (int i = 0; i < validServices.length; i++) {
            for (ExternalService service : services) {
                if (validServices[i].hashCode() == service.hashCode()) {
                    validServices[i] = service;
                }
            }
        }
        return validConfiguration;
    }

    public void update(Configuration configuration) {
        configurationRepository.save(configuration);
    }

    StorageFactory createStorageFactory() {
        return new StorageFactory(getConfiguration());
    }

    StorageFactory createStorageFactory(Configuration configuration) {
        return new StorageFactory(configuration);
    }
}