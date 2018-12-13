package io.github.t3r1jj.fcms.backend.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.testng.annotations.Test;

public class ModelTest {
    @Test
    public void testHashEqualsContract_ExternalService() {
        EqualsVerifier.forClass(ExternalService.class)
                .suppress(Warning.STRICT_HASHCODE)
                .verify();
    }

    @Test
    public void testHashEqualsContract_Configuration() {
        EqualsVerifier.forClass(Configuration.class)
                .withNonnullFields("id")
                .suppress(Warning.STRICT_HASHCODE, Warning.NONFINAL_FIELDS)
                .verify();
    }

    @Test
    public void testHashEqualsContract_StoredRecordMeta() {
        EqualsVerifier.forClass(StoredRecordMeta.class)
                .suppress(Warning.STRICT_HASHCODE)
                .verify();
    }

    @Test
    public void testHashEqualsContract_StoredRecord() {
        StoredRecord record1 = new StoredRecord("a", "b", null, null);
        StoredRecord record2 = new StoredRecord("c", "d", null, null);
        EqualsVerifier.forClass(StoredRecord.class)
                .withPrefabValues(StoredRecord.class, record1, record2)
                .withNonnullFields("id")
                .withIgnoredFields("data")
                .suppress(Warning.STRICT_HASHCODE, Warning.NONFINAL_FIELDS)
                .verify();
    }
}
