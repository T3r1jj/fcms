package io.github.t3r1jj.fcms.external.upstream

import io.github.t3r1jj.fcms.external.Record
import org.apache.commons.io.IOUtils
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class MegauploadIT {

    @Test
    fun upload() {
        val name = System.currentTimeMillis().toString()
        val record = Record("$name.tmp", "", "Some text".byteInputStream())
        val uploadedRecord = Megaupload().upload(record)
        assertEquals(record.name, uploadedRecord.name)
        assertTrue(uploadedRecord.name.isNotBlank())
    }

    @Test
    fun download() {
        val name = System.currentTimeMillis().toString()
        val data = "Some text"
        var record = Record("$name.tmp", "", data.byteInputStream())
        val unreadRecord = record.copy(data = data.byteInputStream())
        record = Megaupload().upload(record)
        val uploadedRecord = Megaupload().download(record.path)
        assertTrue(uploadedRecord.path.isNotBlank())
        assertEquals(unreadRecord.name, uploadedRecord.name)
        assertTrue(IOUtils.contentEquals(unreadRecord.data, uploadedRecord.data))
    }

    @Test
    fun isPresent() {
        val name = System.currentTimeMillis().toString()
        val record = Record("$name.tmp", "", "Some text".byteInputStream())
        val uploadedRecord = Megaupload().upload(record)
        assertTrue(Megaupload().isPresent(uploadedRecord.path))
    }
}