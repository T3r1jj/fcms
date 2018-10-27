package io.github.t3r1jj.fcms.external

import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class DropboxTest {
    companion object {
        private const val DROPBOX_TEST_TOKEN_KEY = "FCMS_TEST_DROPBOX_ACCESS_TOKEN"
        private val accessToken = System.getenv(DROPBOX_TEST_TOKEN_KEY)
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
    }

    @Test
    fun testDownload() {
    }

    @Test
    fun testDelete() {
    }

    @Test
    fun testIsPresent() {
    }

    @Test
    fun testGetInfo() {
    }

    @Test
    fun testLogout() {
    }
}