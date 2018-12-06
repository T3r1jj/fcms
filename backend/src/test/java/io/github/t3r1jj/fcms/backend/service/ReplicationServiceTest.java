package io.github.t3r1jj.fcms.backend.service;

import io.github.t3r1jj.fcms.backend.controller.exception.ResourceNotFoundException;
import io.github.t3r1jj.fcms.backend.model.Configuration;
import io.github.t3r1jj.fcms.backend.model.Event;
import io.github.t3r1jj.fcms.backend.model.ExternalService;
import io.github.t3r1jj.fcms.backend.model.StoredRecord;
import io.github.t3r1jj.storapi.authenticated.AuthenticatedStorage;
import io.github.t3r1jj.storapi.data.Record;
import io.github.t3r1jj.storapi.data.RecordMeta;
import io.github.t3r1jj.storapi.data.exception.StorageException;
import io.github.t3r1jj.storapi.data.exception.StorageUnauthenticatedException;
import io.github.t3r1jj.storapi.upstream.CleanableStorage;
import io.github.t3r1jj.storapi.upstream.UpstreamStorage;
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
    private AuthenticatedStorage authenticatedStorage;
    @Mock
    private AuthenticatedStorage unauthenticatedStorage;
    @Mock
    private UpstreamStorage upstreamStorage;
    @Mock
    private StorageFactory storageFactory;
    @Mock
    private NotificationService notificationService;
    private ReplicationService replicationService;

    private Configuration configuration;
    private ExternalService service;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        replicationService = new ReplicationService(configurationService, recordService, historyService, notificationService);
    }

    private void setUpDefaultConfig(String serviceName, boolean enabled) {
        setUpDefaultConfig(serviceName, enabled, true);
    }

    private void setUpDefaultConfig(String serviceName, boolean enabled, boolean primary) {
        service = new ExternalService(serviceName, primary, enabled, new ExternalService.ApiKey("label123", "key123"));
        configuration = new Configuration(new ExternalService[]{service});
    }

    @Test
    public void testUploadToPrimary() {
        String serviceName = "service name";
        setUpDefaultConfig(serviceName, true);
        doReturn(unauthenticatedStorage).when(storageFactory).createAuthenticatedStorage(serviceName);
        doReturn(storageFactory).when(configurationService).createStorageFactory();
        when(storageFactory.getConfiguration()).thenReturn(configuration);
        doThrow(new StorageUnauthenticatedException("Mocked sue exception", authenticatedStorage)).when(unauthenticatedStorage).upload(any(), any());

        byte[] data = "some text".getBytes();
        StoredRecord recordToStore = new StoredRecord("1", "1", data, null);
        replicationService.uploadToPrimary(recordToStore);

        verify(authenticatedStorage).login();
        verify(authenticatedStorage).upload(eq(new Record(recordToStore.getMeta().getName(), recordToStore.getId().toString(), new ByteArrayInputStream(data))), any());
        verify(authenticatedStorage).logout();
    }

    @Test
    public void testReplicateDataToPrimary() {
        String serviceName = "service name";
        setUpDefaultConfig(serviceName, true, true);
        doReturn(authenticatedStorage).when(storageFactory).createUpstreamStorage(service);
        doReturn(storageFactory).when(configurationService).createStorageFactory();
        when(storageFactory.getConfiguration()).thenReturn(configuration);

        byte[] data = "some text".getBytes();
        StoredRecord recordToStore = new StoredRecord("1", "1", data, null);
        boolean replicated = replicationService.replicateRecordTo(recordToStore, true);
        assertTrue(replicated);
        verify(authenticatedStorage).upload(eq(new Record(recordToStore.getMeta().getName(), recordToStore.getId().toString(), new ByteArrayInputStream(data))), any());
    }

    @Test(expectedExceptions = {RuntimeException.class})
    public void testReplicateDataToPrimaryFailsOnEmptyData() {
        String serviceName = "service name";
        setUpDefaultConfig(serviceName, true, true);
        doReturn(authenticatedStorage).when(storageFactory).createAuthenticatedStorage(serviceName);
        doReturn(storageFactory).when(configurationService).createStorageFactory();
        when(storageFactory.getConfiguration()).thenReturn(configuration);

        byte[] data = "".getBytes();
        StoredRecord recordToStore = new StoredRecord("1", "1", data, null);
        replicationService.replicateRecordTo(recordToStore, true);
    }

    @Test(expectedExceptions = {RuntimeException.class})
    public void testReplicateDataToPrimaryFailsOnNullData() {
        String serviceName = "service name";
        setUpDefaultConfig(serviceName, true, true);
        doReturn(authenticatedStorage).when(storageFactory).createAuthenticatedStorage(serviceName);
        doReturn(storageFactory).when(configurationService).createStorageFactory();
        when(storageFactory.getConfiguration()).thenReturn(configuration);

        byte[] data = null;
        StoredRecord recordToStore = new StoredRecord("1", "1", data, null);
        replicationService.replicateRecordTo(recordToStore, true);
    }


    @Test
    public void testReplicateDataToPrimaryLimitedByServiceNumber() {
        String serviceName = "service name";
        setUpDefaultConfig(serviceName, true, false);
        doReturn(authenticatedStorage).when(storageFactory).createAuthenticatedStorage(serviceName);
        doReturn(storageFactory).when(configurationService).createStorageFactory();
        when(storageFactory.getConfiguration()).thenReturn(configuration);

        byte[] data = "some text".getBytes();
        StoredRecord recordToStore = new StoredRecord("1", "1", data, null);
        recordToStore.getBackups().put(serviceName, null);
        boolean replicated = replicationService.replicateRecordTo(recordToStore, true);
        assertFalse(replicated);
        verifyNoMoreInteractions(authenticatedStorage);
    }

    @Test
    public void testReplicateDataToSecondary() {
        String serviceName = "service name";
        setUpDefaultConfig(serviceName, true, false);
        doReturn(authenticatedStorage).when(storageFactory).createUpstreamStorage(service);
        doReturn(storageFactory).when(configurationService).createStorageFactory();
        when(storageFactory.getConfiguration()).thenReturn(configuration);

        byte[] data = "some text".getBytes();
        StoredRecord recordToStore = new StoredRecord("1", "1", data, null);
        boolean replicated = replicationService.replicateRecordTo(recordToStore, false);
        assertTrue(replicated);
        verify(authenticatedStorage).upload(eq(new Record(recordToStore.getMeta().getName(), recordToStore.getId().toString(), new ByteArrayInputStream(data))), any());
    }

    @Test(expectedExceptions = {RuntimeException.class})
    public void testReplicateDataToSecondaryFailsOnEmptyData() {
        String serviceName = "service name";
        setUpDefaultConfig(serviceName, true, false);
        doReturn(authenticatedStorage).when(storageFactory).createUpstreamOnlyStorage(serviceName);
        doReturn(storageFactory).when(configurationService).createStorageFactory();
        when(storageFactory.getConfiguration()).thenReturn(configuration);

        byte[] data = "".getBytes();
        StoredRecord recordToStore = new StoredRecord("1", "1", data, null);
        replicationService.replicateRecordTo(recordToStore, false);
    }

    @Test(expectedExceptions = {RuntimeException.class})
    public void testReplicateDataToSecondaryFailsOnNullData() {
        String serviceName = "service name";
        setUpDefaultConfig(serviceName, true, false);
        doReturn(authenticatedStorage).when(storageFactory).createUpstreamOnlyStorage(serviceName);
        doReturn(storageFactory).when(configurationService).createStorageFactory();
        when(storageFactory.getConfiguration()).thenReturn(configuration);

        byte[] data = null;
        StoredRecord recordToStore = new StoredRecord("1", "1", data, null);
        replicationService.replicateRecordTo(recordToStore, false);
    }

    @Test
    public void testReplicateDataToSecondaryLimitedByServiceNumber() {
        String serviceName = "service name";
        setUpDefaultConfig(serviceName, true, false);
        doReturn(authenticatedStorage).when(storageFactory).createUpstreamOnlyStorage(serviceName);
        doReturn(storageFactory).when(configurationService).createStorageFactory();
        when(storageFactory.getConfiguration()).thenReturn(configuration);

        byte[] data = "some text".getBytes();
        StoredRecord recordToStore = new StoredRecord("1", "1", data, null);
        recordToStore.getBackups().put(serviceName, null);
        boolean replicated = replicationService.replicateRecordTo(recordToStore, false);
        assertFalse(replicated);
        verifyNoMoreInteractions(authenticatedStorage);
    }

    @Test(expectedExceptions = {ResourceNotFoundException.class})
    public void testUploadToPrimaryConfigNotFound() {
        String serviceName = "service name";
        Configuration configuration = new Configuration(new ExternalService[]{});
        doReturn(authenticatedStorage).when(storageFactory).createAuthenticatedStorage(serviceName);
        doReturn(storageFactory).when(configurationService).createStorageFactory();
        when(storageFactory.getConfiguration()).thenReturn(configuration);

        byte[] data = "sine text".getBytes();
        StoredRecord recordToStore = new StoredRecord("1", "1", data, null);
        replicationService.uploadToPrimary(recordToStore);
    }

    @Test(expectedExceptions = {ResourceNotFoundException.class})
    public void testUploadToPrimaryConfigNotFoundEnabled() {
        String serviceName = "service name";
        setUpDefaultConfig(serviceName, false);
        doReturn(authenticatedStorage).when(storageFactory).createAuthenticatedStorage(serviceName);
        doReturn(storageFactory).when(configurationService).createStorageFactory();
        when(storageFactory.getConfiguration()).thenReturn(configuration);

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
        doThrow(new StorageException("Mocked authenticatedStorage exception")).when(cleanableStorage).delete(any());
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
        doThrow(new StorageException("Mocked authenticatedStorage exception")).when(cleanableStorage).delete(any());
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
        doThrow(new StorageUnauthenticatedException("Mocked sue exception", authenticatedStorage)).when(cleanableStorage).delete(any());
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
        verify(authenticatedStorage).login();
        verify(authenticatedStorage).delete(metaToDelete);
        verify(authenticatedStorage).logout();
    }


    @Test(expectedExceptions = {RuntimeException.class})
    public void testReplicateNoPrimaryBackup() {
        String serviceName = "service name";
        String secondaryServiceName = "service name 2";
        service = new ExternalService(serviceName, true, true, new ExternalService.ApiKey("label123", "key123"));
        ExternalService secondaryService = new ExternalService(secondaryServiceName, false, true, new ExternalService.ApiKey("label123", "key123"));
        configuration = new Configuration(new ExternalService[]{service, secondaryService});
        configuration.setSecondaryBackupLimit(1);
        configuration.setPrimaryBackupLimit(1);

        doReturn(unauthenticatedStorage).when(storageFactory).createUpstreamStorage(service);
        doReturn(storageFactory).when(configurationService).createStorageFactory();
        when(storageFactory.getConfiguration()).thenReturn(configuration);
        doThrow(new StorageUnauthenticatedException("Mocked sue exception", authenticatedStorage)).when(unauthenticatedStorage).upload(any());
        StoredRecord recordToStore = new StoredRecord("1", "1", null, null);

        replicationService.replicate(recordToStore);
    }

    @Test
    public void testReplicateSuccessfullyToOneSecondary() {
        String serviceName = "service name";
        String secondaryServiceName = "service name 2";
        service = new ExternalService(serviceName, true, true, new ExternalService.ApiKey("label123", "key123"));
        ExternalService secondaryService = new ExternalService(secondaryServiceName, false, true, new ExternalService.ApiKey("label123", "key123"));
        configuration = new Configuration(new ExternalService[]{service, secondaryService});
        configuration.setSecondaryBackupLimit(1);

        doReturn(upstreamStorage).when(storageFactory).createUpstreamStorage(secondaryService);
        doReturn(unauthenticatedStorage).when(storageFactory).createUpstreamStorage(service);
        doReturn(authenticatedStorage).when(storageFactory).createAuthenticatedStorage(service.getName());
        doReturn(storageFactory).when(configurationService).createStorageFactory();
        when(storageFactory.getConfiguration()).thenReturn(configuration);
        doThrow(new StorageUnauthenticatedException("Mocked sue exception", authenticatedStorage)).when(unauthenticatedStorage).upload(any(), any());
        StoredRecord recordToStore = new StoredRecord("1", "1", null, null);
        byte[] data = "some text".getBytes();
        RecordMeta meta = new RecordMeta(recordToStore.getMeta().getName(), "", data.length);
        recordToStore.getBackups().put(serviceName, meta);
        when(authenticatedStorage.download(any(), any())).thenReturn(new Record(recordToStore.getMeta().getName(), recordToStore.getId().toString(), new ByteArrayInputStream(data)));

        doThrow(new StorageUnauthenticatedException("Mocked sue exception", authenticatedStorage)).when(unauthenticatedStorage).isPresent(any());
        doReturn(true).when(authenticatedStorage).isPresent(meta.getPath());

        replicationService.replicate(recordToStore);
        verify(upstreamStorage, times(1)).upload(eq(new Record(recordToStore.getMeta().getName(), recordToStore.getId().toString(), new ByteArrayInputStream(data))), any());
    }

    @Test
    public void testReplicateSuccessfullyToOnePrimary() {
        String serviceName = "service name";
        String secondServiceName = "service name 2";
        service = new ExternalService(serviceName, true, true, new ExternalService.ApiKey("label123", "key123"));
        ExternalService anotherPrimaryService = new ExternalService(secondServiceName, true, true, new ExternalService.ApiKey("label123", "key123"));
        configuration = new Configuration(new ExternalService[]{service, anotherPrimaryService});
        configuration.setPrimaryBackupLimit(2);

        doReturn(unauthenticatedStorage).when(storageFactory).createUpstreamStorage(anotherPrimaryService);
        doReturn(unauthenticatedStorage).when(storageFactory).createUpstreamStorage(service);
        doReturn(authenticatedStorage).when(storageFactory).createAuthenticatedStorage(service.getName());
        doReturn(storageFactory).when(configurationService).createStorageFactory();
        when(storageFactory.getConfiguration()).thenReturn(configuration);
        doThrow(new StorageUnauthenticatedException("Mocked sue exception", authenticatedStorage)).when(unauthenticatedStorage).upload(any(), any());
        StoredRecord recordToStore = new StoredRecord("1", "1", null, null);
        byte[] data = "some text".getBytes();
        RecordMeta meta = new RecordMeta(recordToStore.getMeta().getName(), "", data.length);
        recordToStore.getBackups().put(serviceName, meta);
        when(authenticatedStorage.download(any(), any())).thenReturn(new Record(recordToStore.getMeta().getName(), recordToStore.getId().toString(), new ByteArrayInputStream(data)));

        doThrow(new StorageUnauthenticatedException("Mocked sue exception", authenticatedStorage)).when(unauthenticatedStorage).isPresent(any());
        doReturn(true).when(authenticatedStorage).isPresent(meta.getPath());

        replicationService.replicate(recordToStore);
        verify(authenticatedStorage, times(1)).upload(eq(new Record(recordToStore.getMeta().getName(), recordToStore.getId().toString(), new ByteArrayInputStream(data))), any());
    }

}