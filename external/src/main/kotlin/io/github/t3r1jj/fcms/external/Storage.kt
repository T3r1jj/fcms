package io.github.t3r1jj.fcms.external

import java.io.File

interface Storage {
    fun login()
    fun isLogged(): Boolean
    fun upload(file: File): String
    fun download(filePath: String): File
    fun delete(filePath: String)
    fun isPresent(filePath: String): Boolean
    fun getInfo(): StorageInfo
    fun logout()
}