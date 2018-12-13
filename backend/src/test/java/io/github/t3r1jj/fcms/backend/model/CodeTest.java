package io.github.t3r1jj.fcms.backend.model;

import io.github.t3r1jj.fcms.backend.model.code.AfterReplicationCode;
import io.github.t3r1jj.fcms.backend.model.code.OnReplicationCode;
import io.github.t3r1jj.fcms.backend.service.RecordService;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertFalse;

public class CodeTest {

    @Spy
    private StoredRecord storedRecord;
    @Mock
    private RecordService recordService;

    @BeforeMethod
    public void setUp() {
        storedRecord = spy(new StoredRecord("Stored record passed to dynamic code", "tag"));
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testExecute() {
        OnReplicationCode code = new OnReplicationCode.Builder()
                .setCode("System.out.println(storedRecord.getMeta());")
                .build();
        code.execute(storedRecord);
        verify(storedRecord, times(1)).getMeta();
    }

    @Test
    public void testExecuteShouldPersistDataChange() {
        String newBackup = "new description";
        OnReplicationCode code = new OnReplicationCode.Builder()
                .setCode("storedRecord.getBackups().put(\"" + newBackup + "\", null);")
                .build();
        code.execute(storedRecord);
        assertEquals(storedRecord.getBackups().size(), 1);
    }

    @Test
    public void testExecuteWithCatch() {
        RuntimeException testException = spy(new RuntimeException("test exception"));
        doThrow(testException).when(storedRecord).getMeta();
        OnReplicationCode code = new OnReplicationCode.Builder()
                .setCode("storedRecord.getMeta();")
                .setExceptionHandler("e.getMessage();")
                .build();
        code.execute(storedRecord);
        verify(testException, times(1)).getMessage();
    }

    @Test
    public void testExecuteWithFinally() {
        OnReplicationCode code = new OnReplicationCode.Builder()
                .setCode("storedRecord.getId();")
                .setFinallyHandler("storedRecord.getId();")
                .build();
        code.execute(storedRecord);
        verify(storedRecord, times(2)).getId();
    }

    @Test
    public void testExecuteWithCollectionParam_LambdaNotSupported() {
        StoredRecord[] storedRecords = new StoredRecord[]{storedRecord, storedRecord};
        when(recordService.findAll()).thenReturn(Arrays.asList(storedRecords));
        AfterReplicationCode code = new AfterReplicationCode.Builder()
                .setCode("recordService.findAll().stream().forEach(storedRecord -> System.out.println(storedRecord.getMeta()));")
                .build();
        code.execute(recordService);
        verify(storedRecord, times(0)).getMeta();
    }

    @Test
    public void testExecuteWithCollectionParam_CannotDetermineSimpleTypeNamedStoredRecord() {
        StoredRecord[] storedRecords = new StoredRecord[]{storedRecord, storedRecord};
        when(recordService.findAll()).thenReturn(Arrays.asList(storedRecords));
        AfterReplicationCode code = new AfterReplicationCode.Builder()
                .setCode("        for (StoredRecord storedRecord1 : recordService.findAll().storedRecords) {\n" +
                        "            System.out.println(storedRecord1);\n" +
                        "        }")
                .build();
        code.execute(recordService);
        verify(storedRecord, times(0)).getMeta();
    }

    @Test
    public void testExecuteWithCollectionParam() {
        StoredRecord[] storedRecords = new StoredRecord[]{storedRecord, storedRecord};
        when(recordService.findAll()).thenReturn(Arrays.asList(storedRecords));
        AfterReplicationCode code = new AfterReplicationCode.Builder()
                .setCode("       io.github.t3r1jj.fcms.backend.model.StoredRecord[] storedRecords = (io.github.t3r1jj.fcms.backend.model.StoredRecord[]) recordService.findAll().toArray(new io.github.t3r1jj.fcms.backend.model.StoredRecord[0]);" +
                        "        for (int i = 0; i < storedRecords.length; i++) {\n" +
                        "            System.out.println(storedRecords[i].getMeta());\n" +
                        "        }")
                .build();
        code.execute(recordService);
        verify(storedRecord, times(2)).getMeta();
    }

    @Test
    public void testEvalAfterReplicationCode() {
        AfterReplicationCode code = new AfterReplicationCode.Builder()
                .setCode("unintelligible; recordService.findAll();")
                .build();
        code.execute(recordService);
        verify(recordService, times(0)).findAll();
    }

    @Test
    public void testEvalOnReplicationCode() {
        OnReplicationCode code = new OnReplicationCode.Builder()
                .setCode("unintelligible; storedRecord.getMeta();")
                .build();
        code.execute(storedRecord);
        verify(storedRecord, times(0)).getMeta();
    }

    @Test
    public void testCodeEmptyDefault() {
        OnReplicationCode code = new OnReplicationCode.Builder()
                .build();
        assertTrue(code.isEmpty());
    }

    @Test
    public void testCodeEmpty() {
        OnReplicationCode code = new OnReplicationCode.Builder()
                .setCode("")
                .setExceptionHandler("")
                .setFinallyHandler("")
                .build();
        assertTrue(code.isEmpty());
    }

    @Test
    public void testCodeEmpty_Null() {
        OnReplicationCode code = new OnReplicationCode.Builder()
                .setCode(null)
                .setExceptionHandler(null)
                .setFinallyHandler(null)
                .build();
        assertTrue(code.isEmpty());
    }

    @Test
    public void testCodeEmptyNotEmpty_Code() {
        OnReplicationCode code = new OnReplicationCode.Builder()
                .setCode("a")
                .build();
        assertFalse(code.isEmpty());
    }

    @Test
    public void testCodeEmptyNotEmpty_ExceptionHandler() {
        OnReplicationCode code = new OnReplicationCode.Builder()
                .setExceptionHandler("a")
                .build();
        assertFalse(code.isEmpty());
    }

    @Test
    public void testCodeEmptyNotEmpty_FinallyCode() {
        OnReplicationCode code = new OnReplicationCode.Builder()
                .setFinallyHandler("a")
                .build();
        assertFalse(code.isEmpty());
    }
}