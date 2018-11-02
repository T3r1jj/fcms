package io.github.t3r1jj.fcms.external.data

data class RecordMeta(val name: String, val path: String, val size: Long) {
    internal var id: String? = null
}