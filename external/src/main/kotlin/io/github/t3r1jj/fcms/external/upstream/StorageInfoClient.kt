package io.github.t3r1jj.fcms.external.upstream

import io.github.t3r1jj.fcms.external.data.RecordMeta
import io.github.t3r1jj.fcms.external.data.exception.StorageException

abstract class StorageInfoClient<T>(baseUrl: String, service: Class<T>) : StorageClient<T>(baseUrl, service) {

    open fun isPresent(filePath: String): Boolean {
        return try {
            getInfo(filePath)
            true
        } catch (e: StorageException) {
            false
        }
    }

    abstract fun getInfo(filePath: String): RecordMeta
}