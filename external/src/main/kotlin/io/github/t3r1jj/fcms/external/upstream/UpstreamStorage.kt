package io.github.t3r1jj.fcms.external.upstream

import io.github.t3r1jj.fcms.external.data.Record
import io.github.t3r1jj.fcms.external.data.RecordMeta

interface UpstreamStorage {
    fun upload(record: Record): RecordMeta
    fun download(filePath: String): Record
    fun isPresent(filePath: String): Boolean
}