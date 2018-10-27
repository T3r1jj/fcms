package io.github.t3r1jj.fcms.external

import org.slf4j.Logger
import org.slf4j.LoggerFactory

interface Loggable {
    fun Loggable.logger(): Logger {
        if (this::class.isCompanion) {
            return LoggerFactory.getLogger(this::class.java.enclosingClass)
        }
        return LoggerFactory.getLogger(this::class.java)
    }
}