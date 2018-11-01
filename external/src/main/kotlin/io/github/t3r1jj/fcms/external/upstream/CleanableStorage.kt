package io.github.t3r1jj.fcms.external.upstream

import io.github.t3r1jj.fcms.external.Record

interface CleanableStorage {
    fun delete(record: Record)
}