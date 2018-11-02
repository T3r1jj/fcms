package io.github.t3r1jj.fcms.external.factory

import io.github.t3r1jj.fcms.external.upstream.UpstreamStorage

abstract class NamedByStorageFactory<S : UpstreamStorage> : UpstreamStorageFactory<S> {
    override fun toString(): String {
        return createStorage().toString()
    }
}