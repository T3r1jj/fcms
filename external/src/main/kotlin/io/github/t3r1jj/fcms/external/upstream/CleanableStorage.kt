package io.github.t3r1jj.fcms.external.upstream

import io.github.t3r1jj.fcms.external.data.RecordMeta

interface CleanableStorage {
    fun delete(meta: RecordMeta)
}