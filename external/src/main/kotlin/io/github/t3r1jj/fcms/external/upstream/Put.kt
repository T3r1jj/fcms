package io.github.t3r1jj.fcms.external.upstream

import com.google.gson.Gson
import io.github.t3r1jj.fcms.external.Record
import io.github.t3r1jj.fcms.external.StorageException
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.apache.commons.io.FileUtils
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.HttpURLConnection
import java.net.URL

class Put : UpstreamStorage, CleanableStorage {

    fun getClient(): Retrofit {
        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()


        val retrofit = Retrofit.Builder()
                .baseUrl("https://api.put.re")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()



        return retrofit
    }

    override fun upload(record: Record): Record {
        val client = getClient().create(PutApi::class.java)
        val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), record.data.readBytes())
        val body = MultipartBody.Part.createFormData("file", record.name, requestFile)
        val response = client.upload(body).execute()
        if (response.isSuccessful) {
            return record.copy(path = response.body()!!.data.link).apply { id = response.body()!!.data.deleteToken }
        } else {
            val gson = Gson()
            val error = gson.fromJson(response.errorBody()!!.charStream(), MegauploadErrorResponse::class.java)
            throw StorageException(error.error.message)
        }
    }

    override fun download(filePath: String): Record {
        val url = URL(filePath)
        val connection = url.openConnection()
        val fieldValue = connection.getHeaderField("Content-Disposition")
        try {
            val filename = fieldValue.substring(fieldValue.indexOf("filename=\"") + 10, fieldValue.length - 1)
            val tempFile = java.io.File.createTempFile(System.currentTimeMillis().toString(), null)
            tempFile.deleteOnExit()
            FileUtils.copyInputStreamToFile(connection.getInputStream(), tempFile)
            return Record(filename, filePath, tempFile.inputStream())
        } catch (e: RuntimeException) {
            throw StorageException("File not found", e)
        }
    }

    override fun isPresent(filePath: String): Boolean {
        val url = URL(filePath)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connect()
        val code = connection.responseCode
        return code == 200
    }

    override fun delete(record: Record) {
        val client = getClient().create(PutApi::class.java)
        val response = client.delete(getName(record.path), record.id!!).execute()
        if (!response.isSuccessful) {
            throw StorageException(response.errorBody()!!.string())
        }
    }

    private fun getName(filePath: String): String {
        val pathParts = java.lang.String(filePath).split("/")
        return pathParts[pathParts.size - 2]
    }

}