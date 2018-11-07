package io.github.t3r1jj.fcms.backend.service;

import io.github.t3r1jj.fcms.backend.model.Configuration;
import io.github.t3r1jj.fcms.backend.model.ExternalService;
import io.github.t3r1jj.fcms.external.Storage;
import io.github.t3r1jj.fcms.external.upstream.CleanableStorage;
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
        StorageFactory factory = new StorageFactory();
        Configuration configuration = factory.getConfiguration();
        ExternalService service = Stream.of(configuration.getServices()).filter(s -> s.getName().equals("Mega")).findAny().orElseThrow(() -> new RuntimeException("Service not found"));
        assertThat(service.getApiKeys(), hasItemInArray(hasProperty("label", is("password"))));
    }

    @Test
    public void createAuthenticatedStorage() {
        StorageFactory factory = new StorageFactory();
        Storage storage = factory.createAuthenticatedStorage("Mega");
        assertThat(storage, is(notNullValue()));
        assertThat(storage.toString(), is("Mega"));
    }

    @Test(expectedExceptions = {RuntimeException.class})
    public void createUnknownAuthenticatedStorage() {
        StorageFactory factory = new StorageFactory();
        factory.createAuthenticatedStorage("DAKSJIKA)@(!");
    }

    @Test(expectedExceptions = {RuntimeException.class})
    public void createKnownUnAuthenticatedStorage() {
        StorageFactory factory = new StorageFactory();
        factory.createAuthenticatedStorage("Put");
    }

    @Test
    public void createCleanableStorageMega() {
        StorageFactory factory = new StorageFactory();
        Optional<CleanableStorage> storage = factory.createCleanableStorage("Mega");
        assertThat(storage.isPresent(), is(true));
        assertThat(storage.get().toString(), is("Mega"));
    }

    @Test
    public void createCleanableStoragePut() {
        StorageFactory factory = new StorageFactory();
        Optional<CleanableStorage> storage = factory.createCleanableStorage("Put");
        assertThat(storage.isPresent(), is(true));
        assertThat(storage.get().toString(), is("Put"));
    }

    @Test
    public void createNotCleanableStorageMegaupload() {
        StorageFactory factory = new StorageFactory();
        Optional<CleanableStorage> storage = factory.createCleanableStorage("Megaupload");
        assertThat(storage.isPresent(), is(false));
    }

    @Test
    public void createNotCleanableStorageUnknown() {
        StorageFactory factory = new StorageFactory();
        Optional<CleanableStorage> storage = factory.createCleanableStorage("DAKSJIKA)@(!");
        assertThat(storage.isPresent(), is(false));
    }

    @Test(expectedExceptions = {RuntimeException.class})
    public void createUpstreamStorageIgnoreAuthenticatedStorageMega() {
        StorageFactory factory = new StorageFactory();
        factory.createUpstreamService("Mega");
    }

    @Test(expectedExceptions = {RuntimeException.class})
    public void createUpstreamStorageIgnoreAuthenticatedStorageGoogleDrive() {
        StorageFactory factory = new StorageFactory();
        factory.createUpstreamService("GoogleDrive");
    }

}
