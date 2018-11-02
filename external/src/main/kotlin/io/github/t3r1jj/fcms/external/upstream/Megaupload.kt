package io.github.t3r1jj.fcms.external.upstream

import io.github.t3r1jj.fcms.external.data.Record
import io.github.t3r1jj.fcms.external.data.RecordMeta
import io.github.t3r1jj.fcms.external.data.StorageException
import io.github.t3r1jj.fcms.external.upstream.api.MegauploadApi
import io.github.t3r1jj.fcms.external.upstream.api.MegauploadErrorResponse
import org.apache.commons.io.FileUtils
import org.jsoup.Jsoup
import java.net.URL


open class Megaupload(baseUrl: String) : StorageInfoClient<MegauploadApi>(baseUrl, MegauploadApi::class.java), UpstreamStorage {
    constructor() : this("https://megaupload.nz")

    override fun upload(record: Record): RecordMeta {
        val (size, body) = createFileForm(record)
        val response = client.upload(body).execute()
        if (response.isSuccessful) {
            return RecordMeta(record.name, response.body()!!.data.file.url.full, size)
        } else {
            val error = gson.fromJson(response.errorBody()!!.charStream(), MegauploadErrorResponse::class.java)
            throw StorageException(error.error.message)
        }
    }

    override fun download(filePath: String): Record {
        val doc = Jsoup.connect(filePath).get()
        val link = doc.select("#download-url")
        val header = doc.select("h1")
        val downloadPath = link.attr("href")
        val tempFile = java.io.File.createTempFile(System.currentTimeMillis().toString(), null)
        tempFile.deleteOnExit()
        FileUtils.copyURLToFile(URL(downloadPath), tempFile)
        return Record(header.text(), filePath, tempFile.inputStream())
    }

    override fun getInfo(filePath: String): RecordMeta {
        val response = client.getInfo(getIdFromPath(filePath)).execute()
        if (response.isSuccessful) {
            val info = response.body()!!
            return RecordMeta(info.data.file.metadata.name, filePath, info.data.file.metadata.size.bytes)
        } else {
            val error = gson.fromJson(response.errorBody()!!.charStream(), MegauploadErrorResponse::class.java)
            throw StorageException(error.error.message)
        }
    }

}

