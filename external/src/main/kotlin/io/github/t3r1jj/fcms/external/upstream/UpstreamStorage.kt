package io.github.t3r1jj.fcms.external.upstream

import io.github.t3r1jj.fcms.external.Record

interface UpstreamStorage {
    fun upload(record: Record): Record
    fun download(filePath: String): Record
    fun isPresent(filePath: String): Boolean
}