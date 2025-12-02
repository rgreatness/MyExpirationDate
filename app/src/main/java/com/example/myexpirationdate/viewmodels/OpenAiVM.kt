package com.example.myexpirationdate.viewmodels

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.sqlite.db.SimpleSQLiteQuery
import com.example.myexpirationdate.TAG
import com.example.myexpirationdate.apis.OpenAiApi
import com.example.myexpirationdate.data.ExpirationItemDao
import com.example.myexpirationdate.medApp
import com.example.myexpirationdate.models.ExpirationItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class OpenAiVM(private val expirationItemDao: ExpirationItemDao) : ViewModel() {

    private val _analysisResult = MutableStateFlow("")
    val analysisResult: StateFlow<String> = _analysisResult.asStateFlow()

    fun analyzeImage(bitmap: Bitmap) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Starting image analysis...")
                val base64Image = bitmapToBase64(bitmap)
                val dataUrl = "data:image/jpeg;base64,$base64Image"

                val response = OpenAiApi.getImageResponse(
                    prompt = "Please identify the food items in this image. If the object is edible (like chocolate, candy, fruit, etc.), tell me what food it appears to be.",
                    imageUrl = dataUrl
                )


                Log.d(TAG, "Image analysis completed.")
                Log.d(TAG, "OpenAiVM.analyzeImage response: $response")
                val cleanResponse = normalizeQuery(response)
                val matched = checkExpItems(cleanResponse)
                _analysisResult.value = if (matched.isNotEmpty()) {
                    "Expiration Date: ${matched.joinToString(", ")}"
                } else {
                    "No expiration-related items detected in the image."
                }

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

    fun normalizeQuery(text: String?): List<String> {
        if (text.isNullOrEmpty()) {
            return emptyList()
        }

        val lowerCaseText = text.lowercase()
        val cleanedText = lowerCaseText.replace("[.,/#!$%^&*;:{}=\\-_`~()?]".toRegex(), "")

        val stopWords = setOf("the", "a", "an", "and", "or", "but", "in", "with", "on", "at", "to", "for", "of", "as", "by", "is", "are", "was", "were", "been", "be", "have", "has", "had", "do", "does", "did", "will", "would", "should", "could", "may", "might", "can", "this", "that", "these", "those", "shows", "image", "typically", "consists", "surrounded", "coated", "rolled", "appears")

        val words = cleanedText
            .split("\\s+".toRegex())
            .filter { it.length >= 3 && it !in stopWords }

//        Log.d(TAG, "Normalized keywords: $words")
        return words.toSet().toList()
    }

    fun clearAnalysisResult() {
        _analysisResult.value = ""
    }



    private suspend fun checkExpItems(keywords: List<String>): List<ExpirationItem> {
        return withContext(Dispatchers.IO) {
            if (keywords.isEmpty()) {
                return@withContext emptyList()
            }

//            val allItems = expirationItemDao.getAllExpirationItems().first()
//            Log.d(TAG, "Database contains ${allItems.size} items: ${allItems.map { it.name }}")

            val queryBuilder = StringBuilder("SELECT * FROM expiration_items WHERE ")
            val bindArgs = mutableListOf<Any>()

            keywords.forEachIndexed { index, keyword ->
                if (index > 0) {
                    queryBuilder.append(" OR ")
                }

                val lowerKeyword = keyword.lowercase()
                val pluralForm =
                    if (!lowerKeyword.endsWith("s")) "${lowerKeyword}s" else lowerKeyword
                val singularForm = lowerKeyword.removeSuffix("s").removeSuffix("es")

                queryBuilder.append("(LOWER(name) LIKE ? OR LOWER(name) LIKE ? OR LOWER(name) LIKE ? OR LOWER(name) LIKE ?)")
                bindArgs.add("%$lowerKeyword%")
                bindArgs.add("%$singularForm%")
                bindArgs.add("%$pluralForm%")
                bindArgs.add("$lowerKeyword%")
            }

            val query = SimpleSQLiteQuery(queryBuilder.toString(), bindArgs.toTypedArray())

            try {
                val results = expirationItemDao.getItemsByQuery(query)
//                Log.d(TAG, "Keywords searched: $keywords")
//                Log.d(TAG, "SQL query: ${queryBuilder.toString()}")
//                Log.d(TAG, "Bind args: $bindArgs")
                Log.d(
                    TAG,
                    "Found ${results.size} matching expiration items: ${results.map { it.name }}"
                )
                return@withContext results
            } catch (e: Exception) {
                Log.e(TAG, "Error querying expiration items: ${e.message}", e)
                emptyList()
            }
        }
    }
}

class OpenAiVMFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val dao = medApp.getApp().container.expirationItemDao
        return OpenAiVM(dao) as T
    }
}
