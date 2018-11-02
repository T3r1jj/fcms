package io.github.t3r1jj.fcms.external

abstract class NamedStorage {
    override fun toString(): String {
        return this::class.java.simpleName
    }
}