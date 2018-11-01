package io.github.t3r1jj.fcms.external.upstream

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface MegauploadApi {
    @Multipart
    @POST("/api/upload")
    fun upload(@Part file: MultipartBody.Part): Call<MegauploadSuccessfulResponse>

    @GET("/api/v2/file/{id}/info")
    fun getInfo(@Path("id") id: String): Call<MegauploadSuccessfulResponse>
}

interface OpenloadApi {
    @Multipart
    @POST
    fun upload(@Url url: String, @Part file: MultipartBody.Part): Call<OpenloadSuccessfulResponse>

    @GET("/1/file/ul")
    fun getUploadUrl(): Call<OpenloadSuccessfulResponse>

    @GET("/1/file/info")
    fun getInfo(@Query("file") id: String): Call<OpenloadDefaultResponse>
}