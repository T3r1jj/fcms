package io.github.t3r1jj.fcms.external.factory

import io.github.t3r1jj.fcms.external.upstream.CleanableStorage
import io.github.t3r1jj.fcms.external.upstream.UpstreamStorage

interface UpstreamStorageFactory<S : UpstreamStorage> {
    fun createStorage(): S
    fun asCleanable(storage: S): CleanableStorage? = if (storage is CleanableStorage) storage else null
}