package com.example.myexpirationdate.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myexpirationdate.TAG
import com.example.myexpirationdate.apis.OpenAiApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class OpenAiState(
    val question: String? = null,
    val answer: String? = null
)

class OpenAiVM : ViewModel() {

    var openAiState: MutableStateFlow<OpenAiState> = MutableStateFlow(OpenAiState())

    fun updateQuestion(userQuestion: String) {
        openAiState.update { currentState ->
            currentState.copy(
                question = userQuestion
            )
        }
    }

    fun sendRequest() {
        // TODO: Call the OpenAI api to send the request and get a response
        val question = openAiState.value.question
        if (question != null) {

            viewModelScope.launch {
                try {
                    val response = OpenAiApi.getResponse(prompt = question)
                    openAiState.update { currentState ->
                        currentState.copy(
                            answer = response
                        )
                    }

                } catch (e: Throwable) {
                    Log.e(TAG, "OpenAiVM: error ${e.printStackTrace()}")
                }
            }

        }


    }

    fun sendImageRequest(image_url: String) {
        // TODO:
        viewModelScope.launch {
            try {
                val response =
                    OpenAiApi.getImageResponse(prompt = "What is in this image?", image_url)
                openAiState.update { currentState ->
                    currentState.copy(
                        answer = response
                    )
                }

            } catch (e: Throwable) {
                Log.e(TAG, "OpenAiVM: error ${e.printStackTrace()}")
            }
        }
    }

}