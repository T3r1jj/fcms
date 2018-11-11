package io.github.t3r1jj.fcms.backend.service;

import io.github.t3r1jj.fcms.backend.model.Configuration;
import io.github.t3r1jj.fcms.backend.model.ExternalService;
import io.github.t3r1jj.fcms.backend.repository.ConfigurationRepository;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

public class ConfigurationServiceTest {

    @Mock
    private ConfigurationRepository configurationRepository;
    private ConfigurationService configurationService;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        configurationService = new ConfigurationService(configurationRepository);
    }

    @Test
    public void testGetValidConfigurationDefault() {
        doReturn(Optional.empty()).when(configurationRepository).findById(anyString());

        Configuration defaultConfiguration = new StorageFactory().getConfiguration();
        assertEquals(configurationService.getConfiguration(), defaultConfiguration);
    }


    @Test
    public void testGetPartiallyInvalidConfiguration() {
        Configuration invalidConfiguration = new Configuration(new ExternalService[]{
                new ExternalService("Mega", true, true, new ExternalService.ApiKey("password", "key123"))
        });
        doReturn(Optional.of(invalidConfiguration)).when(configurationRepository).findById(anyString());

        Configuration defaultConfiguration = new StorageFactory().getConfiguration();
        Configuration testedConfiguration = configurationService.getConfiguration();
        assertNotEquals(testedConfiguration, invalidConfiguration);
        assertEquals(testedConfiguration, defaultConfiguration);
    }

    @Test
    public void testGetPartiallyInvalidConfigurationIsMerged() {
        String password = "pwd123";
        String userName = "userName123";
        Configuration invalidConfiguration = new Configuration(new ExternalService[]{
                new ExternalService("Mega", true, true
                        , new ExternalService.ApiKey("userName", userName)
                        , new ExternalService.ApiKey("password", password))
        });
        doReturn(Optional.of(invalidConfiguration)).when(configurationRepository).findById(anyString());

        Configuration defaultConfiguration = new StorageFactory().getConfiguration();
        Configuration testedConfiguration = configurationService.getConfiguration();
        assertNotEquals(testedConfiguration, invalidConfiguration);
        assertNotEquals(testedConfiguration, defaultConfiguration);

        ExternalService service = Stream.of(testedConfiguration.getServices()).filter(s -> s.getName().equals("Mega")).findAny().orElseThrow(() -> new RuntimeException("Service not found"));
        assertThat(service.getApiKeys(), hasItemInArray(hasProperty("label", is("password"))));
        assertThat(service.getApiKeys(), hasItemInArray(hasProperty("value", is(password))));
        assertThat(service.getApiKeys(), hasItemInArray(hasProperty("label", is("userName"))));
        assertThat(service.getApiKeys(), hasItemInArray(hasProperty("value", is(userName))));
    }

    @Test
    public void testGetFullyInvalidConfiguration() {
        String invalidServiceName = "Mocked service name";
        Configuration invalidConfiguration = new Configuration(new ExternalService[]{
                new ExternalService(invalidServiceName, true, true, new ExternalService.ApiKey("label123", "key123"))
        });
        doReturn(Optional.of(invalidConfiguration)).when(configurationRepository).findById(anyString());

        Configuration defaultConfiguration = new StorageFactory().getConfiguration();
        Configuration testedConfiguration = configurationService.getConfiguration();
        assertNotEquals(testedConfiguration, invalidConfiguration);
        assertEquals(testedConfiguration, defaultConfiguration);
    }

    @Test
    public void testGetFullyInvalidConfigurationMergedWithValid() {
        String validServiceName = "Mocked service name";
        Configuration invalidConfiguration = new Configuration(new ExternalService[]{
                new ExternalService(validServiceName, true, true, new ExternalService.ApiKey("label123", "key123"))
        });
        doReturn(Optional.of(invalidConfiguration)).when(configurationRepository).findById(anyString());

        Configuration defaultConfiguration = new StorageFactory().getConfiguration();
        Configuration testedConfiguration = configurationService.getConfiguration();
        assertNotEquals(testedConfiguration, invalidConfiguration);
        assertEquals(testedConfiguration, defaultConfiguration);
    }
}