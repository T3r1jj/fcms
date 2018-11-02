package io.github.t3r1jj.fcms.external.authorized

import io.github.t3r1jj.fcms.external.data.RecordMeta
import io.github.t3r1jj.fcms.external.data.StorageInfo
import io.github.t3r1jj.fcms.external.upstream.CleanableStorage
import io.github.t3r1jj.fcms.external.upstream.UpstreamStorage

//TODO: close streams
//TODO: refactor interfaces
interface Storage : UpstreamStorage, CleanableStorage {
    fun login()
    fun isLogged(): Boolean
    fun findAll(filePath: String): List<RecordMeta>
    fun getInfo(): StorageInfo
    fun logout()
}