package com.example.myexpirationdate.apis

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

@Serializable
data class ImgbbResponse(
    val data: ImgbbData?,
    val success: Boolean,
    val status: Int
)
@Serializable
data class ImgbbData(
    val url: String?,          // direct public URL you want
    val display_url: String?,  // alt public URL
    val delete_url: String?    // if present; useful for cleanup
)

fun Bitmap.toBase64PngNoWrap(): String {
    val baos = ByteArrayOutputStream()
    this.compress(Bitmap.CompressFormat.PNG, 100, baos)
    val bytes = baos.toByteArray()
    return Base64.encodeToString(bytes, Base64.NO_WRAP)
}

fun String.toPlainRequestBody(): RequestBody =
    this.toRequestBody("text/plain".toMediaType())

interface ImageBBApiService{

    @Multipart
    @POST("1/upload")
    suspend fun uploadBase64(
        @Query("key") apiKey: String,
        @Query("expiration") expiration: Int? = null,          // e.g., 600 (10 min). Omit for permanent.
        @Part("image") base64Image: RequestBody,               // base64 string body
        @Part("name") name: RequestBody? = null                     // optional upload file name
    ): ImgbbResponse

}

object ImageBBApi {
    private val httpClient = OkHttpClient.Builder()
        .callTimeout(100, TimeUnit.SECONDS)
        .readTimeout(100, TimeUnit.SECONDS).build()

    private val retrofit = Retrofit.Builder()
        .addConverterFactory(Json{ ignoreUnknownKeys = true}.asConverterFactory("application/json".toMediaType()))
        .baseUrl("https://api.imgbb.com")
        .client(httpClient)
        .build()

    val retrofitService : ImageBBApiService by lazy{
        retrofit.create(ImageBBApiService::class.java)
    }

    suspend fun uploadImage(bitmap: Bitmap) : String? {

        val base64 = bitmap.toBase64PngNoWrap()
        val imageBody = base64.toPlainRequestBody()
        val nameBody = "image_${System.currentTimeMillis()}".toPlainRequestBody()

        try {
            val resp = retrofitService.uploadBase64(
                apiKey = "157effb0ca4f6aadae7005413e85b3c1",
                base64Image = imageBody,
                name = nameBody
            )
            return resp.data?.url  // <- public URL to pass to SD init_image
        }catch (e: Throwable){
            Log.e("MYTAG", "Error message is : ${e.message}")
        }
        return null
    }

}