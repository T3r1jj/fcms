package io.github.t3r1jj.fcms.external.factory

import io.github.t3r1jj.fcms.external.GoogleDrive
import io.github.t3r1jj.fcms.external.Storage

class GoogleDriveFactory : StorageFactory {
    companion object {
        private const val GOOGLEDRIVE_CLIENT_ID_TEST_KEY = "FCMS_TEST_GOOGLEDRIVE_CLIENT_ID"
        private const val GOOGLEDRIVE_CLIENT_SECRET_TEST_KEY = "FCMS_TEST_GOOGLEDRIVE_CLIENT_SECRET"
        private const val GOOGLEDRIVE_REFRESH_TOKEN_TEST_KEY = "FCMS_TEST_GOOGLEDRIVE_REFRESH_TOKEN"
        private val clientId = System.getenv(GOOGLEDRIVE_CLIENT_ID_TEST_KEY)
        private val clientSecret = System.getenv(GOOGLEDRIVE_CLIENT_SECRET_TEST_KEY)
        private val refreshToken = System.getenv(GOOGLEDRIVE_REFRESH_TOKEN_TEST_KEY)
    }

    override fun createStorage(): Storage {
        return GoogleDrive(clientId, clientSecret, refreshToken)
    }

    override fun createStorageWithoutAccess(): Storage {
        return GoogleDrive("", "", "")
    }
}