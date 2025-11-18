
package com.example.myexpirationdate.apis

import android.util.Log
import com.example.myexpirationdate.TAG
import com.example.myexpirationdate.models.ChatRequest
import com.example.myexpirationdate.models.ChatResponse
import com.example.myexpirationdate.models.ContentItem
import com.example.myexpirationdate.models.MessageRequest
import com.example.myexpirationdate.models.URLObject
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

private const val BASE_URL = "https://api.openai.com/"

private const val OPENAI_API_KEY = "your api key here"

private const val AUTH_HEADER = "Authorization: Bearer $OPENAI_API_KEY"

private val json = Json { ignoreUnknownKeys = true }

private val httpClient = OkHttpClient.Builder()
    .callTimeout(50, TimeUnit.SECONDS)
    .readTimeout(50, TimeUnit.SECONDS)
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
    .baseUrl(BASE_URL)
    .client(httpClient)
    .build()

interface OpenAiApiService {
    @Headers(
        "Content-Type: application/json",
        AUTH_HEADER
    )
    @POST("v1/chat/completions")
    suspend fun getChatCompletions(@Body request: ChatRequest): ChatResponse
}

object OpenAiApi {

    private val retrofitService: OpenAiApiService by lazy {
        retrofit.create(OpenAiApiService::class.java)
    }

    suspend fun getResponse(prompt: String): String {
        val chatRequest = ChatRequest(
            model = "gpt-4o-mini",
            messages = listOf(
                MessageRequest(
                    role = "user",
                    content = listOf(ContentItem(type = "text", text = prompt))
                )
            )
        )

        Log.i(TAG, "OPENAI API CALL (text)")
        val chatResponse = retrofitService.getChatCompletions(chatRequest)

        return chatResponse.choices.firstOrNull()?.message?.content ?: ""
    }

    suspend fun getImageResponse(prompt: String, imageUrl: String): String {
        val chatRequest = ChatRequest(
            model = "gpt-4o-mini",
            messages = listOf(
                MessageRequest(
                    role = "user",
                    content = listOf(
                        ContentItem(type = "text", text = prompt),
                        ContentItem(type = "image_url", imageUrl = URLObject(url = imageUrl))
                    )
                )
            )
        )

        Log.i(TAG, "OPENAI API CALL (image)")
        val chatResponse = retrofitService.getChatCompletions(chatRequest)

        return chatResponse.choices.firstOrNull()?.message?.content ?: ""
    }
}
