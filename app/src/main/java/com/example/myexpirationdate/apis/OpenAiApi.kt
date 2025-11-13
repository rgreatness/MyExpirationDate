package com.example.myexpirationdate.apis

import android.util.Log
import com.example.myexpirationdate.TAG
import com.example.myexpirationdate.models.ChatRequest
import com.example.myexpirationdate.models.ChatResponse
import com.example.myexpirationdate.models.ContentItem
import com.example.myexpirationdate.models.ImageRequest
import com.example.myexpirationdate.models.Message
import com.example.myexpirationdate.models.Message2
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

// post https://api.openai.com/v1/chat/completions

const val OPENAI_API_KEY = "put your api key here"
interface OpenAiApiService {

    @Headers(
        "Content-Type: application/json",
        "Authorization: Bearer $OPENAI_API_KEY"
    )
    @POST("v1/chat/completions")
    suspend fun getChatCompletions(@Body request: ChatRequest) : ChatResponse

    @Headers(
        "Content-Type: application/json",
        "Authorization: Bearer ${OPENAI_API_KEY}"
    )
    @POST("v1/chat/completions")
    suspend fun getImageAnalysis(@Body request: ImageRequest) : ChatResponse

}

object OpenAiApi{

    private val httpClient = OkHttpClient.Builder()
        .callTimeout(50, TimeUnit.SECONDS)
        .readTimeout(50, TimeUnit.SECONDS).build()

    private val retrofit = Retrofit.Builder()
        .addConverterFactory(Json{ ignoreUnknownKeys = true}.asConverterFactory("application/json".toMediaType()))
        .baseUrl("https://api.openai.com/")
        .client(httpClient)
        .build()

    val retrofitService : OpenAiApiService by lazy{
        retrofit.create(OpenAiApiService::class.java)
    }

    suspend fun getResponse(prompt: String) : String{
        // TODO:
        val chatRequest = ChatRequest(
            model = "gpt-3.5-turbo",
            messages = listOf(
                Message(
                    role = "user",
                    content = prompt
                )
            )

        )
        Log.i(TAG, "before OPENAI API CALL")
        val chatResponse = retrofitService.getChatCompletions(chatRequest)
        Log.i(TAG, "after OPENAI API CALL response: ${chatResponse.choices[0].message.content}")

        return chatResponse.choices[0].message.content
    }

    suspend fun getImageResponse(prompt: String, image_url: String): String{
        // TODO:
        val imageRequest = ImageRequest(
            model = "gpt-4.1",
            messages = listOf(
                Message2(
                    role = "user",
                    content = listOf(
                        ContentItem(
                            type = "text",
                            text = prompt
                        ),
                        ContentItem(
                            type = "image_url",
                            image_url = URLObject(url = image_url)
                        )
                    )
                )
            )
        )
        Log.i(TAG, "before OPENAI API CALL")
        val chatResponse = retrofitService.getImageAnalysis(imageRequest)
        Log.i(TAG, "after OPENAI API CALL response: ${chatResponse.choices[0].message.content}")

        return chatResponse.choices[0].message.content
    }




}
