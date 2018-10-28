package io.github.t3r1jj.fcms.external

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import java.math.BigInteger
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DropboxIT {
    companion object {
        private const val DROPBOX_TEST_TOKEN_KEY = "FCMS_TEST_DROPBOX_ACCESS_TOKEN"
        private val accessToken = System.getenv(DROPBOX_TEST_TOKEN_KEY)
        private val testRootPath = "/" + this::class.java.`package`.name

        @AfterAll
        @JvmStatic
        fun cleanStorage() {
            val storage = Dropbox(accessToken)
            storage.login()
            storage.findAll("")
                    .filter { it.path.contains(testRootPath) }
                    .forEach { storage.delete(it.path) }
        }
    }

    private val storage = Dropbox(accessToken)

    @Test
    fun testIsNotLogged() {
        val storage = Dropbox("")
        assertFalse(storage.isLogged())
    }

    @Test
    fun testIsLogged() {
        storage.login()
        assertTrue(storage.isLogged())
    }

    @Test
    fun testUpload() {
        val name = System.currentTimeMillis().toString()
        val record = Record("$name.tmp", "$testRootPath/$name.tmp", "Some text".byteInputStream())
        storage.login()
        storage.upload(record)
        assertTrue(storage.isPresent(record.path))
    }

    @Test
    fun testFindAll() {
        val name = System.currentTimeMillis().toString()
        val text = "Some text"
        val record = Record("$name.tmp", "$testRootPath/$name.tmp", text.byteInputStream())
        storage.login()
        storage.upload(record)
        assertThat(storage.findAll(""), hasItem(RecordMeta(record.name, record.path, text.length.toLong())))
    }

    @Test
    fun testDownload() {
        val name = System.currentTimeMillis().toString()
        val text = "Some text"
        val record = Record("$name.tmp", "$testRootPath/$name.tmp", text.byteInputStream())
        val unreadRecord = record.copy(data = text.byteInputStream())
        storage.login()
        storage.upload(record)
        val storedRecord = storage.download(record.path)
        assertEquals(unreadRecord, storedRecord)
    }

    @Test
    fun testDelete() {
        val name = System.currentTimeMillis().toString()
        val record = Record("$name.tmp", "$testRootPath/$name.tmp", "Some text".byteInputStream())
        storage.login()
        storage.upload(record)
        storage.delete(record.path)
        assertFalse(storage.isPresent(record.path))
    }

    @Test
    fun testIsNotPresent() {
        val record = Record("test.txt", "/XXXXXXXXXXXXXXXXXXXX", "Some text".byteInputStream())
        storage.login()
        assertFalse(storage.isPresent(record.path))
    }

    @Test
    fun testIsNotPresentEmptyPath() {
        val record = Record("test.txt", "", "Some text".byteInputStream())
        storage.login()
        assertFalse(storage.isPresent(record.path))
    }

    @Test
    fun testGetInfo() {
        storage.login()
        val info = storage.getInfo()
        assertThat(info.name.toLowerCase(), containsString("dropbox"))
        assertThat(info.totalSpace, greaterThan(BigInteger.ZERO))
    }

    @Test
    fun testGetInfoSpaceUsed() {
        val name = System.currentTimeMillis().toString()
        val record = Record("$name.tmp", "$testRootPath/$name.tmp", "Some text".byteInputStream())
        storage.login()
        storage.upload(record)
        val info = storage.getInfo()
        assertThat(info.usedSpace, greaterThan(BigInteger.ZERO))
        assertThat(info.totalSpace, greaterThan(info.usedSpace))
    }

    @Test
    fun testLogout() {
        storage.login()
        storage.logout()
        assertFalse(storage.isLogged())
    }
}