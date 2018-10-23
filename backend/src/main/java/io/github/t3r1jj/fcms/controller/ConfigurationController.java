package io.github.t3r1jj.fcms.controller;

import io.github.t3r1jj.fcms.model.Configuration;
import io.github.t3r1jj.fcms.service.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/configuration")
class ConfigurationController {
    private final ConfigurationService configurationService;

    @Autowired
    ConfigurationController(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    @GetMapping
    Configuration getConfiguration() {
        return configurationService.get();
    }

    @PostMapping
    void updateConfiguration(@RequestBody Configuration configuration) {
        configurationService.update(configuration);
    }
}