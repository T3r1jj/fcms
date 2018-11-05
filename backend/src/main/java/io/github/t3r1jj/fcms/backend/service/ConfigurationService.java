package io.github.t3r1jj.fcms.backend.service;

import io.github.t3r1jj.fcms.backend.model.Configuration;
import io.github.t3r1jj.fcms.backend.model.ExternalService;
import io.github.t3r1jj.fcms.backend.repository.ConfigurationRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;

@Service
public class ConfigurationService {

    private final ConfigurationRepository configurationRepository;

    @Autowired
    public ConfigurationService(ConfigurationRepository configurationRepository) {
        this.configurationRepository = configurationRepository;
    }

    public Configuration getConfiguration() {
        configurationRepository.findAll().stream().flatMap(a -> Stream.of(a.getApiKeys())).forEach(a -> System.out.println(a.getName()));
        return configurationRepository.findById(Configuration.getDefaultId())
                .orElse(new Configuration(new ExternalService[]{new ExternalService("In memory service", false)}));
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