package io.github.t3r1jj.fcms.external.factory

import io.github.t3r1jj.fcms.external.authorized.Dropbox
import io.github.t3r1jj.fcms.external.authorized.Storage

class DropboxFactory : NamedByStorageFactory<Storage>(), StorageFactory<Storage> {
    companion object {
        private const val DROPBOX_TOKEN_TEST_KEY = "FCMS_TEST_DROPBOX_ACCESS_TOKEN"
        private val accessToken = System.getenv(DROPBOX_TOKEN_TEST_KEY)
    }

    override fun createStorage(): Storage {
        return Dropbox(accessToken)
    }

    override fun createStorageWithoutAccess(): Storage {
        return Dropbox("")
    }
}