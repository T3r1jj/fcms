package io.github.t3r1jj.fcms.backend.model;

import io.github.t3r1jj.fcms.backend.model.code.OnReplicationCallback;
import io.github.t3r1jj.fcms.backend.model.code.AfterReplicationCallback;
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
        OnReplicationCallback code = new OnReplicationCallback.Builder()
                .setCode("System.out.println(storedRecord.getName());")
                .build();
        code.execute(storedRecord);
        verify(storedRecord, times(1)).getName();
    }

    @Test
    public void testExecuteShouldPersistDataChange() {
        String newDescription = "new description";
        OnReplicationCallback code = new OnReplicationCallback.Builder()
                .setCode("storedRecord.setDescription(\"" + newDescription + "\");")
                .build();
        code.execute(storedRecord);
        assertEquals(storedRecord.getDescription(), newDescription);
    }

    @Test
    public void testExecuteWithCatch() {
        RuntimeException testException = spy(new RuntimeException("test exception"));
        doThrow(testException).when(storedRecord).getName();
        OnReplicationCallback code = new OnReplicationCallback.Builder()
                .setCode("storedRecord.getName();")
                .setExceptionHandler("e.getMessage();")
                .build();
        code.execute(storedRecord);
        verify(testException, times(1)).getMessage();
    }

    @Test
    public void testExecuteWithFinally() {
        OnReplicationCallback code = new OnReplicationCallback.Builder()
                .setCode("storedRecord.getName();")
                .setFinallyHandler("storedRecord.getId();")
                .build();
        code.execute(storedRecord);
        verify(storedRecord, times(1)).getId();
    }

    @Test
    public void testExecuteWithCollectionParam_LambdaNotSupported() {
        StoredRecord[] storedRecords = new StoredRecord[]{storedRecord, storedRecord};
        AfterReplicationCallback code = new AfterReplicationCallback.Builder()
                .setCode("storedRecords.stream().forEach(storedRecord -> System.out.println(storedRecord.getName()));")
                .build();
        code.execute(storedRecords);
        verify(storedRecord, times(0)).getName();
    }

    @Test
    public void testExecuteWithCollectionParam_CannotDetermineSimpleTypeNamedStoredRecord() {
        StoredRecord[] storedRecords = new StoredRecord[]{storedRecord, storedRecord};
        AfterReplicationCallback code = new AfterReplicationCallback.Builder()
                .setCode("        for (StoredRecord storedRecord1 : storedRecords) {\n" +
                        "            System.out.println(storedRecord1);\n" +
                        "        }")
                .build();
        code.execute(storedRecords);
        verify(storedRecord, times(0)).getName();
    }

    @Test
    public void testExecuteWithCollectionParam() {
        StoredRecord[] storedRecords = new StoredRecord[]{storedRecord, storedRecord};
        AfterReplicationCallback code = new AfterReplicationCallback.Builder()
                .setCode("        for (int i = 0; i < storedRecords.length; i++) {\n" +
                        "            System.out.println(storedRecords[i].getName());\n" +
                        "        }")
                .build();
        code.execute(storedRecords);
        verify(storedRecord, times(2)).getName();
    }
}