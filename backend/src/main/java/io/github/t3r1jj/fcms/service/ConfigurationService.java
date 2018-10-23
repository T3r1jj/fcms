package io.github.t3r1jj.fcms.service;

import io.github.t3r1jj.fcms.model.Configuration;
import io.github.t3r1jj.fcms.repository.ConfigurationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConfigurationService implements ConfigurationRepository {

    private final ConfigurationRepository configurationRepository;

    @Autowired
    public ConfigurationService(ConfigurationRepository configurationRepository) {
        this.configurationRepository = configurationRepository;
    }

    @Override
    public Configuration get() {
        return configurationRepository.get();
    }

    @Override
    public void update(Configuration configuration) {
        configurationRepository.update(configuration);
    }
}
