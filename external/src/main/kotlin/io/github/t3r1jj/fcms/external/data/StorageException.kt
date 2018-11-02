package io.github.t3r1jj.fcms.external.data

class StorageException(message: String?, cause: Throwable?) : RuntimeException(message, cause) {
    constructor(message: String?) : this(message, null)
}