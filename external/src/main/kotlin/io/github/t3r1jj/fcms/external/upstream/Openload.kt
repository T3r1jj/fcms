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
import org.jsoup.Jsoup
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayInputStream


class Openload : UpstreamStorage {
    fun getClient(): Retrofit {
        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()


        val retrofit = Retrofit.Builder()
                .baseUrl("https://api.openload.co")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()

        return retrofit
    }


    override fun upload(record: Record): Record {
        val client = getClient().create(OpenloadApi::class.java)
        var response = client.getUploadUrl().execute()
        if (response.isSuccessful) {
            val uploadUrl = response.body()!!.result.url
            val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), record.data.readBytes())
            val body = MultipartBody.Part.createFormData("file", record.name, requestFile)
            response = client.upload(uploadUrl, body).execute()
            if (response.isSuccessful) {
                return record.copy(path = response.body()!!.result.url)
            }
        }
        val gson = Gson()
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

    private fun getInfo(filePath: String): RecordMeta {
        val client = getClient().create(OpenloadApi::class.java)
        val id = getId(filePath)
        val response = client.getInfo(id).execute()
        if (response.isSuccessful) {
            val result = response.body()!!
            val jsonInfo = result.result.get(id)
            val gson = Gson()
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

    override fun isPresent(filePath: String): Boolean {
        return try {
            getInfo(filePath)
            true
        } catch (e: StorageException) {
            false
        }
    }

    private fun getId(filePath: String): String {
        val pathParts = java.lang.String(filePath).split("/")
        return pathParts[pathParts.size - 2]
    }
}