package com.example.myexpirationdate.viewmodels

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myexpirationdate.TAG
import com.example.myexpirationdate.apis.OpenAiApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class OpenAiVM : ViewModel() {

    private val _analysisResult = MutableStateFlow("")
    val analysisResult: StateFlow<String> = _analysisResult.asStateFlow()

    fun analyzeImage(bitmap: Bitmap) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Starting image analysis...")
                val base64Image = bitmapToBase64(bitmap)
                val dataUrl = "data:image/jpeg;base64,$base64Image"

                val response = OpenAiApi.getImageResponse(
                    prompt = "What is the food or drug item in this picture?",
                    imageUrl = dataUrl
                )

                _analysisResult.value = response
            } catch (e: Throwable) {
                Log.e(TAG, "OpenAiVM.analyzeImage error: ${e.message}", e)
                _analysisResult.value = "Error analyzing image: ${e.message}"
            }
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos)
        val bytes = baos.toByteArray()
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }
}
