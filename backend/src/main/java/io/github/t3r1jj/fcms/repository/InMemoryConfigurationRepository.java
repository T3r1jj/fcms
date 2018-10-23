package io.github.t3r1jj.fcms.repository;

import io.github.t3r1jj.fcms.model.Configuration;
import io.github.t3r1jj.fcms.model.ExternalService;
import org.springframework.stereotype.Repository;

@Repository
class InMemoryConfigurationRepository implements ConfigurationRepository {

    private Configuration configuration = new Configuration(new ExternalService[]{new ExternalService("In memory service", false)});

    @Override
    public Configuration get() {
        return configuration;
    }

    @Override
    public void update(Configuration configuration) {
        this.configuration = configuration;
    }
}