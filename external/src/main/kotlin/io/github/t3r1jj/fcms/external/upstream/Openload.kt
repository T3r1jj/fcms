package io.github.t3r1jj.fcms.external.upstream

import io.github.t3r1jj.fcms.external.data.Record
import io.github.t3r1jj.fcms.external.data.RecordMeta
import io.github.t3r1jj.fcms.external.data.StorageException
import io.github.t3r1jj.fcms.external.upstream.api.OpenloadApi
import io.github.t3r1jj.fcms.external.upstream.api.OpenloadErrorResponse
import io.github.t3r1jj.fcms.external.upstream.api.OpenloadFileError
import io.github.t3r1jj.fcms.external.upstream.api.OpenloadFileInfo
import org.jsoup.Jsoup
import java.io.ByteArrayInputStream


class Openload(baseUrl: String) : StorageInfoClient<OpenloadApi>(baseUrl, OpenloadApi::class.java), UpstreamStorage {
    constructor() : this("https://api.openload.co")

    override fun upload(record: Record): RecordMeta {
        var response = client.getUploadUrl().execute()
        if (response.isSuccessful) {
            val uploadUrl = response.body()!!.result.url
            val (size, body) = createFileForm(record)
            response = client.upload(uploadUrl, body).execute()
            if (response.isSuccessful) {
                return RecordMeta(record.name, response.body()!!.result.url, size)
            }
        }
        val error = gson.fromJson(response.errorBody()!!.charStream(), OpenloadErrorResponse::class.java)
        throw StorageException(error.message)
    }

    override fun download(filePath: String): Record {
        val sitePath = filePath.replace("//openload.co", "//oload.icu")
        val userAgent = "Mozilla/5.0 (Windows NT 6.1; Win64; x64; Trident/7.0; rv:11.0) like Gecko"
        val doc = Jsoup.connect(sitePath)
                .userAgent(userAgent)
                .get()
        val dataLink = doc.select("[data-src]")
        val header = doc.select(".other-title-bold").first()
        val downloadPath = (if (sitePath.contains("https:")) "https:" else "http:") + dataLink.attr("data-src")
        val response = Jsoup.connect(downloadPath).userAgent(userAgent)
                .ignoreContentType(true).execute()
        return Record(header.text(), filePath, ByteArrayInputStream(response.bodyAsBytes()))
    }

    override fun getInfo(filePath: String): RecordMeta {
        val id = getIdFromPath(filePath)
        val response = client.getInfo(id).execute()
        if (response.isSuccessful) {
            val result = response.body()!!
            val jsonInfo = result.result.get(id)
            try {
                val info = gson.fromJson(jsonInfo, OpenloadFileInfo::class.java)
                return RecordMeta(info.name, filePath, info.size)
            } catch (e: RuntimeException) {
                val error = gson.fromJson(jsonInfo, OpenloadFileError::class.java)
                throw StorageException("Error while getting info: " + error.status)
            }
        }
        throw StorageException(response.message())
    }

}