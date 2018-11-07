package io.github.t3r1jj.fcms.backend.service;

import io.github.t3r1jj.fcms.backend.controller.RecordController;
import io.github.t3r1jj.fcms.backend.model.Configuration;
import io.github.t3r1jj.fcms.backend.model.Event;
import io.github.t3r1jj.fcms.backend.model.ExternalService;
import io.github.t3r1jj.fcms.backend.model.StoredRecord;
import io.github.t3r1jj.fcms.external.authenticated.AuthenticatedStorage;
import io.github.t3r1jj.fcms.external.data.Record;
import io.github.t3r1jj.fcms.external.data.RecordMeta;
import io.github.t3r1jj.fcms.external.data.exception.StorageException;
import io.github.t3r1jj.fcms.external.data.exception.StorageUnauthenticatedException;
import io.github.t3r1jj.fcms.external.upstream.CleanableStorage;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class ReplicationServiceTest {

    @Mock
    private ConfigurationService configurationService;
    @Mock
    private RecordService recordService;
    @Mock
    private HistoryService historyService;
    @Mock
    private CleanableStorage cleanableStorage;
    @Mock
    private AuthenticatedStorage storage;
    @Mock
    private StorageFactory storageFactory;
    private ReplicationService replicationService;

    private Configuration configuration;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        replicationService = new ReplicationService(configurationService, recordService, historyService);
    }

    private void setUpDefaultConfig(String serviceName, boolean enabled) {
        setUpDefaultConfig(serviceName, enabled, true);
    }

    private void setUpDefaultConfig(String serviceName, boolean enabled, boolean primary) {
        configuration = new Configuration(new ExternalService[]{
                new ExternalService(serviceName, primary, enabled, new ExternalService.ApiKey("label123", "key123"))});
    }

    @Test
    public void testUploadToPrimary() {
        String serviceName = "service name";
        setUpDefaultConfig(serviceName, true);
        doReturn(storage).when(storageFactory).createAuthenticatedStorage(serviceName);
        doReturn(storageFactory).when(configurationService).createStorageFactory(configuration);
        doReturn(configuration).when(configurationService).getConfiguration();

        byte[] data = "some text".getBytes();
        StoredRecord recordToStore = new StoredRecord("1", "1", data, null);
        replicationService.uploadToPrimary(recordToStore);

        verify(storage).login();
        verify(storage).upload(new Record(recordToStore.getName(), recordToStore.getId().toString(), new ByteArrayInputStream(data)));
        verify(storage).logout();
    }

    @Test
    public void testReplicateToPrimary() {
        String serviceName = "service name";
        setUpDefaultConfig(serviceName, true, true);
        configuration.setSecondaryBackupCount(1);
        doReturn(storage).when(storageFactory).createAuthenticatedStorage(serviceName);
        doReturn(storageFactory).when(configurationService).createStorageFactory(configuration);
        doReturn(configuration).when(configurationService).getConfiguration();

        byte[] data = "some text".getBytes();
        StoredRecord recordToStore = new StoredRecord("1", "1", data, null);
        boolean replicated = replicationService.replicateToPrimary(recordToStore);
        assertTrue(replicated);
        verify(storage).upload(new Record(recordToStore.getName(), recordToStore.getId().toString(), new ByteArrayInputStream(data)));
    }

    @Test(expectedExceptions = {RuntimeException.class})
    public void testReplicateToPrimaryFailsOnEmptyData() {
        String serviceName = "service name";
        setUpDefaultConfig(serviceName, true, true);
        configuration.setSecondaryBackupCount(1);
        doReturn(storage).when(storageFactory).createAuthenticatedStorage(serviceName);
        doReturn(storageFactory).when(configurationService).createStorageFactory(configuration);
        doReturn(configuration).when(configurationService).getConfiguration();

        byte[] data = "".getBytes();
        StoredRecord recordToStore = new StoredRecord("1", "1", data, null);
        replicationService.replicateToPrimary(recordToStore);
    }

    @Test(expectedExceptions = {RuntimeException.class})
    public void testReplicateToPrimaryFailsOnNullData() {
        String serviceName = "service name";
        setUpDefaultConfig(serviceName, true, true);
        configuration.setSecondaryBackupCount(1);
        doReturn(storage).when(storageFactory).createAuthenticatedStorage(serviceName);
        doReturn(storageFactory).when(configurationService).createStorageFactory(configuration);
        doReturn(configuration).when(configurationService).getConfiguration();

        byte[] data = null;
        StoredRecord recordToStore = new StoredRecord("1", "1", data, null);
        replicationService.replicateToPrimary(recordToStore);
    }


    @Test
    public void testReplicateToPrimaryLimitedByServiceNumber() {
        String serviceName = "service name";
        setUpDefaultConfig(serviceName, true, false);
        doReturn(storage).when(storageFactory).createAuthenticatedStorage(serviceName);
        doReturn(storageFactory).when(configurationService).createStorageFactory(configuration);
        doReturn(configuration).when(configurationService).getConfiguration();

        byte[] data = "some text".getBytes();
        StoredRecord recordToStore = new StoredRecord("1", "1", data, null);
        recordToStore.getBackups().put(serviceName, null);
        boolean replicated = replicationService.replicateToPrimary(recordToStore);
        assertFalse(replicated);
        verifyNoMoreInteractions(storage);
    }

    @Test
    public void testReplicateToSecondary() {
        String serviceName = "service name";
        setUpDefaultConfig(serviceName, true, false);
        doReturn(storage).when(storageFactory).createUpstreamService(serviceName);
        doReturn(storageFactory).when(configurationService).createStorageFactory(configuration);
        doReturn(configuration).when(configurationService).getConfiguration();

        byte[] data = "some text".getBytes();
        StoredRecord recordToStore = new StoredRecord("1", "1", data, null);
        boolean replicated = replicationService.replicateToSecondary(recordToStore);
        assertTrue(replicated);
        verify(storage).upload(new Record(recordToStore.getName(), recordToStore.getId().toString(), new ByteArrayInputStream(data)));
    }

    @Test(expectedExceptions = {RuntimeException.class})
    public void testReplicateToSecondaryFailsOnEmptyData() {
        String serviceName = "service name";
        setUpDefaultConfig(serviceName, true, false);
        doReturn(storage).when(storageFactory).createUpstreamService(serviceName);
        doReturn(storageFactory).when(configurationService).createStorageFactory(configuration);
        doReturn(configuration).when(configurationService).getConfiguration();

        byte[] data = "".getBytes();
        StoredRecord recordToStore = new StoredRecord("1", "1", data, null);
        replicationService.replicateToSecondary(recordToStore);
    }

    @Test(expectedExceptions = {RuntimeException.class})
    public void testReplicateToSecondaryFailsOnNullData() {
        String serviceName = "service name";
        setUpDefaultConfig(serviceName, true, false);
        doReturn(storage).when(storageFactory).createUpstreamService(serviceName);
        doReturn(storageFactory).when(configurationService).createStorageFactory(configuration);
        doReturn(configuration).when(configurationService).getConfiguration();

        byte[] data = null;
        StoredRecord recordToStore = new StoredRecord("1", "1", data, null);
        replicationService.replicateToSecondary(recordToStore);
    }

    @Test
    public void testReplicateToSecondaryLimitedByServiceNumber() {
        String serviceName = "service name";
        setUpDefaultConfig(serviceName, true, false);
        doReturn(storage).when(storageFactory).createUpstreamService(serviceName);
        doReturn(storageFactory).when(configurationService).createStorageFactory(configuration);
        doReturn(configuration).when(configurationService).getConfiguration();

        byte[] data = "some text".getBytes();
        StoredRecord recordToStore = new StoredRecord("1", "1", data, null);
        recordToStore.getBackups().put(serviceName, null);
        boolean replicated = replicationService.replicateToSecondary(recordToStore);
        assertFalse(replicated);
        verifyNoMoreInteractions(storage);
    }

    @Test(expectedExceptions = {RecordController.ResourceNotFoundException.class})
    public void testUploadToPrimaryConfigNotFound() {
        String serviceName = "service name";
        Configuration configuration = new Configuration(new ExternalService[]{});
        doReturn(storage).when(storageFactory).createAuthenticatedStorage(serviceName);
        doReturn(storageFactory).when(configurationService).createStorageFactory(configuration);
        doReturn(configuration).when(configurationService).getConfiguration();

        byte[] data = "sine text".getBytes();
        StoredRecord recordToStore = new StoredRecord("1", "1", data, null);
        replicationService.uploadToPrimary(recordToStore);
    }

    @Test(expectedExceptions = {RecordController.ResourceNotFoundException.class})
    public void testUploadToPrimaryConfigNotFoundEnabled() {
        String serviceName = "service name";
        setUpDefaultConfig(serviceName, false);
        doReturn(storage).when(storageFactory).createAuthenticatedStorage(serviceName);
        doReturn(storageFactory).when(configurationService).createStorageFactory(configuration);
        doReturn(configuration).when(configurationService).getConfiguration();

        byte[] data = "sine text".getBytes();
        StoredRecord recordToStore = new StoredRecord("1", "1", data, null);
        replicationService.uploadToPrimary(recordToStore);
    }

    @Test
    public void testDeleteCascading() {
        StoredRecord parentRecord = new StoredRecord("1", "1");
        StoredRecord childRecord = new StoredRecord("22", "22", null, parentRecord.getId().toString());
        StoredRecord grandchildRecord = new StoredRecord("333", "333", null, parentRecord.getId().toString());
        parentRecord.getVersions().add(childRecord);
        grandchildRecord.getVersions().add(grandchildRecord);

        replicationService.deleteCascading(childRecord, false, parentRecord);
        assertTrue(childRecord.getVersions().isEmpty(), "Grandchild removed");
        assertTrue(parentRecord.getVersions().isEmpty(), "Child removed");
    }

    @Test
    public void testDeleteCascadingWithBackups() {
        when(configurationService.createStorageFactory()).thenReturn(new StorageFactory(null));

        StoredRecord parentRecord = new StoredRecord("1", "1");
        StoredRecord childRecord = new StoredRecord("22", "22", null, parentRecord.getId().toString());
        parentRecord.getVersions().add(childRecord);
        childRecord.getBackups().put("service", new RecordMeta("a", "b", 4));

        replicationService.deleteCascading(childRecord, false, parentRecord);
        assertTrue(childRecord.getBackups().isEmpty(), "Backups removed");
        verify(historyService).addAndNotify(isA(Event.class));
    }

    @Test(expectedExceptions = {StorageException.class})
    public void testDeleteCascadingWithBackupsWithStorageException() {
        doThrow(new StorageException("Mocked storage exception")).when(cleanableStorage).delete(any());
        doReturn(Optional.of(cleanableStorage)).when(storageFactory).createCleanableStorage(any());
        doReturn(storageFactory).when(configurationService).createStorageFactory();

        StoredRecord parentRecord = new StoredRecord("1", "1");
        StoredRecord childRecord = new StoredRecord("22", "22", null, parentRecord.getId().toString());
        parentRecord.getVersions().add(childRecord);
        childRecord.getBackups().put("service", new RecordMeta("a", "b", 4));

        replicationService.deleteCascading(childRecord, false, parentRecord);
        assertTrue(childRecord.getBackups().isEmpty(), "Backups removed");
        verifyNoMoreInteractions(historyService);
    }

    @Test(expectedExceptions = {RuntimeException.class})
    public void testDeleteCascadingWithBackupsWithRuntimeException() {
        doThrow(new RuntimeException("Mocked runtime exception")).when(cleanableStorage).delete(any());
        doReturn(Optional.of(cleanableStorage)).when(storageFactory).createCleanableStorage(any());
        doReturn(storageFactory).when(configurationService).createStorageFactory();

        StoredRecord parentRecord = new StoredRecord("1", "1");
        StoredRecord childRecord = new StoredRecord("22", "22", null, parentRecord.getId().toString());
        parentRecord.getVersions().add(childRecord);
        childRecord.getBackups().put("service", new RecordMeta("a", "b", 4));

        replicationService.deleteCascading(childRecord, false, parentRecord);
        assertTrue(childRecord.getBackups().isEmpty(), "Backups removed");
        verifyNoMoreInteractions(historyService);
    }

    @Test
    public void testForceDeleteCascadingWithBackupsWithStorageException() {
        doThrow(new StorageException("Mocked storage exception")).when(cleanableStorage).delete(any());
        doReturn(Optional.of(cleanableStorage)).when(storageFactory).createCleanableStorage(any());
        doReturn(storageFactory).when(configurationService).createStorageFactory();

        StoredRecord parentRecord = new StoredRecord("1", "1");
        StoredRecord childRecord = new StoredRecord("22", "22", null, parentRecord.getId().toString());
        parentRecord.getVersions().add(childRecord);
        childRecord.getBackups().put("service", new RecordMeta("a", "b", 4));

        replicationService.deleteCascading(childRecord, true, parentRecord);
        assertTrue(childRecord.getBackups().isEmpty(), "Backups removed");
        verify(historyService, times(1)).addAndNotify(any());
    }

    @Test
    public void testForceDeleteCascadingWithBackupsWithRuntimeException() {
        doThrow(new RuntimeException("Mocked runtime exception")).when(cleanableStorage).delete(any());
        doReturn(Optional.of(cleanableStorage)).when(storageFactory).createCleanableStorage(any());
        doReturn(storageFactory).when(configurationService).createStorageFactory();

        StoredRecord parentRecord = new StoredRecord("1", "1");
        StoredRecord childRecord = new StoredRecord("22", "22", null, parentRecord.getId().toString());
        parentRecord.getVersions().add(childRecord);
        childRecord.getBackups().put("service", new RecordMeta("a", "b", 4));

        replicationService.deleteCascading(childRecord, true, parentRecord);
        assertTrue(childRecord.getBackups().isEmpty(), "Backups removed");
        verify(historyService, times(1)).addAndNotify(any());
    }

    @Test
    public void testDeleteRequiresLogin() {
        doThrow(new StorageUnauthenticatedException("Mocked sue exception", storage)).when(cleanableStorage).delete(any());
        doReturn(Optional.of(cleanableStorage)).when(storageFactory).createCleanableStorage(any());
        doReturn(storageFactory).when(configurationService).createStorageFactory();

        StoredRecord parentRecord = new StoredRecord("1", "1");
        StoredRecord childRecord = new StoredRecord("22", "22", null, parentRecord.getId().toString());
        parentRecord.getVersions().add(childRecord);
        RecordMeta metaToDelete = new RecordMeta("a", "b", 4);
        childRecord.getBackups().put("service", metaToDelete);

        replicationService.deleteCascading(childRecord, false, parentRecord);
        assertTrue(childRecord.getBackups().isEmpty(), "Backups removed");
        verifyNoMoreInteractions(historyService);
        verify(storage).login();
        verify(storage).delete(metaToDelete);
        verify(storage).logout();
    }

}