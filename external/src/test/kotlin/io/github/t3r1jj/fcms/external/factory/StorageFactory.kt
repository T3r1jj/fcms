package io.github.t3r1jj.fcms.external.factory

import io.github.t3r1jj.fcms.external.upstream.UpstreamStorage

interface StorageFactory<S : UpstreamStorage> : UpstreamStorageFactory<S> {
    fun createStorageWithoutAccess(): S
}
