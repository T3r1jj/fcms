package io.github.t3r1jj.fcms.external

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import java.math.BigInteger
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GoogleDriveIT {
    companion object {
        private const val GOOGLEDRIVE_CLIENT_ID_TEST_KEY = "FCMS_TEST_GOOGLEDRIVE_CLIENT_ID"
        private const val GOOGLEDRIVE_CLIENT_SECRET_TEST_KEY = "FCMS_TEST_GOOGLEDRIVE_CLIENT_SECRET"
        private const val GOOGLEDRIVE_REFRESH_TOKEN_TEST_KEY = "FCMS_TEST_GOOGLEDRIVE_REFRESH_TOKEN"
        private val clientId = System.getenv(GOOGLEDRIVE_CLIENT_ID_TEST_KEY)
        private val clientSecret = System.getenv(GOOGLEDRIVE_CLIENT_SECRET_TEST_KEY)
        private val refreshToken = System.getenv(GOOGLEDRIVE_REFRESH_TOKEN_TEST_KEY)

        private val testRootPath = "/" + this::class.java.`package`.name

        @AfterAll
        @JvmStatic
        fun cleanStorage() {
            val storage = GoogleDrive(clientId, clientSecret, refreshToken)
            storage.login()
            storage.findAll("")
                    .filter { it.path.contains(testRootPath) }
                    .forEach { storage.delete(it.path) }
        }
    }

    private val storage = GoogleDrive(clientId, clientSecret, refreshToken)

    @Test
    fun testIsNotLogged() {
        val storage = GoogleDrive("", "", "")
        assertFalse(storage.isLogged())
    }

    @Test
    fun testIsNotLoggedWrongCredentials() {
        val storage = GoogleDrive("", "", "")
        assertFailsWith(com.google.api.client.auth.oauth2.TokenResponseException::class) {
            storage.login()
            storage.isLogged()
        }
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
        assertThat(info.name.toLowerCase(), containsString("googledrive"))
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