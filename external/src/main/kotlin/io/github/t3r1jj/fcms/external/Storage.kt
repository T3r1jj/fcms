package io.github.t3r1jj.fcms.external
//TODO: close streams
//TODO: refactor interfaces
interface Storage {
    fun login()
    fun isLogged(): Boolean
    fun upload(record: Record)
    fun download(filePath: String): Record
    fun findAll(filePath: String): List<RecordMeta>
    fun delete(filePath: String)
    fun isPresent(filePath: String): Boolean
    fun getInfo(): StorageInfo
    fun logout()
}