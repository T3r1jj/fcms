package io.github.t3r1jj.fcms.external.upstream

import io.github.t3r1jj.fcms.external.Record
import org.apache.commons.io.IOUtils
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class PutIT {

    lateinit var uploadedRecord: Record

    @AfterEach
    fun tearDown() {
        Put().delete(uploadedRecord)
    }

    @Test
    fun upload() {
        val name = System.currentTimeMillis().toString()
        val record = Record("$name.tmp", "", "Some text".byteInputStream())
        uploadedRecord = Put().upload(record)
        assertEquals(record.name, uploadedRecord.name)
        assertTrue(uploadedRecord.name.isNotBlank())
    }

    @Test
    fun download() {
        val name = System.currentTimeMillis().toString()
        val data = "Some text"
        val record = Record("$name.tmp", "", data.byteInputStream())
        val unreadRecord = record.copy(data = data.byteInputStream())
        uploadedRecord = Put().upload(record)
        val downloadedRecord = Put().download(uploadedRecord.path)
        assertTrue(downloadedRecord.path.isNotBlank())
        assertEquals(unreadRecord.name, downloadedRecord.name)
        assertTrue(IOUtils.contentEquals(unreadRecord.data, downloadedRecord.data))
    }

    @Test
    fun isPresent() {
        val name = System.currentTimeMillis().toString()
        val record = Record("$name.tmp", "", "Some text".byteInputStream())
        uploadedRecord = Put().upload(record)
        assertTrue(Put().isPresent(uploadedRecord.path))
    }
}