package io.github.t3r1jj.fcms.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.t3r1jj.fcms.model.Configuration;
import io.github.t3r1jj.fcms.model.ExternalService;

@RestController
@RequestMapping("/api/configuration")
class ConfigurationController {
    @GetMapping
    Configuration getConfiguration() {
        return new Configuration(new ExternalService[] { new ExternalService("Mocked service name", true) });
    }
}