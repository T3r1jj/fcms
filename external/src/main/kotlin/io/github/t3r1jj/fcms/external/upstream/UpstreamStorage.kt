package io.github.t3r1jj.fcms.external.upstream

import io.github.t3r1jj.fcms.external.Storage
import io.github.t3r1jj.fcms.external.data.Record
import io.github.t3r1jj.fcms.external.data.RecordMeta

interface UpstreamStorage : Storage {
    /**
     * Returns [RecordMeta] for uploaded [record], filePath may be different than provided one.
     */
    fun upload(record: Record): RecordMeta

    /**
     * Returns [Record] for [filePath] from received [RecordMeta] from upload.
     */
    fun download(filePath: String): Record

    /**
     * Returns true if file at [filePath] received from [RecordMeta] from after upload is present, else returns false.
     */
    fun isPresent(filePath: String): Boolean
}