package io.github.t3r1jj.fcms.backend.service;

import io.github.t3r1jj.fcms.backend.model.Configuration;
import io.github.t3r1jj.fcms.backend.model.ExternalService;
import io.github.t3r1jj.storapi.Storage;
import io.github.t3r1jj.storapi.upstream.CleanableStorage;
import org.testng.annotations.Test;

import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.testng.Assert.assertTrue;

public class StorageFactoryTest {
    private final StorageFactory factory = new StorageFactory();

    @Test
    public void createWithDefaultConfig() {
        Configuration configuration = factory.getConfiguration();
        assertTrue(configuration.getServices().length > 0);
        assertThat(configuration.getServices().length, is(greaterThan(0)));
        assertThat(configuration.getServices(), hasItemInArray(hasProperty("name", is("Dropbox"))));
        assertThat(configuration.getServices(), hasItemInArray(hasProperty("name", is("Put"))));
        assertThat(configuration.getServices(), hasItemInArray(hasProperty("name", is("Megaupload"))));
        assertThat(configuration.getServices(), hasItemInArray(hasProperty("name", is("AnonFile"))));
    }

    @Test
    public void createWithDefaultConfigHasMegaWithNamedParameter() {
        Configuration configuration = factory.getConfiguration();
        ExternalService service = Stream.of(configuration.getServices()).filter(s -> s.getName().equals("Mega")).findAny().orElseThrow(() -> new RuntimeException("Service not found"));
        assertThat(service.getApiKeys(), hasItemInArray(hasProperty("label", is("password"))));
    }

    @Test
    public void createAuthenticatedStorage() {
        Storage storage = factory.createAuthenticatedStorage("Mega");
        assertThat(storage, is(notNullValue()));
        assertThat(storage.toString(), is("Mega"));
    }

    @Test(expectedExceptions = {RuntimeException.class})
    public void createUnknownAuthenticatedStorage() {
        factory.createAuthenticatedStorage("DAKSJIKA)@(!");
    }

    @Test(expectedExceptions = {RuntimeException.class})
    public void createKnownUnAuthenticatedStorage() {
        factory.createAuthenticatedStorage("Put");
    }

    @Test
    public void createCleanableStorageMega() {
        Optional<CleanableStorage> storage = factory.createCleanableStorage("Mega");
        assertThat(storage.isPresent(), is(true));
        assertThat(storage.get().toString(), is("Mega"));
    }

    @Test
    public void createCleanableStoragePut() {
        Optional<CleanableStorage> storage = factory.createCleanableStorage("Put");
        assertThat(storage.isPresent(), is(true));
        assertThat(storage.get().toString(), is("Put"));
    }

    @Test
    public void createNotCleanableStorageMegaupload() {
        Optional<CleanableStorage> storage = factory.createCleanableStorage("Megaupload");
        assertThat(storage.isPresent(), is(false));
    }

    @Test
    public void createNotCleanableStorageUnknown() {
        Optional<CleanableStorage> storage = factory.createCleanableStorage("DAKSJIKA)@(!");
        assertThat(storage.isPresent(), is(false));
    }

    @Test(expectedExceptions = {RuntimeException.class})
    public void createUpstreamStorageIgnoreAuthenticatedStorageMega() {
        factory.createUpstreamOnlyStorage("Mega");
    }

    @Test(expectedExceptions = {RuntimeException.class})
    public void createUpstreamStorageIgnoreAuthenticatedStorageGoogleDrive() {
        factory.createUpstreamOnlyStorage("GoogleDrive");
    }

    @Test(expectedExceptions = {RuntimeException.class})
    public void createUpstreamOnlyMegaNot() {
        factory.createUpstreamStorage(new ExternalService("Mega", false, false, new ExternalService.ApiKey("login", "a"), new ExternalService.ApiKey("password", "a")));
    }

    @Test
    public void createUpstreamMega() {
        factory.createUpstreamStorage(new ExternalService("Mega", true, false, new ExternalService.ApiKey("login", "a"), new ExternalService.ApiKey("password", "a")));
    }

    @Test
    public void createUpstreamOnlyPut() {
        factory.createUpstreamStorage(new ExternalService("Put", false, false));
    }

    @Test(expectedExceptions = {RuntimeException.class})
    public void createUpstreamPutPrimaryNotAuthenticated() {
        factory.createUpstreamStorage(new ExternalService("Put", true, false));
    }

}
