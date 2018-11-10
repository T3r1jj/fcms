package io.github.t3r1jj.fcms.external.authenticated

import io.github.t3r1jj.fcms.external.NamedStorage
import io.github.t3r1jj.fcms.external.data.Record
import io.github.t3r1jj.fcms.external.data.RecordMeta
import io.github.t3r1jj.fcms.external.data.StorageInfo
import io.github.t3r1jj.fcms.external.data.exception.StorageUnauthenticatedException

abstract class AuthenticatedStorageTemplate : NamedStorage(), AuthenticatedStorage {
    private fun throwIfNotAuthenticated() {
        if (!isLogged()) {
            throw StorageUnauthenticatedException("This action requires storage authentication (login).", this)
        }
    }

    final override fun upload(record: Record): RecordMeta {
        throwIfNotAuthenticated()
        return doAuthenticatedUpload(record)
    }

    final override fun download(filePath: String): Record {
        throwIfNotAuthenticated()
        return doAuthenticatedDownload(filePath)
    }

    final override fun findAll(filePath: String): List<RecordMeta> {
        throwIfNotAuthenticated()
        return doAuthenticatedFindAll(filePath)
    }

    final override fun getInfo(): StorageInfo {
        throwIfNotAuthenticated()
        return doAuthenticatedGetInfo()
    }

    final override fun delete(meta: RecordMeta) {
        throwIfNotAuthenticated()
        return doAuthenticatedDelete(meta)
    }

    abstract fun doAuthenticatedUpload(record: Record): RecordMeta
    abstract fun doAuthenticatedDownload(filePath: String): Record
    abstract fun doAuthenticatedFindAll(filePath: String): List<RecordMeta>
    abstract fun doAuthenticatedGetInfo(): StorageInfo
    abstract fun doAuthenticatedDelete(meta: RecordMeta)

}