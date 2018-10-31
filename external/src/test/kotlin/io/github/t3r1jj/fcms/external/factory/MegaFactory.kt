package io.github.t3r1jj.fcms.external.factory

import io.github.t3r1jj.fcms.external.mega.Mega
import io.github.t3r1jj.fcms.external.Storage

class MegaFactory : StorageFactory {
    companion object {
        private const val MEGA_USERNAME_TEST_KEY = "FCMS_TEST_MEGA_USERNAME"
        private const val MEGA_PASSWORD_TEST_KEY = "FCMS_TEST_MEGA_PASSWORD"
        private val userName = System.getenv(MEGA_USERNAME_TEST_KEY)
        private val password = System.getenv(MEGA_PASSWORD_TEST_KEY)
    }

    override fun createStorage(): Storage {
        return Mega(userName, password)
    }

    override fun createStorageWithoutAccess(): Storage {
        return Mega("", "")
    }
}