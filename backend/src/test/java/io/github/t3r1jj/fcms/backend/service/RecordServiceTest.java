package io.github.t3r1jj.fcms.backend.service;

import org.bson.types.ObjectId;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class RecordServiceTest {

    @Test
    public void testStringToObjectId() {
        RecordService recordService = new RecordService(null, null);
        ObjectId newId = ObjectId.get();
        assertEquals(newId, recordService.stringToObjectId(newId.toString()));
    }
}