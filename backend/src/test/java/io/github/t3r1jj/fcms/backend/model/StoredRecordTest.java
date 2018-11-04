package io.github.t3r1jj.fcms.backend.model;

import org.bson.types.ObjectId;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class StoredRecordTest {

    @Test
    public void testStringToObjectId() {
        ObjectId newId = ObjectId.get();
        assertEquals(newId, StoredRecord.stringToObjectId(newId.toString()));
    }
}