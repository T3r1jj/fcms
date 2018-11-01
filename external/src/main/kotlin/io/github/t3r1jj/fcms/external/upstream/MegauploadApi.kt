package io.github.t3r1jj.fcms.external.upstream

import io.github.t3r1jj.fcms.external.upstream.data.MegauploadSuccessfulResponse
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface MegauploadApi {
    //https://megaupload.nz/api/upload
    @Multipart
    @POST("/api/upload")
    fun upload(@Part file: MultipartBody.Part): Call<MegauploadSuccessfulResponse>

    @GET("/api/v2/file/{id}/info")
    fun getInfo(@Path("id") id: String): Call<MegauploadSuccessfulResponse>
}