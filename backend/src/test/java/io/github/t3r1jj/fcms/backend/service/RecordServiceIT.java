package io.github.t3r1jj.fcms.backend.service;

import io.github.t3r1jj.fcms.backend.model.StoredRecord;
import io.github.t3r1jj.fcms.backend.repository.StoredRecordRepository;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

@SpringBootTest
public class RecordServiceIT extends AbstractTestNGSpringContextTests {

    private RecordService recordService;
    @Autowired
    private StoredRecordRepository recordRepository;
    @Mock
    private ReplicationService replicationService;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        recordService = new RecordService(recordRepository, replicationService);
    }

    @AfterMethod
    public void tearDown() {
        recordRepository.deleteAll();
    }

    @Test
    public void store() {
        StoredRecord storedRecord = new StoredRecord("a", "a");
        StoredRecord childRecord = new StoredRecord("2", "2", null, storedRecord.getId().toString());
        recordService.store(storedRecord);
        recordService.store(childRecord);
        assertEquals(recordRepository.count(), 1);
        storedRecord = recordService.findAll().iterator().next();
        assertEquals(storedRecord.getVersions().get(0), childRecord);
    }

    @Test
    public void updateRoot() {
        StoredRecord storedRecord = new StoredRecord("a", "a");
        recordService.store(storedRecord);
        String newDescription = "new description";
        storedRecord.setDescription(newDescription);
        recordService.update(storedRecord);
        storedRecord = recordRepository.findById(storedRecord.getId()).get();
        assertEquals(newDescription, storedRecord.getDescription());
        assertEquals(recordRepository.count(), 1);
    }

    @Test
    public void updateChild() {
        StoredRecord storedRecord = new StoredRecord("a", "a");
        StoredRecord childRecord = new StoredRecord("2", "2", null, storedRecord.getId().toString());
        recordService.store(storedRecord);
        recordService.store(childRecord);
        String newDescription = "new description";
        childRecord.setDescription(newDescription);
        recordService.update(childRecord);
        storedRecord = recordRepository.findById(storedRecord.getId()).get();
        assertEquals(storedRecord.getVersions().get(0).getDescription(), newDescription);
        assertEquals(recordRepository.count(), 1);
    }

    @Test
    public void findAll() {
        StoredRecord storedRecord = new StoredRecord("a", "a");
        StoredRecord storedRecord2 = new StoredRecord("2", "2");
        StoredRecord storedRecord3 = new StoredRecord("33", "33", null, storedRecord2.getId().toString());
        recordService.store(storedRecord);
        assertEquals(recordService.findAll().size(), 1);
        recordService.store(storedRecord2);
        assertEquals(recordService.findAll().size(), 2);
        recordService.store(storedRecord3);
        assertEquals(recordService.findAll().size(), 2);
    }

    @Test
    public void updateDescription() {
        StoredRecord storedRecord = new StoredRecord("a", "a");
        assertNull(storedRecord.getDescription());
        recordService.store(storedRecord);
        String newDescription = "new description";
        recordService.updateDescription(storedRecord.getId().toString(), newDescription);
        assertEquals(recordRepository.findById(storedRecord.getId()).get().getDescription(), newDescription);
    }

    @Test
    public void storeShouldCauseReplication() {
        StoredRecord storedRecord = new StoredRecord("a", "a");
        assertNull(storedRecord.getDescription());
        recordService.store(storedRecord);
        verify(replicationService, times(1)).replicateToPrimary(storedRecord);
    }
}