package io.github.t3r1jj.fcms.backend.controller;

import com.mongodb.client.MongoDatabase;
import io.github.t3r1jj.fcms.backend.model.ExternalService;
import io.github.t3r1jj.fcms.backend.model.Health;
import io.github.t3r1jj.fcms.backend.service.ConfigurationService;
import io.github.t3r1jj.fcms.backend.service.HistoryService;
import io.github.t3r1jj.fcms.backend.service.StorageFactory;
import io.github.t3r1jj.storapi.authenticated.AuthenticatedStorage;
import io.github.t3r1jj.storapi.data.StorageInfo;
import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/health")
public class HealthController {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private final ConfigurationService configurationService;
    private final HistoryService historyService;
    private final MongoTemplate mongoTemplate;

    @Autowired
    public HealthController(ConfigurationService configurationService, HistoryService historyService, MongoTemplate mongoTemplate) {
        this.configurationService = configurationService;
        this.historyService = historyService;
        this.mongoTemplate = mongoTemplate;
    }

    @GetMapping
    public Health getHealth() {
        Health health = new Health();
        health.bandwidth = historyService.getBandwidthFromEvents();
        health.storageQuotas = new ArrayList<>();
        health.dbLimit = "unknown";
        health.dbSize = getDbSize();
        fillStorageQuota(health.storageQuotas);
        return health;
    }

    private String getDbSize() {
        MongoDatabase db = mongoTemplate.getDb();
        Document document = db.runCommand(new BsonDocument("dbStats", new BsonInt32(1)).append("scale",
                new BsonInt32(1024 * 1024)));
        return Double.valueOf(document.get("dataSize").toString()).longValue() + " MB";
    }

    private void fillStorageQuota(List<StorageInfo> storageQuotas) {
        StorageFactory storageFactory = configurationService.createStorageFactory();
        storageFactory.getConfiguration().stream()
                .filter(ExternalService::isPrimary)
                .forEach(s -> {
                    try {
                        AuthenticatedStorage authenticatedStorage = storageFactory.createAuthenticatedStorage(s.getName());
                        authenticatedStorage.login();
                        storageQuotas.add(authenticatedStorage.getInfo());
                        authenticatedStorage.logout();
                    } catch (Exception e) {
                        logger.debug("Could not get StorageInfo for " + s.getName(), e);
                    }
                });
    }
}
