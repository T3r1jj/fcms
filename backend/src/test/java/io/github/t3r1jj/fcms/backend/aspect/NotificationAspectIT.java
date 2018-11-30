package io.github.t3r1jj.fcms.backend.aspect;

import io.github.t3r1jj.fcms.backend.model.StoredRecord;
import io.github.t3r1jj.fcms.backend.repository.StoredRecordRepository;
import io.github.t3r1jj.fcms.backend.service.NotificationService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class NotificationAspectIT extends AbstractTestNGSpringContextTests {

    @Autowired
    private NotificationAspect notificationAspect;

    @SpyBean
    @Autowired
    private NotificationService notificationService;

    @Autowired
    private StoredRecordRepository recordRepository;

    @BeforeMethod
    public void cleanUp() {
        recordRepository.deleteAll();
        reset(notificationService);
    }

    @AfterMethod
    public void cleanDown() {
        recordRepository.deleteAll();
    }

    @Test
    public void testAfterReturningFromSave() {
        recordRepository.save(new StoredRecord(ObjectId.get()));
        verify(notificationService).broadcast(any());
    }

    @Test
    public void testAfterReturningFromSaveAll() {
        recordRepository.saveAll(Arrays.asList(new StoredRecord(ObjectId.get()), new StoredRecord(ObjectId.get())));
        verify(notificationService, times(2)).broadcast(any());
    }

    @Test
    public void testAfterReturningFromDeleteAll() {
        recordRepository.deleteAll(Arrays.asList(new StoredRecord(ObjectId.get()), new StoredRecord(ObjectId.get())));
        verify(notificationService, times(2)).broadcast(any());
    }

    @Test
    public void testAfterReturningFromDelete() {
        recordRepository.delete(new StoredRecord(ObjectId.get()));
        verify(notificationService).broadcast(any());
    }

    @Test
    public void testAfterReturningFromDeleteById() {
        recordRepository.deleteById(ObjectId.get());
        verify(notificationService).broadcast(any());
    }
}