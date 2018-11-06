package io.github.t3r1jj.fcms.external.data.exception

import io.github.t3r1jj.fcms.external.authenticated.AuthenticatedStorage

open class StorageUnauthenticatedException(message: String?, val storage: AuthenticatedStorage)
    : StorageException(message)