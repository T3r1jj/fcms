package io.github.t3r1jj.fcms.external.upstream

import com.google.gson.Gson
import io.github.t3r1jj.fcms.external.Record
import io.github.t3r1jj.fcms.external.RecordMeta
import io.github.t3r1jj.fcms.external.StorageException
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.apache.commons.io.FileUtils
import org.jsoup.Jsoup
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.URL


class Megaupload : UpstreamStorage {
    fun getClient(): Retrofit {
//https://forumfiles.com
        //https://anonfile.com
        //https://bayfiles.com
        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()


        val retrofit = Retrofit.Builder()
                .baseUrl("https://megaupload.nz")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()



        return retrofit
    }

    override fun upload(record: Record): Record {
        val client = getClient().create(MegauploadApi::class.java)
        val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), record.data.readBytes())
        val body = MultipartBody.Part.createFormData("file", record.name, requestFile)
        val response = client.upload(body).execute()
        if (response.isSuccessful) {
            return record.copy(path = response.body()!!.data.file.url.full)
        } else {
            val gson = Gson()
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

    private fun getInfo(filePath: String): RecordMeta {
        val client = getClient().create(MegauploadApi::class.java)
        val response = client.getInfo(getId(filePath)).execute()
        if (response.isSuccessful) {
            val info = response.body()!!
            return RecordMeta(info.data.file.metadata.name, filePath, info.data.file.metadata.size.bytes)
        } else {
            val gson = Gson()
            val error = gson.fromJson(response.errorBody()!!.charStream(), MegauploadErrorResponse::class.java)
            throw StorageException(error.error.message)
        }
    }

    private fun getId(filePath: String): String {
        val pathParts = java.lang.String(filePath).split("/")
        return pathParts[pathParts.size - 2]
    }

    override fun isPresent(filePath: String): Boolean {
        return try {
            getInfo(filePath)
            true
        } catch (e: StorageException) {
            false
        }
    }

}

