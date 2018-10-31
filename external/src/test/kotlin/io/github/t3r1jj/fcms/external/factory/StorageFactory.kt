package io.github.t3r1jj.fcms.external.factory

import io.github.t3r1jj.fcms.external.Storage

interface StorageFactory {
    fun createStorage(): Storage
    fun createStorageWithoutAccess(): Storage
}
