package io.github.t3r1jj.fcms.backend.service;

import com.mongodb.client.MongoDatabase;
import io.github.t3r1jj.fcms.backend.model.Configuration;
import io.github.t3r1jj.fcms.backend.model.ExternalService;
import io.github.t3r1jj.fcms.backend.model.Health;
import io.github.t3r1jj.storapi.authenticated.AuthenticatedStorage;
import io.github.t3r1jj.storapi.data.StorageInfo;
import io.github.t3r1jj.storapi.data.exception.StorageException;
import org.bson.Document;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.math.BigInteger;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.testng.AssertJUnit.assertEquals;

public class HealthServiceTest {

    @Mock
    private MongoTemplate mongoTemplate;
    @Mock
    private ConfigurationService configurationService;
    @Mock
    private HistoryService historyService;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetHealth() {
        // DB size and limit
        MongoDatabase mongoDatabase = mock(MongoDatabase.class);
        Document dbSizeDocument = mock(Document.class);
        when(mongoTemplate.getDb()).thenReturn(mongoDatabase);
        when(mongoDatabase.runCommand(any())).thenReturn(dbSizeDocument);
        when(dbSizeDocument.get("dataSize")).thenReturn("1024");

        // Bandwidth
        List<Health.BandwidthSize> bandwidth = Collections.singletonList(new Health.BandwidthSize(BigInteger.ONE, BigInteger.TEN, Instant.now()));
        when(historyService.getBandwidthFromEvents()).thenReturn(bandwidth);

        // Storage info
        ExternalService[] services = new ExternalService[]{
                new ExternalService("primary included", true, true),
                new ExternalService("primary included auth", true, true),
                new ExternalService("primary included not enabled", true, false),
                new ExternalService("secondary not included", false, true),
                new ExternalService("primary wrong credentials", true, true),
        };
        AuthenticatedStorage okStorage = mock(AuthenticatedStorage.class);
        AuthenticatedStorage notOkStorage = mock(AuthenticatedStorage.class);
        when(okStorage.getInfo()).thenReturn(new StorageInfo("mocked", BigInteger.ONE, BigInteger.TEN));
        doThrow(new StorageException("mocked")).when(notOkStorage).login();
        StorageFactory storageFactory = spy(new StorageFactory());
        Configuration configuration = spy(storageFactory.getConfiguration());
        when(configuration.stream()).thenReturn(Stream.of(services));
        doReturn(okStorage).when(storageFactory).createAuthenticatedStorage(services[0].getName());
        doReturn(okStorage).when(storageFactory).createAuthenticatedStorage(services[1].getName());
        doReturn(okStorage).when(storageFactory).createAuthenticatedStorage(services[2].getName());
        doReturn(notOkStorage).when(storageFactory).createAuthenticatedStorage(services[4].getName());

        when(storageFactory.getConfiguration()).thenReturn(configuration);
        when(configurationService.createStorageFactory()).thenReturn(storageFactory);

        HealthService healthService = new HealthService(configurationService, historyService, mongoTemplate);
        Health health = healthService.getHealth();
        assertEquals(health.dbSize, "1024 MB");
        assertEquals(health.dbLimit, "unknown");
        assertEquals(health.bandwidth, bandwidth);
        assertEquals(health.storageQuotas.size(), 4);
        assertEquals(health.storageQuotas.get(3), new StorageInfo(services[4].getName(), BigInteger.valueOf(-1), BigInteger.valueOf(-1)));
    }
}