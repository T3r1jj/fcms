package io.github.t3r1jj.fcms.backend.model;

import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

public class CodeTest {

    @Spy
    private StoredRecord storedRecord;

    @BeforeMethod
    public void setUp() {
        storedRecord = spy(new StoredRecord("Stored record passed to dynamic code", "tag"));
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testExecute() {
        Code code = new CodeBuilder()
                .setCode("System.out.println(storedRecord.getName());")
                .createCode();
        code.execute(storedRecord);
        verify(storedRecord, times(1)).getName();
    }

    @Test
    public void testExecuteShouldPersistDataChange() {
        String newDescription = "new description";
        Code code = new CodeBuilder()
                .setCode("storedRecord.setDescription(\"" + newDescription + "\");")
                .createCode();
        code.execute(storedRecord);
        assertEquals(storedRecord.getDescription(), newDescription);
    }

    @Test
    public void testExecuteWithCatch() {
        RuntimeException testException = spy(new RuntimeException("test exception"));
        doThrow(testException).when(storedRecord).getName();
        Code code = new CodeBuilder()
                .setCode("storedRecord.getName();")
                .setExceptionHandler("e.getMessage();")
                .createCode();
        code.execute(storedRecord);
        verify(testException, times(1)).getMessage();
    }

    @Test
    public void testExecuteWithFinally() {
        Code code = new CodeBuilder()
                .setCode("storedRecord.getName();")
                .setFinallyHandler("storedRecord.getId();")
                .createCode();
        code.execute(storedRecord);
        verify(storedRecord, times(1)).getId();
    }
}