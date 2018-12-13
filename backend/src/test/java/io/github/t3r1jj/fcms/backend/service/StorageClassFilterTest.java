package io.github.t3r1jj.fcms.backend.service;

import io.github.t3r1jj.storapi.Storage;
import io.github.t3r1jj.storapi.upstream.UpstreamStorage;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertEquals;

public class StorageClassFilterTest {

    @Test
    public void testGetExcludeClass_ExcludeEmpty() {
        StorageClassFilter storageClassFilter = new StorageClassFilter(Storage.class);
        assertEquals(storageClassFilter.getExcludeClass(), Void.class);
    }

    @Test
    public void testGetExcludeClass_ExcludeNotEmpty() {
        Class<UpstreamStorage> excludeClass = UpstreamStorage.class;
        StorageClassFilter storageClassFilter = new StorageClassFilter(Storage.class, excludeClass);
        assertEquals(storageClassFilter.getExcludeClass(), excludeClass);
    }
}