package com.example.myexpirationdate.repos

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.util.Log
import com.example.myexpirationdate.apis.ImageBBApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ImageRepository {
    private const val PREFS = "imgbb_prefs"
    private const val KEY_URLS = "imgbb_urls"
    private const val TAG = "ImageRepository"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    suspend fun uploadAndSave(context: Context, bitmap: Bitmap): String? {
        return withContext(Dispatchers.IO) {
            try {
                val url = ImageBBApi.uploadImage(bitmap)
                if (!url.isNullOrBlank()) {
                    val set = prefs(context).getStringSet(KEY_URLS, mutableSetOf())?.toMutableSet()
                        ?: mutableSetOf()
                    set.add(url)
                    prefs(context).edit().putStringSet(KEY_URLS, set).apply()
                    Log.d(TAG, "Uploaded image and saved URL: $url")
                    url
                } else {
                    Log.w(TAG, "ImageBB upload returned null/empty URL")
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception in uploadAndSave", e)
                null
            }
        }
    }

    fun getSavedImageUrls(context: Context): List<String> {
        val set = prefs(context).getStringSet(KEY_URLS, emptySet())
        return set?.toList() ?: emptyList()
    }

    fun clearSavedUrls(context: Context) {
        prefs(context).edit().remove(KEY_URLS).apply()
    }
}
